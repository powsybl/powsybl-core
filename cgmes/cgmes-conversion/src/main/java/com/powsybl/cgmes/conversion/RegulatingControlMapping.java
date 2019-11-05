/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import com.powsybl.triplestore.api.PropertyBag;

import java.util.*;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
public class RegulatingControlMapping {

    static final String REGULATING_CONTROL = "RegulatingControl";
    static final String TAP_CHANGER_CONTROL = "TapChangerControl";
    private static final String TERMINAL = "Terminal";
    static final String MISSING_IIDM_TERMINAL = "IIDM terminal for this CGMES topological node: %s";
    private static final String VOLTAGE = "voltage";
    private static final String REGULATING_CONTROL_REF = "Regulating control %s";
    private static final String TAP_CHANGER_CONTROL_ENABLED = "tapChangerControlEnabled";

    private final Context context;
    private final RegulatingControlMappingForGenerators regulatingControlMappingForGenerators;
    private final RegulatingControlMappingForTransformers regulatingControlMappingForTransformers;

    public RegulatingControlMapping(Context context) {
        this.context = context;
        regulatingControlMappingForGenerators = new RegulatingControlMappingForGenerators(this);
        regulatingControlMappingForTransformers = new RegulatingControlMappingForTransformers(this);
    }

    public RegulatingControlMappingForGenerators forGenerators() {
        return regulatingControlMappingForGenerators;
    }

    public RegulatingControlMappingForTransformers forTransformers() {
        return regulatingControlMappingForTransformers;
    }

    public Context context() {
        return context;
    }

    static class RegulatingControl {
        final String mode;
        final String cgmesTerminal;
        final String topologicalNode;
        final boolean enabled;
        final double targetValue;
        final double targetDeadband;

        private final Map<String, Boolean> idsEq = new HashMap<>();

        RegulatingControl(PropertyBag p) {
            this.mode = p.get("mode").toLowerCase();
            this.cgmesTerminal = p.getId(TERMINAL);
            this.topologicalNode = p.getId("topologicalNode");
            this.enabled = p.asBoolean("enabled", true);
            this.targetValue = p.asDouble("targetValue");
            this.targetDeadband = p.asDouble("targetDeadband", Double.NaN);
        }

        void hasCorrectlySetEq(String id) {
            idsEq.put(id, true);
        }
    }

    private Map<String, RegulatingControl> cachedRegulatingControls = new HashMap<>();

    Map<String, RegulatingControl> cachedRegulatingControls() {
        return cachedRegulatingControls;
    }

    void cacheRegulatingControls(PropertyBag p) {
        cachedRegulatingControls.put(p.getId(REGULATING_CONTROL), new RegulatingControl(p));
    }

    public void setRegulatingControl(PropertyBag p, Terminal defaultTerminal, RatioTapChangerAdder adder) {
        if (p.containsKey(TAP_CHANGER_CONTROL)) {
            String controlId = p.getId(TAP_CHANGER_CONTROL);
            RegulatingControl control = cachedRegulatingControls.get(controlId);
            if (control != null) {
                if (control.mode.endsWith(VOLTAGE) || (p.containsKey("tculControlMode") && p.get("tculControlMode").endsWith("volt"))) {
                    addRegulatingControlVoltage(p, control, adder, defaultTerminal, context);
                    return;
                } else if (!control.mode.endsWith("fixed")) {
                    context.fixed(control.mode, "Unsupported regulation mode for Ratio tap changer. Considered as a fixed ratio tap changer.");
                }
            } else {
                context.missing(String.format(REGULATING_CONTROL_REF, controlId));
            }
        }
        adder.setLoadTapChangingCapabilities(false);
    }

