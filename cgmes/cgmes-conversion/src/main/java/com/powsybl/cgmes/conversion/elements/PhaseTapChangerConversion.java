/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.conversion.elements;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;

import com.powsybl.cgmes.conversion.elements.transformers.NewThreeWindingsTransformerConversion;
import com.powsybl.cgmes.model.CgmesModelException;
import com.powsybl.iidm.network.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.powsybl.cgmes.conversion.Context;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.triplestore.api.PropertyBag;
import com.powsybl.triplestore.api.PropertyBags;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 *
 * @deprecated Use {@link NewThreeWindingsTransformerConversion} or {@link NewThreeWindingsTransformerConversion} instead.
 */
@Deprecated
public class PhaseTapChangerConversion extends AbstractIdentifiedObjectConversion {

    private static final String REGULATING_CONTROL_ENABLED = "regulatingControlEnabled";

    public PhaseTapChangerConversion(PropertyBag ptc, Context context) {
        super("PhaseTapChanger", ptc, context);

        configIsInvertVoltageStepIncrementOutOfPhase = false;

        tx = context.tapChangerTransformers().transformer2(id);
        ptcType = ptc.getLocal("phaseTapChangerType").toLowerCase();
        lowStep = ptc.asInt("lowStep");
        highStep = ptc.asInt("highStep");
        neutralStep = ptc.asInt("neutralStep");
        defaultStep = ptc.asInt("normalStep", neutralStep);
        if (tx != null) {
            side = context.tapChangerTransformers().whichSide(id);
        } else {
            side = -1;
        }
        ltcFlag = ptc.asBoolean("ltcFlag", false);
        LOG.debug("PhaseTapChanger {} with ltcFlag {}", id, ltcFlag);
    }

    @Override
    public boolean valid() {
        if (tx == null) {
            // Phase tap changers are supported only for two winding transformers
            // If the transformer exists but is three winding
            // check if the tap is at neutral and if it is not regulating
            // to decide if we ignore or mark as invalid
            ThreeWindingsTransformer tx3 = context.tapChangerTransformers().transformer3(id);
            if (tx3 == null) {
                String end = p.getId("TransformerEnd");
                missing(String.format("PowerTransformer, transformerEnd is %s", end));
                return false;
            } else {
                String reason0 = String.format(
                        "Not supported for 3wtx. txId 'name' 'substation': %s '%s' '%s'",
                        tx3.getId(),
                        tx3.getNameOrId(),
                        tx3.getSubstation().getNameOrId());
                // Check if the step is at neutral and regulating control is disabled
                int position = fromContinuous(p.asDouble("SVtapStep", neutralStep));
                boolean regulating = p.asBoolean(REGULATING_CONTROL_ENABLED, false);
                if (position == neutralStep && !regulating) {
                    Supplier<String> reason = () -> reason0 + ", but is at neutralStep and regulating control disabled";
                    ignored(reason);
                } else {
                    Supplier<String> reason = () -> String.format(
                            "%s, tap step: %d, neutral [low, high]: %d [%d, %d], regulating control enabled: %b",
                            reason0,
                            position,
                            neutralStep,
                            lowStep,
                            highStep,
                            regulating);
                    invalid(reason);
                }
                return false;
            }
        }
        if (!presentMandatoryProperty(CgmesNames.TRANSFORMER_WINDING_RATED_U)) {
            return false;
        }
        if (!inRange("defaultStep", defaultStep, lowStep, highStep)) {
            return false;
        }
        if (!validType()) {
            invalid("Unexpected phaseTapChangerType " + ptcType);
            return false;
        }
        return true;
    }

