package com.powsybl.cgmes.conversion.elements.full;

import java.util.Comparator;
import java.util.Map;

import org.apache.commons.math3.complex.Complex;

import com.powsybl.cgmes.conversion.Context;
import com.powsybl.cgmes.conversion.elements.AbstractConductingEquipmentConversion;
import com.powsybl.cgmes.conversion.elements.full.TapChanger.StepAdder;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.iidm.network.PhaseTapChanger;
import com.powsybl.iidm.network.PhaseTapChanger.RegulationMode;
import com.powsybl.triplestore.api.PropertyBag;
import com.powsybl.triplestore.api.PropertyBags;

public abstract class AbstractTransformerFullConversion
        extends AbstractConductingEquipmentConversion {

    private static final String REGULATING_CONTROL_ENABLED = "regulatingControlEnabled";

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
                    .setR(r * rFixed)
                    .setX(x * xFixed)
                    .setG1(g1 * g1Fixed)
                    .setB1(b1 * b1Fixed)
                    .setG2(g2 * g2Fixed)
                    .setB2(b2 * b2Fixed)
                    .endStep();
        });
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
        boolean isRegulating = rtc.isRegulating();
        RegulationMode regulationMode = rtc.getRegulationMode();
        String regulationTerminal = rtc.getRegulationTerminal();
        double regulationValue = rtc.getRegulationValue();
        boolean isLoadTapChangingCapabilities = rtc.isLoadTapChangingCapabilities();
        int lowStep = rtc.getLowTapPosition();
        int position = rtc.getTapPosition();
        tapChanger.setLowTapPosition(lowStep).setTapPosition((int) position)
                .setLoadTapChangingCapabilities(isLoadTapChangingCapabilities)
                .setRegulating(isRegulating).setRegulationMode(regulationMode)
                .setRegulationTerminal(regulationTerminal).setRegulationValue(regulationValue);
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
        tapChanger.setRegulationMode(PhaseTapChanger.RegulationMode.FIXED_TAP);
        tapChanger.setRegulationValue(Double.NaN);
        tapChanger.setRegulationTerminal(null);
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

    // private TapChangerStepConversion calculateNeutralStep(double ratio, double angle) {
    // TapChangerStepConversion step = new TapChangerStepConversion();
    // Complex a = new Complex(ratio * Math.cos(Math.toRadians(angle)),
    // ratio * Math.sin(Math.toRadians(angle)));
    // Complex na = a.reciprocal();
    // step.ratio = na.abs();
    // step.angle = Math.toDegrees(na.getArgument());
    // step.r = 0.0;
    // step.x = 0.0;
    // step.g1 = 0.0;
    // step.b1 = 0.0;
    // step.g2 = 0.0;
    // step.b2 = 0.0;
    // return step;
    // }

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

    protected TapChanger createPhaseAngleClockTapChanger(int phaseAngleClock) {
        if (phaseAngleClock == 0) {
            return null;
        }
        double degrees = getPhaseAngleClockDegrees(phaseAngleClock);
        TapChanger tapChanger = new TapChanger();
        tapChanger.setLowTapPosition(0);
        tapChanger.setTapPosition(0);
        tapChanger.setLoadTapChangingCapabilities(false);
        tapChanger.beginStep()
                .setRatio(1.0)
                .setAngle(degrees)
                .endStep();
        resetTapChangerRegulation(tapChanger);
        return tapChanger;
    }

    double getPhaseAngleClockDegrees(int phaseAngleClock) {
        double phaseAngleClockDegree = 0.0;
        phaseAngleClockDegree += phaseAngleClock * 30.0;
        phaseAngleClockDegree = Math.IEEEremainder(phaseAngleClockDegree, 360.0);
        if (phaseAngleClockDegree > 180.0) {
            phaseAngleClockDegree -= 360.0;
        }
        return phaseAngleClockDegree;
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

    protected TapChanger getRatioTapChanger(PropertyBag ratioTapChanger, String rtcTerminal) {
        if (ratioTapChanger == null) {
            return null;
        }
        TapChanger tapChanger = new TapChanger();
        int lowStep = ratioTapChanger.asInt("lowStep");
        int highStep = ratioTapChanger.asInt("highStep");
        int neutralStep = ratioTapChanger.asInt("neutralStep");
        double position = ratioTapChanger.asDouble("SVtapStep", neutralStep);
        if (position > highStep || position < lowStep) {
            position = neutralStep;
        }
        tapChanger.setLowTapPosition(lowStep).setTapPosition((int) position);

        addRatioRegulationMode(ratioTapChanger, rtcTerminal, tapChanger);

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
                .comparingInt((PropertyBag p) -> p.asInt("step"));
        table.sort(byStep);
        for (PropertyBag point : table) {
            int step = point.asInt("step");
            double ratio = fixing(point, "ratio", 1.0, tableId, step);
            double r = fixing(point, "r", 0, tableId, step);
            double x = fixing(point, "x", 0, tableId, step);
            double g = fixing(point, "g", 0, tableId, step);
            double b = fixing(point, "b", 0, tableId, step);
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
        double stepVoltageIncrement = ratioTapChanger.asDouble("stepVoltageIncrement");
        int highStep = ratioTapChanger.asInt("highStep");
        int neutralStep = ratioTapChanger.asInt("neutralStep");
        for (int step = tapChanger.getLowTapPosition(); step <= highStep; step++) {
            double ratio = 1.0 + (step - neutralStep) * (stepVoltageIncrement / 100.0);
            tapChanger.beginStep()
                    .setRatio(ratio)
                    .endStep();
        }
    }

    private void addRatioRegulationMode(PropertyBag ratioTapChanger, String rtcTerminal,
            TapChanger tapChanger) {
        String tapChangerControl = ratioTapChanger.getId("TapChangerControl");
        if (tapChangerControl != null) {
            addRatioRegulatingControl(ratioTapChanger, rtcTerminal, tapChanger);
        } else {
            tapChanger.setLoadTapChangingCapabilities(false);
        }
    }

    private void addRatioRegulatingControl(PropertyBag ratioTapChanger, String rtcTerminal,
            TapChanger tapChanger) {
        String mode = ratioTapChanger.getLocal("regulatingControlMode").toLowerCase();
        if (mode.endsWith("voltage")) {
            addRatioRegulatingControlVoltage(ratioTapChanger, rtcTerminal, tapChanger);
        } else if (mode.endsWith("fixed")) {
            tapChanger.setLoadTapChangingCapabilities(false);
        } else {
            tapChanger.setLoadTapChangingCapabilities(false);
            ignored(mode, "Unsupported regulation mode");
        }
    }

    private void addRatioRegulatingControlVoltage(PropertyBag ratioTapChanger, String rtcTerminal,
            TapChanger tapChanger) {
        double regulatingControlValue = ratioTapChanger.asDouble("regulatingControlTargetValue");
        boolean regulating = ratioTapChanger.asBoolean("regulatingControlEnabled", false);
        // Even if regulating is false, we reset the target voltage if it is not valid
        double targetV = regulatingControlValue;
        if (targetV <= 0) {
            String reg = ratioTapChanger.getId("TapChangerControl");
            ignored(reg, String.format("Regulating control has a bad target voltage %f", targetV));
            regulating = false;
            targetV = Float.NaN;
        }

        tapChanger.setLoadTapChangingCapabilities(true)
                .setRegulating(regulating)
                .setRegulationValue(targetV)
                .setRegulationTerminal(rtcTerminal);
    }

    protected TapChanger getPhaseTapChanger(PropertyBag phaseTapChanger, String ptcTerminal,
            double ratedU, double xtx) {
        if (phaseTapChanger == null) {
            return null;
        }
        TapChanger tapChanger = new TapChanger();
        int lowStep = phaseTapChanger.asInt("lowStep");
        int highStep = phaseTapChanger.asInt("highStep");
        int neutralStep = phaseTapChanger.asInt("neutralStep");
        double position = phaseTapChanger.asDouble("SVtapStep", neutralStep);
        if (position > highStep || position < lowStep) {
            position = neutralStep;
        }
        tapChanger.setLowTapPosition(lowStep).setTapPosition((int) position);

        addPhaseRegulatingControl(phaseTapChanger, ptcTerminal, ratedU, tapChanger);

        addPhaseSteps(phaseTapChanger, tapChanger, xtx);
        return tapChanger;
    }

    protected void addPhaseSteps(PropertyBag phaseTapChanger, TapChanger tapChanger, double xtx) {
        String tableId = phaseTapChanger.getId(CgmesNames.PHASE_TAP_CHANGER_TABLE);
        String phaseTapChangerType = phaseTapChanger.getLocal("phaseTapChangerType").toLowerCase();
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
                .comparingInt((PropertyBag p) -> p.asInt("step"));
        table.sort(byStep);
        for (PropertyBag point : table) {
            int step = point.asInt("step");
            double angle = fixing(point, "angle", 0.0, tableId, step);
            double ratio = fixing(point, "ratio", 1.0, tableId, step);
            double r = fixing(point, "r", 0, tableId, step);
            double x = fixing(point, "x", 0, tableId, step);
            double g = fixing(point, "g", 0, tableId, step);
            double b = fixing(point, "b", 0, tableId, step);
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
        int lowStep = phaseTapChanger.asInt("lowStep");
        int highStep = phaseTapChanger.asInt("highStep");
        int neutralStep = phaseTapChanger.asInt("neutralStep");
        double stepVoltageIncrement = phaseTapChanger.asDouble("voltageStepIncrement");
        double windingConnectionAngle = phaseTapChanger.asDouble("windingConnectionAngle");
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
        double xMin = phaseTapChanger.asDouble("xStepMin", phaseTapChanger.asDouble("xMin"));
        double xMax = phaseTapChanger.asDouble("xStepMax", phaseTapChanger.asDouble("xMax"));
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
        int lowStep = phaseTapChanger.asInt("lowStep");
        int highStep = phaseTapChanger.asInt("highStep");
        int neutralStep = phaseTapChanger.asInt("neutralStep");
        double stepVoltageIncrement = phaseTapChanger.asDouble("voltageStepIncrement");
        double stepPhaseShiftIncrement = phaseTapChanger.asDouble("stepPhaseShiftIncrement");
        for (int step = lowStep; step <= highStep; step++) {
            double angle = 0.0;
            if (!Double.isNaN(stepPhaseShiftIncrement) && stepPhaseShiftIncrement != 0.0) {
                angle = (step - neutralStep) * stepPhaseShiftIncrement;
            } else {
                double dy = (step - neutralStep) * (stepVoltageIncrement / 100.0);
                angle = Math.toDegrees(2 * Math.asin(dy / 2));
            }
            tapChanger.beginStep()
                    .setAngle(angle)
                    .endStep();
        }
        double xMin = phaseTapChanger.asDouble("xStepMin", phaseTapChanger.asDouble("xMin"));
        double xMax = phaseTapChanger.asDouble("xStepMax", phaseTapChanger.asDouble("xMax"));
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

    private void addPhaseRegulatingControl(PropertyBag phaseTapChanger, String ptcTerminal,
            double ratedU, TapChanger tapChanger) {
        String regulatingControl = phaseTapChanger.getId("TapChangerControl");
        String regulatingControlMode = phaseTapChanger.getLocal("regulatingControlMode");
        if (regulatingControl != null) {
            if (regulatingControlMode.endsWith("currentFlow")) {
                addCurrentFlowRegControl(phaseTapChanger, ptcTerminal, ratedU, tapChanger);
            } else if (regulatingControlMode.endsWith("activePower")) {
                addActivePowerRegControl(phaseTapChanger, ptcTerminal, tapChanger);
            } else if (regulatingControlMode.endsWith("fixed")) {
                // Nothing to do
            } else {
                ignored(regulatingControlMode, "Unsupported regulating mode");
            }
        }
    }

    private void addActivePowerRegControl(PropertyBag phaseTapChanger, String ptcTerminal,
            TapChanger tapChanger) {
        String treg = phaseTapChanger.getId("RegulatingControlTerminal");
        boolean regulatingControlEnabled = phaseTapChanger.asBoolean(REGULATING_CONTROL_ENABLED,
                true);
        double targetV = -phaseTapChanger.asDouble("regulatingControlTargetValue");
        tapChanger.setRegulationMode(PhaseTapChanger.RegulationMode.ACTIVE_POWER_CONTROL)
                .setRegulationTerminal(ptcTerminal)
                .setRegulating(regulatingControlEnabled)
                .setRegulationValue(regulationValue(targetV, ptcTerminal, treg));
    }

    private void addCurrentFlowRegControl(PropertyBag phaseTapChanger, String ptcTerminal,
            double ratedU, TapChanger tapChanger) {
        String treg = phaseTapChanger.getId("RegulatingControlTerminal");
        boolean regulatingControlEnabled = phaseTapChanger.asBoolean(REGULATING_CONTROL_ENABLED,
                true);
        double targetV = phaseTapChanger.asDouble("regulatingControlTargetValue");
        targetV *= ratedU;
        tapChanger.setRegulationMode(PhaseTapChanger.RegulationMode.CURRENT_LIMITER)
                .setRegulationValue(regulationValue(targetV, ptcTerminal, treg))
                .setRegulating(regulatingControlEnabled)
                .setRegulationTerminal(ptcTerminal);
    }

    private boolean isSymmetrical(String tapChangerType) {
        return tapChangerType != null && tapChangerType.endsWith("symmetrical");
    }

    private boolean isAsymmetrical(String tapChangerType) {
        return tapChangerType != null && tapChangerType.endsWith("asymmetrical");
    }

    private boolean isTabular(String tapChangerType, String tableId) {
        return tableId != null && tapChangerType != null && tapChangerType.endsWith("tabular");
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

    // IIDM
    private double regulationValue(double targetV, String ptcTerminal, String treg) {
        if (!treg.equals(ptcTerminal)) {
            return -targetV;
        }
        return targetV;
    }

    // return classes

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