    private void addRegulatingControlVoltage(PropertyBag p, RegulatingControl control, RatioTapChangerAdder adder, Terminal defaultTerminal, Context context) {
        // Even if regulating is false, we reset the target voltage if it is not valid
        if (control.targetValue <= 0) {
            context.ignored(p.getId(TAP_CHANGER_CONTROL), String.format("Regulating control has a bad target voltage %f", control.targetValue));
            adder.setRegulating(false)
                    .setTargetV(Double.NaN);
        } else {
            adder.setRegulating(control.enabled || p.asBoolean(TAP_CHANGER_CONTROL_ENABLED, false))
                    .setTargetDeadband(control.targetDeadband)
                    .setTargetV(control.targetValue);
        }
        setRegulatingTerminal(p, control, defaultTerminal, adder);
    }

    private void setRegulatingTerminal(PropertyBag p, RegulatingControl control, Terminal defaultTerminal, RatioTapChangerAdder adder) {
        if (context.terminalMapping().find(control.cgmesTerminal) != null) {
            adder.setRegulationTerminal(context.terminalMapping().find(control.cgmesTerminal));
            control.idsEq.put(p.getId("RatioTapChanger"), true);
        } else {
            adder.setRegulationTerminal(defaultTerminal);
            if (!context.terminalMapping().areAssociated(p.getId(TERMINAL), control.topologicalNode)) {
                control.idsEq.put(p.getId("RatioTapChanger"), false);
            }
        }
    }

    public void setRegulatingControl(String idEq, PropertyBag p, StaticVarCompensatorAdder adder) {
        if (!p.asBoolean("controlEnabled", false)) {
            adder.setRegulationMode(StaticVarCompensator.RegulationMode.OFF);
            return;
        }
        if (p.containsKey(REGULATING_CONTROL)) {
            RegulatingControl control = cachedRegulatingControls.get(p.getId(REGULATING_CONTROL));
            if (control != null) {
                if (!control.enabled) {
                    adder.setRegulationMode(StaticVarCompensator.RegulationMode.OFF);
                    return;
                }
                if (context.terminalMapping().areAssociated(p.getId(TERMINAL), control.topologicalNode)) {
                    setRegulatingControl(control, adder, idEq);
                } else {
                    context.pending(String.format("Remote control for static var compensator %s replaced by voltage local control at nominal voltage", idEq),
                            "IIDM model does not support remote control for static var compensators");
                    setDefaultRegulatingControl(p, adder, idEq);
                }
            } else {
                context.missing(String.format(REGULATING_CONTROL_REF, p.getId(REGULATING_CONTROL)));
                setDefaultRegulatingControl(p, adder, idEq);
            }
        } else {
            setDefaultRegulatingControl(p, adder, idEq);
        }
    }

    private void setDefaultRegulatingControl(PropertyBag p, StaticVarCompensatorAdder adder, String idEq) {
        if (p.getId("controlMode").toLowerCase().endsWith(VOLTAGE)) {
            adder.setRegulationMode(StaticVarCompensator.RegulationMode.VOLTAGE)
                    .setVoltageSetPoint(p.asDouble("voltageSetPoint"));
        } else if (p.getId("controlMode").toLowerCase().endsWith("reactivepower")) {
            adder.setRegulationMode(StaticVarCompensator.RegulationMode.REACTIVE_POWER)
                    .setReactivePowerSetPoint(p.asDouble("q"));
        } else {
            context.fixed("SVCControlMode", String.format("Invalid control mode for static var compensator %s. Regulating control is disabled", idEq));
            adder.setRegulationMode(StaticVarCompensator.RegulationMode.OFF);
        }
    }

    private void setRegulatingControl(RegulatingControl control, StaticVarCompensatorAdder adder, String idEq) {
        if (control.mode.toLowerCase().endsWith(VOLTAGE)) {
            adder.setRegulationMode(StaticVarCompensator.RegulationMode.VOLTAGE)
                    .setVoltageSetPoint(control.targetValue);
        } else if (control.mode.toLowerCase().endsWith("reactivepower")) {
            adder.setRegulationMode(StaticVarCompensator.RegulationMode.REACTIVE_POWER)
                    .setReactivePowerSetPoint(control.targetValue);
        } else {
            context.fixed(control.mode, String.format("Invalid control mode for static var compensator %s. Regulating control is disabled", idEq));
            adder.setRegulationMode(StaticVarCompensator.RegulationMode.OFF);
        }
    }

