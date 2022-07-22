/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.util;

import java.util.Objects;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.complex.ComplexUtils;

import com.powsybl.iidm.network.Branch;
import com.powsybl.iidm.network.ThreeWindingsTransformer;
import com.powsybl.iidm.network.ThreeWindingsTransformer.Side;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
public class TwtData {

    private static final String UNEXPECTED_SIDE = "Unexpected side";

    private final String id;

    private final double p1;
    private final double q1;
    private final double p2;
    private final double q2;
    private final double p3;
    private final double q3;

    private final LegData leg1Data;
    private final LegData leg2Data;
    private final LegData leg3Data;

    private double computedP1;
    private double computedQ1;
    private double computedP2;
    private double computedQ2;
    private double computedP3;
    private double computedQ3;

    private double starU;
    private double starTheta;

    private final int phaseAngleClock2;
    private final int phaseAngleClock3;
    private final double ratedU0;

    public TwtData(ThreeWindingsTransformer twt, double epsilonX, boolean applyReactanceCorrection) {
        this(twt, 0, 0, epsilonX, applyReactanceCorrection, false);
    }

    public TwtData(ThreeWindingsTransformer twt, double epsilonX, boolean applyReactanceCorrection,
        boolean twtSplitShuntAdmittance) {
        this(twt, 0, 0, epsilonX, applyReactanceCorrection, twtSplitShuntAdmittance);
    }

