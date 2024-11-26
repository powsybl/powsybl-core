/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.cgmes.conversion.elements.transformers;

import com.powsybl.cgmes.conversion.Context;
import com.powsybl.cgmes.conversion.RegulatingControlMappingForTransformers;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.triplestore.api.PropertyBag;
import com.powsybl.triplestore.api.PropertyBags;

import java.util.Comparator;
import java.util.function.Supplier;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */

public class CgmesPhaseTapChangerBuilder extends AbstractCgmesTapChangerBuilder {

    private final String type;
    private final String typeLowerCase;
    private final String tableId;
    private final double xtx;

    CgmesPhaseTapChangerBuilder(PropertyBag phaseTapChanger, double xtx, Context context) {
        super(phaseTapChanger, context);
        this.type = p.getLocal(CgmesNames.PHASE_TAP_CHANGER_TYPE);
        // To optimise comparisons with valid types
        this.typeLowerCase = this.type.toLowerCase();
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

        // We keep the original type of tap changer (linear, symmetrical, asymmetrical)
        // The type stored here will eventually be used to determine the class in the SSH export
        // If only SSH export is written, the type used should match the original one
        return super.build().setType(toClassTypeFromClassOrKind(type));
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
            PropertyBags tablePoints = context.cgmes().groupedPhaseTapChangerTablePoints().getOrDefault(tableId, null);
            if (tablePoints == null) {
                addStepsLinear();
                return;
            }
            if (isTableValid(tableId, tablePoints)) {
                addStepsFromTable(tablePoints);
            } else {
                addStepsLinear();
            }
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
        double xMin = getXMin();
        double xMax = getXMax();
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
        int neutralStep = p.asInt(CgmesNames.NEUTRAL_STEP);
        double stepVoltageIncrement = p.asDouble(CgmesNames.VOLTAGE_STEP_INCREMENT);
        double stepPhaseShiftIncrement = p.asDouble(CgmesNames.STEP_PHASE_SHIFT_INCREMENT);
        for (int step = lowStep; step <= highStep; step++) {
            double angle;
            if (!Double.isNaN(stepPhaseShiftIncrement) && stepPhaseShiftIncrement != 0.0) {
                angle = (step - neutralStep) * stepPhaseShiftIncrement;
            } else {
                double dy = (step - neutralStep) * (stepVoltageIncrement / 100.0);
                angle = Math.toDegrees(2 * Math.atan(dy / 2));
            }
            tapChanger.beginStep()
                .setRatio(1.0)
                .setAngle(angle)
                .endStep();
        }
        stepXforLinearAndSymmetrical();
    }

    private void stepXforLinearAndSymmetrical() {
        double xMin = getXMin();
        double xMax = getXMax();
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
        return typeLowerCase != null && typeLowerCase.endsWith(CgmesNames.LINEAR);
    }

    private boolean isTabular() {
        return tableId != null && typeLowerCase != null && typeLowerCase.endsWith(CgmesNames.TABULAR);
    }

    private boolean isSymmetrical() {
        return typeLowerCase != null && !typeLowerCase.endsWith(CgmesNames.ASYMMETRICAL) && typeLowerCase.endsWith(CgmesNames.SYMMETRICAL);
    }

    private boolean isAsymmetrical() {
        return typeLowerCase != null && typeLowerCase.endsWith(CgmesNames.ASYMMETRICAL);
    }

    private double getXMin() {
        // xMin has been deprecated in CGMES 3, and is optional
        // Also, if the value read is inconsistent, x of the transformer must be used.
        // Quoting the definition from CGMES 3:
        // "PowerTransformerEnd.x shall be consistent with PhaseTapChangerLinear.xMin
        // and PhaseTapChangerNonLinear.xMin.
        // In case of inconsistency, PowerTransformerEnd.x shall be used."
        double xMin = p.asDouble(CgmesNames.X_STEP_MIN, p.asDouble(CgmesNames.X_MIN, 0));
        if (xMin <= 0) {
            return xtx;
        }
        return xMin;
    }

    private double getXMax() {
        return p.asDouble(CgmesNames.X_STEP_MAX, p.asDouble(CgmesNames.X_MAX));
    }

    private static String toClassTypeFromClassOrKind(String type) {
        // If type is obtained from CIM14 kind PhaseTapChanger.phaseTapChangerType
        // It has the pattern PhaseTapChangerKind.<type>
        // where type can be symmetrical, asymmetrical
        if (type.startsWith("PhaseTapChangerKind.")) {
            int idot = type.indexOf('.');
            String kind = type.substring(idot + 1);
            String camelKind = kind.substring(0, 1).toUpperCase() + kind.substring(1);
            return "PhaseTapChanger" + camelKind;
        }
        // Otherwise, type has been read from the class name,
        // we do not have to transform it
        return type;
    }

}
