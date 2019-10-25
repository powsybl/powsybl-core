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

    void applyTwoWindingsTapChangersRegulatingControl(Network network) {
        network.getTwoWindingsTransformerStream().forEach(this::applyTapChangersRegulatingControl);
    }

    private void applyTapChangersRegulatingControl(TwoWindingsTransformer twt) {
        CgmesRegulatingControlForTwoWindingsTransformer rc = t2xMapping.get(twt.getId());
        applyTapChangersRegulatingControl(twt, rc);
    }

    private void applyTapChangersRegulatingControl(TwoWindingsTransformer twt, CgmesRegulatingControlForTwoWindingsTransformer rc) {
        if (rc == null) {
            return;
        }

        RegulatingControl rtcControl = getTapChangerControl(rc.ratioTapChanger);
        boolean rtcRegulating = rtcControl != null && (rtcControl.enabled || rc.ratioTapChanger.tapChangerControlEnabled);

        RegulatingControl ptcControl = getTapChangerControl(rc.phaseTapChanger);

        if (twt.getRatioTapChanger() != null && twt.getPhaseTapChanger() != null) {
            setPhaseTapChangerControl(rc.phaseTapChanger, ptcControl, twt.getPhaseTapChanger());
            // only one regulatingControl enabled
            if (rc.ratioTapChanger != null && rtcRegulating && twt.getPhaseTapChanger().isRegulating()) {
                context.fixed(twt.getId(), "Unsupported two regulating controls enabled. Disable the ratioTapChanger");
                rtcRegulating = false;
            }
            setRatioTapChangerControl(rtcRegulating, rc.ratioTapChanger, rtcControl, twt.getRatioTapChanger());
        } else if (twt.getRatioTapChanger() != null) {
            setRatioTapChangerControl(rtcRegulating, rc.ratioTapChanger, rtcControl, twt.getRatioTapChanger());
        } else if (twt.getPhaseTapChanger() != null) {
            setPhaseTapChangerControl(rc.phaseTapChanger, ptcControl, twt.getPhaseTapChanger());
        }
    }

    private void setRatioTapChangerControl(boolean regulating, CgmesRegulatingControlRatio rc, RegulatingControl control, RatioTapChanger rtc) {
        if (control == null) {
            return;
        }

        if (isControlModeVoltage(control.mode, rc.tculControlMode)) {
            setRtcRegulatingControlVoltage(rc.id, regulating, control, rtc, context);
        } else if (!isControlModeFixed(control.mode)) {
            context.fixed(control.mode,
                    "Unsupported regulation mode for Ratio tap changer. Considered as a fixed ratio tap changer.");
        }

        control.hasCorrectlySetEq(rc.id);
    }

    private void setRtcRegulatingControlVoltage(String rtcId, boolean regulating, RegulatingControl control, RatioTapChanger rtc, Context context) {
        Terminal terminal = parent.findRegulatingTerminal(control.cgmesTerminal, control.topologicalNode);
        if (terminal == null) {
            context.missing(String.format(RegulatingControlMapping.MISSING_IIDM_TERMINAL, control.topologicalNode));
            return;
        }

        // Even if regulating is false, we reset the target voltage if it is not valid
        if (control.targetValue <= 0) {
            context.ignored(rtcId,
                    String.format("Regulating control has a bad target voltage %f", control.targetValue));
            return;
        }

        // Order is important
        rtc.setRegulationTerminal(terminal)
                .setTargetV(control.targetValue)
                .setTargetDeadband(control.targetDeadband)
                .setRegulating(regulating);
    }

    private void setPhaseTapChangerControl(CgmesRegulatingControlPhase rc, RegulatingControl control, PhaseTapChanger ptc) {
        if (control == null) {
            return;
        }

        if (control.mode.endsWith("currentflow")) {
            setPtcRegulatingControlCurrentFlow(rc.tapChangerControlEnabled, rc.ltcFlag, control, ptc, context);
        } else if (control.mode.endsWith("activepower")) {
            setPtcRegulatingControlActivePower(rc.tapChangerControlEnabled, rc.ltcFlag, control, ptc, context);
        } else if (!control.mode.endsWith("fixed")) {
            context.fixed(control.mode, "Unsupported regulating mode for Phase tap changer. Considered as FIXED_TAP");
        }

        control.hasCorrectlySetEq(rc.id);
    }

    private void setPtcRegulatingControlCurrentFlow(boolean tapChangerControlEnabled, boolean ltcFlag, RegulatingControl control, PhaseTapChanger ptc, Context context) {
        setPtcRegulatingControl(tapChangerControlEnabled, control, ptc, context);
        setPtcRegulatingMode(ltcFlag, PhaseTapChanger.RegulationMode.CURRENT_LIMITER, ptc);
    }

    private void setPtcRegulatingControlActivePower(boolean tapChangerControlEnabled, boolean ltcFlag, RegulatingControl control, PhaseTapChanger ptc, Context context) {
        setPtcRegulatingControl(tapChangerControlEnabled, control, ptc, context);
        setPtcRegulatingMode(ltcFlag, PhaseTapChanger.RegulationMode.ACTIVE_POWER_CONTROL, ptc);
    }

    private void setPtcRegulatingControl(boolean tapChangerControlEnabled, RegulatingControl control, PhaseTapChanger ptc, Context context) {
        Terminal terminal = parent.findRegulatingTerminal(control.cgmesTerminal, control.topologicalNode);
        if (terminal == null) {
            context.missing(String.format(RegulatingControlMapping.MISSING_IIDM_TERMINAL, control.topologicalNode));
            return;
        }

        // Order is important
        ptc.setRegulationTerminal(terminal)
                .setRegulationValue(control.targetValue)
                .setTargetDeadband(control.targetDeadband)
                .setRegulating(tapChangerControlEnabled || control.enabled);
    }

    private void setPtcRegulatingMode(boolean ltcFlag, PhaseTapChanger.RegulationMode regulationMode, PhaseTapChanger ptc) {
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
            ptc.setRegulationMode(regulationMode);
        }
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

    private static class CgmesRegulatingControlForTwoWindingsTransformer {
        CgmesRegulatingControlRatio ratioTapChanger;
        CgmesRegulatingControlPhase phaseTapChanger;
    }

    private static class CgmesRegulatingControlRatio extends CgmesRegulatingControl {
        String tculControlMode; // mode in SSH values of RTC
    }

    private static class CgmesRegulatingControlPhase extends CgmesRegulatingControl {
        boolean ltcFlag;
    }

    private static class CgmesRegulatingControl {
        String id;
        String regulatingControlId;
        boolean tapChangerControlEnabled; // enabled status in SSH values of PTC/RTC
    }

    private final RegulatingControlMapping parent;
    private final Context context;
    private final Map<String, CgmesRegulatingControlForTwoWindingsTransformer> t2xMapping;
}
