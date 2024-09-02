/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.shortcircuit;

import com.powsybl.iidm.network.extensions.util.FortescueUtil;
import com.powsybl.math.matrix.DenseMatrix;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.commons.math3.util.Pair;

/**
 * The aim of this class is to store the magnitude and angle of current or voltage on the three phases, and
 * the magnitude and angle of current or voltage for the Fortescue positive, zero and negative components.
 *
 * @author Coline Piloquet {@literal <coline.piloquet at rte-france.com>}
 * @author Jean-Baptiste Heyberger {@literal <jbheyberger at gmail.com>}
 */
public class FortescueValue {

    public class ThreePhaseValue {
        // Value on the three phases.
        private final double magnitudeA;
        private final double magnitudeB;
        private final double magnitudeC;
        private final double angleA;
        private final double angleB;
        private final double angleC;

        public ThreePhaseValue(double magnitudeA, double magnitudeB, double magnitudeC, double angleA, double angleB, double angleC) {
            this.magnitudeA = magnitudeA;
            this.magnitudeB = magnitudeB;
            this.magnitudeC = magnitudeC;
            this.angleA = angleA;
            this.angleB = angleB;
            this.angleC = angleC;
        }

        /**
         * The magnitude on phase A
         */
        public double getMagnitudeA() {
            return magnitudeA;
        }

        /**
         * The magnitude on phase B
         */
        public double getMagnitudeB() {
            return magnitudeB;
        }

        /**
         * The magnitude on phase C
         */
        public double getMagnitudeC() {
            return magnitudeC;
        }

        /**
         * The angle on phase A
         */
        public double getAngleA() {
            return angleA;
        }

        /**
         * The angle on phase B
         */
        public double getAngleB() {
            return angleB;
        }

        /**
         * The angle on phase C
         */
        public double getAngleC() {
            return angleC;
        }

    }

    private final double positiveMagnitude;
    private final double zeroMagnitude;
    private final double negativeMagnitude;
    private final double positiveAngle;
    private final double zeroAngle;
    private final double negativeAngle;

    public FortescueValue(double positiveMagnitude, double zeroMagnitude, double negativeMagnitude, double positiveAngle, double zeroAngle, double negativeAngle) {
        this.positiveMagnitude = positiveMagnitude;
        this.positiveAngle = positiveAngle;
        this.zeroMagnitude = zeroMagnitude;
        this.negativeMagnitude = negativeMagnitude;
        this.zeroAngle = zeroAngle;
        this.negativeAngle = negativeAngle;
    }

    public FortescueValue(double positiveMagnitude, double positiveAngle) {
        this(positiveMagnitude, Double.NaN, Double.NaN, positiveAngle, Double.NaN, Double.NaN);
    }

    public FortescueValue(double positiveMagnitude) {
        this(positiveMagnitude, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN);
    }

    /**
     * The magnitude on the positive sequence
     */
    public double getPositiveMagnitude() {
        return positiveMagnitude;
    }

    /**
     * The magnitude on the zero sequence
     */
    public double getZeroMagnitude() {
        return zeroMagnitude;
    }

    /**
     * The magnitude on the negative sequence
     */
    public double getNegativeMagnitude() {
        return negativeMagnitude;
    }

    /**
     * The angle on the positive sequence
     */
    public double getPositiveAngle() {
        return positiveAngle;
    }

    /**
     * The angle on the zero sequence
     */
    public double getZeroAngle() {
        return zeroAngle;
    }

    /**
     * The angle on the negative sequence
     */
    public double getNegativeAngle() {
        return negativeAngle;
    }

    /**
     * Convert the value from the positive, zero and negative sequence to the A, B and C phase components.
     * @return the three phase components.
     */
    public ThreePhaseValue toThreePhaseValue() {

        // [G1]   [ 1  1  1 ]   [Gh]
        // [G2] = [ 1  a²  a] * [Gd]
        // [G3]   [ 1  a  a²]   [Gi]

        Vector2D positiveSequence = FortescueUtil.getCartesianFromPolar(positiveMagnitude, positiveAngle);
        Vector2D zeroSequence = FortescueUtil.getCartesianFromPolar(zeroMagnitude, zeroAngle);
        Vector2D negativeSequence = FortescueUtil.getCartesianFromPolar(negativeMagnitude, negativeAngle);

        DenseMatrix mGfortescue = new DenseMatrix(6, 1);
        mGfortescue.add(0, 0, zeroSequence.getX());
        mGfortescue.add(1, 0, zeroSequence.getY());
        mGfortescue.add(2, 0, positiveSequence.getX());
        mGfortescue.add(3, 0, positiveSequence.getY());
        mGfortescue.add(4, 0, negativeSequence.getX());
        mGfortescue.add(5, 0, negativeSequence.getY());

        DenseMatrix mGphase = FortescueUtil.getFortescueMatrix().times(mGfortescue).toDense();

        Pair<Double, Double> phaseA = FortescueUtil.getPolarFromCartesian(mGphase.get(0, 0), mGphase.get(1, 0));
        Pair<Double, Double> phaseB = FortescueUtil.getPolarFromCartesian(mGphase.get(2, 0), mGphase.get(3, 0));
        Pair<Double, Double> phaseC = FortescueUtil.getPolarFromCartesian(mGphase.get(4, 0), mGphase.get(5, 0));

        return new ThreePhaseValue(phaseA.getKey() / Math.sqrt(3), phaseB.getKey() / Math.sqrt(3), phaseC.getKey() / Math.sqrt(3), phaseA.getValue(), phaseB.getValue(), phaseC.getValue());
    }

}
