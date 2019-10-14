package com.powsybl.cgmes.conversion;

import java.util.HashMap;
import java.util.Map;

import com.powsybl.cgmes.conversion.RegulatingControlMapping.RegulatingControl;
import com.powsybl.cgmes.model.CgmesModelException;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.PhaseTapChanger;
import com.powsybl.iidm.network.PhaseTapChangerAdder;
import com.powsybl.iidm.network.RatioTapChanger;
import com.powsybl.iidm.network.RatioTapChangerAdder;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.TwoWindingsTransformer;
import com.powsybl.triplestore.api.PropertyBag;

/**
 * @author José Antonio Marqués <marquesja at aia.es>
 * @author Marcos de Miguel <demiguelm at aia.es>
 */

public class RegulatingControlMappingForTransformers {

    private static final String TAP_CHANGER_CONTROL_ENABLED = "tapChangerControlEnabled";

    public RegulatingControlMappingForTransformers(RegulatingControlMapping parent) {
        this.parent = parent;
        this.context = parent.context();
        t2xMapping = new HashMap<>();
    }

    public void add(String transformerId, RegulatingControlRatio rcRtc, RegulatingControlPhase rcPtc) {
        if (t2xMapping.containsKey(transformerId)) {
            throw new CgmesModelException("Transformer already added, Transformer id : " + transformerId);
        }

        RegulatingControlForTwoWingingsTransformer rc = new RegulatingControlForTwoWingingsTransformer();
        rc.ratioTapChanger = rcRtc;
        rc.phaseTapChanger = rcPtc;
        t2xMapping.put(transformerId, rc);
    }

    public RegulatingControlRatio buildEmptyRegulatingControlRatio() {
        RegulatingControlRatio rtc = new RegulatingControlRatio();
        rtc.id = null;
        rtc.regulatingControlId = null;
        rtc.tculControlMode = null;
        rtc.tapChangerControlEnabled = false;

        return rtc;
    }

    public RegulatingControlRatio buildRegulatingControlRatio(String id, PropertyBag tc) {
        String regulatingControlId = getRegulatingControlId(tc);
        String tculControlMode = tc.get("tculControlMode");
        boolean tapChangerControlEnabled = tc.asBoolean(TAP_CHANGER_CONTROL_ENABLED, false);
        return buildRegulatingControlRatio(id, regulatingControlId, tculControlMode, tapChangerControlEnabled);
    }

    private RegulatingControlRatio buildRegulatingControlRatio(String id, String regulatingControlId,
        String tculControlMode, boolean tapChangerControlEnabled) {
        RegulatingControlRatio rtc = new RegulatingControlRatio();
        rtc.id = id;
        rtc.regulatingControlId = regulatingControlId;
        rtc.tculControlMode = tculControlMode;
        rtc.tapChangerControlEnabled = tapChangerControlEnabled;
        return rtc;
    }

    public RegulatingControlPhase buildEmptyRegulatingControlPhase() {
        RegulatingControlPhase rtc = new RegulatingControlPhase();
        rtc.regulatingControlId = null;
        rtc.tapChangerControlEnabled = false;

        return rtc;
    }

    public RegulatingControlPhase buildRegulatingControlPhase(PropertyBag tc) {
        String regulatingControlId = getRegulatingControlId(tc);
        boolean tapChangerControlEnabled = tc.asBoolean(TAP_CHANGER_CONTROL_ENABLED, false);
        boolean ltcFlag = tc.asBoolean("ltcFlag", false);
        return buildRegulatingControlPhase(regulatingControlId, tapChangerControlEnabled, ltcFlag);
    }

    private RegulatingControlPhase buildRegulatingControlPhase(String regulatingControlId,
        boolean tapChangerControlEnabled, boolean ltcFlag) {
        RegulatingControlPhase rtc = new RegulatingControlPhase();
        rtc.regulatingControlId = regulatingControlId;
        rtc.tapChangerControlEnabled = tapChangerControlEnabled;
        rtc.ltcFlag = ltcFlag;
        return rtc;
    }

    public static void initializeRatioTapChanger(RatioTapChangerAdder adder) {
        adder.setRegulationTerminal(null);
        adder.setTargetV(Double.NaN);
        adder.setTargetDeadband(Double.NaN);
        adder.setRegulating(false);
    }

    public static void initializePhaseTapChanger(PhaseTapChangerAdder adder) {
        adder.setRegulationTerminal(null);
        adder.setRegulationValue(Double.NaN);
        adder.setTargetDeadband(Double.NaN);
        adder.setRegulationMode(PhaseTapChanger.RegulationMode.FIXED_TAP);
        adder.setRegulating(false);
    }

    void applyTwoWindings(Network network) {
        network.getTwoWindingsTransformerStream().forEach(this::applyTwoWindings);
    }

    private void applyTwoWindings(TwoWindingsTransformer twt) {
        RegulatingControlForTwoWingingsTransformer rc = t2xMapping.get(twt.getId());
        applyTwoWindings(twt, rc);
    }

