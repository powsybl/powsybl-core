/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.util;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;

import java.util.Objects;

import static com.powsybl.iidm.network.util.DanglingLineData.zeroImpedance;

/**
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
 */
public class DanglingLineBoundaryImpl implements Boundary {
    // Notes about SV utility class usage here:
    // - side represents the network side, which is always Side.ONE for a dangling line.
    // - DanglingLine model has shunt admittance on network side only, hence splitShuntAdmittance argument in SV methods must be set to false.

    private final DanglingLine parent;

    public DanglingLineBoundaryImpl(DanglingLine parent) {
        this.parent = Objects.requireNonNull(parent);
    }

    @Override
    public double getV() {
        if (useHypothesis(parent)) {
            DanglingLineData danglingLineData = new DanglingLineData(parent);
            return danglingLineData.getBoundaryBusU();
        }
        Terminal t = parent.getTerminal();
        Bus b = t.getBusView().getBus();
        if (zeroImpedance(parent)) {
            return getV(b);
        } else {
            return new SV(t.getP(), t.getQ(), getV(b), getAngle(b), TwoSides.ONE).otherSideU(parent, false);
        }
    }

    @Override
    public double getAngle() {
        if (useHypothesis(parent)) {
            DanglingLineData danglingLineData = new DanglingLineData(parent);
            return Math.toDegrees(danglingLineData.getBoundaryBusTheta());
        }
        Terminal t = parent.getTerminal();
        Bus b = t.getBusView().getBus();
        if (zeroImpedance(parent)) {
            return getAngle(b);
        } else {
            return new SV(t.getP(), t.getQ(), getV(b), getAngle(b), TwoSides.ONE).otherSideA(parent, false);
        }
    }

    @Override
    public double getP() {
        if (useHypothesis(parent)) {
            return -parent.getP0();
        }
        Terminal t = parent.getTerminal();
        Bus b = t.getBusView().getBus();
        if (zeroImpedance(parent)) {
            return -t.getP();
        } else {
            return new SV(t.getP(), t.getQ(), getV(b), getAngle(b), TwoSides.ONE).otherSideP(parent, false);
        }
    }

    @Override
    public double getQ() {
        if (useHypothesis(parent)) {
            return -parent.getQ0();
        }
        Terminal t = parent.getTerminal();
        Bus b = t.getBusView().getBus();
        if (zeroImpedance(parent)) {
            return -t.getQ();
        } else {
            return new SV(t.getP(), t.getQ(), getV(b), getAngle(b), TwoSides.ONE).otherSideQ(parent, false);
        }
    }

    @Override
    public double getI() {
        if (useHypothesis(parent)) {
            return Math.hypot(getP(), getQ()) / (Math.sqrt(3.) * getVWithFallbackOnNominalV() / 1000);
        }
        Terminal t = parent.getTerminal();
        Bus b = t.getBusView().getBus();
        if (zeroImpedance(parent)) {
            return t.getI();
        } else {
            return new SV(t.getP(), t.getQ(), getVWithFallbackOnNominalV(b), getAngle(b), TwoSides.ONE).otherSideI(parent, true);
        }
    }

    @Override
    public DanglingLine getDanglingLine() {
        return parent;
    }

    @Override
    public VoltageLevel getNetworkSideVoltageLevel() {
        return parent.getTerminal().getVoltageLevel();
    }

    private double getVWithFallbackOnNominalV() {
        double v = getV();
        return Double.isFinite(v) ? v : getNetworkSideVoltageLevel().getNominalV();
    }

    private static double getV(Bus b) {
        return b == null ? Double.NaN : b.getV();
    }

    private static double getVWithFallbackOnNominalV(Bus b) {
        double v = getV(b);
        if (Double.isFinite(v)) {
            return v;
        }
        if (b != null) {
            return b.getVoltageLevel().getNominalV();
        }
        throw new PowsyblException("Could not return V with fallback on nominal V because there is no bus");
    }

    private static double getAngle(Bus b) {
        return b == null ? Double.NaN : b.getAngle();
    }

    private static boolean valid(double p0, double q0) {
        return !Double.isNaN(p0) && !Double.isNaN(q0);
    }

    private static boolean useHypothesis(DanglingLine parent) {
        // We prefer to use P0 and Q0 if the dangling line is not paired and P0 and Q0 are valid, but we cannot retrieve
        // P, Q, angle and voltage at boundary if the dangling line has a generation part: a previous global load flow
        // run is needed, especially if the generation is regulating voltage.
        // This could be improved later.
        return !parent.isPaired() && valid(parent.getP0(), parent.getQ0()) && parent.getGeneration() == null;
    }
}
