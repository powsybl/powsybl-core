/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion;

import com.powsybl.cgmes.conversion.TransformerRegulatingControlMapping.RegulatingDataRatio;
import com.powsybl.cgmes.conversion.TransformerRegulatingControlMapping.ThreeWindingTransformerRegulatingData;
import com.powsybl.cgmes.conversion.GeneratorRegulatingControlMapping.GeneratorRegulatingData;
import com.powsybl.cgmes.conversion.TransformerRegulatingControlMapping.RegulatingDataPhase;
import com.powsybl.cgmes.conversion.TransformerRegulatingControlMapping.TwoWindingTransformerRegulatingData;
import com.powsybl.iidm.network.*;
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
    private static final String PHASE_TAP_CHANGER = "PhaseTapChanger";

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

    public void setRegulatingControl(String idEq, PropertyBag p, GeneratorAdder adder, VoltageLevel vl) {
        if (p.containsKey(REGULATING_CONTROL)) {
            String controlId = p.getId(REGULATING_CONTROL);
            RegulatingControl control = cachedRegulatingControls.get(controlId);
            if (control != null) {
                if (control.mode.endsWith(VOLTAGE)) {
                    setTargetValue(control.targetValue, vl.getNominalV(), controlId, adder);
                    adder.setVoltageRegulatorOn(control.enabled);
                    setRegulatingTerminal(control, p.getId(TERMINAL), idEq, adder);
                    return;
                } else {
                    context.ignored(control.mode, String.format("Unsupported regulation mode for generator %s", idEq));
                }
            } else {
                context.missing(String.format("Regulating control %s for equipment %s", controlId, idEq));
            }
        }
        adder.setVoltageRegulatorOn(false)
                .setTargetV(Double.NaN);
    }

    private void setRegulatingTerminal(RegulatingControl control, String terminalEq, String idEq, GeneratorAdder adder) {
        if (context.terminalMapping().find(control.cgmesTerminal) != null) {
            adder.setRegulatingTerminal(context.terminalMapping().find(control.cgmesTerminal));
        } else if (!context.terminalMapping().areAssociated(terminalEq, control.topologicalNode)) {
            control.idsEq.put(idEq, false);
            return;
        }
        control.idsEq.put(idEq, true);
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

    private void addRegulatingControlVoltage(PropertyBag p, RegulatingControl control, RatioTapChangerAdder adder,
        Terminal defaultTerminal, Context context) {
        // Even if regulating is false, we reset the target voltage if it is not valid
        if (control.targetValue <= 0) {
            context.ignored(p.getId(TAP_CHANGER_CONTROL),
                String.format("Regulating control has a bad target voltage %f", control.targetValue));
            adder.setRegulating(false)
                .setTargetV(Double.NaN);
        } else {
            adder.setRegulating(control.enabled || p.asBoolean("tapChangerControlEnabled", false))
                .setTargetDeadband(control.targetDeadband)
                .setTargetV(control.targetValue);
        }
        setRegulatingTerminal(p, control, defaultTerminal, adder);
    }

    private void setRegulatingTerminal(PropertyBag p, RegulatingControl control, Terminal defaultTerminal,
        RatioTapChangerAdder adder) {
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
                .setRegulating(control.enabled);
        setRegulatingTerminal(p, control, defaultTerminal, adder);
    }

    private void addActivePowerRegControl(PropertyBag p, RegulatingControl control, Terminal defaultTerminal, PhaseTapChangerAdder adder, int side, TwoWindingsTransformer t2w) {
        adder.setRegulationMode(PhaseTapChanger.RegulationMode.ACTIVE_POWER_CONTROL)
                .setTargetDeadband(control.targetDeadband)
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
        //cachedRegulatingControls.clear();
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
        setTwoWindingsTransformersRegulatingControl(network);
        setThreeWindingsTransformersRegulatingControl(network);

        cachedRegulatingControls.clear();
    }

    private void setGeneratorsRegulatingControl(Network network) {
        network.getGeneratorStream().forEach(gen -> {
            setGeneratorRegulatingControl(gen);
        });
    }

    private void setGeneratorRegulatingControl(Generator gen) {
        GeneratorRegulatingData rd = context.generatorRegulatingControlMapping().find(gen.getId());
        Terminal defaultTerminal = getGeneratorDefaultTerminal(gen);
        setGeneratorRegulatingControl(gen.getId(), defaultTerminal, rd, gen);
    }

    private Terminal getGeneratorDefaultTerminal(Generator gen) {
        return gen.getTerminal();
    }

    private void setGeneratorRegulatingControl(String genId, Terminal defaultTerminal, GeneratorRegulatingData rd,
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
            setGeneratorRegulatingControlVoltage(controlId, defaultTerminal, rd.nominalVoltage, control, context, gen);
        } else {
            context.ignored(control.mode, String.format("Unsupported regulation mode for generator %s", genId));
        }
    }

    private void setGeneratorRegulatingControlVoltage(String controlId, Terminal defaultTerminal, double nominalVoltage,
        RegulatingControl control, Context context, Generator gen) {

        Terminal terminal = findRegulatingTerminal(control.cgmesTerminal, control.topologicalNode);
        if (terminal == null) {
            terminal = defaultTerminal;
        }
        gen.setRegulatingTerminal(terminal);

        double targetV = Double.NaN;
        if (control.targetValue <= 0.0 || Double.isNaN(control.targetValue)) {
            targetV = nominalVoltage;
            context.fixed(controlId, "Invalid value for regulating target value", control.targetValue, nominalVoltage);
        } else {
            targetV = control.targetValue;
        }
        gen.setTargetV(targetV);

        boolean voltageRegulatorOn = false;
        if (control.enabled) {
            voltageRegulatorOn = true;
        }

        gen.setVoltageRegulatorOn(voltageRegulatorOn);
    }

    private void setTwoWindingsTransformersRegulatingControl(Network network) {
        network.getTwoWindingsTransformerStream().forEach(twt -> {
            setTwoWindingsTransformerRegulatingControl(twt);
        });
    }

    private void setTwoWindingsTransformerRegulatingControl(TwoWindingsTransformer twt) {
        TwoWindingTransformerRegulatingData rd = context.transformerRegulatingControlMapping().findTwo(twt.getId());

        if (rd != null) {
            Terminal defaultTerminalRatio = getT2wDefaultTerminalRatio(twt, rd.ratioTapChanger);
            setRtcRegulatingControl(defaultTerminalRatio, twt.getRatioTapChanger(), rd.ratioTapChanger);

            Terminal defaultTerminalPhase = getT2wDefaultTerminalPhase(twt, rd.phaseTapChanger);
            int targetValueSigne = getT2wTargetValueSignePhase(twt, rd.phaseTapChanger, context);
            setPtcRegulatingControl(defaultTerminalPhase, targetValueSigne, twt.getPhaseTapChanger(),
                rd.phaseTapChanger);
        }
    }

    private Terminal getT2wDefaultTerminalRatio(TwoWindingsTransformer t2w, RegulatingDataRatio rd) {
        if (rd == null || !rd.regulating) {
            return t2w.getTerminal1();
        }
        if (rd.side == 1) {
            return t2w.getTerminal1();
        } else {
            return t2w.getTerminal2();
        }
    }

    private Terminal getT2wDefaultTerminalPhase(TwoWindingsTransformer t2w, RegulatingDataPhase rd) {
        if (rd == null || !rd.regulating) {
            return t2w.getTerminal1();
        }
        if (rd.side == 1) {
            return t2w.getTerminal1();
        } else {
            return t2w.getTerminal2();
        }
    }

    private int getT2wTargetValueSignePhase(TwoWindingsTransformer t2w, RegulatingDataPhase rd,
        Context context) {
        if (rd == null || !rd.regulating) {
            return 1;
        }
        String controlId = rd.regulatingControlId;
        if (controlId == null) {
            return 1;
        }
        RegulatingControl control = cachedRegulatingControls.get(controlId);
        if (control == null) {
            return 1;
        }
        if ((context.terminalMapping().find(control.cgmesTerminal).equals(t2w.getTerminal1()) && rd.side == 2)
            || (context.terminalMapping().find(control.cgmesTerminal).equals(t2w.getTerminal2()) && rd.side == 1)) {
            return -1;
        }
        return 1;
    }

    private void setThreeWindingsTransformersRegulatingControl(Network network) {
        network.getThreeWindingsTransformerStream().forEach(twt -> {
            setThreeWindingsTransformerRegulatingControl(twt);
        });
    }

    private void setThreeWindingsTransformerRegulatingControl(ThreeWindingsTransformer twt) {
        ThreeWindingTransformerRegulatingData rd = context.transformerRegulatingControlMapping().findThree(twt.getId());
        if (rd != null) {
            TwoWindingTransformerRegulatingData rd1 = rd.winding1;
            if (rd1 != null) {
                setRtcRegulatingControl(twt.getLeg1().getTerminal(), twt.getLeg1().getRatioTapChanger(),
                    rd1.ratioTapChanger);
                setPtcRegulatingControl(twt.getLeg1().getTerminal(), 1, twt.getLeg1().getPhaseTapChanger(),
                    rd1.phaseTapChanger);
            }
            TwoWindingTransformerRegulatingData rd2 = rd.winding2;
            if (rd2 != null) {
                setRtcRegulatingControl(twt.getLeg2().getTerminal(), twt.getLeg2().getRatioTapChanger(),
                    rd2.ratioTapChanger);
                setPtcRegulatingControl(twt.getLeg2().getTerminal(), 1, twt.getLeg2().getPhaseTapChanger(),
                    rd2.phaseTapChanger);
            }
            TwoWindingTransformerRegulatingData rd3 = rd.winding3;
            if (rd3 != null) {
                setRtcRegulatingControl(twt.getLeg3().getTerminal(), twt.getLeg3().getRatioTapChanger(),
                    rd3.ratioTapChanger);
                setPtcRegulatingControl(twt.getLeg3().getTerminal(), 1, twt.getLeg3().getPhaseTapChanger(),
                    rd3.phaseTapChanger);
            }
        }
    }

    private void setRtcRegulatingControl(Terminal defaultTerminal, RatioTapChanger rtc,
        RegulatingDataRatio rd) {
        if (rd == null || !rd.regulating || rtc == null) {
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

        if (isControlModeVoltage(control.mode, rd.tculControlMode)) {
            setRtcRegulatingControlVoltage(defaultTerminal, rd.id, rd.tapChangerControlEnabled, control, rtc, context);
        } else if (!isControlModeFixed(control.mode)) {
            context.fixed(control.mode,
                "Unsupported regulation mode for Ratio tap changer. Considered as a fixed ratio tap changer.");
        }
    }

    private boolean isControlModeVoltage(String controlMode) {
        if (controlMode != null && controlMode.endsWith("voltage")) {
            return true;
        }
        return false;
    }

    private boolean isControlModeVoltage(String controlMode, String tculControlMode) {
        if (isControlModeVoltage(controlMode)) {
            return true;
        }
        if (tculControlMode != null && tculControlMode.endsWith("volt")) {
            return true;
        }
        return false;
    }

    private boolean isControlModeFixed(String controlMode) {
        if (controlMode != null && controlMode.endsWith("fixed")) {
            return true;
        }
        return false;
    }

    private void setRtcRegulatingControlVoltage(Terminal defaultTerminal, String rtcId,
        boolean tapChangerControlEnabled, RegulatingControl control,
        RatioTapChanger rtc, Context context) {

        Terminal terminal = findRegulatingTerminal(control.cgmesTerminal, control.topologicalNode);
        if (terminal != null) {
            rtc.setRegulationTerminal(terminal);
        } else {
            rtc.setRegulationTerminal(defaultTerminal);
        }

        // Even if regulating is false, we reset the target voltage if it is not valid
        if (control.targetValue <= 0) {
            context.ignored(rtcId,
                String.format("Regulating control has a bad target voltage %f", control.targetValue));
            rtc.setRegulating(false).setTargetDeadband(Double.NaN).setTargetV(Double.NaN);
        } else {
            // Order it is important
            rtc.setTargetDeadband(control.targetDeadband)
                .setTargetV(control.targetValue);
            rtc.setRegulating(control.enabled || tapChangerControlEnabled);
        }
    }

    private void setPtcRegulatingControl(Terminal defaultTerminal, int targetValueSigne, PhaseTapChanger ptc,
        RegulatingDataPhase rd) {
        if (rd == null || !rd.regulating || ptc == null) {
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

        if (control.mode.endsWith("currentflow")) {
            setPtcRegulatingControlCurrentFlow(defaultTerminal, control, targetValueSigne, ptc, context);
        } else if (control.mode.endsWith("activepower")) {
            setPtcRegulatingControlActivePower(defaultTerminal, control, targetValueSigne, ptc, context);
        } else if (!control.mode.endsWith("fixed")) {
            context.fixed(control.mode, "Unsupported regulating mode for Phase tap changer. Considered as FIXED_TAP");
        }
    }

    private void setPtcRegulatingControlCurrentFlow(Terminal defaultTerminal, RegulatingControl control,
        int targetValueSigne, PhaseTapChanger ptc, Context context) {
        // order it is important
        ptc.setRegulationMode(PhaseTapChanger.RegulationMode.CURRENT_LIMITER)
            .setTargetDeadband(control.targetDeadband)
            .setRegulationValue(getTargetValue(control.targetValue, targetValueSigne));
        ptc.setRegulating(control.enabled);

        setRegulatingTerminal(defaultTerminal, control, ptc);
    }

    private void setPtcRegulatingControlActivePower(Terminal defaultTerminal, RegulatingControl control,
        int targetValueSigne, PhaseTapChanger ptc, Context context) {
        // Order it is important
        ptc.setRegulationMode(PhaseTapChanger.RegulationMode.ACTIVE_POWER_CONTROL)
            .setTargetDeadband(control.targetDeadband)
            .setRegulationValue(getTargetValue(-control.targetValue, targetValueSigne));
        ptc.setRegulating(control.enabled);

        setRegulatingTerminal(defaultTerminal, control, ptc);
    }

    private void setRegulatingTerminal(Terminal defaultTerminal, RegulatingControl control, PhaseTapChanger ptc) {
        Terminal terminal = findRegulatingTerminal(control.cgmesTerminal, control.topologicalNode);
        if (terminal != null) {
            ptc.setRegulationTerminal(terminal);
        } else {
            ptc.setRegulationTerminal(defaultTerminal);
        }
    }

    private double getTargetValue(double targetValue, int signe) {
        return targetValue * signe;
    }

    private Terminal findRegulatingTerminal(String cgmesTerminal, String topologicalNode) {
        return Optional.ofNullable(context.terminalMapping().find(cgmesTerminal))
            .orElseGet(() -> context.terminalMapping().findFromTopologicalNode(topologicalNode));
    }

    public RegulatingControlId getTapChangerRegulatingControl(PropertyBag p) {
        boolean regulating = false;
        String regulatingControlId = null;

        if (p.containsKey(TAP_CHANGER_CONTROL)) {
            String controlId = p.getId(TAP_CHANGER_CONTROL);

            RegulatingControl control = cachedRegulatingControls.get(controlId);
            if (control != null) {
                regulating = true;
                regulatingControlId = controlId;
            }
        }

        RegulatingControlId rci = new RegulatingControlId();
        rci.regulating = regulating;
        rci.regulatingControlId = regulatingControlId;

        return rci;
    }

    public RegulatingControlId getGeneratorRegulatingControl(PropertyBag p) {
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
        RegulatingControlId rci = new RegulatingControlId();
        rci.regulating = regulating;
        rci.regulatingControlId = regulatingControlId;

        return rci;
    }

    public static class RegulatingControlId {
        public boolean regulating;
        public String regulatingControlId;
    }
}