    public TwtData(ThreeWindingsTransformer twt, int phaseAngleClock2, int phaseAngleClock3, double epsilonX,
        boolean applyReactanceCorrection, boolean twtSplitShuntAdmittance) {
        Objects.requireNonNull(twt);
        id = twt.getId();

        p1 = twt.getLeg1().getTerminal().getP();
        q1 = twt.getLeg1().getTerminal().getQ();
        p2 = twt.getLeg2().getTerminal().getP();
        q2 = twt.getLeg2().getTerminal().getQ();
        p3 = twt.getLeg3().getTerminal().getP();
        q3 = twt.getLeg3().getTerminal().getQ();

        this.ratedU0 = twt.getRatedU0();
        this.phaseAngleClock2 = phaseAngleClock2;
        this.phaseAngleClock3 = phaseAngleClock3;

        leg1Data = new LegData(twt.getLeg1(), ratedU0, 0, epsilonX, applyReactanceCorrection, twtSplitShuntAdmittance);
        leg2Data = new LegData(twt.getLeg2(), ratedU0, phaseAngleClock2, epsilonX, applyReactanceCorrection, twtSplitShuntAdmittance);
        leg3Data = new LegData(twt.getLeg3(), ratedU0, phaseAngleClock3, epsilonX, applyReactanceCorrection, twtSplitShuntAdmittance);

        if ((leg1Data.getR() == 0.0 && leg1Data.getX() == 0.0)
            || leg2Data.getR() == 0.0 && leg2Data.getX() == 0.0
            || leg3Data.getR() == 0.0 && leg3Data.getX() == 0.0) {
            throw new AssertionError("ThreeWindingsTransformer with some zero impedance leg: " + id);
        }

        // Assume the ratedU at the star bus is equal to ratedU of Leg1

        if (leg1Data.isConnected() && leg2Data.isConnected() && leg3Data.isConnected()
            && valid(leg1Data.getU(), leg1Data.getTheta()) && valid(leg2Data.getU(), leg2Data.getTheta())
            && valid(leg3Data.getU(), leg3Data.getTheta())) {

            calculateThreeConnectedLegsFlowAndStarBusVoltage(leg1Data.getU(), leg1Data.getTheta(), leg2Data.getU(),
                leg2Data.getTheta(), leg3Data.getU(), leg3Data.getTheta(), leg1Data.getBranchAdmittanceMatrix(),
                leg2Data.getBranchAdmittanceMatrix(), leg3Data.getBranchAdmittanceMatrix());
        } else if (leg1Data.isConnected() && leg2Data.isConnected() && valid(leg1Data.getU(), leg1Data.getTheta())
            && valid(leg2Data.getU(), leg2Data.getTheta())) {

            LinkData.Flow flow = calculateTwoConnectedLegsFlow(leg1Data.getU(), leg1Data.getTheta(), leg2Data.getU(),
                leg2Data.getTheta(), leg1Data.getBranchAdmittanceMatrix(), leg2Data.getBranchAdmittanceMatrix(),
                leg3Data.getBranchAdmittanceMatrix());
            computedP1 = flow.fromTo.getReal();
            computedQ1 = flow.fromTo.getImaginary();
            computedP2 = flow.toFrom.getReal();
            computedQ2 = flow.toFrom.getImaginary();
            computedP3 = 0.0;
            computedQ3 = 0.0;

            Complex v0 = calculateTwoConnectedLegsStarBusVoltage(leg1Data.getU(), leg1Data.getTheta(), leg2Data.getU(),
                leg2Data.getTheta(), leg1Data.getBranchAdmittanceMatrix(), leg2Data.getBranchAdmittanceMatrix(),
                leg3Data.getBranchAdmittanceMatrix());
            starU = v0.abs();
            starTheta = v0.getArgument();
        } else if (leg1Data.isConnected() && leg3Data.isConnected() && valid(leg1Data.getU(), leg1Data.getTheta())
            && valid(leg3Data.getU(), leg3Data.getTheta())) {

            LinkData.Flow flow = calculateTwoConnectedLegsFlow(leg1Data.getU(), leg1Data.getTheta(), leg3Data.getU(),
                leg3Data.getTheta(), leg1Data.getBranchAdmittanceMatrix(), leg3Data.getBranchAdmittanceMatrix(),
                leg2Data.getBranchAdmittanceMatrix());
            computedP1 = flow.fromTo.getReal();
            computedQ1 = flow.fromTo.getImaginary();
            computedP2 = 0.0;
            computedQ2 = 0.0;
            computedP3 = flow.toFrom.getReal();
            computedQ3 = flow.toFrom.getImaginary();

            Complex v0 = calculateTwoConnectedLegsStarBusVoltage(leg1Data.getU(), leg1Data.getTheta(), leg3Data.getU(),
                leg3Data.getTheta(), leg1Data.getBranchAdmittanceMatrix(), leg3Data.getBranchAdmittanceMatrix(),
                leg2Data.getBranchAdmittanceMatrix());

            starU = v0.abs();
            starTheta = v0.getArgument();
        } else if (leg2Data.isConnected() && leg3Data.isConnected() && valid(leg2Data.getU(), leg2Data.getTheta())
            && valid(leg3Data.getU(), leg3Data.getTheta())) {

            LinkData.Flow flow = calculateTwoConnectedLegsFlow(leg2Data.getU(), leg2Data.getTheta(), leg3Data.getU(),
                leg3Data.getTheta(), leg2Data.getBranchAdmittanceMatrix(), leg3Data.getBranchAdmittanceMatrix(),
                leg1Data.getBranchAdmittanceMatrix());
            computedP1 = 0.0;
            computedQ1 = 0.0;
            computedP2 = flow.fromTo.getReal();
            computedQ2 = flow.fromTo.getImaginary();
            computedP3 = flow.toFrom.getReal();
            computedQ3 = flow.toFrom.getImaginary();

            Complex v0 = calculateTwoConnectedLegsStarBusVoltage(leg2Data.getU(), leg2Data.getTheta(), leg3Data.getU(),
                leg3Data.getTheta(), leg2Data.getBranchAdmittanceMatrix(), leg3Data.getBranchAdmittanceMatrix(),
                leg1Data.getBranchAdmittanceMatrix());
            starU = v0.abs();
            starTheta = v0.getArgument();
        } else if (leg1Data.isConnected() && valid(leg1Data.getU(), leg1Data.getTheta())) {

            Complex flow = calculateOneConnectedLegFlow(leg1Data.getU(), leg1Data.getTheta(),
                leg1Data.getBranchAdmittanceMatrix(), leg2Data.getBranchAdmittanceMatrix(),
                leg3Data.getBranchAdmittanceMatrix());
            computedP1 = flow.getReal();
            computedQ1 = flow.getImaginary();
            computedP2 = 0.0;
            computedQ2 = 0.0;
            computedP3 = 0.0;
            computedQ3 = 0.0;

            Complex v0 = calculateOneConnectedLegStarBusVoltage(leg1Data.getU(), leg1Data.getTheta(),
                leg1Data.getBranchAdmittanceMatrix(), leg2Data.getBranchAdmittanceMatrix(),
                leg3Data.getBranchAdmittanceMatrix());
            starU = v0.abs();
            starTheta = v0.getArgument();
        } else if (leg2Data.isConnected() && valid(leg2Data.getU(), leg2Data.getTheta())) {

            Complex flow = calculateOneConnectedLegFlow(leg2Data.getU(), leg2Data.getTheta(),
                leg2Data.getBranchAdmittanceMatrix(), leg1Data.getBranchAdmittanceMatrix(),
                leg3Data.getBranchAdmittanceMatrix());

            computedP1 = 0.0;
            computedQ1 = 0.0;
            computedP2 = flow.getReal();
            computedQ2 = flow.getImaginary();
            computedP3 = 0.0;
            computedQ3 = 0.0;

            Complex v0 = calculateOneConnectedLegStarBusVoltage(leg2Data.getU(), leg2Data.getTheta(),
                leg2Data.getBranchAdmittanceMatrix(), leg1Data.getBranchAdmittanceMatrix(),
                leg3Data.getBranchAdmittanceMatrix());
            starU = v0.abs();
            starTheta = v0.getArgument();
        } else if (leg3Data.isConnected() && valid(leg3Data.getU(), leg3Data.getTheta())) {

            Complex flow = calculateOneConnectedLegFlow(leg3Data.getU(), leg3Data.getTheta(),
                leg3Data.getBranchAdmittanceMatrix(), leg1Data.getBranchAdmittanceMatrix(),
                leg2Data.getBranchAdmittanceMatrix());

            computedP1 = 0.0;
            computedQ1 = 0.0;
            computedP2 = 0.0;
            computedQ2 = 0.0;
            computedP3 = flow.getReal();
            computedQ3 = flow.getImaginary();

            Complex v0 = calculateOneConnectedLegStarBusVoltage(leg3Data.getU(), leg3Data.getTheta(),
                leg3Data.getBranchAdmittanceMatrix(), leg1Data.getBranchAdmittanceMatrix(),
                leg2Data.getBranchAdmittanceMatrix());
            starU = v0.abs();
            starTheta = v0.getArgument();
        } else {

            computedP1 = Double.NaN;
            computedQ1 = Double.NaN;
            computedP2 = Double.NaN;
            computedQ2 = Double.NaN;
            computedP3 = Double.NaN;
            computedQ3 = Double.NaN;

            starU = Double.NaN;
            starTheta = Double.NaN;
        }
    }