    private void applyTwoWindings(TwoWindingsTransformer twt, RegulatingControlForTwoWingingsTransformer rc) {
        if (rc == null) {
            return;
        }
        if (twt.getRatioTapChanger() != null && twt.getPhaseTapChanger() != null) {
            RegulatingControlRatioAttributes rcaRatio = getRatioTapChanger(rc.ratioTapChanger);
            RegulatingControlPhaseAttributes rcaPhase = getPhaseTapChanger(rc.phaseTapChanger);

            // only one regulatingControl enabled
            if (rcaRatio != null && rcaPhase != null && rcaRatio.regulating && rcaPhase.regulating) {
                context.fixed(twt.getId(), "Unsupported two regulating controls enabled. Disable the ratioTapChanger");
                rcaRatio.regulating = false;
            }

            applyRatioTapChanger(rcaRatio, twt.getRatioTapChanger());
            applyPhaseTapChanger(rcaPhase, twt.getPhaseTapChanger());
            removeRatioControlIdFromCachedRegulatingControls(rcaRatio, rc.ratioTapChanger.regulatingControlId);
            removePhaseControlIdFromCachedRegulatingControls(rcaPhase, rc.phaseTapChanger.regulatingControlId);
        } else if (twt.getRatioTapChanger() != null) {
            RegulatingControlRatioAttributes rca = getRatioTapChanger(rc.ratioTapChanger);
            applyRatioTapChanger(rca, twt.getRatioTapChanger());
            removeRatioControlIdFromCachedRegulatingControls(rca, rc.ratioTapChanger.regulatingControlId);
        } else if (twt.getPhaseTapChanger() != null) {
            RegulatingControlPhaseAttributes rca = getPhaseTapChanger(rc.phaseTapChanger);
            applyPhaseTapChanger(rca, twt.getPhaseTapChanger());
            removePhaseControlIdFromCachedRegulatingControls(rca, rc.phaseTapChanger.regulatingControlId);
        }
    }

    private RegulatingControlRatioAttributes getRatioTapChanger(RegulatingControlRatio rc) {
        if (rc == null) {
            return null;
        }

        String controlId = rc.regulatingControlId;
        if (controlId == null) {
            context.missing("Regulating control Id not defined");
            return null;
        }

        RegulatingControl control = parent.cachedRegulatingControls().get(controlId);
        if (control == null) {
            context.missing(String.format("Regulating control %s", controlId));
            return null;
        }

        RegulatingControlRatioAttributes rca = null;
        if (isControlModeVoltage(control.mode, rc.tculControlMode)) {
            rca = getRtcRegulatingControlVoltage(rc.id, rc.tapChangerControlEnabled, control, context);
        } else if (!isControlModeFixed(control.mode)) {
            context.fixed(control.mode,
                "Unsupported regulation mode for Ratio tap changer. Considered as a fixed ratio tap changer.");
        }
        return rca;
    }

    private RegulatingControlPhaseAttributes getPhaseTapChanger(RegulatingControlPhase rc) {
        if (rc == null) {
            return null;
        }

        String controlId = rc.regulatingControlId;
        if (controlId == null) {
            context.missing("Regulating control Id not defined");
            return null;
        }

        RegulatingControl control = parent.cachedRegulatingControls().get(controlId);
        if (control == null) {
            context.missing(String.format("Regulating control %s", controlId));
            return null;
        }

        RegulatingControlPhaseAttributes rca = null;
        if (control.mode.endsWith("currentflow")) {
            rca = getPtcRegulatingControlCurrentFlow(rc.tapChangerControlEnabled, rc.ltcFlag, control, context);
        } else if (control.mode.endsWith("activepower")) {
            rca = getPtcRegulatingControlActivePower(rc.tapChangerControlEnabled, rc.ltcFlag, control, context);
        } else if (!control.mode.endsWith("fixed")) {
            context.fixed(control.mode, "Unsupported regulating mode for Phase tap changer. Considered as FIXED_TAP");
        }
        return rca;
    }

    private RegulatingControlRatioAttributes getRtcRegulatingControlVoltage(String rtcId, boolean tapChangerControlEnabled,
        RegulatingControl control, Context context) {

        Terminal terminal = parent.findRegulatingTerminal(control.cgmesTerminal, control.topologicalNode);
        if (terminal == null) {
            context.missing(String.format(RegulatingControlMapping.MISSING_IIDM_TERMINAL, control.topologicalNode));
            return null;
        }

        // Even if regulating is false, we reset the target voltage if it is not valid
        if (control.targetValue <= 0) {
            context.ignored(rtcId,
                String.format("Regulating control has a bad target voltage %f", control.targetValue));
            return null;
        }

        RegulatingControlRatioAttributes rca = new RegulatingControlRatioAttributes();
        rca.terminal = terminal;
        rca.targetValue = control.targetValue;
        rca.targetDeadband = control.targetDeadband;
        rca.regulating = control.enabled || tapChangerControlEnabled;

        return rca;
    }

