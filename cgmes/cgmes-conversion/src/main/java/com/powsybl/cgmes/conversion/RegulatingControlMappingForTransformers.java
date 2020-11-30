/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.powsybl.cgmes.conversion.RegulatingControlMapping.RegulatingControl;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.PhaseTapChanger;
import com.powsybl.iidm.network.RatioTapChanger;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.ThreeWindingsTransformer;
import com.powsybl.iidm.network.TopologyKind;
import com.powsybl.iidm.network.TwoWindingsTransformer;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.triplestore.api.PropertyBag;

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
        RegulatingControl control, PhaseTapChanger ptc, List<? extends Terminal> terminalEnds) {
        if (control == null || ptc == null) {
            return;
        }

        boolean okSet = false;
        if (control.mode.endsWith("currentflow")) {
            okSet = setPtcRegulatingControlCurrentFlow(regulating, rc.ltcFlag, control, ptc, context, terminalEnds);
        } else if (control.mode.endsWith("activepower")) {
            okSet = setPtcRegulatingControlActivePower(regulating, rc.ltcFlag, control, ptc, context, terminalEnds);
        } else if (!control.mode.endsWith("fixed")) {
            context.fixed(control.mode, "Unsupported regulating mode for Phase tap changer. Considered as FIXED_TAP");
        }
        control.setCorrectlySet(okSet);
    }

    private boolean setPtcRegulatingControlCurrentFlow(boolean regulating, boolean ltcFlag, RegulatingControl control,
        PhaseTapChanger ptc, Context context, List<? extends Terminal> terminalEnds) {
        PhaseTapChanger.RegulationMode regulationMode = getPtcRegulatingMode(ltcFlag,
                PhaseTapChanger.RegulationMode.CURRENT_LIMITER);
        return setPtcRegulatingControl(regulating, regulationMode, control, ptc, context, terminalEnds);
    }

    private boolean setPtcRegulatingControlActivePower(boolean regulating, boolean ltcFlag, RegulatingControl control,
        PhaseTapChanger ptc, Context context, List<? extends Terminal> terminalEnds) {
        PhaseTapChanger.RegulationMode regulationMode = getPtcRegulatingMode(ltcFlag,
                PhaseTapChanger.RegulationMode.ACTIVE_POWER_CONTROL);
        return setPtcRegulatingControl(regulating, regulationMode, control, ptc, context, terminalEnds);
    }

    private boolean setPtcRegulatingControl(boolean regulating, PhaseTapChanger.RegulationMode regulationMode,
        RegulatingControl control, PhaseTapChanger ptc, Context context, List<? extends Terminal> terminalEnds) {
        Terminal terminal = parent.findRegulatingTerminal(control.cgmesTerminal);
        if (terminal == null) {
            context.missing(String.format(RegulatingControlMapping.MISSING_IIDM_TERMINAL, control.cgmesTerminal));
            return false;
        }
        Terminal terminalEnd = moveRegulatingTerminalToTerminalEnd(terminal, terminalEnds);

        // Order is important
        ptc.setRegulationTerminal(terminalEnd)
                .setRegulationValue(control.targetValue)
                .setTargetDeadband(control.targetDeadband)
                .setRegulationMode(regulationMode)
                .setRegulating(regulating);

        return true;
    }

    private PhaseTapChanger.RegulationMode getPtcRegulatingMode(boolean ltcFlag,
                                                                PhaseTapChanger.RegulationMode regulationMode) {
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

        PhaseTapChanger.RegulationMode finalRegulationMode = PhaseTapChanger.RegulationMode.FIXED_TAP;
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

    // Select as terminal one terminal end if it is possible (if regulatingTerminal
    // is directly connected to one terminal end)
    private static Terminal moveRegulatingTerminalToTerminalEnd(Terminal regulatingTerminal,
        List<? extends Terminal> terminalEnds) {
        if (regulatingTerminal.getVoltageLevel().getTopologyKind() != TopologyKind.NODE_BREAKER) {
            return regulatingTerminal;
        }
        if (terminalEnds.contains(regulatingTerminal)) {
            return regulatingTerminal;
        }
        Map<Integer, Set<Integer>> adjacency = buildAdjacency(terminalEnds);

        for (Terminal terminal : terminalEnds) {
            if (connectedTerminals(adjacency, regulatingTerminal, terminal)) {
                return terminal;
            }
        }

        return regulatingTerminal;
    }

    private static Map<Integer, Set<Integer>> buildAdjacency(List<? extends Terminal> terminalEnds) {
        Map<Integer, Set<Integer>> adjacency = new HashMap<>();

        terminalEnds.forEach(terminal -> {
            VoltageLevel voltageLevel = terminal.getVoltageLevel();
            voltageLevel.getNodeBreakerView().getInternalConnections().forEach(internalConnection -> {
                adjacency.computeIfAbsent(internalConnection.getNode1(), k -> new HashSet<>()).add(internalConnection.getNode2());
                adjacency.computeIfAbsent(internalConnection.getNode2(), k -> new HashSet<>()).add(internalConnection.getNode1());
            });
        });
        return adjacency;
    }

    private static boolean connectedTerminals(Map<Integer, Set<Integer>> adjacency, Terminal regulatingTerminal, Terminal terminalEnd) {
        Set<Integer> visitedNodes = new HashSet<>();
        ArrayList<Integer> allConnected = new ArrayList<>();

        allConnected.add(regulatingTerminal.getNodeBreakerView().getNode());
        visitedNodes.add(regulatingTerminal.getNodeBreakerView().getNode());

        // Expand, adding in each step all non-visited adjacent nodes, exit if the node associated to terminalEnd is found
        int k = 0;
        while (k < allConnected.size()) {
            int node = allConnected.get(k);
            if (node == terminalEnd.getNodeBreakerView().getNode()) {
                return true;
            }
            if (adjacency.containsKey(node)) {
                adjacency.get(node).forEach(ad -> {
                    if (visitedNodes.contains(ad)) {
                        return;
                    }
                    allConnected.add(ad);
                    visitedNodes.add(ad);
                });
            }
            k++;
        }

        return false;
    }

    private final RegulatingControlMapping parent;
    private final Context context;
    private final Map<String, CgmesRegulatingControlForTwoWindingsTransformer> t2xMapping;
    private final Map<String, CgmesRegulatingControlForThreeWindingsTransformer> t3xMapping;
}
