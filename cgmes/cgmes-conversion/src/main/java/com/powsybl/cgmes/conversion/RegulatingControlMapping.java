/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion;

import com.powsybl.iidm.network.*;
import com.powsybl.triplestore.api.PropertyBag;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
public class RegulatingControlMapping {

    private static final String REGULATING_CONTROL = "RegulatingControl";
    private static final String TAP_CHANGER_CONTROL = "TapChangerControl";
    private static final String TERMINAL = "Terminal";
    private static final String MISSING_IIDM_TERMINAL = "IIDM terminal for this CGMES topological node: %s";

    private final Context context;

    public RegulatingControlMapping(Context context) {
        this.context = context;
    }

    class RegulatingControl {
        private final String mode;
        private final String cgmesTerminal;
        private final String topologicalNode;
        private final boolean enabled;
        private final double targetValue;

        private String idEq;

        RegulatingControl(PropertyBag p) {
            this.mode = p.get("mode").toLowerCase();
            this.cgmesTerminal = p.getId(TERMINAL);
            this.topologicalNode = p.getId("topologicalNode");
            this.enabled = p.asBoolean("enabled", true);
            this.targetValue = p.asDouble("targetValue");
        }
    }

    private Map<String, RegulatingControl> cachedRegulatingControls = new HashMap<>();

    public void cacheRegulatingControls(PropertyBag p) {
        cachedRegulatingControls.put(p.getId(REGULATING_CONTROL), new RegulatingControl(p));
    }

    public void setRegulatingControl(String idEq, PropertyBag p, GeneratorAdder adder, VoltageLevel vl) {
        if (p.containsKey(REGULATING_CONTROL)) {
            String controlId = p.getId(REGULATING_CONTROL);
            RegulatingControl control = cachedRegulatingControls.get(controlId);
            if (control != null) {
                if (control.mode.endsWith("voltage")) {
                    setTargetValue(control.targetValue, vl.getNominalV(), controlId, adder);
                    adder.setVoltageRegulatorOn(control.enabled);
                    setRegulatingTerminal(control, p.getId(TERMINAL), idEq, controlId, adder);
                    return;
                } else {
                    context.ignored(control.mode, String.format("Unsupported regulation mode for generator %s", idEq));
                }
                cachedRegulatingControls.remove(controlId);
            } else {
                context.missing(String.format("Regulating control %s for equipment %s", controlId, idEq));
            }
        }
        adder.setVoltageRegulatorOn(false)
                .setTargetV(Double.NaN);
    }

    private void setRegulatingTerminal(RegulatingControl control, String terminalEq, String idEq, String controlId, GeneratorAdder adder) {
        if (context.terminalMapping().find(control.cgmesTerminal) != null) {
            adder.setRegulatingTerminal(context.terminalMapping().find(control.cgmesTerminal));
        } else if (!context.terminalMapping().areAssociated(terminalEq, control.topologicalNode)) {
            control.idEq = idEq;
            return;
        }
        cachedRegulatingControls.remove(controlId);
    }

    private void setTargetValue(double targetValue, double defaultValue, String controlId, GeneratorAdder adder) {
        if (targetValue == 0 || Double.isNaN(targetValue)) {
            context.fixed(controlId, "Invalid value for regulating target value",
                    targetValue, defaultValue);
            adder.setTargetV(defaultValue);
        } else {
            adder.setTargetV(targetValue);
        }
    }

