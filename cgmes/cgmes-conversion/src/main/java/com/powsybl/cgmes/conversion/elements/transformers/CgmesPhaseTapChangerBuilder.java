/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.conversion.elements.transformers;

import java.util.Comparator;
import java.util.function.Supplier;

import com.powsybl.cgmes.conversion.Context;
import com.powsybl.cgmes.conversion.RegulatingControlMappingForTransformers;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.triplestore.api.PropertyBag;
import com.powsybl.triplestore.api.PropertyBags;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */

public class CgmesPhaseTapChangerBuilder extends AbstractCgmesTapChangerBuilder {

    private final double xtx;

    CgmesPhaseTapChangerBuilder(PropertyBag phaseTapChanger, double xtx, Context context) {
        super(phaseTapChanger, context);
        this.xtx = xtx;
    }

    @Override
    public TapChanger build() {
        if (!validType()) {
            Supplier<String> type = () -> "Unexpected type " + p.getLocal(CgmesNames.PHASE_TAP_CHANGER_TYPE).toLowerCase();
            context.invalid(CgmesNames.PHASE_TAP_CHANGER_TYPE, type);
            return null;
        }
        return super.build();
    }

    private boolean validType() {
        String tableId = p.getId(CgmesNames.PHASE_TAP_CHANGER_TABLE);
        String type = p.getLocal(CgmesNames.PHASE_TAP_CHANGER_TYPE).toLowerCase();
        return isTabular(type, tableId) || isSymmetrical(type) || isAsymmetrical(type);
    }

    @Override
    protected void addRegulationData() {
        String regulatingControlId = RegulatingControlMappingForTransformers.getRegulatingControlId(p);
        tapChanger.setId(p.getId(CgmesNames.PHASE_TAP_CHANGER))
                .setRegulating(context.regulatingControlMapping().forTransformers().getRegulating(regulatingControlId))
                .setRegulatingControlId(regulatingControlId)
                .setTapChangerControlEnabled(p.asBoolean(CgmesNames.TAP_CHANGER_CONTROL_ENABLED, false));
    }

    @Override
    protected void addSteps() {
        String tableId = p.getId(CgmesNames.PHASE_TAP_CHANGER_TABLE);
        String phaseTapChangerType = p.getLocal(CgmesNames.PHASE_TAP_CHANGER_TYPE).toLowerCase();
        if (isTabular(phaseTapChangerType, tableId)) {
            addStepsFromTable(tableId);
        } else if (isAsymmetrical(phaseTapChangerType)) {
            addStepsAsymmetrical();
        } else if (isSymmetrical(phaseTapChangerType)) {
            addStepsSymmetrical();
        } else {
            // Add a single step with default values by default
            tapChanger.beginStep().endStep();
        }
    }

