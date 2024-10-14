/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.cgmes.conversion.elements.transformers;

import com.powsybl.cgmes.conversion.*;
import com.powsybl.cgmes.conversion.elements.AbstractIdentifiedObjectConversion;
import com.powsybl.cgmes.conversion.elements.AbstractObjectConversion;
import com.powsybl.cgmes.extensions.CgmesTapChanger;
import com.powsybl.cgmes.extensions.CgmesTapChangers;
import com.powsybl.cgmes.model.CgmesModelException;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.TapChanger;

import com.powsybl.triplestore.api.PropertyBag;
import com.powsybl.triplestore.api.PropertyBags;

import java.util.Objects;

import static com.powsybl.cgmes.conversion.CgmesReports.badTargetDeadbandRegulatingControlReport;
import static com.powsybl.cgmes.conversion.CgmesReports.badVoltageTargetValueRegulatingControlReport;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
public class TapChangerUpdate extends AbstractIdentifiedObjectConversion {

    private final String connectableId;

    public TapChangerUpdate(String connectableId, PropertyBags tcs, Context context) {
        super("TapChangers", tcs, context);
        this.connectableId = Objects.requireNonNull(connectableId);
    }

    @Override
    public boolean valid() {
        throw new ConversionException("Unsupported method");
    }

    @Override
    public void convert() {
        throw new ConversionException("Unsupported method");
    }

    @Override
    public void update(Network network) {
        Connectable<?> twt = network.getConnectable(connectableId);
        if (twt == null) {
            return;
        }
        if (twt.getType().equals(IdentifiableType.TWO_WINDINGS_TRANSFORMER)) {
            updateTapChangersTwoWindings((TwoWindingsTransformer) twt, findRcTwoWindings((TwoWindingsTransformer) twt));
        } else if (twt.getType().equals(IdentifiableType.THREE_WINDINGS_TRANSFORMER)) {
            updateTapChangersThreeWindings((ThreeWindingsTransformer) twt, findRcThreeWindings((ThreeWindingsTransformer) twt));
        } else {
            throw new CgmesModelException("Unexpected connectable type: " + twt.getType().name());
        }
    }

    private RCTwoWindingsTransformer findRcTwoWindings(TwoWindingsTransformer t2wt) {

        PropertyBag rtcPb = findTwoWindingsPropertyBag(t2wt, CgmesNames.RATIO_TAP_CHANGER);
        PropertyBag ptcPb = findTwoWindingsPropertyBag(t2wt, CgmesNames.PHASE_TAP_CHANGER);

        return new RCTwoWindingsTransformer(
                findRc(t2wt, rtcPb, findTapChangerId(rtcPb, CgmesNames.RATIO_TAP_CHANGER), ""),
                findRc(t2wt, ptcPb, findTapChangerId(ptcPb, CgmesNames.PHASE_TAP_CHANGER), ""));
    }

    private RCThreeWindingsTransformer findRcThreeWindings(ThreeWindingsTransformer t3wt) {

        PropertyBag rtc1Pb = findThreeWindingsPropertyBag(t3wt, CgmesNames.RATIO_TAP_CHANGER, "1");
        PropertyBag ptc1Pb = findThreeWindingsPropertyBag(t3wt, CgmesNames.PHASE_TAP_CHANGER, "1");
        PropertyBag rtc2Pb = findThreeWindingsPropertyBag(t3wt, CgmesNames.RATIO_TAP_CHANGER, "2");
        PropertyBag ptc2Pb = findThreeWindingsPropertyBag(t3wt, CgmesNames.PHASE_TAP_CHANGER, "2");
        PropertyBag rtc3Pb = findThreeWindingsPropertyBag(t3wt, CgmesNames.RATIO_TAP_CHANGER, "3");
        PropertyBag ptc3Pb = findThreeWindingsPropertyBag(t3wt, CgmesNames.PHASE_TAP_CHANGER, "3");

        return new RCThreeWindingsTransformer(
                findRc(t3wt, rtc1Pb, findTapChangerId(rtc1Pb, CgmesNames.RATIO_TAP_CHANGER), "1"),
                findRc(t3wt, ptc1Pb, findTapChangerId(ptc1Pb, CgmesNames.PHASE_TAP_CHANGER), "1"),
                findRc(t3wt, rtc2Pb, findTapChangerId(rtc2Pb, CgmesNames.RATIO_TAP_CHANGER), "2"),
                findRc(t3wt, ptc2Pb, findTapChangerId(ptc2Pb, CgmesNames.PHASE_TAP_CHANGER), "2"),
                findRc(t3wt, rtc3Pb, findTapChangerId(rtc3Pb, CgmesNames.RATIO_TAP_CHANGER), "3"),
                findRc(t3wt, ptc3Pb, findTapChangerId(ptc3Pb, CgmesNames.PHASE_TAP_CHANGER), "3"));
    }