    public void setRegulatingControl(PropertyBag p, Terminal defaultTerminal, RatioTapChangerAdder adder) {
        if (p.containsKey(TAP_CHANGER_CONTROL)) {
            String controlId = p.getId(TAP_CHANGER_CONTROL);
            RegulatingControl control = cachedRegulatingControls.get(controlId);
            if (control != null) {
                if (control.mode.endsWith("voltage") || (p.containsKey("tculControlMode") && p.get("tculControlMode").endsWith("volt"))) {
                    addRegulatingControlVoltage(p, control, adder, defaultTerminal, context);
                    return;
                } else if (!control.mode.endsWith("fixed")) {
                    context.fixed(control.mode, "Unsupported regulation mode for Ratio tap changer. Considered as a fixed ratio tap changer.");
                }
                cachedRegulatingControls.remove(controlId);
            } else {
                context.missing(String.format("Regulating control %s", controlId));
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
            adder.setRegulating(control.enabled || p.asBoolean("tapChangerControlEnabled", false))
                    .setTargetV(control.targetValue);
        }
        adder.setLoadTapChangingCapabilities(true);
        setRegulatingTerminal(p, control, defaultTerminal, adder);
    }

    private void setRegulatingTerminal(PropertyBag p, RegulatingControl control, Terminal defaultTerminal, RatioTapChangerAdder adder) {
        if (context.terminalMapping().find(control.cgmesTerminal) != null) {
            adder.setRegulationTerminal(context.terminalMapping().find(control.cgmesTerminal));
        } else {
            adder.setRegulationTerminal(defaultTerminal);
            if (!context.terminalMapping().areAssociated(p.getId(TERMINAL), control.topologicalNode)) {
                control.idEq = p.getId("RatioTapChanger");
                return;
            }
        }
        cachedRegulatingControls.remove(p.getId(TAP_CHANGER_CONTROL));
    }

    public void setRegulatingControl(PropertyBag p, Terminal defaultTerminal, PhaseTapChangerAdder adder, TwoWindingsTransformer t2w) {
        if (p.containsKey(TAP_CHANGER_CONTROL)) {
            RegulatingControl control = cachedRegulatingControls.get(p.getId(TAP_CHANGER_CONTROL));
            if (control != null) {
                int side = context.tapChangerTransformers().whichSide(p.getId("PhaseTapChanger"));
                if (control.mode.endsWith("currentflow")) {
                    addCurrentFlowRegControl(p, control, defaultTerminal, adder, side, t2w);
                    return;
                } else if (control.mode.endsWith("activepower")) {
                    addActivePowerRegControl(p, control, defaultTerminal, adder, side, t2w);
                    return;
                } else if (!control.mode.endsWith("fixed")) {
                    context.fixed(control.mode, "Unsupported regulating mode for Phase tap changer. Considered as FIXED_TAP");
                }
                cachedRegulatingControls.remove(p.getId(TAP_CHANGER_CONTROL));
            } else {
                context.missing(String.format("Regulating control %s", p.getId(TAP_CHANGER_CONTROL)));
            }
        }
    }

    private void addCurrentFlowRegControl(PropertyBag p, RegulatingControl control, Terminal defaultTerminal, PhaseTapChangerAdder adder, int side, TwoWindingsTransformer t2w) {
        adder.setRegulationMode(PhaseTapChanger.RegulationMode.CURRENT_LIMITER)
                .setRegulationValue(getTargetValue(control.targetValue, control.cgmesTerminal, side, t2w))
                .setRegulating(control.enabled);
        setRegulatingTerminal(p, control, defaultTerminal, adder);
    }

    private void addActivePowerRegControl(PropertyBag p, RegulatingControl control, Terminal defaultTerminal, PhaseTapChangerAdder adder, int side, TwoWindingsTransformer t2w) {
        adder.setRegulationMode(PhaseTapChanger.RegulationMode.ACTIVE_POWER_CONTROL)
                .setRegulating(control.enabled)
                .setRegulationValue(getTargetValue(-control.targetValue, control.cgmesTerminal, side, t2w));
        setRegulatingTerminal(p, control, defaultTerminal, adder);
    }

    private double getTargetValue(double targetValue, String regTerminalId, int side, TwoWindingsTransformer t2w) {
        if ((context.terminalMapping().find(regTerminalId).equals(t2w.getTerminal1()) && side == 2)
                || (context.terminalMapping().find(regTerminalId).equals(t2w.getTerminal2()) && side == 1)) {
            return -targetValue;
        }
        return targetValue;
    }

    private void setRegulatingTerminal(PropertyBag p, RegulatingControl control, Terminal defaultTerminal, PhaseTapChangerAdder adder) {
        if (context.terminalMapping().find(control.cgmesTerminal) != null) {
            adder.setRegulationTerminal(context.terminalMapping().find(control.cgmesTerminal));
        } else {
            adder.setRegulationTerminal(defaultTerminal);
            if (!context.terminalMapping().areAssociated(p.getId(TERMINAL), control.topologicalNode)) {
                control.idEq = p.getId("PhaseTapChanger");
                return;
            }
        }
        cachedRegulatingControls.remove(p.getId(TAP_CHANGER_CONTROL));
    }

    public void setAllRemoteRegulatingTerminals() {
        cachedRegulatingControls.entrySet().removeIf(this::setRemoteRegulatingTerminal);
        cachedRegulatingControls.forEach((key, value) -> context.pending("Regulating terminal", String.format("The setting of the regulating terminal of the regulating control %s is not handled.", key)));
    }

    private boolean setRemoteRegulatingTerminal(Map.Entry<String, RegulatingControl> entry) {
        RegulatingControl control = entry.getValue();
        if (control.idEq != null) {
            Identifiable i = context.network().getIdentifiable(control.idEq);
            if (i == null) {
                String tc = control.idEq;
                if (context.tapChangerTransformers().transformer2(tc) != null) {
                    return setRemoteRegulatingTerminal(tc, control, context.tapChangerTransformers().transformer2(tc));
                } else if (context.tapChangerTransformers().transformer3(tc) != null) {
                    return setRemoteRegulatingTerminal(tc, control, context.tapChangerTransformers().transformer3(tc));
                }
            } else if (i instanceof Generator) {
                return setRemoteRegulatingTerminal(control, (Generator) i);
            }
        }
        return false;
    }

    private Terminal findRemoteRegulatingTerminal(String cgmesTerminal, String topologicalNode) {
        return Optional.ofNullable(context.terminalMapping().find(cgmesTerminal))
                .orElseGet(() -> context.terminalMapping().findFromTopologicalNode(topologicalNode));
    }

    private boolean setRemoteRegulatingTerminal(String tc, RegulatingControl control, TwoWindingsTransformer t2w) {
        Terminal regTerminal = findRemoteRegulatingTerminal(control.cgmesTerminal, control.topologicalNode);
        if (regTerminal == null) {
            context.missing(String.format(MISSING_IIDM_TERMINAL, control.topologicalNode));
            return false;
        }
        if (context.tapChangerTransformers().type(tc).equals("rtc")) {
            t2w.getRatioTapChanger().setRegulationTerminal(regTerminal);
            return true;
        } else if (context.tapChangerTransformers().type(tc).equals("ptc")) {
            t2w.getPhaseTapChanger().setRegulationTerminal(regTerminal);
            return true;
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

    private boolean setRemoteRegulatingTerminal(RegulatingControl control, Generator g) {
        Terminal regTerminal = findRemoteRegulatingTerminal(control.cgmesTerminal, control.topologicalNode);
        if (regTerminal == null) {
            context.missing(String.format(MISSING_IIDM_TERMINAL, control.topologicalNode));
            return false;
        }
        g.setRegulatingTerminal(regTerminal);
        return true;
    }
}