    private void calculateThreeConnectedLegsFlowAndStarBusVoltage(double u1, double theta1, double u2, double theta2,
        double u3, double theta3, LinkData.BranchAdmittanceMatrix branchAdmittanceLeg1,
        LinkData.BranchAdmittanceMatrix branchAdmittanceLeg2, LinkData.BranchAdmittanceMatrix branchAdmittanceLeg3) {

        Complex v1 = ComplexUtils.polar2Complex(u1, theta1);
        Complex v2 = ComplexUtils.polar2Complex(u2, theta2);
        Complex v3 = ComplexUtils.polar2Complex(u3, theta3);

        Complex v0 = branchAdmittanceLeg1.y21().multiply(v1).add(branchAdmittanceLeg2.y21().multiply(v2))
            .add(branchAdmittanceLeg3.y21().multiply(v3)).negate()
            .divide(branchAdmittanceLeg1.y22().add(branchAdmittanceLeg2.y22()).add(branchAdmittanceLeg3.y22()));

        LinkData.Flow flowLeg1 = LinkData.flowBothEnds(branchAdmittanceLeg1.y11(), branchAdmittanceLeg1.y12(),
            branchAdmittanceLeg1.y21(), branchAdmittanceLeg1.y22(), v1, v0);

        LinkData.Flow flowLeg2 = LinkData.flowBothEnds(branchAdmittanceLeg2.y11(), branchAdmittanceLeg2.y12(),
            branchAdmittanceLeg2.y21(), branchAdmittanceLeg2.y22(), v2, v0);

        LinkData.Flow flowLeg3 = LinkData.flowBothEnds(branchAdmittanceLeg3.y11(), branchAdmittanceLeg3.y12(),
            branchAdmittanceLeg3.y21(), branchAdmittanceLeg3.y22(), v3, v0);

        computedP1 = flowLeg1.fromTo.getReal();
        computedQ1 = flowLeg1.fromTo.getImaginary();
        computedP2 = flowLeg2.fromTo.getReal();
        computedQ2 = flowLeg2.fromTo.getImaginary();
        computedP3 = flowLeg3.fromTo.getReal();
        computedQ3 = flowLeg3.fromTo.getImaginary();

        starU = v0.abs();
        starTheta = v0.getArgument();
    }