    private static String findTapChangerId(PropertyBag p, String propertyTag) {
        return p != null ? p.getId(propertyTag) : null;
    }

    private PropertyBag findTwoWindingsPropertyBag(TwoWindingsTransformer t2wt, String propertyTag) {
        return ps.stream().filter(p -> isValid(t2wt, p, propertyTag)).findFirst().orElse(null);
    }

    private static boolean isValid(TwoWindingsTransformer t2wt, PropertyBag p, String propertyTag) {
        String tapChangerId = findTapChangerId(p, propertyTag);
        return tapChangerId != null && isNotHidden(t2wt, tapChangerId);
    }

    private PropertyBag findThreeWindingsPropertyBag(ThreeWindingsTransformer t3wt, String propertyTag, String end) {
        return ps.stream().filter(p -> isValid(t3wt, p, propertyTag, end)).findFirst().orElse(null);
    }

    private static boolean isValid(ThreeWindingsTransformer t3wt, PropertyBag p, String propertyTag, String end) {
        String tapChangerId = findTapChangerId(p, propertyTag);
        String endTChangerId = t3wt.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + propertyTag + end).orElse(null);
        return tapChangerId != null && isNotHidden(t3wt, tapChangerId) && tapChangerId.equals(endTChangerId);
    }

    private RC findRc(Connectable<?> twt, PropertyBag p, String tapChangerId, String end) {
        if (p == null) {
            return null;
        }
        int normalStep = getNormalStep(twt, tapChangerId);
        int step = initialTapPosition(p, normalStep, context);
        boolean tapChangerControlEnabled = p.asBoolean(CgmesNames.TAP_CHANGER_CONTROL_ENABLED, false);
        String regulatingControlId = getControlId(twt, tapChangerId);
        int terminalSign = findTerminalSign(twt.getProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "terminalSign" + end));
        return new RC(tapChangerId, step, tapChangerControlEnabled, regulatingControlId, terminalSign);
    }

    private static int initialTapPosition(PropertyBag p, int defaultStep, Context context) {
        return switch (context.config().getProfileForInitialValuesShuntSectionsTapPositions()) {
            case SSH ->
                    AbstractObjectConversion.fromContinuous(p.asDouble(CgmesNames.STEP, p.asDouble(CgmesNames.SV_TAP_STEP, defaultStep)));
            case SV ->
                    AbstractObjectConversion.fromContinuous(p.asDouble(CgmesNames.SV_TAP_STEP, p.asDouble(CgmesNames.STEP, defaultStep)));
            default ->
                    throw new CgmesModelException("Unexpected profile used for initial values: " + context.config().getProfileForInitialValuesShuntSectionsTapPositions());
        };
    }

    private static <C extends Connectable<C>> String getControlId(Connectable<C> twt, String tapChangerId) {
        CgmesTapChangers<C> cgmesTcs = twt.getExtension(CgmesTapChangers.class);
        if (cgmesTcs != null) {
            CgmesTapChanger cgmesTc = cgmesTcs.getTapChanger(tapChangerId);
            return cgmesTc != null ? cgmesTc.getControlId() : null;
        }
        return null;
    }

    private static <C extends Connectable<C>> int getNormalStep(Connectable<C> twt, String tapChangerId) {
        CgmesTapChangers<C> cgmesTcs = twt.getExtension(CgmesTapChangers.class);
        if (cgmesTcs != null) {
            CgmesTapChanger cgmesTc = cgmesTcs.getTapChanger(tapChangerId);
            if (cgmesTc != null) {
                return cgmesTc.getStep().orElseThrow();
            }
        }
        throw new ConversionException("normalStep must be defined in transformer: " + twt.getId());
    }

    private static <C extends Connectable<C>> boolean isNotHidden(Connectable<C> twt, String tapChangerId) {
        CgmesTapChangers<C> cgmesTcs = twt.getExtension(CgmesTapChangers.class);
        if (cgmesTcs != null) {
            CgmesTapChanger cgmesTc = cgmesTcs.getTapChanger(tapChangerId);
            return !(cgmesTc != null && cgmesTc.isHidden());
        }
        return true;
    }

    private int findTerminalSign(String terminalSign) {
        return terminalSign != null ? Integer.parseInt(terminalSign) : 1;
    }

    private void updateTapChangersTwoWindings(TwoWindingsTransformer t2wt, RCTwoWindingsTransformer rc) {

        RegulatingControlUpdate.RegulatingControl rtcControl = getTapChangerControl(rc.ratioTapChanger);
        boolean rtcRegulating = isTapChangerRegulating(rtcControl, rc.ratioTapChanger);
        RegulatingControlUpdate.RegulatingControl ptcControl = getTapChangerControl(rc.phaseTapChanger);
        boolean ptcRegulating = isTapChangerRegulating(ptcControl, rc.phaseTapChanger);

        setPhaseTapChangerControl(ptcRegulating, rc.phaseTapChanger, ptcControl, t2wt.getPhaseTapChanger());
        boolean regulatingSet = t2wt.hasPhaseTapChanger() && t2wt.getPhaseTapChanger().isRegulating();

        rtcRegulating = checkOnlyOneEnabled(t2wt.getId(), rtcRegulating, regulatingSet, "ratioTapChanger");
        setRatioTapChangerControl(rtcRegulating, rc.ratioTapChanger, rtcControl, t2wt.getRatioTapChanger());

        setTapChangerTapPosition(rc.ratioTapChanger, t2wt.getRatioTapChanger());
        setTapChangerTapPosition(rc.phaseTapChanger, t2wt.getPhaseTapChanger());
    }

    private void updateTapChangersThreeWindings(ThreeWindingsTransformer t3wt, RCThreeWindingsTransformer rc) {
        if (rc == null) {
            return;
        }

        RegulatingControlUpdate.RegulatingControl rtcControl1 = getTapChangerControl(rc.ratioTapChanger1);
        boolean rtcRegulating1 = isTapChangerRegulating(rtcControl1, rc.ratioTapChanger1);
        RegulatingControlUpdate.RegulatingControl ptcControl1 = getTapChangerControl(rc.phaseTapChanger1);
        boolean ptcRegulating1 = isTapChangerRegulating(ptcControl1, rc.phaseTapChanger1);

        RegulatingControlUpdate.RegulatingControl rtcControl2 = getTapChangerControl(rc.ratioTapChanger2);
        boolean rtcRegulating2 = isTapChangerRegulating(rtcControl2, rc.ratioTapChanger2);
        RegulatingControlUpdate.RegulatingControl ptcControl2 = getTapChangerControl(rc.phaseTapChanger2);
        boolean ptcRegulating2 = isTapChangerRegulating(ptcControl2, rc.phaseTapChanger2);

        RegulatingControlUpdate.RegulatingControl rtcControl3 = getTapChangerControl(rc.ratioTapChanger3);
        boolean rtcRegulating3 = isTapChangerRegulating(rtcControl3, rc.ratioTapChanger3);
        RegulatingControlUpdate.RegulatingControl ptcControl3 = getTapChangerControl(rc.phaseTapChanger3);
        boolean ptcRegulating3 = isTapChangerRegulating(ptcControl3, rc.phaseTapChanger3);

        setPhaseTapChangerControl(ptcRegulating1, rc.phaseTapChanger1, ptcControl1, t3wt.getLeg1().getPhaseTapChanger());
        boolean regulatingSet = t3wt.getLeg1().hasPhaseTapChanger() && t3wt.getLeg1().getPhaseTapChanger().isRegulating();

        rtcRegulating1 = checkOnlyOneEnabled(t3wt.getId(), rtcRegulating1, regulatingSet, "ratioTapChanger at Leg1");
        setRatioTapChangerControl(rtcRegulating1, rc.ratioTapChanger1, rtcControl1, t3wt.getLeg1().getRatioTapChanger());
        regulatingSet = regulatingSet
                || t3wt.getLeg1().hasRatioTapChanger() && t3wt.getLeg1().getRatioTapChanger().isRegulating();

        ptcRegulating2 = checkOnlyOneEnabled(t3wt.getId(), ptcRegulating2, regulatingSet, "phaseTapChanger at Leg2");
        setPhaseTapChangerControl(ptcRegulating2, rc.phaseTapChanger2, ptcControl2, t3wt.getLeg2().getPhaseTapChanger());
        regulatingSet = regulatingSet
                || t3wt.getLeg2().hasPhaseTapChanger() && t3wt.getLeg2().getPhaseTapChanger().isRegulating();

        rtcRegulating2 = checkOnlyOneEnabled(t3wt.getId(), rtcRegulating2, regulatingSet, "ratioTapChanger at Leg2");
        setRatioTapChangerControl(rtcRegulating2, rc.ratioTapChanger2, rtcControl2, t3wt.getLeg2().getRatioTapChanger());
        regulatingSet = regulatingSet
                || t3wt.getLeg2().hasRatioTapChanger() && t3wt.getLeg2().getRatioTapChanger().isRegulating();

        ptcRegulating3 = checkOnlyOneEnabled(t3wt.getId(), ptcRegulating3, regulatingSet, "phaseTapChanger at Leg3");
        setPhaseTapChangerControl(ptcRegulating3, rc.phaseTapChanger3, ptcControl3, t3wt.getLeg3().getPhaseTapChanger());
        regulatingSet = regulatingSet
                || t3wt.getLeg3().hasPhaseTapChanger() && t3wt.getLeg3().getPhaseTapChanger().isRegulating();

        rtcRegulating3 = checkOnlyOneEnabled(t3wt.getId(), rtcRegulating3, regulatingSet, "ratioTapChanger at Leg3");
        setRatioTapChangerControl(rtcRegulating3, rc.ratioTapChanger3, rtcControl3, t3wt.getLeg3().getRatioTapChanger());

        setTapChangerTapPosition(rc.ratioTapChanger1, t3wt.getLeg1().getRatioTapChanger());
        setTapChangerTapPosition(rc.phaseTapChanger1, t3wt.getLeg1().getPhaseTapChanger());
        setTapChangerTapPosition(rc.ratioTapChanger2, t3wt.getLeg2().getRatioTapChanger());
        setTapChangerTapPosition(rc.phaseTapChanger2, t3wt.getLeg2().getPhaseTapChanger());
        setTapChangerTapPosition(rc.ratioTapChanger3, t3wt.getLeg3().getRatioTapChanger());
        setTapChangerTapPosition(rc.phaseTapChanger3, t3wt.getLeg3().getPhaseTapChanger());
    }

    private static boolean isTapChangerRegulating(RegulatingControlUpdate.RegulatingControl rc, RC tcrc) {
        return rc != null && rc.getEnabled() && tcrc.tapChangerControlEnabled;
    }

    private boolean checkOnlyOneEnabled(String transformerId, boolean regulating, boolean setRegulating, String disabledTapChanger) {
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

    private void setRatioTapChangerControl(boolean regulating, RC rc, RegulatingControlUpdate.RegulatingControl control, RatioTapChanger rtc) {
        if (control == null || rtc == null) {
            return;
        }

        if (isControlModeVoltage(rtc.getRegulationMode())) {
            setRtcRegulatingControlVoltage(rc.id, regulating, control, rtc, context);
        } else if (!isControlModeFixed(rtc.getRegulationMode())) {
            throw new CgmesModelException("Unexpected RatioTapChanger regulation mode: " + rtc.getRegulationMode().name());
        }
    }

    private void setRtcRegulatingControlVoltage(String rtcId, boolean regulating, RegulatingControlUpdate.RegulatingControl control, RatioTapChanger rtc, Context context) {

        // We always keep the targetValue
        // It targetValue is not valid, emit a warning and deactivate regulating control
        boolean validTargetValue = control.getTargetValue() > 0;
        if (!validTargetValue) {
            context.invalid(rtcId, "Regulating control has a bad target voltage " + control.getTargetValue());
            badVoltageTargetValueRegulatingControlReport(context.getReportNode(), rtcId, control.getTargetValue());
        }

        boolean validTargetDeadband = control.getTargetDeadband() >= 0;
        if (!validTargetDeadband) {
            context.invalid(rtcId, "Regulating control has a bad target deadband " + control.getTargetDeadband());
            badTargetDeadbandRegulatingControlReport(context.getReportNode(), rtcId, control.getTargetDeadband());
        }

        rtc.setTargetV(control.getTargetValue())
                .setTargetDeadband(validTargetDeadband ? control.getTargetDeadband() : Double.NaN)
                .setRegulating(regulating && validTargetValue && validTargetDeadband);
    }

    private void setPhaseTapChangerControl(boolean regulating, RC rc, RegulatingControlUpdate.RegulatingControl control, PhaseTapChanger ptc) {
        if (control == null || ptc == null) {
            return;
        }

        if (isControlModeCurrent(ptc.getRegulationMode())) {
            setPtcRegulatingControl(rc.id, regulating, rc.terminalSign, ptc.getRegulationMode(), control, ptc, context);
        } else if (isControlModeActivePower(ptc.getRegulationMode())) {
            setPtcRegulatingControl(rc.id, regulating, rc.terminalSign, ptc.getRegulationMode(), control, ptc, context);
        } else if (isControlModeFixedTap(ptc.getRegulationMode())) {
            setPtcRegulatingControl(rc.id, regulating, rc.terminalSign, ptc.getRegulationMode(), control, ptc, context);
        } else {
            throw new CgmesModelException("Unexpected PhaseTapChanger regulation mode: " + ptc.getRegulationMode().name());
        }
    }

    private void setPtcRegulatingControl(String ptcId, boolean regulating, int terminalSign, PhaseTapChanger.RegulationMode regulationMode,
                                            RegulatingControlUpdate.RegulatingControl control, PhaseTapChanger ptc, Context context) {

        boolean fixedRegulating = regulating;
        if (regulating && regulationMode == PhaseTapChanger.RegulationMode.FIXED_TAP) {
            context.fixed(ptcId, "RegulationMode: regulating is set to true whereas regulationMode is set to FIXED_TAP: regulating fixed to false");
            fixedRegulating = false;
        }

        boolean validTargetDeadband = control.getTargetDeadband() >= 0;
        if (!validTargetDeadband) {
            context.invalid(ptcId, "Regulating control has a bad target deadband " + control.getTargetDeadband());
            badTargetDeadbandRegulatingControlReport(context.getReportNode(), ptcId, control.getTargetDeadband());
        }

        ptc.setRegulationValue(control.getTargetValue() * terminalSign)
                .setTargetDeadband(validTargetDeadband ? control.getTargetDeadband() : Double.NaN)
                .setRegulating(fixedRegulating && validTargetDeadband);
    }

    private static boolean isControlModeVoltage(RatioTapChanger.RegulationMode mode) {
        return mode != null && mode.equals(RatioTapChanger.RegulationMode.VOLTAGE);
    }

    private static boolean isControlModeFixed(RatioTapChanger.RegulationMode mode) {
        return mode == null;
    }

    private static boolean isControlModeCurrent(PhaseTapChanger.RegulationMode mode) {
        return mode != null && mode.equals(PhaseTapChanger.RegulationMode.CURRENT_LIMITER);
    }

    private static boolean isControlModeActivePower(PhaseTapChanger.RegulationMode mode) {
        return mode != null && mode.equals(PhaseTapChanger.RegulationMode.ACTIVE_POWER_CONTROL);
    }

    private static boolean isControlModeFixedTap(PhaseTapChanger.RegulationMode mode) {
        return mode != null && mode.equals(PhaseTapChanger.RegulationMode.FIXED_TAP);
    }

    private RegulatingControlUpdate.RegulatingControl getTapChangerControl(RC rc) {
        if (rc == null) {
            return null;
        }
        String controlId = rc.regulatingControlId;
        if (controlId == null) {
            return null;
        }
        return context.regulatingControlUpdate().getRegulatingControl(controlId).orElse(null);
    }

    private static void setTapChangerTapPosition(RC rc, TapChanger<?, ?, ?, ?> tc) {
        if (rc != null && tc != null) {
            tc.setTapPosition(rc.step);
        }
    }

    private record RCThreeWindingsTransformer(RC ratioTapChanger1, RC phaseTapChanger1, RC ratioTapChanger2,
                                              RC phaseTapChanger2, RC ratioTapChanger3, RC phaseTapChanger3) {
    }

    private record RCTwoWindingsTransformer(RC ratioTapChanger, RC phaseTapChanger) {
    }

    private record RC(String id, int step, boolean tapChangerControlEnabled, String regulatingControlId, int terminalSign) {
        // id = TapChangerId
    }
}
