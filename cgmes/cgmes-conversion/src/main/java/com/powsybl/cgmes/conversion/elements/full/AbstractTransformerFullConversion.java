package com.powsybl.cgmes.conversion.elements.full;

import java.util.Comparator;
import java.util.Map;

import org.apache.commons.math3.complex.Complex;

import com.powsybl.cgmes.conversion.Context;
import com.powsybl.cgmes.conversion.RegulatingControlMapping.TapChangerRegulatingControl;
import com.powsybl.cgmes.conversion.TransformerRegulatingControlMapping.RegulatingDataPhase;
import com.powsybl.cgmes.conversion.TransformerRegulatingControlMapping.RegulatingDataRatio;
import com.powsybl.cgmes.conversion.elements.AbstractConductingEquipmentConversion;
import com.powsybl.cgmes.conversion.elements.full.TapChanger.StepAdder;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.iidm.network.PhaseTapChangerAdder;
import com.powsybl.iidm.network.RatioTapChangerAdder;
import com.powsybl.triplestore.api.PropertyBag;
import com.powsybl.triplestore.api.PropertyBags;

/**
 * @author José Antonio Marqués <marquesja at aia.es>
 * @author Marcos de Miguel <demiguelm at aia.es>
 */

public abstract class AbstractTransformerFullConversion
    extends AbstractConductingEquipmentConversion {

    protected static final String STRING_POWER_TRANSFORMER = "PowerTransformer";
    protected static final String STRING_R = "r";
    protected static final String STRING_X = "x";
    protected static final String STRING_RATIO_TAP_CHANGER  = "RatioTapChanger";
    protected static final String STRING_PHASE_TAP_CHANGER  = "PhaseTapChanger";
    protected static final String STRING_RATEDU = "ratedU";
    protected static final String STRING_G = "g";
    protected static final String STRING_B = "b";
    protected static final String STRING_PHASE_ANGLE_CLOCK = "phaseAngleClock";
    protected static final String STRING_STEP_VOLTAGE_INCREMENT = "stepVoltageIncrement";
    protected static final String STRING_STEP_PHASE_SHIFT_INCREMENT = "stepPhaseShiftIncrement";

    protected static final String STRING_LOW_STEP = "lowStep";
    protected static final String STRING_HIGH_STEP = "highStep";
    protected static final String STRING_NEUTRAL_STEP = "neutralStep";
    protected static final String STRING_SV_TAP_STEP = "SVtapStep";
    protected static final String STRING_LTC_FLAG = "ltcFlag";
    protected static final String STRING_STEP = "step";
    protected static final String STRING_RATIO = "ratio";
    protected static final String STRING_ANGLE = "angle";
    protected static final String STRING_PHASE_TAP_CHANGER_TYPE = "phaseTapChangerType";
    protected static final String STRING_X_STEP_MIN = "xStepMin";
    protected static final String STRING_X_STEP_MAX = "xStepMax";
    protected static final String STRING_X_MIN = "xMin";
    protected static final String STRING_X_MAX = "xMax";
    protected static final String STRING_VOLTAGE_STEP_INCREMENT = "voltageStepIncrement";
    protected static final String STRING_WINDING_CONNECTION_ANGLE = "windingConnectionAngle";
    protected static final String STRING_SYMMETRICAL = "symmetrical";
    protected static final String STRING_ASYMMETRICAL = "asymmetrical";
    protected static final String STRING_TABULAR = "tabular";
    protected static final String STRING_TCUL_CONTROL_MODE = "tculControlMode";
    protected static final String STRING_TAP_CHANGER_CONTROL_ENABLED = "tapChangerControlEnabled";

    protected enum TapChangerType {
        NULL, FIXED, NON_REGULATING, REGULATING
    }

    public AbstractTransformerFullConversion(String type, PropertyBags ends, Context context) {
        super(type, ends, context);
    }

    // Move tapChanger
    protected TapChanger moveTapChangerFrom1To2(TapChanger tc) {
        switch (tapChangerType(tc)) {
            case NULL:
                return null;
            case FIXED:
            case NON_REGULATING:
            case REGULATING:
                return moveTapChanger(tc);
        }
        return null;
    }

    protected TapChanger moveTapChangerFrom2To1(TapChanger tc) {
        return moveTapChangerFrom1To2(tc);
    }

    private TapChanger moveTapChanger(TapChanger tc) {
        TapChanger tapChanger = baseCloneTapChanger(tc);
        moveTapChangerSteps(tapChanger, tc);
        return tapChanger;
    }

    // combine two tap changers
    protected TapChanger combineTapChangers(TapChanger tc1, TapChanger tc2) {
        switch (tapChangerType(tc1)) {
            case NULL:
                return combineTapChangerNull(tc2);
            case FIXED:
                return combineTapChangerFixed(tc1, tc2);
            case NON_REGULATING:
                return combineTapChangerNonRegulating(tc1, tc2);
            case REGULATING:
                return combineTapChangerRegulating(tc1, tc2);
        }
        return null;
    }

    private TapChanger combineTapChangerNull(TapChanger tc) {
        switch (tapChangerType(tc)) {
            case NULL:
                return null;
            case FIXED:
            case NON_REGULATING:
            case REGULATING:
                return tc;
        }
        return null;
    }

    private TapChanger combineTapChangerFixed(TapChanger fixTc, TapChanger tc2) {
        switch (tapChangerType(tc2)) {
            case NULL:
                return fixTc;
            case FIXED:
            case NON_REGULATING:
            case REGULATING:
                return combineTapChanger(tc2, fixTc);
        }
        return null;
    }

    private TapChanger combineTapChangerNonRegulating(TapChanger tcNonRegulating, TapChanger tc2) {
        switch (tapChangerType(tc2)) {
            case NULL:
                return tcNonRegulating;
            case FIXED:
                return combineTapChanger(tcNonRegulating, tc2);
            case NON_REGULATING:
                TapChanger ntc2 = tapChangerFixPosition(tc2);
                return combineTapChanger(tcNonRegulating, ntc2);
            case REGULATING:
                TapChanger ntcNonRegulating = tapChangerFixPosition(tcNonRegulating);
                return combineTapChanger(tc2, ntcNonRegulating);
        }
        return null;
    }

    private TapChanger combineTapChangerRegulating(TapChanger tcRegulating, TapChanger tc2) {
        switch (tapChangerType(tc2)) {
            case NULL:
                return tcRegulating;
            case FIXED:
                return combineTapChanger(tcRegulating, tc2);
            case NON_REGULATING:
            case REGULATING:
                TapChanger ntc2 = tapChangerFixPosition(tc2);
                return combineTapChanger(tcRegulating, ntc2);
        }
        return null;
    }

    private TapChanger combineTapChanger(TapChanger tc1, TapChanger tc2) {
        TapChanger tapChanger = baseCloneTapChanger(tc1);
        combineTapChangerFixTapChangerSteps(tapChanger, tc1, tc2);
        return tapChanger;
    }

    private TapChanger tapChangerFixPosition(TapChanger tc) {
        TapChanger tapChanger = baseCloneTapChanger(tc);
        tapChanger.setLowTapPosition(tapChanger.getTapPosition());
        StepAdder step = getFixStepTapChanger(tc);
        double ratio = step.getRatio();
        double angle = step.getAngle();
        double r = step.getR();
        double x = step.getX();
        double g1 = step.getG1();
        double b1 = step.getB1();
        double g2 = step.getG2();
        double b2 = step.getB2();
        tapChanger.beginStep()
            .setRatio(ratio)
            .setAngle(angle)
            .setR(r)
            .setX(x)
            .setG1(g1)
            .setB1(b1)
            .setG2(g2)
            .setB2(b2)
            .endStep();
        return tapChanger;
    }

    private void combineTapChangerFixTapChangerSteps(TapChanger tapChanger, TapChanger tc1,
        TapChanger fixTc) {
        StepAdder stepFixed = getFixStepTapChanger(fixTc);
        double ratioFixed = stepFixed.getRatio();
        double angleFixed = stepFixed.getAngle();
        double rFixed = stepFixed.getR();
        double xFixed = stepFixed.getX();
        double g1Fixed = stepFixed.getG1();
        double b1Fixed = stepFixed.getB1();
        double g2Fixed = stepFixed.getG2();
        double b2Fixed = stepFixed.getB2();
        Complex aFixed = new Complex(ratioFixed * Math.cos(Math.toRadians(angleFixed)),
            ratioFixed * Math.sin(Math.toRadians(angleFixed)));
        tc1.getSteps().forEach(step -> {
            double ratio = step.getRatio();
            double angle = step.getAngle();
            double r = step.getR();
            double x = step.getX();
            double g1 = step.getG1();
            double b1 = step.getB1();
            double g2 = step.getG2();
            double b2 = step.getB2();
            Complex a = new Complex(ratio * Math.cos(Math.toRadians(angle)),
                ratio * Math.sin(Math.toRadians(angle)));
            Complex na = a.multiply(aFixed);
            tapChanger.beginStep()
                .setRatio(na.abs())
                .setAngle(Math.toDegrees(na.getArgument()))
                .setR(combineTapChangerCorrection(rFixed, r))
                .setX(combineTapChangerCorrection(xFixed, x))
                .setG1(combineTapChangerCorrection(g1Fixed, g1))
                .setB1(combineTapChangerCorrection(b1Fixed, b1))
                .setG2(combineTapChangerCorrection(g2Fixed, g2))
                .setB2(combineTapChangerCorrection(b2Fixed, b2))
                .endStep();
        });
    }

    private double combineTapChangerCorrection(double fixedCorrection, double correction) {
        if (fixedCorrection != 0.0 && correction != 0.0) {
            return fixedCorrection * correction;
        } else if (fixedCorrection != 0.0) {
            return fixedCorrection;
        } else if (correction != 0.0) {
            return correction;
        } else {
            return 0.0;
        }
    }

    private void moveTapChangerSteps(TapChanger tapChanger, TapChanger tc) {
        tc.getSteps().forEach(step -> {
            double ratio = step.getRatio();
            double angle = step.getAngle();
            double r = step.getR();
            double x = step.getX();
            double g1 = step.getG1();
            double b1 = step.getB1();
            double g2 = step.getG2();
            double b2 = step.getB2();
            TapChangerStepConversion convertedStep = calculateConversionStep(ratio, angle, r, x, g1,
                b1, g2, b2);
            tapChanger.beginStep()
                .setRatio(convertedStep.ratio)
                .setAngle(convertedStep.angle)
                .setR(convertedStep.r)
                .setX(convertedStep.x)
                .setG1(convertedStep.g1)
                .setB1(convertedStep.b1)
                .setG2(convertedStep.g2)
                .setB2(convertedStep.b2)
                .endStep();
        });
    }

    private TapChanger baseCloneTapChanger(TapChanger rtc) {
        TapChanger tapChanger = new TapChanger();
        String id = rtc.getId();
        boolean isLtcFlag = rtc.isLtcFlag();
        boolean isRegulating = rtc.isRegulating();
        String regulatingControlId = rtc.getRegulatingControlId();
        int side = rtc.getSide();
        String tculControlMode = rtc.getTculControlMode();
        boolean isTapChangerControlEnabled = rtc.isTapChangerControlEnabled();
        int lowStep = rtc.getLowTapPosition();
        int position = rtc.getTapPosition();
        tapChanger.setLowTapPosition(lowStep)
            .setTapPosition((int) position)
            .setLtcFlag(isLtcFlag)
            .setId(id)
            .setRegulating(isRegulating)
            .setRegulatingControlId(regulatingControlId)
            .setSide(side)
            .setTculControlMode(tculControlMode)
            .setTapChangerControlEnabled(isTapChangerControlEnabled);
        return tapChanger;
    }

    protected void cleanTapChanger(TapChanger tapChanger, boolean rClean, boolean xClean,
        boolean g1Clean, boolean b1Clean, boolean g2Clean, boolean b2Clean) {
        if (tapChanger == null) {
            return;
        }
        tapChanger.getSteps().forEach(step -> {
            if (rClean) {
                step.setR(0.0);
            }
            if (xClean) {
                step.setX(0.0);
            }
            if (g1Clean) {
                step.setG1(0.0);
            }
            if (b1Clean) {
                step.setB1(0.0);
            }
            if (g2Clean) {
                step.setG2(0.0);
            }
            if (b2Clean) {
                step.setB2(0.0);
            }
        });
    }

    private void resetTapChangerRegulation(TapChanger tapChanger) {
        tapChanger.setRegulating(false);
        tapChanger.setRegulatingControlId(null);
        tapChanger.setTculControlMode(null);
        tapChanger.setTapChangerControlEnabled(false);
    }

    private TapChangerStepConversion calculateConversionStep(double ratio, double angle, double r,
        double x, double g1, double b1, double g2, double b2) {
        TapChangerStepConversion step = new TapChangerStepConversion();
        Complex a = new Complex(ratio * Math.cos(Math.toRadians(angle)),
            ratio * Math.sin(Math.toRadians(angle)));
        Complex na = a.reciprocal();
        step.ratio = na.abs();
        step.angle = Math.toDegrees(na.getArgument());
        step.r = 100 * (impedanceConversion(1 + r / 100, a) - 1);
        step.x = 100 * (impedanceConversion(1 + x / 100, a) - 1);
        step.g1 = 100 * (admittanceConversion(1 + g1 / 100, a) - 1);
        step.b1 = 100 * (admittanceConversion(1 + b1 / 100, a) - 1);
        step.g2 = 100 * (admittanceConversion(1 + g2 / 100, a) - 1);
        step.b2 = 100 * (admittanceConversion(1 + b2 / 100, a) - 1);
        return step;
    }

    protected RatioConversion identityRatioConversion(double r, double x, double g1,
        double b1, double g2, double b2) {
        RatioConversion ratio = new RatioConversion();
        ratio.r = r;
        ratio.x = x;
        ratio.g1 = g1;
        ratio.b1 = b1;
        ratio.g2 = g2;
        ratio.b2 = b2;
        return ratio;
    }

    protected RatioConversion moveRatioFrom2To1(double a0, double angle, double r, double x, double g1,
        double b1, double g2, double b2) {
        return moveRatio(a0, angle, r, x, g1, b1, g2, b2);
    }

    protected RatioConversion moveRatioFrom1To2(double a0, double angle, double r, double x, double g1,
        double b1, double g2, double b2) {
        return moveRatio(a0, angle, r, x, g1, b1, g2, b2);
    }

    private RatioConversion moveRatio(double a0, double angle, double r, double x, double g1,
        double b1, double g2, double b2) {
        RatioConversion ratio = new RatioConversion();
        Complex a = new Complex(a0 * Math.cos(Math.toRadians(angle)),
            a0 * Math.sin(Math.toRadians(angle)));
        ratio.r = impedanceConversion(r, a);
        ratio.x = impedanceConversion(x, a);
        ratio.g1 = admittanceConversion(g1, a);
        ratio.b1 = admittanceConversion(b1, a);
        ratio.g2 = admittanceConversion(g2, a);
        ratio.b2 = admittanceConversion(b2, a);
        return ratio;
    }

    private double admittanceConversion(double correction, Complex a) {
        double a2 = a.abs() * a.abs();
        return correction / a2;
    }

    private double impedanceConversion(double correction, Complex a) {
        double a2 = a.abs() * a.abs();
        return correction * a2;
    }

    protected TapChangerType tapChangerType(TapChanger tc) {
        if (isTapChangerNull(tc)) {
            return TapChangerType.NULL;
        }
        if (isTapChangerFixed(tc)) {
            return TapChangerType.FIXED;
        }
        if (!isTapChangerRegulating(tc)) {
            return TapChangerType.NON_REGULATING;
        } else {
            return TapChangerType.REGULATING;
        }
    }

    private boolean isTapChangerNull(TapChanger tc) {
        return tc == null;
    }

    private boolean isTapChangerFixed(TapChanger tc) {
        return tc.getSteps().size() == 1;
    }

    private boolean isTapChangerRegulating(TapChanger tc) {
        return tc.isRegulating();
    }

    private void getPhaseDefaultStep(TapChanger tapChanger) {
        tapChanger.beginStep()
            .endStep();
    }

    private StepAdder getFixStepTapChanger(TapChanger tc) {
        if (isTapChangerFixed(tc)) {
            return tc.getSteps().get(0);
        }
        int position = tc.getTapPosition();
        int lowPosition = tc.getLowTapPosition();
        return tc.getSteps().get(position - lowPosition);
    }

    protected TapChanger fixTapChangerRegulation(TapChanger tc) {
        if (tapChangerType(tc) == TapChangerType.REGULATING) {
            TapChanger newtc = baseCloneTapChanger(tc);
            resetTapChangerRegulation(newtc);

            return newtc;
        }

        return tc;
    }

    protected void negatePhaseTapChanger(TapChanger tc) {
        if (tc == null) {
            return;
        }
        tc.getSteps().forEach(step -> {
            double angle = step.getAngle();
            step.setAngle(-angle);
        });
    }

    protected boolean isDefinedTapChanger(TapChanger tc) {
        return !tapChangerType(tc).equals(TapChangerType.NULL);
    }

    // propertyBag
    protected PropertyBag getTransformerTapChanger(PropertyBag end, String id,
        Map<String, PropertyBag> powerTransformerTapChanger) {

        String tcId = end.getId(id);
        PropertyBag tc = powerTransformerTapChanger.get(tcId);
        return tc;
    }

    protected TapChanger getRatioTapChanger(PropertyBag ratioTapChanger, String rtcTerminal, int side) {
        if (ratioTapChanger == null) {
            return null;
        }
        TapChanger tapChanger = new TapChanger();
        int lowStep = ratioTapChanger.asInt(STRING_LOW_STEP);
        int highStep = ratioTapChanger.asInt(STRING_HIGH_STEP);
        int neutralStep = ratioTapChanger.asInt(STRING_NEUTRAL_STEP);
        double position = ratioTapChanger.asDouble(STRING_SV_TAP_STEP, neutralStep);
        if (position > highStep || position < lowStep) {
            position = neutralStep;
        }
        tapChanger.setLowTapPosition(lowStep).setTapPosition((int) position);

        boolean ltcFlag = ratioTapChanger.asBoolean(STRING_LTC_FLAG, false);
        tapChanger.setLtcFlag(ltcFlag);

        addRatioRegulationData(ratioTapChanger, rtcTerminal, tapChanger, side);

        addRatioSteps(ratioTapChanger, tapChanger);
        return tapChanger;
    }

    private void addRatioSteps(PropertyBag ratioTapChanger, TapChanger tapChanger) {
        String tableId = ratioTapChanger.getId(CgmesNames.RATIO_TAP_CHANGER_TABLE);
        if (tableId != null) {
            addTabularRatioSteps(ratioTapChanger, tapChanger, tableId);
        } else {
            addNonTabularRatioSteps(ratioTapChanger, tapChanger);
        }
    }

    private void addTabularRatioSteps(PropertyBag ratioTapChanger, TapChanger tapChanger,
        String tableId) {
        PropertyBags table = context.ratioTapChangerTable(tableId);
        Comparator<PropertyBag> byStep = Comparator
            .comparingInt((PropertyBag p) -> p.asInt(STRING_STEP));
        table.sort(byStep);
        for (PropertyBag point : table) {
            int step = point.asInt(STRING_STEP);
            double ratio = fixing(point, STRING_RATIO, 1.0, tableId, step);
            double r = fixing(point, STRING_R, 0, tableId, step);
            double x = fixing(point, STRING_X, 0, tableId, step);
            double g = fixing(point, STRING_G, 0, tableId, step);
            double b = fixing(point, STRING_B, 0, tableId, step);
            tapChanger.beginStep()
                .setRatio(ratio)
                .setR(r)
                .setX(x)
                .setG1(g)
                .setB1(b)
                .endStep();
        }
    }

    private void addNonTabularRatioSteps(PropertyBag ratioTapChanger, TapChanger tapChanger) {
        double stepVoltageIncrement = ratioTapChanger.asDouble(STRING_STEP_VOLTAGE_INCREMENT);
        int highStep = ratioTapChanger.asInt(STRING_HIGH_STEP);
        int neutralStep = ratioTapChanger.asInt(STRING_NEUTRAL_STEP);
        for (int step = tapChanger.getLowTapPosition(); step <= highStep; step++) {
            double ratio = 1.0 + (step - neutralStep) * (stepVoltageIncrement / 100.0);
            tapChanger.beginStep()
                .setRatio(ratio)
                .endStep();
        }
    }

    private void addRatioRegulationData(PropertyBag ratioTapChanger, String rtcTerminal,
        TapChanger tapChanger, int side) {
        TapChangerRegulatingControl tcrc = context.regulatingControlMapping()
            .getTapChangerRegulatingControl(ratioTapChanger);
        tapChanger.setId(ratioTapChanger.getId(STRING_RATIO_TAP_CHANGER))
        .setRegulating(tcrc.regulating)
            .setRegulatingControlId(tcrc.regulatingControlId)
            .setSide(side)
            .setTculControlMode(ratioTapChanger.get(STRING_TCUL_CONTROL_MODE))
            .setTapChangerControlEnabled(ratioTapChanger.asBoolean(STRING_TAP_CHANGER_CONTROL_ENABLED, false));
    }

    protected TapChanger getPhaseTapChanger(PropertyBag phaseTapChanger, String ptcTerminal,
        double ratedU, double xtx, int side) {
        if (phaseTapChanger == null) {
            return null;
        }
        TapChanger tapChanger = new TapChanger();
        int lowStep = phaseTapChanger.asInt(STRING_LOW_STEP);
        int highStep = phaseTapChanger.asInt(STRING_HIGH_STEP);
        int neutralStep = phaseTapChanger.asInt(STRING_NEUTRAL_STEP);
        double position = phaseTapChanger.asDouble(STRING_SV_TAP_STEP, neutralStep);
        if (position > highStep || position < lowStep) {
            position = neutralStep;
        }
        tapChanger.setLowTapPosition(lowStep).setTapPosition((int) position);

        boolean ltcFlag = phaseTapChanger.asBoolean(STRING_LTC_FLAG, false);
        tapChanger.setLtcFlag(ltcFlag);

        addPhaseRegulationData(phaseTapChanger, tapChanger, side);

        addPhaseSteps(phaseTapChanger, tapChanger, xtx);
        return tapChanger;
    }

    protected void addPhaseSteps(PropertyBag phaseTapChanger, TapChanger tapChanger, double xtx) {
        String tableId = phaseTapChanger.getId(CgmesNames.PHASE_TAP_CHANGER_TABLE);
        String phaseTapChangerType = phaseTapChanger.getLocal(STRING_PHASE_TAP_CHANGER_TYPE).toLowerCase();
        if (isTabular(phaseTapChangerType, tableId)) {
            addTabularPhaseSteps(phaseTapChanger, tapChanger, tableId);
        } else if (isAsymmetrical(phaseTapChangerType)) {
            addAsymmetricalPhaseSteps(phaseTapChanger, tapChanger, tableId, xtx);
        } else if (isSymmetrical(phaseTapChangerType)) {
            addSymmetricalPhaseSteps(phaseTapChanger, tapChanger, tableId, xtx);
        } else {
            getPhaseDefaultStep(tapChanger);
        }
    }

    private void addTabularPhaseSteps(PropertyBag phaseTapChanger, TapChanger tapChanger,
        String tableId) {
        PropertyBags table = context.phaseTapChangerTable(tableId);
        if (table == null) {
            return;
        }
        Comparator<PropertyBag> byStep = Comparator
            .comparingInt((PropertyBag p) -> p.asInt(STRING_STEP));
        table.sort(byStep);
        for (PropertyBag point : table) {
            int step = point.asInt(STRING_STEP);
            double angle = fixing(point, STRING_ANGLE, 0.0, tableId, step);
            double ratio = fixing(point, STRING_RATIO, 1.0, tableId, step);
            double r = fixing(point, STRING_R, 0, tableId, step);
            double x = fixing(point, STRING_X, 0, tableId, step);
            double g = fixing(point, STRING_G, 0, tableId, step);
            double b = fixing(point, STRING_B, 0, tableId, step);
            tapChanger.beginStep()
                .setAngle(angle)
                .setRatio(ratio)
                .setR(r)
                .setX(x)
                .setG1(g)
                .setB1(b)
                .endStep();
        }
    }

    private void addAsymmetricalPhaseSteps(PropertyBag phaseTapChanger, TapChanger tapChanger,
        String tableId, double xtx) {
        int lowStep = phaseTapChanger.asInt(STRING_LOW_STEP);
        int highStep = phaseTapChanger.asInt(STRING_HIGH_STEP);
        int neutralStep = phaseTapChanger.asInt(STRING_NEUTRAL_STEP);
        double stepVoltageIncrement = phaseTapChanger.asDouble(STRING_VOLTAGE_STEP_INCREMENT);
        double windingConnectionAngle = phaseTapChanger.asDouble(STRING_WINDING_CONNECTION_ANGLE);
        for (int step = lowStep; step <= highStep; step++) {
            double dx = 1.0 + (step - neutralStep) * (stepVoltageIncrement / 100.0)
                * Math.cos(Math.toRadians(windingConnectionAngle));
            double dy = (step - neutralStep) * (stepVoltageIncrement / 100.0)
                * Math.sin(Math.toRadians(windingConnectionAngle));
            double ratio = Math.hypot(dx, dy);
            double angle = Math.toDegrees(Math.atan2(dy, dx));
            tapChanger.beginStep()
                .setAngle(angle)
                .setRatio(ratio)
                .endStep();
        }
        double xMin = phaseTapChanger.asDouble(STRING_X_STEP_MIN, phaseTapChanger.asDouble(STRING_X_MIN));
        double xMax = phaseTapChanger.asDouble(STRING_X_STEP_MAX, phaseTapChanger.asDouble(STRING_X_MAX));
        if (Double.isNaN(xMin) || Double.isNaN(xMax) || xMin < 0 || xMax <= 0 || xMin > xMax) {
            return;
        }
        double alphaMax = tapChanger.getSteps().stream().map(step -> step.getAngle())
            .mapToDouble(Double::doubleValue).max().orElse(Double.NaN);
        tapChanger.getSteps().forEach(step -> {
            double alpha = step.getAngle();
            double x = getStepXforAsymmetrical(xMin, xMax, alpha, alphaMax, windingConnectionAngle);
            step.setX((x - xtx) / xtx * 100);
        });
    }

    private double getStepXforAsymmetrical(double xStepMin, double xStepMax, double alphaDegrees,
        double alphaMaxDegrees, double thetaDegrees) {
        double alpha = Math.toRadians(alphaDegrees);
        double alphaMax = Math.toRadians(alphaMaxDegrees);
        double theta = Math.toRadians(thetaDegrees);
        double numer = Math.sin(theta) - Math.tan(alphaMax) * Math.cos(theta);
        double denom = Math.sin(theta) - Math.tan(alpha) * Math.cos(theta);
        return xStepMin + (xStepMax - xStepMin)
            * Math.pow(Math.tan(alpha) / Math.tan(alphaMax) * numer / denom, 2);
    }

    private void addSymmetricalPhaseSteps(PropertyBag phaseTapChanger, TapChanger tapChanger,
        String tableId, double xtx) {
        int lowStep = phaseTapChanger.asInt(STRING_LOW_STEP);
        int highStep = phaseTapChanger.asInt(STRING_HIGH_STEP);
        int neutralStep = phaseTapChanger.asInt(STRING_NEUTRAL_STEP);
        double stepVoltageIncrement = phaseTapChanger.asDouble(STRING_VOLTAGE_STEP_INCREMENT);
        double stepPhaseShiftIncrement = phaseTapChanger.asDouble(STRING_STEP_PHASE_SHIFT_INCREMENT);
        for (int step = lowStep; step <= highStep; step++) {
            double angle = 0.0;
            if (!Double.isNaN(stepPhaseShiftIncrement) && stepPhaseShiftIncrement != 0.0) {
                angle = (step - neutralStep) * stepPhaseShiftIncrement;
            } else {
                double dy = (step - neutralStep) * (stepVoltageIncrement / 100.0);
                angle = Math.toDegrees(2 * Math.asin(dy / 2));
            }
            tapChanger.beginStep()
                .setRatio(1.0)
                .setAngle(angle)
                .endStep();
        }
        double xMin = phaseTapChanger.asDouble(STRING_X_STEP_MIN, phaseTapChanger.asDouble(STRING_X_MIN));
        double xMax = phaseTapChanger.asDouble(STRING_X_STEP_MAX, phaseTapChanger.asDouble(STRING_X_MAX));
        if (Double.isNaN(xMin) || Double.isNaN(xMax) || xMin < 0 || xMax <= 0 || xMin > xMax) {
            return;
        }
        double alphaMax = tapChanger.getSteps().stream().map(step -> step.getAngle())
            .mapToDouble(Double::doubleValue).max().orElse(Double.NaN);
        tapChanger.getSteps().forEach(step -> {
            double alpha = step.getAngle();
            double x = getStepXforSymmetrical(xMin, xMax, alpha, alphaMax);
            step.setX((x - xtx) / xtx * 100);
        });
    }

    private double getStepXforSymmetrical(double xStepMin, double xStepMax, double alphaDegrees,
        double alphaMaxDegrees) {
        double alpha = Math.toRadians(alphaDegrees);
        double alphaMax = Math.toRadians(alphaMaxDegrees);
        return xStepMin
            + (xStepMax - xStepMin) * Math.pow(Math.sin(alpha / 2) / Math.sin(alphaMax / 2), 2);
    }

    private void addPhaseRegulationData(PropertyBag phaseTapChanger, TapChanger tapChanger, int side) {
        TapChangerRegulatingControl tcrc = context.regulatingControlMapping()
            .getTapChangerRegulatingControl(phaseTapChanger);
        tapChanger.setId(phaseTapChanger.getId(STRING_PHASE_TAP_CHANGER))
            .setRegulating(tcrc.regulating)
            .setRegulatingControlId(tcrc.regulatingControlId)
            .setSide(side);
    }

    private boolean isSymmetrical(String tapChangerType) {
        return tapChangerType != null && tapChangerType.endsWith(STRING_SYMMETRICAL);
    }

    private boolean isAsymmetrical(String tapChangerType) {
        return tapChangerType != null && tapChangerType.endsWith(STRING_ASYMMETRICAL);
    }

    private boolean isTabular(String tapChangerType, String tableId) {
        return tableId != null && tapChangerType != null && tapChangerType.endsWith(STRING_TABULAR);
    }

    private double fixing(PropertyBag point, String attr, double defaultValue, String tableId,
        int step) {
        double value = point.asDouble(attr, defaultValue);
        if (Double.isNaN(value)) {
            fixed(
                "RatioTapChangerTablePoint " + attr + " for step " + step + " in table "
                    + tableId,
                "invalid value " + point.get(attr));
            return defaultValue;
        }
        return value;
    }

    protected void setToIidmRatioTapChanger(TapChanger rtc, RatioTapChangerAdder rtca) {
        boolean isLtcFlag = rtc.isLtcFlag();
        int lowStep = rtc.getLowTapPosition();
        int position = rtc.getTapPosition();
        rtca.setLoadTapChangingCapabilities(isLtcFlag).setLowTapPosition(lowStep).setTapPosition((int) position);

        rtc.getSteps().forEach(step -> {
            double ratio0 = step.getRatio();
            double r0 = step.getR();
            double x0 = step.getX();
            double b01 = step.getB1();
            double g01 = step.getG1();
            // double b02 = step.getB2();
            // double g02 = step.getG2();
            // Only b01 and g01 instead of b01 + b02 and g01 + g02
            rtca.beginStep()
                .setRho(1 / ratio0)
                .setR(r0)
                .setX(x0)
                .setB(b01)
                .setG(g01)
                .endStep();
        });
        rtca.add();
    }

    protected RegulatingDataRatio buildContextRegulatingDataRatio(TapChanger tc) {
        if (tc != null) {
            return context.transformerRegulatingControlMapping().buildRegulatingDataRatio(tc.getId(), tc.isRegulating(),
                tc.getRegulatingControlId(), tc.getSide(), tc.getTculControlMode(), tc.isTapChangerControlEnabled());
        } else {
            return context.transformerRegulatingControlMapping().buildEmptyRegulatingDataRatio();
        }
    }

    protected RegulatingDataPhase buildContextRegulatingDataPhase(TapChanger tc) {
        if (tc != null) {
            return context.transformerRegulatingControlMapping().buildRegulatingDataPhase(tc.getId(), tc.isRegulating(),
                tc.getRegulatingControlId(), tc.getSide());
        } else {
            return context.transformerRegulatingControlMapping().buildEmptyRegulatingDataPhase();
        }
    }

    protected void setToIidmPhaseTapChanger(TapChanger ptc, PhaseTapChangerAdder ptca) {
        // TODO record LtcFlag
        int lowStep = ptc.getLowTapPosition();
        int position = ptc.getTapPosition();
        ptca.setLowTapPosition(lowStep).setTapPosition((int) position);

        ptc.getSteps().forEach(step -> {
            double ratio0 = step.getRatio();
            double angle0 = step.getAngle();
            double r0 = step.getR();
            double x0 = step.getX();
            double b01 = step.getB1();
            double g01 = step.getG1();
            // double b02 = step.getB2();
            // double g02 = step.getG2();
            // Only b01 and g01 instead of b01 + b02 and g01 + g02
            ptca.beginStep()
                .setRho(1 / ratio0)
                .setAlpha(-angle0)
                .setR(r0)
                .setX(x0)
                .setB(b01)
                .setG(g01)
                .endStep();
        });
        ptca.add();
    }

    protected static class ConvertedEnd1 {
        double g;
        double b;
        TapChanger ratioTapChanger;
        TapChanger phaseTapChanger;
        double ratedU;
        String terminal;
        int phaseAngleClock;
    }

    static class PhaseAngleClockAll {
        int phaseAngleClock1;
        int phaseAngleClock2;
    }

    static class ShuntAll {
        double g1;
        double b1;
        double g2;
        double b2;
    }

    static class TapChangerAll {
        TapChanger ratioTapChanger1;
        TapChanger phaseTapChanger1;
        TapChanger ratioTapChanger2;
        TapChanger phaseTapChanger2;
    }

    static class TapChangerStepConversion {
        double ratio;
        double angle;
        double r;
        double x;
        double g1;
        double b1;
        double g2;
        double b2;
    }

    static class RatioConversion {
        double r;
        double x;
        double g1;
        double b1;
        double g2;
        double b2;
    }
}
