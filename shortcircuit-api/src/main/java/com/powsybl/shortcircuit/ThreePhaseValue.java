/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.shortcircuit;

/**
 * The aim of this class is to store the magnitude and angle of current or voltage on the three phases, and the
 * the magnitude and angle of current or voltage for the Fortescue direct, zero and indirect components.
 *
 * @author Coline Piloquet <coline.piloquet at rte-france.com>
 */
public class ThreePhaseValue {
    // Results on three phases.
    private final double magnitude1;
    private final double magnitude2;
    private final double magnitude3;
    private final double phase1;
    private final double phase2;
    private final double phase3;
    // Fortescue results.
    private final double directMagnitude;
    private final double zeroMagnitude;
    private final double inverseMagnitude;
    private final double directPhase;
    private final double zeroPhase;
    private final double inversePhase;

    public ThreePhaseValue(double magnitude1, double magnitude2, double magnitude3, double phase1, double phase2, double phase3,
                           double directMagnitude, double zeroMagnitude, double inverseMagnitude, double directPhase, double zeroPhase, double inversePhase) {
        this.magnitude1 = magnitude1;
        this.magnitude2 = magnitude2;
        this.magnitude3 = magnitude3;
        this.phase1 = phase1;
        this.phase2 = phase2;
        this.phase3 = phase3;
        this.directMagnitude = directMagnitude;
        this.directPhase = directPhase;
        this.zeroMagnitude = zeroMagnitude;
        this.inverseMagnitude = inverseMagnitude;
        this.zeroPhase = zeroPhase;
        this.inversePhase = inversePhase;
    }

    public ThreePhaseValue(double magnitude1, double magnitude2, double magnitude3, double phase1, double phase2, double phase3,
                           double directMagnitude, double directPhase) {
        this(magnitude1, magnitude2, magnitude3, phase1, phase2, phase3, directMagnitude, directPhase, Double.NaN, Double.NaN, Double.NaN, Double.NaN);
    }

    public ThreePhaseValue(double directMagnitude, double directPhase) {
        this(Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, directMagnitude, directPhase);
    }

    public ThreePhaseValue(double directMagnitude) {
        this(Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, directMagnitude, Double.NaN);
    }

    public double getMagnitude1() {
        return magnitude1;
    }

    public double getMagnitude2() {
        return magnitude2;
    }

    public double getMagnitude3() {
        return magnitude3;
    }

    public double getPhase1() {
        return phase1;
    }

    public double getPhase2() {
        return phase2;
    }

    public double getPhase3() {
        return phase3;
    }

    public double getDirectMagnitude() {
        return directMagnitude;
    }

    public double getZeroMagnitude() {
        return zeroMagnitude;
    }

    public double getInverseMagnitude() {
        return inverseMagnitude;
    }

    public double getDirectPhase() {
        return directPhase;
    }

    public double getZeroPhase() {
        return zeroPhase;
    }

    public double getInversePhase() {
        return inversePhase;
    }
}