    private LinkData.Flow calculateTwoConnectedLegsFlow(double u1, double theta1, double u2, double theta2,
        LinkData.BranchAdmittanceMatrix admittanceMatrixLeg1, LinkData.BranchAdmittanceMatrix admittanceMatrixLeg2,
        LinkData.BranchAdmittanceMatrix admittanceMatrixOpenLeg) {

        Complex v1 = ComplexUtils.polar2Complex(u1, theta1);
        Complex v2 = ComplexUtils.polar2Complex(u2, theta2);

        LinkData.BranchAdmittanceMatrix admittance = calculateTwoConnectedLegsAdmittance(admittanceMatrixLeg1,
            admittanceMatrixLeg2, admittanceMatrixOpenLeg);

        return LinkData.flowBothEnds(admittance.y11(), admittance.y12(), admittance.y21(), admittance.y22(), v1, v2);
    }

    private Complex calculateTwoConnectedLegsStarBusVoltage(double u1, double theta1, double u2, double theta2,
        LinkData.BranchAdmittanceMatrix admittanceMatrixLeg1, LinkData.BranchAdmittanceMatrix admittanceMatrixLeg2,
        LinkData.BranchAdmittanceMatrix admittanceMatrixOpenLeg) {

        Complex v1 = ComplexUtils.polar2Complex(u1, theta1);
        Complex v2 = ComplexUtils.polar2Complex(u2, theta2);

        Complex yshO = LinkData.kronAntenna(admittanceMatrixOpenLeg.y11(), admittanceMatrixOpenLeg.y12(), admittanceMatrixOpenLeg.y21(), admittanceMatrixOpenLeg.y22(), true);
        return (admittanceMatrixLeg1.y21().multiply(v1).add(admittanceMatrixLeg2.y21().multiply(v2))).negate()
                .divide(admittanceMatrixLeg1.y22().add(admittanceMatrixLeg2.y22()).add(yshO));
    }

    private Complex calculateOneConnectedLegFlow(double u, double theta, LinkData.BranchAdmittanceMatrix admittanceMatrixLeg,
        LinkData.BranchAdmittanceMatrix admittanceMatrixFirstOpenLeg,
        LinkData.BranchAdmittanceMatrix admittanceMatrixSecondOpenLeg) {

        Complex ysh = calculateOneConnectedLegShunt(admittanceMatrixLeg,
            admittanceMatrixFirstOpenLeg, admittanceMatrixSecondOpenLeg);

        return LinkData.flowYshunt(ysh, u, theta);
    }

