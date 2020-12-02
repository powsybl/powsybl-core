/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion;

import com.powsybl.cgmes.conversion.RegulatingControlMapping.RegulatingControl;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.PhaseTapChanger.RegulationMode;
import com.powsybl.triplestore.api.PropertyBag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author José Antonio Marqués <marquesja at aia.es>
 * @author Marcos de Miguel <demiguelm at aia.es>
 */

public class RegulatingControlMappingForTransformers {

    private static final String TAP_CHANGER_CONTROL = "TapChangerControl";

    RegulatingControlMappingForTransformers(RegulatingControlMapping parent, Context context) {
        this.parent = parent;
        this.context = context;
        t2xMapping = new HashMap<>();
        t3xMapping = new HashMap<>();
    }

    public void add(String transformerId, CgmesRegulatingControlRatio rcRtc, CgmesRegulatingControlPhase rcPtc) {
        CgmesRegulatingControlForTwoWindingsTransformer rc = new CgmesRegulatingControlForTwoWindingsTransformer();
        rc.ratioTapChanger = rcRtc;
        rc.phaseTapChanger = rcPtc;
        t2xMapping.put(transformerId, rc);
    }

    public void add(String transformerId, CgmesRegulatingControlRatio rcRtc1, CgmesRegulatingControlPhase rcPtc1,
                    CgmesRegulatingControlRatio rcRtc2, CgmesRegulatingControlPhase rcPtc2, CgmesRegulatingControlRatio rcRtc3,
                    CgmesRegulatingControlPhase rcPtc3) {
        CgmesRegulatingControlForThreeWindingsTransformer rc = new CgmesRegulatingControlForThreeWindingsTransformer();
        rc.ratioTapChanger1 = rcRtc1;
        rc.phaseTapChanger1 = rcPtc1;
        rc.ratioTapChanger2 = rcRtc2;
        rc.phaseTapChanger2 = rcPtc2;
        rc.ratioTapChanger3 = rcRtc3;
        rc.phaseTapChanger3 = rcPtc3;
        t3xMapping.put(transformerId, rc);
    }

    public CgmesRegulatingControlRatio buildRegulatingControlRatio(String id, String regulatingControlId,
                                                                   String tculControlMode, boolean tapChangerControlEnabled) {
        CgmesRegulatingControlRatio rtc = new CgmesRegulatingControlRatio();
        rtc.id = id;
        rtc.regulatingControlId = regulatingControlId;
        rtc.tculControlMode = tculControlMode;
        rtc.tapChangerControlEnabled = tapChangerControlEnabled;
        return rtc;
    }

    public CgmesRegulatingControlPhase buildRegulatingControlPhase(String regulatingControlId,
        boolean tapChangerControlEnabled, boolean ltcFlag) {
        CgmesRegulatingControlPhase rtc = new CgmesRegulatingControlPhase();
        rtc.regulatingControlId = regulatingControlId;
        rtc.tapChangerControlEnabled = tapChangerControlEnabled;
        rtc.ltcFlag = ltcFlag;
        return rtc;
    }

    void applyTapChangersRegulatingControl(Network network) {
        network.getTwoWindingsTransformerStream().forEach(this::applyTapChangersRegulatingControl);
        network.getThreeWindingsTransformerStream().forEach(this::applyTapChangersRegulatingControl);
    }

    private void applyTapChangersRegulatingControl(TwoWindingsTransformer twt) {
        CgmesRegulatingControlForTwoWindingsTransformer rc = t2xMapping.get(twt.getId());
        applyTapChangersRegulatingControl(twt, rc);
    }

