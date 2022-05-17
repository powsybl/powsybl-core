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
public class FortescueValues {

    public class ThreePhaseValues {
        // Values on the three phases.
        private final double magnitude1;
        private final double magnitude2;
        private final double magnitude3;
        private final double angle1;
        private final double angle2;
        private final double angle3;

        public ThreePhaseValues(double magnitude1, double magnitude2, double magnitude3, double angle1, double angle2, double angle3) {
            this.magnitude1 = magnitude1;
            this.magnitude2 = magnitude2;
            this.magnitude3 = magnitude3;
            this.angle1 = angle1;
            this.angle2 = angle2;
            this.angle3 = angle3;
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

        public double getAngle1() {
            return angle1;
        }

        public double getAngle2() {
            return angle2;
        }

        public double getAngle3() {
            return angle3;
        }
    }

    private final double directMagnitude;
    private final double zeroMagnitude;
    private final double inverseMagnitude;
    private final double directAngle;
    private final double zeroAngle;
    private final double inverseAngle;

    public FortescueValues(double directMagnitude, double zeroMagnitude, double inverseMagnitude, double directAngle, double zeroAngle, double inverseAngle) {
        this.directMagnitude = directMagnitude;
        this.directAngle = directAngle;
        this.zeroMagnitude = zeroMagnitude;
        this.inverseMagnitude = inverseMagnitude;
        this.zeroAngle = zeroAngle;
        this.inverseAngle = inverseAngle;
    }

    public FortescueValues(double directMagnitude, double directAngle) {
        this(directMagnitude, Double.NaN, Double.NaN, directAngle, Double.NaN, Double.NaN);
    }

    public FortescueValues(double directMagnitude) {
        this(directMagnitude, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN);
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

    public double getDirectAngle() {
        return directAngle;
    }

    public double getZeroAngle() {
        return zeroAngle;
    }

    public double getInverseAngle() {
        return inverseAngle;
    }

    ThreePhaseValues toThreePhaseValues() {
        // TODO.
        ThreePhaseValues threePhaseValues = new ThreePhaseValues(0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
        return threePhaseValues;
    }
}
