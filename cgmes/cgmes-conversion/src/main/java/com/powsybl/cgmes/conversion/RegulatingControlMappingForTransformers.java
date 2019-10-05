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
        String regulatingControlId = parent.getRegulatingControlId(tc);
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
        String regulatingControlId = parent.getRegulatingControlId(tc);
        boolean tapChangerControlEnabled = tc.asBoolean(TAP_CHANGER_CONTROL_ENABLED, false);
        return buildRegulatingControlPhase(regulatingControlId, tapChangerControlEnabled);
    }

    private RegulatingControlPhase buildRegulatingControlPhase(String regulatingControlId,
        boolean tapChangerControlEnabled) {
        RegulatingControlPhase rtc = new RegulatingControlPhase();
        rtc.regulatingControlId = regulatingControlId;
        rtc.tapChangerControlEnabled = tapChangerControlEnabled;

        return rtc;
    }

    public void initializeRatioTapChanger(RatioTapChangerAdder adder) {
        adder.setRegulationTerminal(null);
        adder.setTargetV(Double.NaN);
        adder.setTargetDeadband(Double.NaN);
        adder.setRegulating(false);
    }

    public void initializePhaseTapChanger(PhaseTapChangerAdder adder) {
        adder.setRegulationTerminal(null);
        adder.setRegulationValue(Double.NaN);
        adder.setTargetDeadband(Double.NaN);
        adder.setRegulationMode(null);
        adder.setRegulating(false);
    }

    public void applyTwoWindings(Network network) {
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

            if (rcaRatio.regulating && rcaPhase.regulating) {
                context.fixed(twt.getId(), "Unsupported two regulating controls enabled. Disable the ratioTapChanger");
                rcaRatio.regulating = false;
            }

            applyRatioTapChanger(rcaRatio, twt.getRatioTapChanger());
            applyPhaseTapChanger(rcaPhase, twt.getPhaseTapChanger());
        } else if (twt.getRatioTapChanger() != null) {
            RegulatingControlRatioAttributes rca = getRatioTapChanger(rc.ratioTapChanger);
            applyRatioTapChanger(rca, twt.getRatioTapChanger());
        } else if (twt.getPhaseTapChanger() != null) {
            RegulatingControlPhaseAttributes rca = getPhaseTapChanger(rc.phaseTapChanger);
            applyPhaseTapChanger(rca, twt.getPhaseTapChanger());
        }
    }

    private RegulatingControlRatioAttributes getRatioTapChanger(RegulatingControlRatio rc) {
        if (rc == null) {
            return null;
        }

        String controlId = rc.regulatingControlId;
        if (controlId == null) {
            context.missing(String.format("Regulating control Id not defined"));
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
            context.missing(String.format("Regulating control Id not defined"));
            return null;
        }

        RegulatingControl control = parent.cachedRegulatingControls().get(controlId);
        if (control == null) {
            context.missing(String.format("Regulating control %s", controlId));
            return null;
        }

        RegulatingControlPhaseAttributes rca = null;
        if (control.mode.endsWith("currentflow")) {
            rca = getPtcRegulatingControlActivePower(rc.tapChangerControlEnabled, control, context);
        } else if (control.mode.endsWith("activepower")) {
            rca = getPtcRegulatingControlCurrentFlow(rc.tapChangerControlEnabled, control, context);
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
        RegulatingControl control, Context context) {
        RegulatingControlPhaseAttributes rca = getPtcRegulatingControl(tapChangerControlEnabled, control, context);
        rca.regulationMode = PhaseTapChanger.RegulationMode.CURRENT_LIMITER;

        return rca;
    }

    private RegulatingControlPhaseAttributes getPtcRegulatingControlActivePower(boolean tapChangerControlEnabled,
        RegulatingControl control, Context context) {
        RegulatingControlPhaseAttributes rca = getPtcRegulatingControl(tapChangerControlEnabled, control, context);
        rca.regulationMode = PhaseTapChanger.RegulationMode.ACTIVE_POWER_CONTROL;

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

    private void applyRatioTapChanger(RegulatingControlRatioAttributes rca, RatioTapChanger rtc) {
        if (rca == null) {
            return;
        }
        // order it is important
        rtc.setRegulationTerminal(rca.terminal)
            .setTargetDeadband(rca.targetDeadband)
            .setTargetV(rca.targetValue);
        rtc.setRegulating(rca.regulating);
    }

    private void applyPhaseTapChanger(RegulatingControlPhaseAttributes rca, PhaseTapChanger ptc) {
        if (rca == null) {
            return;
        }
        // Order it is important
        ptc.setRegulationMode(rca.regulationMode)
            .setRegulationTerminal(rca.terminal)
            .setTargetDeadband(rca.targetDeadband)
            .setRegulationValue(rca.targetValue);
        ptc.setRegulating(rca.regulating);
    }

    private boolean isControlModeVoltage(String controlMode, String tculControlMode) {
        if (parent.isControlModeVoltage(controlMode)) {
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
    }

    private static class RegulatingControlForTwoWingingsTransformer {
        RegulatingControlRatio ratioTapChanger;
        RegulatingControlPhase phaseTapChanger;
    }

    private final RegulatingControlMapping parent;
    private final Context context;
    private final Map<String, RegulatingControlForTwoWingingsTransformer> t2xMapping;
}