    private void applyTapChangersRegulatingControl(TwoWindingsTransformer twt,
                                                   CgmesRegulatingControlForTwoWindingsTransformer rc) {
        if (rc == null) {
            return;
        }

        RegulatingControl rtcControl = getTapChangerControl(rc.ratioTapChanger);
        boolean rtcRegulating = rtcControl != null && (rtcControl.enabled || rc.ratioTapChanger.tapChangerControlEnabled);
        RegulatingControl ptcControl = getTapChangerControl(rc.phaseTapChanger);
        boolean ptcRegulating = ptcControl != null && (ptcControl.enabled || rc.phaseTapChanger.tapChangerControlEnabled);

        setPhaseTapChangerControl(ptcRegulating, rc.phaseTapChanger, ptcControl, twt.getPhaseTapChanger(), twt.getTerminals());
        boolean regulatingSet = twt.hasPhaseTapChanger() && twt.getPhaseTapChanger().isRegulating();

        rtcRegulating = checkOnlyOneEnabled(twt.getId(), rtcRegulating, regulatingSet, "ratioTapChanger");
        setRatioTapChangerControl(rtcRegulating, rc.ratioTapChanger, rtcControl, twt.getRatioTapChanger());
    }

    private void applyTapChangersRegulatingControl(ThreeWindingsTransformer twt) {
        CgmesRegulatingControlForThreeWindingsTransformer rc = t3xMapping.get(twt.getId());
        applyTapChangersRegulatingControl(twt, rc);
    }

    private void applyTapChangersRegulatingControl(ThreeWindingsTransformer twt,
                                                   CgmesRegulatingControlForThreeWindingsTransformer rc) {
        if (rc == null) {
            return;
        }

        RegulatingControl rtcControl1 = getTapChangerControl(rc.ratioTapChanger1);
        boolean rtcRegulating1 = getRtcRegulating(rtcControl1, rc.ratioTapChanger1);
        RegulatingControl ptcControl1 = getTapChangerControl(rc.phaseTapChanger1);
        boolean ptcRegulating1 = getPtcRegulating(ptcControl1, rc.phaseTapChanger1);

        RegulatingControl rtcControl2 = getTapChangerControl(rc.ratioTapChanger2);
        boolean rtcRegulating2 = getRtcRegulating(rtcControl2, rc.ratioTapChanger2);
        RegulatingControl ptcControl2 = getTapChangerControl(rc.phaseTapChanger2);
        boolean ptcRegulating2 = getPtcRegulating(ptcControl2, rc.phaseTapChanger2);

        RegulatingControl rtcControl3 = getTapChangerControl(rc.ratioTapChanger3);
        boolean rtcRegulating3 = getRtcRegulating(rtcControl3, rc.ratioTapChanger3);
        RegulatingControl ptcControl3 = getTapChangerControl(rc.phaseTapChanger3);
        boolean ptcRegulating3 = getPtcRegulating(ptcControl3, rc.phaseTapChanger3);

        setPhaseTapChangerControl(ptcRegulating1, rc.phaseTapChanger1, ptcControl1, twt.getLeg1().getPhaseTapChanger(), twt.getTerminals());
        boolean regulatingSet = twt.getLeg1().hasPhaseTapChanger() && twt.getLeg1().getPhaseTapChanger().isRegulating();

        rtcRegulating1 = checkOnlyOneEnabled(twt.getId(), rtcRegulating1, regulatingSet, "ratioTapChanger at Leg1");
        setRatioTapChangerControl(rtcRegulating1, rc.ratioTapChanger1, rtcControl1, twt.getLeg1().getRatioTapChanger());
        regulatingSet = regulatingSet || (twt.getLeg1().hasRatioTapChanger() && twt.getLeg1().getRatioTapChanger().isRegulating());

        ptcRegulating2 = checkOnlyOneEnabled(twt.getId(), ptcRegulating2, regulatingSet, "phaseTapChanger at Leg2");
        setPhaseTapChangerControl(ptcRegulating2, rc.phaseTapChanger2, ptcControl2, twt.getLeg2().getPhaseTapChanger(), twt.getTerminals());
        regulatingSet = regulatingSet || (twt.getLeg2().hasPhaseTapChanger() && twt.getLeg2().getPhaseTapChanger().isRegulating());

        rtcRegulating2 = checkOnlyOneEnabled(twt.getId(), rtcRegulating2, regulatingSet, "ratioTapChanger at Leg2");
        setRatioTapChangerControl(rtcRegulating2, rc.ratioTapChanger2, rtcControl2, twt.getLeg2().getRatioTapChanger());
        regulatingSet = regulatingSet || (twt.getLeg2().hasRatioTapChanger() && twt.getLeg2().getRatioTapChanger().isRegulating());

        ptcRegulating3 = checkOnlyOneEnabled(twt.getId(), ptcRegulating3, regulatingSet, "phaseTapChanger at Leg3");
        setPhaseTapChangerControl(ptcRegulating3, rc.phaseTapChanger3, ptcControl3, twt.getLeg3().getPhaseTapChanger(), twt.getTerminals());
        regulatingSet = regulatingSet || (twt.getLeg3().hasPhaseTapChanger() && twt.getLeg3().getPhaseTapChanger().isRegulating());

        rtcRegulating3 = checkOnlyOneEnabled(twt.getId(), rtcRegulating3, regulatingSet, "ratioTapChanger at Leg3");
        setRatioTapChangerControl(rtcRegulating3, rc.ratioTapChanger3, rtcControl3, twt.getLeg3().getRatioTapChanger());
    }

