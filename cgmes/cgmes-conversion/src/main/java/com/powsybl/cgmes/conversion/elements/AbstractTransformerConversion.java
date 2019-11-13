/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.conversion.elements;

import java.util.Comparator;
import java.util.Map;

import com.powsybl.cgmes.conversion.Context;
import com.powsybl.cgmes.model.CgmesModelException;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.triplestore.api.PropertyBag;
import com.powsybl.triplestore.api.PropertyBags;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
public abstract class AbstractTransformerConversion
    extends AbstractConductingEquipmentConversion {

    protected static final String STRING_POWER_TRANSFORMER = "PowerTransformer";
    protected static final String STRING_R = "r";
    protected static final String STRING_X = "x";
    protected static final String STRING_RATIO_TAP_CHANGER = "RatioTapChanger";
    protected static final String STRING_PHASE_TAP_CHANGER = "PhaseTapChanger";
    protected static final String STRING_RATEDU = "ratedU";
    protected static final String STRING_G = "g";
    protected static final String STRING_B = "b";
    protected static final String STRING_PHASE_ANGLE_CLOCK = "phaseAngleClock";
    protected static final String STRING_STEP_VOLTAGE_INCREMENT = "stepVoltageIncrement";
    protected static final String STRING_STEP_PHASE_SHIFT_INCREMENT = "stepPhaseShiftIncrement";

    protected static final String STRING_LOW_STEP = "lowStep";
    protected static final String STRING_HIGH_STEP = "highStep";
    protected static final String STRING_NEUTRAL_STEP = "neutralStep";
    protected static final String STRING_NORMAL_STEP = "normalStep";
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

    public AbstractTransformerConversion(String type, PropertyBags ends, Context context) {
        super(type, ends, context);
    }

    // propertyBag
    protected PropertyBag getTransformerTapChanger(PropertyBag end, String id,
        Map<String, PropertyBag> powerTransformerTapChanger) {

        String tcId = end.getId(id);
        return powerTransformerTapChanger.get(tcId);
    }

    // Ratio Tap Changer
    protected TapChangerConversion getRatioTapChanger(PropertyBag ratioTapChanger) {
        if (ratioTapChanger == null) {
            return null;
        }
        TapChangerConversion tapChanger = new TapChangerConversion();
        int lowStep = ratioTapChanger.asInt(STRING_LOW_STEP);
        int highStep = ratioTapChanger.asInt(STRING_HIGH_STEP);
        int neutralStep = ratioTapChanger.asInt(STRING_NEUTRAL_STEP);
        int normalStep = ratioTapChanger.asInt(STRING_NORMAL_STEP, neutralStep);
        int position = getTapPosition(ratioTapChanger, normalStep);
        if (position > highStep || position < lowStep) {
            position = neutralStep;
        }
        tapChanger.setLowTapPosition(lowStep).setTapPosition(position);

        boolean ltcFlag = ratioTapChanger.asBoolean(STRING_LTC_FLAG, false);
        tapChanger.setLtcFlag(ltcFlag);

        addRatioRegulationData(ratioTapChanger, tapChanger);

        addRatioSteps(ratioTapChanger, tapChanger);
        return tapChanger;
    }

    private int getTapPosition(PropertyBag tapChanger, int defaultStep) {
        switch (context.config().getProfileUsedForInitialStateValues()) {
            case SSH:
                return fromContinuous(tapChanger.asDouble(STRING_STEP, tapChanger.asDouble(STRING_SV_TAP_STEP, defaultStep)));
            case SV:
                return fromContinuous(tapChanger.asDouble(STRING_SV_TAP_STEP, tapChanger.asDouble(STRING_STEP, defaultStep)));
            default:
                throw new CgmesModelException("Unexpected profile used for initial flows values: " + context.config().getProfileUsedForInitialStateValues());
        }
    }

    private void addRatioRegulationData(PropertyBag ratioTapChanger, TapChangerConversion tapChanger) {
        tapChanger.setId(ratioTapChanger.getId(STRING_RATIO_TAP_CHANGER))
            .setRegulatingControlId(context.regulatingControlMapping().forTransformers().getRegulatingControlId(ratioTapChanger))
            .setTculControlMode(ratioTapChanger.get(STRING_TCUL_CONTROL_MODE))
            .setTapChangerControlEnabled(ratioTapChanger.asBoolean(STRING_TAP_CHANGER_CONTROL_ENABLED, false));
    }

    private void addRatioSteps(PropertyBag ratioTapChanger, TapChangerConversion tapChanger) {
        String tableId = ratioTapChanger.getId(CgmesNames.RATIO_TAP_CHANGER_TABLE);
        if (tableId != null) {
            addTabularRatioSteps(tapChanger, tableId);
        } else {
            addNonTabularRatioSteps(ratioTapChanger, tapChanger);
        }
    }

    private void addTabularRatioSteps(TapChangerConversion tapChanger, String tableId) {
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

    private void addNonTabularRatioSteps(PropertyBag ratioTapChanger, TapChangerConversion tapChanger) {
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

    // Phase Tap Changer
    protected TapChangerConversion getPhaseTapChanger(PropertyBag phaseTapChanger, double xtx) {
        if (phaseTapChanger == null) {
            return null;
        }
        TapChangerConversion tapChanger = new TapChangerConversion();
        int lowStep = phaseTapChanger.asInt(STRING_LOW_STEP);
        int highStep = phaseTapChanger.asInt(STRING_HIGH_STEP);
        int neutralStep = phaseTapChanger.asInt(STRING_NEUTRAL_STEP);
        int normalStep = phaseTapChanger.asInt(STRING_NORMAL_STEP, neutralStep);
        int position = getTapPosition(phaseTapChanger, normalStep);
        if (position > highStep || position < lowStep) {
            position = neutralStep;
        }
        tapChanger.setLowTapPosition(lowStep).setTapPosition(position);

        boolean ltcFlag = phaseTapChanger.asBoolean(STRING_LTC_FLAG, false);
        tapChanger.setLtcFlag(ltcFlag);

        addPhaseRegulationData(phaseTapChanger, tapChanger);

        addPhaseSteps(phaseTapChanger, tapChanger, xtx);
        return tapChanger;
    }

    private void addPhaseRegulationData(PropertyBag phaseTapChanger, TapChangerConversion tapChanger) {
        tapChanger.setId(phaseTapChanger.getId(STRING_PHASE_TAP_CHANGER))
            .setRegulatingControlId(context.regulatingControlMapping().forTransformers().getRegulatingControlId(phaseTapChanger))
            .setTapChangerControlEnabled(phaseTapChanger.asBoolean(STRING_TAP_CHANGER_CONTROL_ENABLED, false));
    }

    protected void addPhaseSteps(PropertyBag phaseTapChanger, TapChangerConversion tapChanger, double xtx) {
        String tableId = phaseTapChanger.getId(CgmesNames.PHASE_TAP_CHANGER_TABLE);
        String phaseTapChangerType = phaseTapChanger.getLocal(STRING_PHASE_TAP_CHANGER_TYPE).toLowerCase();
        if (isTabular(phaseTapChangerType, tableId)) {
            addTabularPhaseSteps(tapChanger, tableId);
        } else if (isAsymmetrical(phaseTapChangerType)) {
            addAsymmetricalPhaseSteps(phaseTapChanger, tapChanger, xtx);
        } else if (isSymmetrical(phaseTapChangerType)) {
            addSymmetricalPhaseSteps(phaseTapChanger, tapChanger, xtx);
        } else {
            getPhaseDefaultStep(tapChanger);
        }
    }

    private void addTabularPhaseSteps(TapChangerConversion tapChanger, String tableId) {
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

    private void addAsymmetricalPhaseSteps(PropertyBag phaseTapChanger, TapChangerConversion tapChanger, double xtx) {
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

    private void addSymmetricalPhaseSteps(PropertyBag phaseTapChanger, TapChangerConversion tapChanger, double xtx) {
        int lowStep = phaseTapChanger.asInt(STRING_LOW_STEP);
        int highStep = phaseTapChanger.asInt(STRING_HIGH_STEP);
        int neutralStep = phaseTapChanger.asInt(STRING_NEUTRAL_STEP);
        double stepVoltageIncrement = phaseTapChanger.asDouble(STRING_VOLTAGE_STEP_INCREMENT);
        double stepPhaseShiftIncrement = phaseTapChanger.asDouble(STRING_STEP_PHASE_SHIFT_INCREMENT);
        for (int step = lowStep; step <= highStep; step++) {
            double angle;
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
            step.setX(100 * (x - xtx) / xtx);
        });
    }

    private double getStepXforSymmetrical(double xStepMin, double xStepMax, double alphaDegrees,
        double alphaMaxDegrees) {
        double alpha = Math.toRadians(alphaDegrees);
        double alphaMax = Math.toRadians(alphaMaxDegrees);
        return xStepMin
            + (xStepMax - xStepMin) * Math.pow(Math.sin(alpha / 2) / Math.sin(alphaMax / 2), 2);
    }

    private void getPhaseDefaultStep(TapChangerConversion tapChanger) {
        tapChanger.beginStep()
            .endStep();
    }

    private boolean isTabular(String tapChangerType, String tableId) {
        return tableId != null && tapChangerType != null && tapChangerType.endsWith(STRING_TABULAR);
    }

    private boolean isSymmetrical(String tapChangerType) {
        return tapChangerType != null && tapChangerType.endsWith(STRING_SYMMETRICAL);
    }

    private boolean isAsymmetrical(String tapChangerType) {
        return tapChangerType != null && tapChangerType.endsWith(STRING_ASYMMETRICAL);
    }

}