    private RegulatingControlPhaseAttributes getPtcRegulatingControlCurrentFlow(boolean tapChangerControlEnabled,
        boolean ltcFlag, RegulatingControl control, Context context) {
        RegulatingControlPhaseAttributes rca = getPtcRegulatingControl(tapChangerControlEnabled, control, context);
        rca = setRegulatingMode(rca, ltcFlag, PhaseTapChanger.RegulationMode.CURRENT_LIMITER);

        return rca;
    }

    private RegulatingControlPhaseAttributes getPtcRegulatingControlActivePower(boolean tapChangerControlEnabled,
        boolean ltcFlag, RegulatingControl control, Context context) {
        RegulatingControlPhaseAttributes rca = getPtcRegulatingControl(tapChangerControlEnabled, control, context);
        rca = setRegulatingMode(rca, ltcFlag, PhaseTapChanger.RegulationMode.ACTIVE_POWER_CONTROL);

        return rca;
    }

    private RegulatingControlPhaseAttributes getPtcRegulatingControl(boolean tapChangerControlEnabled,
        RegulatingControl control, Context context) {

        Terminal terminal = parent.findRegulatingTerminal(control.cgmesTerminal, control.topologicalNode);
        if (terminal == null) {
            context.missing(String.format(RegulatingControlMapping.MISSING_IIDM_TERMINAL, control.topologicalNode));
            return null;
        }

        RegulatingControlPhaseAttributes rca = new RegulatingControlPhaseAttributes();
        rca.terminal = terminal;
        rca.targetValue = control.targetValue;
        rca.targetDeadband = control.targetDeadband;
        rca.regulating = control.enabled || tapChangerControlEnabled;

        return rca;
    }

    private RegulatingControlPhaseAttributes setRegulatingMode(RegulatingControlPhaseAttributes rca, boolean ltcFlag,
        PhaseTapChanger.RegulationMode regulationMode) {
        if (rca == null) {
            return rca;
        }

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
        if (ltcFlag) {
            rca.regulationMode = regulationMode;
        }
        return rca;
    }

    private static void applyRatioTapChanger(RegulatingControlRatioAttributes rca, RatioTapChanger rtc) {
        if (rca == null) {
            return;
        }
        // order it is important
        rtc.setRegulationTerminal(rca.terminal)
            .setTargetDeadband(rca.targetDeadband)
            .setTargetV(rca.targetValue);
        rtc.setRegulating(rca.regulating);
    }

    private static void applyPhaseTapChanger(RegulatingControlPhaseAttributes rca, PhaseTapChanger ptc) {
        if (rca == null) {
            return;
        }
        // Order it is important
        ptc.setRegulationTerminal(rca.terminal)
            .setTargetDeadband(rca.targetDeadband)
            .setRegulationValue(rca.targetValue)
            .setRegulationMode(rca.regulationMode);
        ptc.setRegulating(rca.regulating);
    }

    private void removeRatioControlIdFromCachedRegulatingControls(RegulatingControlRatioAttributes rcaRatio, String controlId) {
        if (rcaRatio != null) {
            parent.cachedRegulatingControls().remove(controlId);
        }
    }

    private void removePhaseControlIdFromCachedRegulatingControls(RegulatingControlPhaseAttributes rcaPhase, String controlId) {
        if (rcaPhase != null) {
            parent.cachedRegulatingControls().remove(controlId);
        }
    }

    private String getRegulatingControlId(PropertyBag p) {
        String regulatingControlId = null;

        if (p.containsKey(RegulatingControlMapping.TAP_CHANGER_CONTROL)) {
            String controlId = p.getId(RegulatingControlMapping.TAP_CHANGER_CONTROL);
            RegulatingControl control = parent.cachedRegulatingControls().get(controlId);
            if (control != null) {
                regulatingControlId = controlId;
            }
        }

        return regulatingControlId;
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

    private static class RegulatingControlRatioAttributes {
        Terminal terminal;
        double targetValue;
        double targetDeadband;
        boolean regulating;
    }

    private static class RegulatingControlPhaseAttributes {
        PhaseTapChanger.RegulationMode regulationMode;
        Terminal terminal;
        double targetValue;
        double targetDeadband;
        boolean regulating;
    }

    public static class RegulatingControlRatio {
        String id;
        String regulatingControlId;
        String tculControlMode;
        boolean tapChangerControlEnabled;
    }

    public static class RegulatingControlPhase {
        String regulatingControlId;
        boolean tapChangerControlEnabled;
        boolean ltcFlag;
    }

    private static class RegulatingControlForTwoWingingsTransformer {
        RegulatingControlRatio ratioTapChanger;
        RegulatingControlPhase phaseTapChanger;
    }

    private final RegulatingControlMapping parent;
    private final Context context;
    private final Map<String, RegulatingControlForTwoWingingsTransformer> t2xMapping;
}