    private Complex calculateOneConnectedLegStarBusVoltage(double u, double theta,
        LinkData.BranchAdmittanceMatrix admittanceMatrixLeg, LinkData.BranchAdmittanceMatrix admittanceMatrixFirstOpenLeg,
        LinkData.BranchAdmittanceMatrix admittanceMatrixSecondOpenLeg) {

        Complex v = ComplexUtils.polar2Complex(u, theta);

        Complex ysh1O = LinkData.kronAntenna(admittanceMatrixFirstOpenLeg.y11(), admittanceMatrixFirstOpenLeg.y12(),
            admittanceMatrixFirstOpenLeg.y21(), admittanceMatrixFirstOpenLeg.y22(), true);
        Complex ysh2O = LinkData.kronAntenna(admittanceMatrixSecondOpenLeg.y11(), admittanceMatrixSecondOpenLeg.y12(),
            admittanceMatrixSecondOpenLeg.y21(), admittanceMatrixSecondOpenLeg.y22(), true);

        return admittanceMatrixLeg.y21().multiply(v).negate().divide(admittanceMatrixLeg.y22().add(ysh1O).add(ysh2O));
    }

    private LinkData.BranchAdmittanceMatrix calculateTwoConnectedLegsAdmittance(
        LinkData.BranchAdmittanceMatrix firstCloseLeg,
        LinkData.BranchAdmittanceMatrix secondCloseLeg, LinkData.BranchAdmittanceMatrix openLeg) {

        Complex ysh = LinkData.kronAntenna(openLeg.y11(), openLeg.y12(), openLeg.y21(), openLeg.y22(), true);
        LinkData.BranchAdmittanceMatrix secondCloseLegMod = new LinkData.BranchAdmittanceMatrix(secondCloseLeg.y11(),
            secondCloseLeg.y12(), secondCloseLeg.y21(), secondCloseLeg.y22().add(ysh));
        return LinkData.kronChain(firstCloseLeg, Branch.Side.TWO, secondCloseLegMod, Branch.Side.TWO);
    }

    private Complex calculateOneConnectedLegShunt(LinkData.BranchAdmittanceMatrix closeLeg,
        LinkData.BranchAdmittanceMatrix firstOpenLeg, LinkData.BranchAdmittanceMatrix secondOpenLeg) {
        Complex ysh1 = LinkData.kronAntenna(firstOpenLeg.y11(), firstOpenLeg.y12(), firstOpenLeg.y21(), firstOpenLeg.y22(),
            true);
        Complex ysh2 = LinkData.kronAntenna(secondOpenLeg.y11(), secondOpenLeg.y12(), secondOpenLeg.y21(),
            secondOpenLeg.y22(), true);
        Complex y22 = closeLeg.y22().add(ysh1).add(ysh2);

        return LinkData.kronAntenna(closeLeg.y11(), closeLeg.y12(), closeLeg.y21(), y22, false);
    }

    private static boolean valid(double voltage, double theta) {
        if (Double.isNaN(voltage) || voltage <= 0.0) {
            return false;
        }
        return !Double.isNaN(theta);
    }

    public String getId() {
        return id;
    }

    public double getComputedP(Side side) {
        Objects.requireNonNull(side);
        switch (side) {
            case ONE:
                return computedP1;
            case TWO:
                return computedP2;
            case THREE:
                return computedP3;
            default:
                throw new AssertionError(UNEXPECTED_SIDE + ": " + side);
        }
    }

    public double getComputedQ(Side side) {
        Objects.requireNonNull(side);
        switch (side) {
            case ONE:
                return computedQ1;
            case TWO:
                return computedQ2;
            case THREE:
                return computedQ3;
            default:
                throw new AssertionError(UNEXPECTED_SIDE + ": " + side);
        }
    }

    public double getP(Side side) {
        Objects.requireNonNull(side);
        switch (side) {
            case ONE:
                return p1;
            case TWO:
                return p2;
            case THREE:
                return p3;
            default:
                throw new AssertionError(UNEXPECTED_SIDE + ": " + side);
        }
    }

    public double getQ(Side side) {
        Objects.requireNonNull(side);
        switch (side) {
            case ONE:
                return q1;
            case TWO:
                return q2;
            case THREE:
                return q3;
            default:
                throw new AssertionError(UNEXPECTED_SIDE + ": " + side);
        }
    }

