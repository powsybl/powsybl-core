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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.powsybl.cgmes.conversion.Conversion;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.iidm.network.PhaseTapChanger;
import com.powsybl.iidm.network.PhaseTapChangerAdder;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.ThreeWindingsTransformer;
import com.powsybl.iidm.network.TwoWindingsTransformer;
import com.powsybl.triplestore.api.PropertyBag;
import com.powsybl.triplestore.api.PropertyBags;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class PhaseTapChangerConversion extends AbstractIdentifiedObjectConversion {

    public PhaseTapChangerConversion(PropertyBag ptc, Conversion.Context context) {
        super("PhaseTapChanger", ptc, context);

        configIsInvertVoltageStepIncrementOutOfPhase = false;

        tx = context.tapChangerTransformers().transformer2(id);
        ptcType = ptc.getLocal("phaseTapChangerType").toLowerCase();
        lowStep = ptc.asInt("lowStep");
        highStep = ptc.asInt("highStep");
        neutralStep = ptc.asInt("neutralStep");
        defaultStep = ptc.asInt("normalStep", neutralStep);
        side = context.tapChangerTransformers().whichSide(id);
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
                        tx3.getName(),
                        tx3.getSubstation().getName());
                // Check if the step is at neutral and regulating control is disabled
                int position = fromContinuous(p.asDouble("SVtapStep", neutralStep));
                boolean regulating = p.asBoolean("regulatingControlEnabled", false);
                if (position == neutralStep && !regulating) {
                    String reason = String.format(
                            "%s, but is at neutralStep and regulating control disabled", reason0);
                    ignored(reason);
                } else {
                    String reason = String.format(
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
            invalid(String.format("Unexpected phaseTapChangerType %s", ptcType));
            return false;
        }
        return true;
    }

    @Override
    public void convert() {
        int position = fromContinuous(p.asDouble("SVtapStep", defaultStep));
        PhaseTapChangerAdder ptca = tx.newPhaseTapChanger()
                .setLowTapPosition(lowStep)
                .setTapPosition(position);

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

            List<Double> alphas = new ArrayList<>();
            List<Double> rhos = new ArrayList<>();
            fillAlphasRhos(du0, du, theta, alphas, rhos);
            addSteps(alphas, rhos, theta, ptca);
        }

        addRegulatingControl(ptca);

        ptca.add();
    }

    private void addStepsFromTable(PhaseTapChangerAdder ptca) {
        String tableId = p.getId("PhaseTapChangerTable");
        LOG.debug("PhaseTapChanger {} table {}", id, tableId);
        PropertyBags table = context.cgmes().phaseTapChangerTable(tableId);
        Comparator<PropertyBag> byStep = Comparator.comparingInt((PropertyBag p) -> p.asInt("step"));
        table.sort(byStep);
        for (PropertyBag point : table) {
            double alpha = point.asDouble("angle");
            double rho = point.asDouble("ratio");
            // When given in PhaseTapChangerTablePoint
            // r, x, g, b of the step are already percentage deviations of nominal values
            double r = point.asDouble("r", 0);
            double x = point.asDouble("x", 0);
            double g = point.asDouble("g", 0);
            double b = point.asDouble("b", 0);
            int step = point.asInt("step");
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

    private void fillAlphasRhos(
            double du0, double du, double theta,
            List<Double> alphas,
            List<Double> rhos) {
        if (asymmetrical()) {
            fillAlphaRhoListsAsymmetrical(du0, du, theta, alphas, rhos);
        } else if (symmetrical()) {
            fillAlphaRhoListsSymmetrical(du0, du, theta, alphas, rhos);
        }
    }

    private void fillAlphaRhoListsAsymmetrical(
            double du0, double du, double theta,
            List<Double> alphas,
            List<Double> rhos) {
        for (int step = lowStep; step <= highStep; step++) {
            int n = step - neutralStep;
            double dx = (n * du - du0) * Math.cos(theta);
            double dy = (n * du - du0) * Math.sin(theta);
            double alpha = Math.atan2(dy, 1 + dx);
            double rho = 1 / Math.hypot(dy, 1 + dx);
            alphas.add(alpha);
            rhos.add(rho);

            LOG.debug("ACTUAL    n,dx,dy,alpha,rho  {} {} {} {} {}", n, dx, dy, alpha, rho);
        }
    }

    private void fillAlphaRhoListsSymmetrical(
            double du0, double du, double theta,
            List<Double> alphas,
            List<Double> rhos) {
        double stepPhaseShiftIncrement = p.asDouble("stepPhaseShiftIncrement");
        boolean stepPhaseShiftIncrementIsSet = p.containsKey("stepPhaseShiftIncrement");
        if (stepPhaseShiftIncrementIsSet && stepPhaseShiftIncrement != 0) {
            for (int step = lowStep; step <= highStep; step++) {
                int n = step - neutralStep;
                double alpha = n * Math.toRadians(
                        (configIsInvertVoltageStepIncrementOutOfPhase ? -1 : 1)
                                * stepPhaseShiftIncrement);
                double rho = 1.0;
                alphas.add(alpha);
                rhos.add(rho);
            }
        } else {
            for (int step = lowStep; step <= highStep; step++) {
                int n = step - neutralStep;
                double dy = (n * du / 2 - du0) * Math.sin(theta);
                double alpha = 2 * Math.asin(dy);
                double rho = 1.0;
                alphas.add(alpha);
                rhos.add(rho);
            }
        }
    }

    private void addSteps(
            List<Double> alphas, List<Double> rhos,
            double theta,
            PhaseTapChangerAdder ptca) {

        double[] xs = new double[2];
        boolean xStepRangeIsConsistent = gatherxStepMinMax(xs);
        double xStepMin = xs[0];
        double xStepMax = xs[1];

        double alphaMax = alphas.stream()
                .mapToDouble(Double::doubleValue)
                .max()
                .orElse(Double.NaN);
        LOG.debug("ACTUAL    alphaMax {}", alphaMax);
        LOG.debug("ACTUAL    xStepMin, xStepMax {}, {}", xStepMin, xStepMax);

        // rho0 adjustment computed the same way that is done in TwoWindingsTransformer,
        // using factor rho0square as a float
        double rho0 = tx.getRatedU2() / tx.getRatedU1();
        double rho0square = rho0 * rho0;

        for (int i = 0; i < alphas.size(); i++) {
            double alpha = alphas.get(i);
            double rho = rhos.get(i);
            double x = 0.0;
            if (!xStepRangeIsConsistent || alphaMax == 0) {
                x = tx.getX();
            } else {
                if (asymmetrical()) {
                    x = getStepXforAsymmetrical(xStepMin, xStepMax, alpha, alphaMax, theta);
                } else if (symmetrical()) {
                    x = getStepXforSymmetrical(xStepMin, xStepMax, alpha, alphaMax);
                }
                x = adjustx(x, rho0square);
            }
            double dx = (x - tx.getX()) / tx.getX() * 100;
            ptca.beginStep()
                    .setAlpha(Math.toDegrees(alpha))
                    .setRho(rho)
                    .setR(0)
                    .setX(dx)
                    .setG(0)
                    .setB(0)
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
            String reason = String.format("Inconsistent xStepMin, xStepMax [%f, %f]",
                    xStepMin,
                    xStepMax);
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

    private void addRegulatingControl(PhaseTapChangerAdder ptca) {
        // TODO How to obtain terminal of corresponding winding ?
        // Only one transformer end is available for the tap changer
        String terminal1 = p.getId("Terminal1");
        String terminal2 = p.getId("Terminal2");

        String regulatingControl = p.getId("RegulatingControl");
        String regulatingControlMode = p.getLocal("regulatingControlMode");
        double regulatingControlTargetValue = p.asDouble("regulatingControlTargetValue");
        String regulatingControlTerminal = p.getId("RegulatingControlTerminal");
        if (regulatingControl != null) {
            if (regulatingControlMode.endsWith("currentFlow")) {
                Terminal treg;
                if (regulatingControlTerminal.equals(terminal1)) {
                    treg = tx.getTerminal1();
                } else if (regulatingControlTerminal.equals(terminal2)) {
                    treg = tx.getTerminal2();
                } else {
                    treg = context.terminalMapping().find(regulatingControlTerminal);
                }
                ptca.setRegulationMode(PhaseTapChanger.RegulationMode.CURRENT_LIMITER)
                        .setRegulationValue(regulatingControlTargetValue)
                        .setRegulating(true)
                        .setRegulationTerminal(treg);
            } else if (regulatingControlMode.endsWith("fixed")) {
                // Nothing to do
            } else {
                ignored(regulatingControlMode, "Unsupported regulating mode");
            }
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

    private boolean configIsInvertVoltageStepIncrementOutOfPhase;

    private static final Logger LOG = LoggerFactory.getLogger(PhaseTapChangerConversion.class);
}