    @Override
    public void convert() {
        //int position = fromContinuous(p.asDouble("step", p.asDouble("SVtapStep", defaultStep)));
        PhaseTapChangerAdder ptca = tx.newPhaseTapChanger()
                .setLowTapPosition(lowStep)
                .setTapPosition(getTapPosition());

        if (tabular()) {
            addStepsFromTable(ptca);
        } else {
            double du0 = du0();
            double du = du();
            double theta = theta();
            if (LOG.isDebugEnabled()) {
                LOG.debug("ACTUAL side {}, tx {}, id {}", side, tx.getId(), id);
                LOG.debug("ACTUAL du0,du,theta {} {} {}", du0, du, theta);
            }

            List<Double> angles = new ArrayList<>();
            List<Double> ratios = new ArrayList<>();
            fillAnglesRatios(du0, du, theta, angles, ratios);
            addSteps(angles, ratios, theta, ptca);
        }

        ptca.add();
    }

    private void addStepsFromTable(PhaseTapChangerAdder ptca) {
        String tableId = p.getId(CgmesNames.PHASE_TAP_CHANGER_TABLE);
        if (tableId == null) {
            missing(CgmesNames.PHASE_TAP_CHANGER_TABLE);
            return;
        }
        LOG.debug("PhaseTapChanger {} table {}", id, tableId);
        PropertyBags table = context.cgmes().phaseTapChangerTable(tableId);
        if (table.isEmpty()) {
            missing("points for PhaseTapChangerTable " + tableId);
            return;
        }
        Comparator<PropertyBag> byStep = Comparator.comparingInt((PropertyBag p) -> p.asInt("step"));
        table.sort(byStep);
        for (PropertyBag point : table) {

            // CGMES uses ratio to define the relationship between voltage ends while IIDM uses rho
            // ratio and rho as complex numbers are reciprocals. Given V1 and V2 the complex voltages at end 1 and end 2 of a branch we have:
            // V2 = V1 * rho and V2 = V1 / ratio
            // This is why we have: rho=1/ratio and alpha=-angle
            double alpha = -point.asDouble("angle");
            double rho = 1 / point.asDouble("ratio", 1.0);

            // When given in PhaseTapChangerTablePoint
            // r, x, g, b of the step are already percentage deviations of nominal values
            int step = point.asInt("step");
            double r = fixing(point, "r", 0, tableId, step);
            double x = fixing(point, "x", 0, tableId, step);
            double g = fixing(point, "g", 0, tableId, step);
            double b = fixing(point, "b", 0, tableId, step);
            // Impedance/admittance deviation is required when tap changer is defined at
            // side 2
            // (In IIDM model the ideal ratio is always at side 1, left of impedance)
            double dz = 0;
            double dy = 0;
            if (side != 1) {
                double rho2 = rho * rho;
                dz = (1 / rho2 - 1) * 100;
                dy = (rho2 - 1) * 100;
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug("    {} {} {} {} {} {} {} {} {}", step, alpha, rho, r, x, g, b, dz, dy);
            }
            // We have to merge previous explicit corrections defined for the tap
            // with dz, dy that appear when moving ideal ratio to side 1
            // R' = R * (1 + r/100) * (1 + dz/100) ==> r' = r + dz + r * dz / 100
            ptca.beginStep()
                    .setAlpha(alpha * (side == 1 ? 1 : -1))
                    .setRho(side == 1 ? rho : 1 / rho)
                    .setR(r + dz + r * dz / 100)
                    .setX(x + dz + r * dz / 100)
                    .setG(g + dy + g * dy / 100)
                    .setB(b + dy + b * dy / 100)
                    .endStep();
        }
    }

    private double fixing(PropertyBag point, String attr, double defaultValue, String tableId, int step) {
        double value = point.asDouble(attr, defaultValue);
        if (Double.isNaN(value)) {
            fixed(
                "PhaseTapChangerTablePoint " + attr + " for step " + step + " in table " + tableId,
                "invalid value " + point.get(attr));
            return defaultValue;
        }
        return value;
    }

    private double du0() {
        double neutralU = p.asDouble("neutralU");
        double transformerWindingRatedU = p.asDouble(CgmesNames.TRANSFORMER_WINDING_RATED_U);
        double du0 = neutralU / transformerWindingRatedU;
        if (Math.abs(du0) > 0.5) {
            du0 = 0;
        }
        return du0;
    }