    public double getU(Side side) {
        Objects.requireNonNull(side);
        switch (side) {
            case ONE:
                return leg1Data.getU();
            case TWO:
                return leg2Data.getU();
            case THREE:
                return leg3Data.getU();
            default:
                throw new AssertionError(UNEXPECTED_SIDE + ": " + side);
        }
    }

    public double getStarU() {
        return starU;
    }

    public double getTheta(Side side) {
        Objects.requireNonNull(side);
        switch (side) {
            case ONE:
                return leg1Data.getTheta();
            case TWO:
                return leg2Data.getTheta();
            case THREE:
                return leg3Data.getTheta();
            default:
                throw new AssertionError(UNEXPECTED_SIDE + ": " + side);
        }
    }

    public double getStarTheta() {
        return starTheta;
    }

    public double getR(Side side) {
        Objects.requireNonNull(side);
        switch (side) {
            case ONE:
                return leg1Data.getR();
            case TWO:
                return leg2Data.getR();
            case THREE:
                return leg3Data.getR();
            default:
                throw new AssertionError(UNEXPECTED_SIDE + ": " + side);
        }
    }

    public double getX(Side side) {
        Objects.requireNonNull(side);
        switch (side) {
            case ONE:
                return leg1Data.getX();
            case TWO:
                return leg2Data.getX();
            case THREE:
                return leg3Data.getX();
            default:
                throw new AssertionError(UNEXPECTED_SIDE + ": " + side);
        }
    }

    public double getG1(Side side) {
        Objects.requireNonNull(side);
        switch (side) {
            case ONE:
                return leg1Data.getG1();
            case TWO:
                return leg2Data.getG1();
            case THREE:
                return leg3Data.getG1();
            default:
                throw new AssertionError(UNEXPECTED_SIDE + ": " + side);
        }
    }

    public double getB1(Side side) {
        Objects.requireNonNull(side);
        switch (side) {
            case ONE:
                return leg1Data.getB1();
            case TWO:
                return leg2Data.getB1();
            case THREE:
                return leg3Data.getB1();
            default:
                throw new AssertionError(UNEXPECTED_SIDE + ": " + side);
        }
    }

    public double getG2(Side side) {
        Objects.requireNonNull(side);
        switch (side) {
            case ONE:
                return leg1Data.getG2();
            case TWO:
                return leg2Data.getG2();
            case THREE:
                return leg3Data.getG2();
            default:
                throw new AssertionError(UNEXPECTED_SIDE + ": " + side);
        }
    }

    public double getB2(Side side) {
        Objects.requireNonNull(side);
        switch (side) {
            case ONE:
                return leg1Data.getB2();
            case TWO:
                return leg2Data.getB2();
            case THREE:
                return leg3Data.getB2();
            default:
                throw new AssertionError(UNEXPECTED_SIDE + ": " + side);
        }
    }

    public double getRatedU(Side side) {
        Objects.requireNonNull(side);
        switch (side) {
            case ONE:
                return leg1Data.getRatedU();
            case TWO:
                return leg2Data.getRatedU();
            case THREE:
                return leg3Data.getRatedU();
            default:
                throw new AssertionError(UNEXPECTED_SIDE + ": " + side);
        }
    }

    public boolean isConnected(Side side) {
        Objects.requireNonNull(side);
        switch (side) {
            case ONE:
                return leg1Data.isConnected();
            case TWO:
                return leg2Data.isConnected();
            case THREE:
                return leg3Data.isConnected();
            default:
                throw new AssertionError(UNEXPECTED_SIDE + ": " + side);
        }
    }

    public boolean isMainComponent(Side side) {
        Objects.requireNonNull(side);
        switch (side) {
            case ONE:
                return leg1Data.isMainComponent();
            case TWO:
                return leg2Data.isMainComponent();
            case THREE:
                return leg3Data.isMainComponent();
            default:
                throw new AssertionError(UNEXPECTED_SIDE + ": " + side);
        }
    }

    public int getPhaseAngleClock2() {
        return phaseAngleClock2;
    }

    public int getPhaseAngleClock3() {
        return phaseAngleClock3;
    }

    public double getRatedU0() {
        return ratedU0;
    }
}