    private static boolean getRtcRegulating(RegulatingControl rc, CgmesRegulatingControlRatio r) {
        return rc != null && (rc.enabled || r.tapChangerControlEnabled);
    }

    private static boolean getPtcRegulating(RegulatingControl rc, CgmesRegulatingControlPhase r) {
        return rc != null && (rc.enabled || r.tapChangerControlEnabled);
    }

    private boolean checkOnlyOneEnabled(String transformerId, boolean regulating, boolean setRegulating,
                                        String disabledTapChanger) {
        if (!regulating) {
            return false;
        }
        if (setRegulating) {
            context.fixed(transformerId,
                "Unsupported more than one regulating control enabled. Disable " + disabledTapChanger);
            return false;
        }
        return true;
    }

    private void setRatioTapChangerControl(boolean regulating, CgmesRegulatingControlRatio rc,
                                           RegulatingControl control, RatioTapChanger rtc) {
        if (control == null || rtc == null) {
            return;
        }

        boolean okSet = false;
        if (isControlModeVoltage(control.mode, rc.tculControlMode)) {
            okSet = setRtcRegulatingControlVoltage(rc.id, regulating, control, rtc, context);
        } else if (!isControlModeFixed(control.mode)) {
            context.fixed(control.mode,
                "Unsupported regulation mode for Ratio tap changer. Considered as a fixed ratio tap changer.");
        }
        control.setCorrectlySet(okSet);
    }

    private boolean setRtcRegulatingControlVoltage(String rtcId, boolean regulating, RegulatingControl control,
                                                   RatioTapChanger rtc, Context context) {
        Terminal terminal = parent.findRegulatingTerminal(control.cgmesTerminal);
        if (terminal == null) {
            context.missing(String.format(RegulatingControlMapping.MISSING_IIDM_TERMINAL, control.cgmesTerminal));
            return false;
        }

        // We always keep the targetValue
        // It targetValue is not valid, emit a warning and deactivate regulating control
        boolean validTargetValue = control.targetValue > 0;
        if (!validTargetValue) {
            context.invalid(rtcId,
                "Regulating control has a bad target voltage " + control.targetValue);
        }

        // Order is important
        rtc.setRegulationTerminal(terminal)
                .setTargetV(control.targetValue)
                .setTargetDeadband(control.targetDeadband)
                .setRegulating(regulating && validTargetValue);

        return true;
    }

    private void setPhaseTapChangerControl(boolean regulating, CgmesRegulatingControlPhase rc,
        RegulatingControl control, PhaseTapChanger ptc, List<? extends Terminal> transformerTerminals) {
        if (control == null || ptc == null) {
            return;
        }

        boolean okSet = false;
        if (control.mode.endsWith("currentflow")) {
            okSet = setPtcRegulatingControl(regulating, getPtcRegulatingMode(rc.ltcFlag, RegulationMode.CURRENT_LIMITER), control, ptc, context, transformerTerminals);
        } else if (control.mode.endsWith("activepower")) {
            okSet = setPtcRegulatingControl(regulating, getPtcRegulatingMode(rc.ltcFlag, RegulationMode.ACTIVE_POWER_CONTROL), control, ptc, context, transformerTerminals);
        } else if (!control.mode.endsWith("fixed")) {
            context.fixed(control.mode, "Unsupported regulating mode for Phase tap changer. Considered as FIXED_TAP");
        }
        control.setCorrectlySet(okSet);
    }