    private void addStepsFromTable(String tableId) {
        PropertyBags table = context.phaseTapChangerTable(tableId);
        if (table == null) {
            return;
        }
        Comparator<PropertyBag> byStep = Comparator
                .comparingInt((PropertyBag p) -> p.asInt(CgmesNames.STEP));
        table.sort(byStep);
        for (PropertyBag point : table) {
            int step = point.asInt(CgmesNames.STEP);
            double angle = fixing(point, CgmesNames.ANGLE, 0.0, tableId, step);
            double ratio = fixing(point, CgmesNames.RATIO, 1.0, tableId, step);
            double r = fixing(point, CgmesNames.R, 0, tableId, step);
            double x = fixing(point, CgmesNames.X, 0, tableId, step);
            double g = fixing(point, CgmesNames.G, 0, tableId, step);
            double b = fixing(point, CgmesNames.B, 0, tableId, step);
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

    private void addStepsAsymmetrical() {
        int lowStep = p.asInt(CgmesNames.LOW_STEP);
        int highStep = p.asInt(CgmesNames.HIGH_STEP);
        int neutralStep = p.asInt(CgmesNames.NEUTRAL_STEP);
        double stepVoltageIncrement = p.asDouble(CgmesNames.VOLTAGE_STEP_INCREMENT);
        double windingConnectionAngle = p.asDouble(CgmesNames.WINDING_CONNECTION_ANGLE);
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
        double xMin = p.asDouble(CgmesNames.X_STEP_MIN, p.asDouble(CgmesNames.X_MIN));
        double xMax = p.asDouble(CgmesNames.X_STEP_MAX, p.asDouble(CgmesNames.X_MAX));
        if (Double.isNaN(xMin) || Double.isNaN(xMax) || xMin < 0 || xMax <= 0 || xMin > xMax) {
            return;
        }
        double alphaMax = tapChanger.getSteps().stream().map(TapChanger.Step::getAngle)
                .mapToDouble(Double::doubleValue).max().orElse(Double.NaN);
        tapChanger.getSteps().forEach(step -> {
            double alpha = step.getAngle();
            double x = getStepXforAsymmetrical(xMin, xMax, alpha, alphaMax, windingConnectionAngle);
            step.setX((x - xtx) / xtx * 100);
        });
    }

    private static double getStepXforAsymmetrical(double xStepMin, double xStepMax, double alphaDegrees,
                                                  double alphaMaxDegrees, double thetaDegrees) {
        double alpha = Math.toRadians(alphaDegrees);
        double alphaMax = Math.toRadians(alphaMaxDegrees);
        double theta = Math.toRadians(thetaDegrees);
        double numer = Math.sin(theta) - Math.tan(alphaMax) * Math.cos(theta);
        double denom = Math.sin(theta) - Math.tan(alpha) * Math.cos(theta);
        return xStepMin + (xStepMax - xStepMin)
                * Math.pow(Math.tan(alpha) / Math.tan(alphaMax) * numer / denom, 2);
    }

    private void addStepsSymmetrical() {
        int lowStep = p.asInt(CgmesNames.LOW_STEP);
        int highStep = p.asInt(CgmesNames.HIGH_STEP);
        int neutralStep = p.asInt(CgmesNames.NEUTRAL_STEP);
        double stepVoltageIncrement = p.asDouble(CgmesNames.VOLTAGE_STEP_INCREMENT);
        double stepPhaseShiftIncrement = p.asDouble(CgmesNames.STEP_PHASE_SHIFT_INCREMENT);
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
        double xMin = p.asDouble(CgmesNames.X_STEP_MIN, p.asDouble(CgmesNames.X_MIN));
        double xMax = p.asDouble(CgmesNames.X_STEP_MAX, p.asDouble(CgmesNames.X_MAX));
        if (Double.isNaN(xMin) || Double.isNaN(xMax) || xMin < 0 || xMax <= 0 || xMin > xMax) {
            return;
        }
        double alphaMax = tapChanger.getSteps().stream().map(TapChanger.Step::getAngle)
                .mapToDouble(Double::doubleValue).max().orElse(Double.NaN);
        tapChanger.getSteps().forEach(step -> {
            double alpha = step.getAngle();
            double x = getStepXforSymmetrical(xMin, xMax, alpha, alphaMax);
            step.setX(100 * (x - xtx) / xtx);
        });
    }

    private static double getStepXforSymmetrical(double xStepMin, double xStepMax, double alphaDegrees,
                                                 double alphaMaxDegrees) {
        double alpha = Math.toRadians(alphaDegrees);
        double alphaMax = Math.toRadians(alphaMaxDegrees);
        return xStepMin
                + (xStepMax - xStepMin) * Math.pow(Math.sin(alpha / 2) / Math.sin(alphaMax / 2), 2);
    }

    private static boolean isTabular(String tapChangerType, String tableId) {
        return tableId != null && tapChangerType != null && tapChangerType.endsWith(CgmesNames.TABULAR);
    }

    private static boolean isSymmetrical(String tapChangerType) {
        return tapChangerType != null && !tapChangerType.endsWith(CgmesNames.ASYMMETRICAL) && tapChangerType.endsWith(CgmesNames.SYMMETRICAL);
    }

    private static boolean isAsymmetrical(String tapChangerType) {
        return tapChangerType != null && tapChangerType.endsWith(CgmesNames.ASYMMETRICAL);
    }
}