    private void setAllRemoteRegulatingTerminals() {
        cachedRegulatingControls.entrySet().removeIf(this::setRemoteRegulatingTerminal);
    }

    private boolean setRemoteRegulatingTerminal(Map.Entry<String, RegulatingControl> entry) {
        RegulatingControl control = entry.getValue();
        if (!control.idsEq.isEmpty()) {
            boolean correctlySet = true;
            for (String idEq : control.idsEq.keySet()) {
                if (!control.idsEq.get(idEq)) {
                    Identifiable i = context.network().getIdentifiable(idEq);
                    if (i == null) {
                        correctlySet = correctlySet && setRemoteRegulatingTerminal(idEq, control);
                    } else {
                        correctlySet = false;
                    }
                }
            }
            return correctlySet;
        }
        return false;
    }

    private Terminal findRemoteRegulatingTerminal(String cgmesTerminal, String topologicalNode) {
        return Optional.ofNullable(context.terminalMapping().find(cgmesTerminal))
                .orElseGet(() -> context.terminalMapping().findFromTopologicalNode(topologicalNode));
    }

    private boolean setRemoteRegulatingTerminal(String tc, RegulatingControl control) {
        if (context.tapChangerTransformers().transformer2(tc) != null) {
            throw new PowsyblException("Unexpeted RemoteRegulatinTerminal for two windings transformer");
        } else if (context.tapChangerTransformers().transformer3(tc) != null) {
            return setRemoteRegulatingTerminal(tc, control, context.tapChangerTransformers().transformer3(tc));
        }
        return false;
    }

    private boolean setRemoteRegulatingTerminal(String tc, RegulatingControl control, ThreeWindingsTransformer t3w) {
        Terminal regTerminal = findRemoteRegulatingTerminal(control.cgmesTerminal, control.topologicalNode);
        if (regTerminal == null) {
            context.missing(String.format(MISSING_IIDM_TERMINAL, control.topologicalNode));
            return false;
        }
        if (context.tapChangerTransformers().type(tc).equals("rtc")) {
            if (context.tapChangerTransformers().whichSide(tc) == 2) {
                t3w.getLeg2().getRatioTapChanger().setRegulationTerminal(regTerminal);
                return true;
            } else if (context.tapChangerTransformers().whichSide(tc) == 3) {
                t3w.getLeg3().getRatioTapChanger().setRegulationTerminal(regTerminal);
                return true;
            }
        }
        return false;
    }

    public void setAllRegulatingControls(Network network) {
        setAllRemoteRegulatingTerminals();

        regulatingControlMappingForGenerators.applyRegulatingControls(network);
        regulatingControlMappingForTransformers.applyTwoWindingsTapChangersRegulatingControl(network);

        cachedRegulatingControls.entrySet().removeIf(entry -> {
            if (entry.getValue().idsEq.isEmpty()) {
                return false;
            }
            for (Map.Entry<String, Boolean> e : entry.getValue().idsEq.entrySet()) {
                if (!e.getValue()) {
                    return false;
                }
            }
            return true;
        });

        cachedRegulatingControls.forEach((key, value) -> context.pending("Regulating terminal",
                String.format("The setting of the regulating terminal of the regulating control %s is not entirely handled.", key)));
        cachedRegulatingControls.clear();
    }

    public Terminal findRegulatingTerminal(String cgmesTerminal, String topologicalNode) {
        return Optional.ofNullable(context.terminalMapping().find(cgmesTerminal))
                .orElseGet(() -> context.terminalMapping().findFromTopologicalNode(topologicalNode));
    }

    static boolean isControlModeVoltage(String controlMode) {
        return controlMode != null && controlMode.endsWith(VOLTAGE);
    }
}