    private boolean setPtcRegulatingControl(boolean regulating, RegulationMode regulationMode,
        RegulatingControl control, PhaseTapChanger ptc, Context context, List<? extends Terminal> transformerTerminals) {
        Terminal regulatingTerminal = parent.findRegulatingTerminal(control.cgmesTerminal);
        if (regulatingTerminal == null) {
            context.missing(String.format(RegulatingControlMapping.MISSING_IIDM_TERMINAL, control.cgmesTerminal));
            return false;
        }
        if (!isValidTerminalForRegulatingFlow(control.cgmesTerminal, regulatingTerminal)) {
            regulatingTerminal = fixRegulatingTerminalForFlow(control.cgmesTerminal, regulatingTerminal, transformerTerminals);
            if (regulatingTerminal == null) {
                context.invalid(RegulatingControlMapping.REGULATING_TERMINAL, () -> String.format(RegulatingControlMapping.INVALID_CGMES_REGULATING_TERMINAL, control.cgmesTerminal));
                return false;
            }
            context.fixed(RegulatingControlMapping.REGULATING_TERMINAL,  () -> String.format(RegulatingControlMapping.INVALID_CGMES_REGULATING_TERMINAL_FIXED, control.cgmesTerminal));
        }

        // Order is important
        ptc.setRegulationTerminal(regulatingTerminal)
                .setRegulationValue(control.targetValue)
                .setTargetDeadband(control.targetDeadband)
                .setRegulationMode(regulationMode)
                .setRegulating(regulating);

        return true;
    }

    private RegulationMode getPtcRegulatingMode(boolean ltcFlag, RegulationMode regulationMode) {
        // According to the following CGMES documentation:
        // IEC TS 61970-600-1, Edition 1.0, 2017-07.
        // "Energy management system application program interface (EMS-API)
        // – Part 600-1: Common Grid Model Exchange Specification (CGMES)
        // – Structure and rules",
        // "Annex E (normative) implementation guide",
        // section "E.9 LTCflag" (pages 76-79)

        // The combination: TapChanger.ltcFlag == False
        // and TapChanger.TapChangerControl Present
        // Is allowed as:
        // "An artificial tap changer can be used to simulate control behavior on power
        // flow"

        // But the ENTSO-E documentation
        // "QUALITY OF CGMES DATASETS AND CALCULATIONS FOR SYSTEM OPERATIONS"
        // 3.1 EDITION, 13 June 2019

        // Contains a rule that states that when ltcFlag == False,
        // Then TapChangerControl should NOT be present

        // Although this combination has been observed in TYNDP test cases,
        // we will forbid it until an explicit ltcFlag is added to IIDM,
        // in the meanwhile, when ltcFlag == False,
        // we avoid regulation by setting RegulationMode in IIDM to FIXED_TAP

        // rca.regulationMode has been initialized to FIXED_TAP

        RegulationMode finalRegulationMode = RegulationMode.FIXED_TAP;
        if (ltcFlag) {
            finalRegulationMode = regulationMode;
        }
        return finalRegulationMode;
    }

    private RegulatingControl getTapChangerControl(CgmesRegulatingControl rc) {
        if (rc == null) {
            return null;
        }

        String controlId = rc.regulatingControlId;
        if (controlId == null) {
            context.missing("Regulating control ID not defined");
            return null;
        }

        RegulatingControl control = parent.cachedRegulatingControls().get(controlId);
        if (control == null) {
            context.missing(String.format("Regulating control %s", controlId));
            return null;
        }

        return control;
    }

    public static String getRegulatingControlId(PropertyBag p) {
        return p.getId(TAP_CHANGER_CONTROL);
    }

    public boolean getRegulating(String controlId) {
        if (controlId != null) {
            RegulatingControl control = parent.cachedRegulatingControls().get(controlId);
            if (control != null) {
                return control.enabled;
            }
        }
        return false;
    }