    private double du() {
        double du;
        double transformerWindingRatedU = p.asDouble(CgmesNames.TRANSFORMER_WINDING_RATED_U);
        double voltageStepIncrementOutOfPhase = p.asDouble("voltageStepIncrementOutOfPhase");
        boolean voltageStepIncrementOutOfPhaseIsSet = p
                .containsKey("voltageStepIncrementOutOfPhase");
        double voltageStepIncrement = p.asDouble("voltageStepIncrement");
        boolean voltageStepIncrementIsSet = p.containsKey("voltageStepIncrement");
        if (voltageStepIncrementOutOfPhaseIsSet && voltageStepIncrementOutOfPhase != 0) {
            du = (configIsInvertVoltageStepIncrementOutOfPhase ? -1 : 1)
                    * voltageStepIncrementOutOfPhase / transformerWindingRatedU;
        } else if (voltageStepIncrementIsSet && voltageStepIncrement != 0) {
            du = voltageStepIncrement / 100;
        } else {
            double defaultValue = 1;
            String reason = "Not present or not valid value for voltageStepIncrementOutOfPhase or voltageStepIncrement";
            invalid("du", reason, defaultValue);
            du = defaultValue / 100;
        }
        return du;
    }

    private double theta() {
        double theta;
        double windingConnectionAngle = p.asDouble("windingConnectionAngle");
        boolean windingConnectionAngleIsSet = p.containsKey("windingConnectionAngle");
        if (windingConnectionAngleIsSet) {
            theta = Math.toRadians(windingConnectionAngle);
        } else {
            theta = Math.PI / 2;
            missing("windingConnnectionAngle", 90);
        }
        return theta;
    }

    private void fillAnglesRatios(
            double du0, double du, double theta,
            List<Double> angles,
            List<Double> ratios) {
        if (asymmetrical()) {
            fillAngleRatioListsAsymmetrical(du0, du, theta, angles, ratios);
        } else if (symmetrical()) {
            fillAngleRatioListsSymmetrical(du0, du, theta, angles, ratios);
        }
    }

    private void fillAngleRatioListsAsymmetrical(
            double du0, double du, double theta,
            List<Double> angles,
            List<Double> ratios) {
        for (int step = lowStep; step <= highStep; step++) {
            int n = step - neutralStep;
            double dx = (n * du - du0) * Math.cos(theta);
            double dy = (n * du - du0) * Math.sin(theta);
            double angle = Math.atan2(dy, 1 + dx);
            double ratio = Math.hypot(dy, 1 + dx);

            angles.add(angle);
            ratios.add(ratio);
            LOG.debug("ACTUAL    n,dx,dy,angle,ratio  {} {} {} {} {}", n, dx, dy, angle, ratio);
        }
    }

    private void fillAngleRatioListsSymmetrical(
            double du0, double du, double theta,
            List<Double> angles,
            List<Double> ratios) {
        double stepPhaseShiftIncrement = p.asDouble("stepPhaseShiftIncrement");
        boolean stepPhaseShiftIncrementIsSet = p.containsKey("stepPhaseShiftIncrement");
        if (stepPhaseShiftIncrementIsSet && stepPhaseShiftIncrement != 0) {
            for (int step = lowStep; step <= highStep; step++) {
                int n = step - neutralStep;
                double angle = n * Math.toRadians(
                        (configIsInvertVoltageStepIncrementOutOfPhase ? -1 : 1)
                                * stepPhaseShiftIncrement);
                double ratio = 1.0;

                angles.add(angle);
                ratios.add(ratio);
            }
        } else {
            for (int step = lowStep; step <= highStep; step++) {
                int n = step - neutralStep;
                double dy = (n * du / 2 - du0) * Math.sin(theta);
                double angle = 2 * Math.asin(dy);
                double ratio = 1.0;

                angles.add(angle);
                ratios.add(ratio);
                LOG.debug("ACTUAL    n,dy,angle,ratio  {} {} {} {}", n, dy, angle, ratio);
            }
        }
    }

