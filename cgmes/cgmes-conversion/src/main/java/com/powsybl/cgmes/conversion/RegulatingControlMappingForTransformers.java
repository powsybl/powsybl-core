package com.powsybl.cgmes.conversion;

import java.util.HashMap;
import java.util.Map;

import com.powsybl.cgmes.conversion.RegulatingControlMapping.RegulatingControl;
import com.powsybl.cgmes.model.CgmesModelException;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.PhaseTapChanger;
import com.powsybl.iidm.network.RatioTapChanger;
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

    public void add(String transformerId, String rtcId, PropertyBag rtc, PropertyBag ptc) {
        if (t2xMapping.containsKey(transformerId)) {
            throw new CgmesModelException("Transformer already added, Transformer id : " + transformerId);
        }

        CgmesRegulatingControlRatio rcRtc = null;
        if (rtc != null) {
            rcRtc = buildRegulatingControlRatio(rtcId, rtc);
        }

        CgmesRegulatingControlPhase rcPtc = null;
        if (ptc != null) {
            rcPtc = context.regulatingControlMapping().forTransformers().buildRegulatingControlPhase(ptc);
        }

        CgmesRegulatingControlForTwoWindingsTransformer rc = new CgmesRegulatingControlForTwoWindingsTransformer();
        rc.ratioTapChanger = rcRtc;
        rc.phaseTapChanger = rcPtc;
        t2xMapping.put(transformerId, rc);
    }

    private CgmesRegulatingControlRatio buildRegulatingControlRatio(String id, PropertyBag tc) {
        String regulatingControlId = getRegulatingControlId(tc);
        String tculControlMode = tc.get("tculControlMode");
        boolean tapChangerControlEnabled = tc.asBoolean(TAP_CHANGER_CONTROL_ENABLED, false);

        CgmesRegulatingControlRatio rtc = new CgmesRegulatingControlRatio();
        rtc.id = id;
        rtc.regulatingControlId = regulatingControlId;
        rtc.tculControlMode = tculControlMode;
        rtc.tapChangerControlEnabled = tapChangerControlEnabled;
        return rtc;
    }

    private CgmesRegulatingControlPhase buildRegulatingControlPhase(PropertyBag tc) {
        String regulatingControlId = getRegulatingControlId(tc);
        boolean tapChangerControlEnabled = tc.asBoolean(TAP_CHANGER_CONTROL_ENABLED, false);
        boolean ltcFlag = tc.asBoolean("ltcFlag", false);

        CgmesRegulatingControlPhase rtc = new CgmesRegulatingControlPhase();
        rtc.regulatingControlId = regulatingControlId;
        rtc.tapChangerControlEnabled = tapChangerControlEnabled;
        rtc.ltcFlag = ltcFlag;
        return rtc;
    }

    void applyTwoWindings(Network network) {
        network.getTwoWindingsTransformerStream().forEach(this::applyTwoWindings);
    }

    private void applyTwoWindings(TwoWindingsTransformer twt) {
        CgmesRegulatingControlForTwoWindingsTransformer rc = t2xMapping.get(twt.getId());
        applyTwoWindings(twt, rc);
    }

    private void applyTwoWindings(TwoWindingsTransformer twt, CgmesRegulatingControlForTwoWindingsTransformer rc) {
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

    private RegulatingControlRatioAttributes getRatioTapChanger(CgmesRegulatingControlRatio rc) {
        RegulatingControl control = getTapChangerControl(rc);
        if (control == null) {
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

    private RegulatingControlPhaseAttributes getPhaseTapChanger(CgmesRegulatingControlPhase rc) {
        RegulatingControl control = getTapChangerControl(rc);
        if (control == null) {
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

    private RegulatingControl getTapChangerControl(CgmesRegulatingControl rc) {
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

        return control;
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
        return setRegulatingMode(rca, ltcFlag, PhaseTapChanger.RegulationMode.CURRENT_LIMITER);
    }

    private RegulatingControlPhaseAttributes getPtcRegulatingControlActivePower(boolean tapChangerControlEnabled,
        boolean ltcFlag, RegulatingControl control, Context context) {
        RegulatingControlPhaseAttributes rca = getPtcRegulatingControl(tapChangerControlEnabled, control, context);
        return setRegulatingMode(rca, ltcFlag, PhaseTapChanger.RegulationMode.ACTIVE_POWER_CONTROL);
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

    // Attributes needed in the IIDM model, ratio tap changer
    private static class RegulatingControlRatioAttributes {
        Terminal terminal;
        double targetValue;
        double targetDeadband;
        boolean regulating;
    }

    // Attributes needed in the IIDM model, phase tap changer
    private static class RegulatingControlPhaseAttributes {
        PhaseTapChanger.RegulationMode regulationMode;
        Terminal terminal;
        double targetValue;
        double targetDeadband;
        boolean regulating;
    }

    // Cgmes data needed to get the IIDM attributes
    private static class CgmesRegulatingControlForTwoWindingsTransformer {
        CgmesRegulatingControlRatio ratioTapChanger;
        CgmesRegulatingControlPhase phaseTapChanger;
    }

    private static class CgmesRegulatingControlRatio extends CgmesRegulatingControl {
        String id;
        String tculControlMode;
    }

    private static class CgmesRegulatingControlPhase extends CgmesRegulatingControl {
        boolean ltcFlag;
    }

    private static class CgmesRegulatingControl {
        String regulatingControlId;
        boolean tapChangerControlEnabled;
    }

    private final RegulatingControlMapping parent;
    private final Context context;
    private final Map<String, CgmesRegulatingControlForTwoWindingsTransformer> t2xMapping;
}