    private static boolean isControlModeVoltage(String controlMode, String tculControlMode) {
        if (RegulatingControlMapping.isControlModeVoltage(controlMode)) {
            return true;
        }
        return tculControlMode != null && tculControlMode.endsWith("volt");
    }

    private static boolean isControlModeFixed(String controlMode) {
        return controlMode != null && controlMode.endsWith("fixed");
    }

    private static class CgmesRegulatingControlForThreeWindingsTransformer {
        CgmesRegulatingControlRatio ratioTapChanger1;
        CgmesRegulatingControlPhase phaseTapChanger1;
        CgmesRegulatingControlRatio ratioTapChanger2;
        CgmesRegulatingControlPhase phaseTapChanger2;
        CgmesRegulatingControlRatio ratioTapChanger3;
        CgmesRegulatingControlPhase phaseTapChanger3;
    }

    private static class CgmesRegulatingControlForTwoWindingsTransformer {
        CgmesRegulatingControlRatio ratioTapChanger;
        CgmesRegulatingControlPhase phaseTapChanger;
    }

    public static class CgmesRegulatingControlRatio extends CgmesRegulatingControl {
        String tculControlMode; // mode in SSH values of RTC
    }

    public static class CgmesRegulatingControlPhase extends CgmesRegulatingControl {
        boolean ltcFlag;
    }

    private static class CgmesRegulatingControl {
        String id;
        String regulatingControlId;
        boolean tapChangerControlEnabled; // enabled status in SSH values of PTC/RTC
    }

    private static boolean isValidTerminalForRegulatingFlow(String regulatingTerminalId, Terminal regulatingTerminal) {
        // When regulated magnitude is a flow (current or active power)
        // regulating terminal must be a terminal where a flow can be measured
        if (regulatingTerminal.getVoltageLevel().getTopologyKind() == TopologyKind.NODE_BREAKER
            && regulatingTerminal.getConnectable().getType() == ConnectableType.BUSBAR_SECTION) {
            LOG.error("Substation {}. Regulating terminal {} is target for a flow magnitude but points to a Busbar section",
                regulatingTerminal.getVoltageLevel().getSubstation().getNameOrId(),
                regulatingTerminalId);
            return false;
        }
        return true;
    }

    private static Terminal fixRegulatingTerminalForFlow(String regulatingTerminalId, Terminal regulatingTerminal, List<? extends Terminal> transformerTerminals) {
        // If the regulating terminal is invalid
        // we will try to change it for one of the transformer terminals,
        // but only if regulating terminal and the transformer terminal
        // are at the same connectivity node.
        // Terminals are at the same connectivity node if they are at the same voltageLevel
        // and are connected only through internal connections in node/breaker view
        Set<Integer> transformerNodes = transformerTerminals.stream()
            .filter(t -> t.getVoltageLevel() == regulatingTerminal.getVoltageLevel())
            .map(t -> t.getNodeBreakerView().getNode()).collect(Collectors.toSet());
        if (transformerNodes.isEmpty()) {
            return null;
        }
        Terminal[] found = {null};
        regulatingTerminal.getVoltageLevel().getNodeBreakerView().traverse(regulatingTerminal.getNodeBreakerView().getNode(),
            (node1, sw, node2) -> {
                boolean isInternalConnection = sw == null;
                if (transformerNodes.contains(node2)) {
                    found[0] = regulatingTerminal.getVoltageLevel().getNodeBreakerView().getTerminal(node2);
                    // Do not continue graph traversal
                    return false;
                }
                // Continue traversing only if this is an internal connection
                return isInternalConnection;
            });
        return found[0];
    }

    private final RegulatingControlMapping parent;
    private final Context context;
    private final Map<String, CgmesRegulatingControlForTwoWindingsTransformer> t2xMapping;
    private final Map<String, CgmesRegulatingControlForThreeWindingsTransformer> t3xMapping;

    private static final Logger LOG = LoggerFactory.getLogger(RegulatingControlMappingForTransformers.class);
}
