/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion;

import com.powsybl.cgmes.conversion.GeneratorRegulatingControlMapping.GeneratorRegulatingData;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.CoordinatedReactiveControl;
import com.powsybl.triplestore.api.PropertyBag;

import java.util.*;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
public class RegulatingControlMapping {

    private static final String REGULATING_CONTROL = "RegulatingControl";
    private static final String TAP_CHANGER_CONTROL = "TapChangerControl";
    private static final String TERMINAL = "Terminal";
    private static final String MISSING_IIDM_TERMINAL = "IIDM terminal for this CGMES topological node: %s";
    private static final String VOLTAGE = "voltage";
    private static final String REGULATING_CONTROL_REF = "Regulating control %s";
    private static final String TAP_CHANGER_CONTROL_ENABLED = "tapChangerControlEnabled";
    private static final String PHASE_TAP_CHANGER = "PhaseTapChanger";
    private static final String QPERCENT = "qPercent";

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
        private final double targetDeadband;

        private final Map<String, Boolean> idsEq = new HashMap<>();

        RegulatingControl(PropertyBag p) {
            this.mode = p.get("mode").toLowerCase();
            this.cgmesTerminal = p.getId(TERMINAL);
            this.topologicalNode = p.getId("topologicalNode");
            this.enabled = p.asBoolean("enabled", true);
            this.targetValue = p.asDouble("targetValue");
            this.targetDeadband = p.asDouble("targetDeadband", Double.NaN);
        }
    }

    private Map<String, RegulatingControl> cachedRegulatingControls = new HashMap<>();

    public void cacheRegulatingControls(PropertyBag p) {
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

    public void setRegulatingControl(PropertyBag p, Terminal defaultTerminal, PhaseTapChangerAdder adder, TwoWindingsTransformer t2w) {
        if (p.containsKey(TAP_CHANGER_CONTROL)) {
            RegulatingControl control = cachedRegulatingControls.get(p.getId(TAP_CHANGER_CONTROL));
            if (control != null) {
                int side = context.tapChangerTransformers().whichSide(p.getId(PHASE_TAP_CHANGER));
                if (control.mode.endsWith("currentflow")) {
                    addCurrentFlowRegControl(p, control, defaultTerminal, adder, side, t2w);
                } else if (control.mode.endsWith("activepower")) {
                    addActivePowerRegControl(p, control, defaultTerminal, adder, side, t2w);
                } else if (!control.mode.endsWith("fixed")) {
                    context.fixed(control.mode, "Unsupported regulating mode for Phase tap changer. Considered as FIXED_TAP");
                }
            } else {
                context.missing(String.format(REGULATING_CONTROL_REF, p.getId(TAP_CHANGER_CONTROL)));
            }
        }
    }

    private void addCurrentFlowRegControl(PropertyBag p, RegulatingControl control, Terminal defaultTerminal, PhaseTapChangerAdder adder, int side, TwoWindingsTransformer t2w) {
        adder.setRegulationMode(PhaseTapChanger.RegulationMode.CURRENT_LIMITER)
                .setRegulationValue(getTargetValue(control.targetValue, control.cgmesTerminal, side, t2w))
                .setTargetDeadband(control.targetDeadband)
                .setRegulating(control.enabled || p.asBoolean(TAP_CHANGER_CONTROL_ENABLED, false));
        setRegulatingTerminal(p, control, defaultTerminal, adder);
    }

    private void addActivePowerRegControl(PropertyBag p, RegulatingControl control, Terminal defaultTerminal, PhaseTapChangerAdder adder, int side, TwoWindingsTransformer t2w) {
        adder.setRegulationMode(PhaseTapChanger.RegulationMode.ACTIVE_POWER_CONTROL)
                .setTargetDeadband(control.targetDeadband)
                .setRegulating(control.enabled || p.asBoolean(TAP_CHANGER_CONTROL_ENABLED, false))
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
            control.idsEq.put(p.getId(PHASE_TAP_CHANGER), true);
        } else {
            adder.setRegulationTerminal(defaultTerminal);
            if (!context.terminalMapping().areAssociated(p.getId(TERMINAL), control.topologicalNode)) {
                control.idsEq.put(p.getId(PHASE_TAP_CHANGER), false);
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

    public void setAllRemoteRegulatingTerminals() {
        cachedRegulatingControls.entrySet().removeIf(this::setRemoteRegulatingTerminal);
        cachedRegulatingControls.forEach((key, value) -> context.pending("Regulating terminal",
                String.format("The setting of the regulating terminal of the regulating control %s is not entirely handled.", key)));
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
                    } else if (i instanceof Generator) {
                        correctlySet = correctlySet && setRemoteRegulatingTerminal(control, (Generator) i);
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
            return setRemoteRegulatingTerminal(tc, control, context.tapChangerTransformers().transformer2(tc));
        } else if (context.tapChangerTransformers().transformer3(tc) != null) {
            return setRemoteRegulatingTerminal(tc, control, context.tapChangerTransformers().transformer3(tc));
        }
        return false;
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

    public void setAllRegulatingControls(Network network) {
        setGeneratorsRegulatingControl(network);

        cachedRegulatingControls.clear();
    }

    private void setGeneratorsRegulatingControl(Network network) {
        network.getGeneratorStream().forEach(gen -> {
            setGeneratorRegulatingControl(gen);
        });
    }

    private void setGeneratorRegulatingControl(Generator gen) {
        GeneratorRegulatingData rd = context.generatorRegulatingControlMapping().find(gen.getId());
        setGeneratorRegulatingControl(gen.getId(), rd, gen);
    }

    private void setGeneratorRegulatingControl(String genId, GeneratorRegulatingData rd,
        Generator gen) {
        if (rd == null || !rd.regulating) {
            return;
        }

        String controlId = rd.regulatingControlId;
        if (controlId == null) {
            context.missing(String.format("Regulating control Id not defined"));
            return;
        }

        RegulatingControl control = cachedRegulatingControls.get(controlId);
        if (control == null) {
            context.missing(String.format("Regulating control %s", controlId));
            return;
        }

        if (isControlModeVoltage(control.mode)) {
            GeneratorControlVoltage gcv = getGeneratorRegulatingControlVoltage(controlId, control, rd.qPercent, context,
                gen);
            setGeneratorRegulatingControlVoltage(gcv, gen);
        } else {
            context.ignored(control.mode, String.format("Unsupported regulation mode for generator %s", genId));
        }
    }

    private GeneratorControlVoltage getGeneratorRegulatingControlVoltage(String controlId,
        RegulatingControl control, double qPercent, Context context, Generator gen) {

        // Take default terminal if it has not been defined
        Terminal terminal = getGeneratorRegulatingTerminal(gen, control.cgmesTerminal, control.topologicalNode);
        if (terminal == null) {
            context.missing(String.format(MISSING_IIDM_TERMINAL, control.topologicalNode));
            return null;
        }

        double targetV = Double.NaN;
        if (control.targetValue <= 0.0 || Double.isNaN(control.targetValue)) {
            targetV = terminalNominalVoltage(terminal);
            context.fixed(controlId, "Invalid value for regulating target value", control.targetValue, targetV);
        } else {
            targetV = control.targetValue;
        }

        boolean voltageRegulatorOn = false;
        if (control.enabled) {
            voltageRegulatorOn = true;
        }

        GeneratorControlVoltage gcv = new GeneratorControlVoltage();
        gcv.terminal = terminal;
        gcv.targetV = targetV;
        gcv.voltageRegulatorOn = voltageRegulatorOn;
        gcv.qPercent = qPercent;

        return gcv;
    }

    public void initializeGeneratorRegulatingControl(GeneratorAdder adder) {
        adder.setRegulatingTerminal(null);
        adder.setTargetV(Double.NaN);
        adder.setVoltageRegulatorOn(false);
    }

    private void setGeneratorRegulatingControlVoltage(GeneratorControlVoltage gcv, Generator gen) {
        if (gcv == null) {
            return;
        }
        gen.setRegulatingTerminal(gcv.terminal);
        gen.setTargetV(gcv.targetV);
        gen.setVoltageRegulatorOn(gcv.voltageRegulatorOn);

        // add qPercent as an extension
        if (!Double.isNaN(gcv.qPercent)) {
            CoordinatedReactiveControl coordinatedReactiveControl = new CoordinatedReactiveControl(gen, gcv.qPercent);
            gen.addExtension(CoordinatedReactiveControl.class, coordinatedReactiveControl);
        }
    }

    private boolean isControlModeVoltage(String controlMode) {
        if (controlMode != null && controlMode.endsWith("voltage")) {
            return true;
        }
        return false;
    }

    private Terminal getGeneratorRegulatingTerminal(Generator gen, String cgmesTerminal, String topologicalNode) {
        // Will take default terminal ONLY if it has not been explicitly defined in CGMES
        Terminal terminal = getGeneratorDefaultTerminal(gen);
        if (cgmesTerminal != null || topologicalNode != null) {
            terminal = findRegulatingTerminal(cgmesTerminal, topologicalNode);
            // If terminal is null here it means that no IIDM terminal has been found
            // from the initial CGMES terminal or topological node,
            // we will consider the regulating control invalid,
            // in this case we will not use the default terminal
            // (no localization of regulating controls)
        }
        return terminal;
    }

    private Terminal getGeneratorDefaultTerminal(Generator gen) {
        return gen.getTerminal();
    }

    private Terminal findRegulatingTerminal(String cgmesTerminal, String topologicalNode) {
        return Optional.ofNullable(context.terminalMapping().find(cgmesTerminal))
            .orElseGet(() -> context.terminalMapping().findFromTopologicalNode(topologicalNode));
    }

    private double terminalNominalVoltage(Terminal terminal) {
        return terminal.getVoltageLevel().getNominalV();
    }

    static class GeneratorControlVoltage {
        Terminal terminal;
        double targetV;
        boolean voltageRegulatorOn;
        double qPercent;
    }

    public RegulatingControlId getGeneratorRegulatingControlId(PropertyBag p) {
        boolean regulating = false;
        String regulatingControlId = null;

        if (p.containsKey(REGULATING_CONTROL)) {
            String controlId = p.getId(REGULATING_CONTROL);
            RegulatingControl control = cachedRegulatingControls.get(controlId);
            if (control != null) {
                regulating = true;
                regulatingControlId = controlId;
            }
        }

        return new RegulatingControlId(regulating, regulatingControlId);
    }

    public double getGeneratorQpercent(PropertyBag p) {
        double qPercent = Double.NaN;
        if (p.containsKey(QPERCENT)) {
            qPercent = p.asDouble(QPERCENT);
        }
        return qPercent;
    }

    public static class RegulatingControlId {
        RegulatingControlId(boolean regulating, String regulatingControlIdValue) {
            this.regulating = regulating;
            this.regulatingControlIdValue = regulatingControlIdValue;
        }

        public boolean isRegulating() {
            return regulating;
        }

        public String getRegulatingControlId() {
            return this.regulatingControlIdValue;
        }

        private final boolean regulating;
        private final String regulatingControlIdValue;
    }
}
