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
import com.powsybl.commons.PowsyblException;
import com.powsybl.triplestore.api.PropertyBag;
import com.powsybl.triplestore.api.PropertyBags;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */

public class CgmesPhaseTapChangerBuilder extends AbstractCgmesTapChangerBuilder {

    private final String type;
    private final String tableId;
    private final double xtx;

    CgmesPhaseTapChangerBuilder(PropertyBag phaseTapChanger, double xtx, Context context) {
        super(phaseTapChanger, context);
        this.type = p.getLocal(CgmesNames.PHASE_TAP_CHANGER_TYPE).toLowerCase();
        this.tableId = p.getId(CgmesNames.PHASE_TAP_CHANGER_TABLE);
        this.xtx = xtx;
    }

    @Override
    public TapChanger build() {
        if (!validType()) {
            Supplier<String> utype = () -> "Unexpected type " + this.type;
            context.invalid(CgmesNames.PHASE_TAP_CHANGER_TYPE, utype);
            return null;
        }
        return super.build();
    }

    private boolean validType() {
        return isLinear() || isTabular() || isSymmetrical() || isAsymmetrical();
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
        if (isLinear()) {
            addStepsLinear();
        } else if (isTabular()) {
            PropertyBags table = context.phaseTapChangerTable(tableId);
            if (table == null) {
                addStepsLinear();
                return;
            }
            int min = table.stream().map(p -> p.asInt(CgmesNames.STEP)).min(Integer::compareTo).orElseThrow(() -> new PowsyblException("Should at least contain one step"));
            for (int i = min; i < min + table.size(); i++) {
                int index = i;
                if (table.stream().noneMatch(p -> p.asInt(CgmesNames.STEP) == index)) {
                    context.ignored("PhaseTapChanger table", () -> String.format("There is at least one missing step (%s) in table %s. Phase tap changer considered linear", index, tableId));
                    addStepsLinear();
                    return;
                }
            }
            addStepsFromTable(table);
        } else if (isAsymmetrical()) {
            addStepsAsymmetrical();
        } else if (isSymmetrical()) {
            addStepsSymmetrical();
        } else {
            // Add a single step with default values by default
            tapChanger.beginStep().endStep();
        }
    }

    private void addStepsLinear() {
        int lowStep = p.asInt(CgmesNames.LOW_STEP);
        int highStep = p.asInt(CgmesNames.HIGH_STEP);
        int neutralStep = p.asInt(CgmesNames.NEUTRAL_STEP);
        double stepPhaseShiftIncrement = p.asDouble(CgmesNames.STEP_PHASE_SHIFT_INCREMENT);
        for (int step = lowStep; step <= highStep; step++) {
            double angle = 0.0;
            if (!Double.isNaN(stepPhaseShiftIncrement) && stepPhaseShiftIncrement != 0.0) {
                angle = (step - neutralStep) * stepPhaseShiftIncrement;
            }
            tapChanger.beginStep()
                .setRatio(1.0)
                .setAngle(angle)
                .endStep();
        }
        stepXforLinearAndSymmetrical();
    }

    private void addStepsFromTable(PropertyBags table) {
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
                .mapToDouble(Double::doubleValue).max().orElse(0);
        tapChanger.getSteps().forEach(step -> {
            if (alphaMax == 0) { // all angles are equal to 0: x is considered equal to 0
                step.setX(0);
                return;
            }
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
        stepXforLinearAndSymmetrical();
    }

    private void stepXforLinearAndSymmetrical() {
        double xMin = p.asDouble(CgmesNames.X_STEP_MIN, p.asDouble(CgmesNames.X_MIN));
        double xMax = p.asDouble(CgmesNames.X_STEP_MAX, p.asDouble(CgmesNames.X_MAX));
        if (Double.isNaN(xMin) || Double.isNaN(xMax) || xMin < 0 || xMax <= 0 || xMin > xMax) {
            return;
        }
        double alphaMax = tapChanger.getSteps().stream().map(TapChanger.Step::getAngle)
            .mapToDouble(Double::doubleValue).max().orElse(0);
        tapChanger.getSteps().forEach(step -> {
            if (alphaMax == 0.0) { // all angles are equal to 0: x is considered equal to 0
                step.setX(0);
                return;
            }
            double alpha = step.getAngle();
            double x = getStepXforLinearAndSymmetrical(xMin, xMax, alpha, alphaMax);
            step.setX(100 * (x - xtx) / xtx);
        });
    }

    private static double getStepXforLinearAndSymmetrical(double xStepMin, double xStepMax, double alphaDegrees,
                                                 double alphaMaxDegrees) {
        double alpha = Math.toRadians(alphaDegrees);
        double alphaMax = Math.toRadians(alphaMaxDegrees);
        return xStepMin
                + (xStepMax - xStepMin) * Math.pow(Math.sin(alpha / 2) / Math.sin(alphaMax / 2), 2);
    }

    private boolean isLinear() {
        return type != null && type.endsWith(CgmesNames.LINEAR);
    }

    private boolean isTabular() {
        return tableId != null && type != null && type.endsWith(CgmesNames.TABULAR);
    }

    private boolean isSymmetrical() {
        return type != null && !type.endsWith(CgmesNames.ASYMMETRICAL) && type.endsWith(CgmesNames.SYMMETRICAL);
    }

    private boolean isAsymmetrical() {
        return type != null && type.endsWith(CgmesNames.ASYMMETRICAL);
    }
}
