/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.util;

import java.util.Objects;

import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.ThreeWindingsTransformer;
import com.powsybl.iidm.network.ThreeWindingsTransformer.Leg1;
import com.powsybl.iidm.network.ThreeWindingsTransformer.Leg2or3;
import com.powsybl.iidm.network.ThreeWindingsTransformer.LegBase;
import com.powsybl.iidm.network.ThreeWindingsTransformer.Side;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 */
public class TwtData {

    private final String id;

    private final double p1;
    private final double q1;
    private final double p2;
    private final double q2;
    private final double p3;
    private final double q3;

    private final double u1;
    private final double theta1;
    private final double u2;
    private final double theta2;
    private final double u3;
    private final double theta3;

    private final double g;
    private final double b;

    private final double r1;
    private final double x1;
    private final double ratedU1;
    private final double r2;
    private final double x2;
    private final double ratedU2;
    private final double r3;
    private final double x3;
    private final double ratedU3;

    private final boolean connected1;
    private final boolean connected2;
    private final boolean connected3;
    private final boolean mainComponent1;
    private final boolean mainComponent2;
    private final boolean mainComponent3;

    private final double computedP1;
    private final double computedQ1;
    private final double computedP2;
    private final double computedQ2;
    private final double computedP3;
    private final double computedQ3;

    public TwtData(ThreeWindingsTransformer twt, double epsilonX, boolean applyReactanceCorrection) {
        Objects.requireNonNull(twt);
        id = twt.getId();

        p1 = twt.getLeg1().getTerminal().getP();
        q1 = twt.getLeg1().getTerminal().getQ();
        p2 = twt.getLeg2().getTerminal().getP();
        q2 = twt.getLeg2().getTerminal().getQ();
        p3 = twt.getLeg3().getTerminal().getP();
        q3 = twt.getLeg3().getTerminal().getQ();

        u1 = getV(twt.getLeg1());
        theta1 = getTheta(twt.getLeg1());
        u2 = getV(twt.getLeg2());
        theta2 = getTheta(twt.getLeg2());
        u3 = getV(twt.getLeg3());
        theta3 = getTheta(twt.getLeg3());

        g = twt.getLeg1().getG();
        b = twt.getLeg1().getB();

        r1 = twt.getLeg1().getR();
        x1 = twt.getLeg1().getX();
        ratedU1 = twt.getLeg1().getRatedU();
        r2 = twt.getLeg2().getR();
        x2 = twt.getLeg2().getX();
        ratedU2 = twt.getLeg2().getRatedU();
        r3 = twt.getLeg3().getR();
        x3 = twt.getLeg3().getX();
        ratedU3 = twt.getLeg3().getRatedU();

        connected1 = twt.getLeg1().getTerminal().isConnected();
        connected2 = twt.getLeg2().getTerminal().isConnected();
        connected3 = twt.getLeg3().getTerminal().isConnected();
        mainComponent1 = isMainComponent(twt.getLeg1());
        mainComponent2 = isMainComponent(twt.getLeg2());
        mainComponent3 = isMainComponent(twt.getLeg3());

        StarBus starBus = new StarBus(twt);
        BranchData leg1BranchData = legBranchData(twt.getId(), twt.getLeg1(), starBus, epsilonX, applyReactanceCorrection);
        computedP1 = leg1BranchData.getComputedP1();
        computedQ1 = leg1BranchData.getComputedQ1();
        BranchData leg2BranchData = legBranchData(twt.getId(), Side.TWO, twt.getLeg2(), twt.getLeg1().getRatedU(), starBus,
                                                  epsilonX, applyReactanceCorrection);
        computedP2 = leg2BranchData.getComputedP1();
        computedQ2 = leg2BranchData.getComputedQ1();
        BranchData leg3BranchData = legBranchData(twt.getId(), Side.THREE, twt.getLeg3(), twt.getLeg1().getRatedU(), starBus,
                                                  epsilonX, applyReactanceCorrection);
        computedP3 = leg3BranchData.getComputedP1();
        computedQ3 = leg3BranchData.getComputedQ1();
    }

    private double getV(LegBase<?> leg) {
        return leg.getTerminal().isConnected() ? leg.getTerminal().getBusView().getBus().getV() : Double.NaN;
    }

    private double getTheta(LegBase<?> leg) {
        return leg.getTerminal().isConnected() ? Math.toRadians(leg.getTerminal().getBusView().getBus().getAngle()) : Double.NaN;
    }