    private void addSteps(
            List<Double> angles, List<Double> ratios,
            double theta,
            PhaseTapChangerAdder ptca) {

        double[] xs = new double[2];
        boolean xStepRangeIsConsistent = gatherxStepMinMax(xs);
        double xStepMin = xs[0];
        double xStepMax = xs[1];

        double angleMax = angles.stream()
                .mapToDouble(Double::doubleValue)
                .max()
                .orElse(Double.NaN);
        LOG.debug("ACTUAL    angleMax {}", angleMax);
        LOG.debug("ACTUAL    xStepMin, xStepMax {}, {}", xStepMin, xStepMax);

        // Take structural ratio
        double ratio0 = tx.getRatedU2() / tx.getRatedU1();
        double ratio0square = ratio0 * ratio0;
        LOG.debug("ACTUAL    u2,u1,ratio0square {}, {}, {}", tx.getRatedU2(), tx.getRatedU1(), ratio0square);

        for (int i = 0; i < angles.size(); i++) {
            double angle = angles.get(i);
            double ratio =  ratios.get(i);
            double x = 0.0;
            if (!xStepRangeIsConsistent || angleMax == 0) {
                x = tx.getX();
            } else {
                if (asymmetrical()) {
                    x = getStepXforAsymmetrical(xStepMin, xStepMax, angle, angleMax, theta);
                } else if (symmetrical()) {
                    x = getStepXforSymmetrical(xStepMin, xStepMax, angle, angleMax);
                }
                x = adjustx(x, ratio0square);
            }
            double dx = (x - tx.getX()) / tx.getX() * 100;

            // In IIDM, all PTC must be side one
            double dr = 0.0;
            double dg = 0.0;
            double db = 0.0;
            if (side != 1) {
                double a2 = ratio * ratio;
                ratio = 1.0 / ratio;
                angle = -angle;
                dr = 100 * ((1 + dr / 100) * a2 - 1);
                dx = 100 * ((1 + dx / 100) * a2 - 1);
                dg = 100 * ((1 + dg / 100) / a2 - 1);
                db = 100 * ((1 + db / 100) / a2 - 1);
            }

            // CGMES uses ratio to define the relationship between voltage ends while IIDM uses rho
            // ratio and rho as complex numbers are reciprocals. Given V1 and V2 the complex voltages at end 1 and end 2 of a branch we have:
            // V2 = V1 * rho and V2 = V1 / ratio
            // This is why we have: rho=1/ratio and alpha=-angle
            double alpha = -angle;
            double rho = 1.0 / ratio;

            ptca.beginStep()
                    .setAlpha(Math.toDegrees(alpha))
                    .setRho(rho)
                    .setR(dr)
                    .setX(dx)
                    .setG(dg)
                    .setB(db)
                    .endStep();
            if (LOG.isDebugEnabled()) {
                int n = (lowStep + i) - neutralStep;
                LOG.debug("ACTUAL    n,rho,alpha,x,dx   {} {} {} {} {}",
                        n, rho, Math.toDegrees(alpha), x, dx);
            }
        }
    }

    private static double getStepXforAsymmetrical(
            double xStepMin, double xStepMax,
            double alpha, double alphaMax,
            double theta) {
        double numer = Math.sin(theta) - Math.tan(alphaMax) * Math.cos(theta);
        double denom = Math.sin(theta) - Math.tan(alpha) * Math.cos(theta);
        return xStepMin + (xStepMax - xStepMin)
                * Math.pow(Math.tan(alpha) / Math.tan(alphaMax) * numer / denom, 2);
    }

    private static double getStepXforSymmetrical(
            double xStepMin, double xStepMax,
            double alpha, double alphaMax) {
        return xStepMin + (xStepMax - xStepMin)
                * Math.pow(Math.sin(alpha / 2) / Math.sin(alphaMax / 2), 2);
    }

    private boolean gatherxStepMinMax(double[] xs) {

        // In ENTSOE Profile1_v14, the documentation about xStepMax, xStepMin:
        // xStepMax: The reactance at the maximum tap step.
        // xStepMin: The reactance at the minimum tap step.

        // In CGMES, the documentation about xMin, xMax:
        // The reactance depend on the tap position according to a "u" shaped curve.
        // The maximum reactance (xMax) appear at the low and high tap positions.
        // The minimum reactance (xMin) appear at the mid tap position.

        // In CGMES, in the reactance formulas for Phase Tap Changers:
        // X(0) is defined as xMin.
        // X(alphaMax) is defined as xMax.

        // So the xMin, xMax values of CGMES are equivalent to xStepMax, xStepMin of
        // CIM1

        double xStepMin = 0;
        double xStepMax = 0;
        boolean xStepMinIsSet = p.containsKey("xStepMin") || p.containsKey("xMin");
        boolean xStepMaxIsSet = p.containsKey("xStepMax") || p.containsKey("xMax");
        if (xStepMinIsSet && xStepMaxIsSet) {
            xStepMin = p.asDouble("xStepMin", p.asDouble("xMin"));
            xStepMax = p.asDouble("xStepMax", p.asDouble("xMax"));
        }

        boolean xStepRangeIsConsistent = true;
        if (xStepMin < 0 || xStepMax <= 0 || xStepMin > xStepMax) {
            xStepRangeIsConsistent = false;
            final double xStepMinParam = xStepMin;
            final double xStepMaxParam = xStepMax;
            Supplier<String> reason = () -> String.format("Inconsistent xStepMin, xStepMax [%f, %f]",
                    xStepMinParam,
                    xStepMaxParam);
            ignored("xStep range", reason);
        }

        xs[0] = xStepMin;
        xs[1] = xStepMax;
        return xStepRangeIsConsistent;
    }

    double adjustx(double x, double rho0square) {
        // TODO Review this adjustment taking into account the way we build x of
        // transformer
        // from x1, x2 of transformerEnds:
        // x = x1 * rho0square + x2
        // Check if in the case conformity/microBE (tap changer 6ebbef67)
        // we should apply formulas for in-phase transformer (fixed tap)
        // and asymmetrical phase shifter
        return x * (side == 1 ? rho0square : 1.0);
    }

    private Terminal regTerminal() {
        if (side == 1) {
            return tx.getTerminal1();
        } else {
            return tx.getTerminal2();
        }
    }

    private int getTapPosition() {
        switch (context.config().getProfileUsedForInitialStateValues()) {
            case SSH:
                return fromContinuous(p.asDouble("step", p.asDouble("SVtapStep", defaultStep)));
            case SV:
                return fromContinuous(p.asDouble("SVtapStep", p.asDouble("step", defaultStep)));
            default:
                throw new CgmesModelException("Unexpected profile used for initial flows values: " + context.config().getProfileUsedForInitialStateValues());
        }
    }

    private boolean validType() {
        return asymmetrical() || symmetrical() || tabular();
    }

    private boolean tabular() {
        return ptcType.endsWith("tabular");
    }

    private boolean asymmetrical() {
        return ptcType.endsWith("asymmetrical");
    }

    private boolean symmetrical() {
        return ptcType.endsWith("symmetrical");
    }

    private final TwoWindingsTransformer tx;
    private final String ptcType;
    private final int lowStep;
    private final int highStep;
    private final int neutralStep;
    private final int defaultStep;
    private final int side;
    private final boolean ltcFlag;

    private boolean configIsInvertVoltageStepIncrementOutOfPhase;

    private static final Logger LOG = LoggerFactory.getLogger(PhaseTapChangerConversion.class);
}