    private boolean isMainComponent(LegBase<?> leg) {
        Bus bus = leg.getTerminal().getBusView().getBus();
        Bus connectableBus = leg.getTerminal().getBusView().getConnectableBus();
        boolean connectableMainComponent = connectableBus != null && connectableBus.isInMainConnectedComponent();
        return bus != null ? bus.isInMainConnectedComponent() : connectableMainComponent;
    }

    private BranchData legBranchData(String twtId, Leg1 leg, StarBus starBus, double epsilonX, boolean applyReactanceCorrection) {
        // In IIDM only the Leg1 has admittance to ground
        // And it is modeled at end corresponding to star bus
        return legBranchData(twtId, Side.ONE, leg, leg.getG(), leg.getB(), leg.getRatedU(), starBus, epsilonX, applyReactanceCorrection);
    }

    private BranchData legBranchData(String twtId, Side side, Leg2or3 leg, double ratedU0, StarBus starBus,
                                     double epsilonX, boolean applyReactanceCorrection) {
        // All (gk, bk) are zero in the IIDM model
        return legBranchData(twtId, side, leg, 0, 0, ratedU0, starBus, epsilonX, applyReactanceCorrection);
    }

    private BranchData legBranchData(String twtId, Side side, LegBase<?> leg, double g, double b, double ratedU0, StarBus starBus,
                                     double epsilonX, boolean applyReactanceCorrection) {
        String id = twtId + "_" + side;
        double r = leg.getR();
        double x = leg.getX();
        double uk = getV(leg);
        double u0 = starBus.getU();
        double thetak = getTheta(leg);
        double theta0 = starBus.getTheta();
        double gk = 0;
        double bk = 0;
        double g0 = g;
        double b0 = b;
        double rhok = ratedU0 / leg.getRatedU();
        double alphak = 0;
        double rho0 = 1;
        double alpha0 = 0;
        boolean buskMainComponent = true;
        boolean bus0MainComponent = true;
        boolean buskConnected = true;
        boolean bus0Connected = true;
        double flowPk = Double.NaN;
        double flowQk = Double.NaN;
        double flowP0 = Double.NaN;
        double flowQ0 = Double.NaN;
        return new BranchData(id, r, x, rhok, rho0, uk, u0, thetak, theta0, alphak, alpha0, gk, g0, bk, b0,
                              flowPk, flowQk, flowP0, flowQ0, buskConnected, bus0Connected, buskMainComponent, bus0MainComponent,
                              epsilonX, applyReactanceCorrection);
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
                throw new AssertionError("Unexpected side: " + side);
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
                throw new AssertionError("Unexpected side: " + side);
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
                throw new AssertionError("Unexpected side: " + side);
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
                throw new AssertionError("Unexpected side: " + side);
        }
    }

    public double getU(Side side) {
        Objects.requireNonNull(side);
        switch (side) {
            case ONE:
                return u1;
            case TWO:
                return u2;
            case THREE:
                return u3;
            default:
                throw new AssertionError("Unexpected side: " + side);
        }
    }

    public double getTheta(Side side) {
        Objects.requireNonNull(side);
        switch (side) {
            case ONE:
                return theta1;
            case TWO:
                return theta2;
            case THREE:
                return theta3;
            default:
                throw new AssertionError("Unexpected side: " + side);
        }
    }

    public double getG() {
        return g;
    }

    public double getB() {
        return b;
    }

    public double getR(Side side) {
        Objects.requireNonNull(side);
        switch (side) {
            case ONE:
                return r1;
            case TWO:
                return r2;
            case THREE:
                return r3;
            default:
                throw new AssertionError("Unexpected side: " + side);
        }
    }

    public double getX(Side side) {
        Objects.requireNonNull(side);
        switch (side) {
            case ONE:
                return x1;
            case TWO:
                return x2;
            case THREE:
                return x3;
            default:
                throw new AssertionError("Unexpected side: " + side);
        }
    }

    public double getRatedU(Side side) {
        Objects.requireNonNull(side);
        switch (side) {
            case ONE:
                return ratedU1;
            case TWO:
                return ratedU2;
            case THREE:
                return ratedU3;
            default:
                throw new AssertionError("Unexpected side: " + side);
        }
    }

    public boolean isConnected(Side side) {
        Objects.requireNonNull(side);
        switch (side) {
            case ONE:
                return connected1;
            case TWO:
                return connected2;
            case THREE:
                return connected3;
            default:
                throw new AssertionError("Unexpected side: " + side);
        }
    }

    public boolean isMainComponent(Side side) {
        Objects.requireNonNull(side);
        switch (side) {
            case ONE:
                return mainComponent1;
            case TWO:
                return mainComponent2;
            case THREE:
                return mainComponent3;
            default:
                throw new AssertionError("Unexpected side: " + side);
        }
    }

}
