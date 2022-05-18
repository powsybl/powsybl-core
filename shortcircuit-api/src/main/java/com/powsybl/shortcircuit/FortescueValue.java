/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.shortcircuit;

import com.powsybl.math.matrix.DenseMatrix;
import com.powsybl.math.matrix.DenseMatrixFactory;
import com.powsybl.math.matrix.Matrix;
import com.powsybl.math.matrix.MatrixFactory;
import org.apache.commons.math3.util.Pair;

/**
 * The aim of this class is to store the magnitude and angle of current or voltage on the three phases, and the
 * the magnitude and angle of current or voltage for the Fortescue direct, zero and indirect components.
 *
 * @author Coline Piloquet <coline.piloquet at rte-france.com>
 */
public class FortescueValue {

    public class ThreePhaseValue {
        // Value on the three phases.
        private final double magnitude1;
        private final double magnitude2;
        private final double magnitude3;
        private final double angle1;
        private final double angle2;
        private final double angle3;

        public ThreePhaseValue(double magnitude1, double magnitude2, double magnitude3, double angle1, double angle2, double angle3) {
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

    public FortescueValue(double directMagnitude, double zeroMagnitude, double inverseMagnitude, double directAngle, double zeroAngle, double inverseAngle) {
        this.directMagnitude = directMagnitude;
        this.directAngle = directAngle;
        this.zeroMagnitude = zeroMagnitude;
        this.inverseMagnitude = inverseMagnitude;
        this.zeroAngle = zeroAngle;
        this.inverseAngle = inverseAngle;
    }

    public FortescueValue(double directMagnitude, double directAngle) {
        this(directMagnitude, Double.NaN, Double.NaN, directAngle, Double.NaN, Double.NaN);
    }

    public FortescueValue(double directMagnitude) {
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

    public Pair<Double, Double> getCartesianFromPolar(double magnitude, double angle) {
        double xValue = magnitude * Math.cos(angle);
        double yValue = magnitude * Math.sin(angle); // TODO : check radians and degrees
        return new Pair<>(xValue, yValue);
    }

    public Pair<Double, Double> getPolarFromCartesian(double xValue, double yValue) {
        double magnitude = Math.sqrt(xValue * xValue + yValue * yValue);
        double phase = Math.atan2(yValue, xValue); // TODO : check radians and degrees
        return new Pair<>(magnitude, phase);
    }

    ThreePhaseValue toThreePhaseValue() {

        // [G1]   [ 1  1  1 ]   [Gh]
        // [G2] = [ 1  a²  a] * [Gd]
        // [G3]   [ 1  a  a²]   [Gi]
        MatrixFactory matrixFactory = new DenseMatrixFactory();

        Pair<Double, Double> directComponent = getCartesianFromPolar(directMagnitude, directAngle);
        Pair<Double, Double> homopolarComponent = getCartesianFromPolar(zeroMagnitude, zeroAngle);
        Pair<Double, Double> inversComponent = getCartesianFromPolar(inverseMagnitude, inverseAngle);

        Matrix mGfortescue = matrixFactory.create(6, 1, 6);
        mGfortescue.add(0, 0, homopolarComponent.getKey());
        mGfortescue.add(1, 0, homopolarComponent.getValue());
        mGfortescue.add(2, 0, directComponent.getKey());
        mGfortescue.add(3, 0, directComponent.getValue());
        mGfortescue.add(4, 0, inversComponent.getKey());
        mGfortescue.add(5, 0, inversComponent.getValue());

        DenseMatrix mGphase = getFortescueMatrix(matrixFactory).times(mGfortescue).toDense();

        Pair<Double, Double> phase1 = getPolarFromCartesian(mGphase.get(0, 0), mGphase.get(1, 0));
        Pair<Double, Double> phase2 = getPolarFromCartesian(mGphase.get(2, 0), mGphase.get(3, 0));
        Pair<Double, Double> phase3 = getPolarFromCartesian(mGphase.get(4, 0), mGphase.get(5, 0));

        ThreePhaseValue threePhaseValue = new ThreePhaseValue(phase1.getKey() / Math.sqrt(3), phase2.getKey() / Math.sqrt(3), phase3.getKey() / Math.sqrt(3), phase1.getValue(), phase2.getValue(), phase3.getValue());
        return threePhaseValue;
    }

    static Matrix getFortescueMatrix(MatrixFactory matrixFactory) {

        // [G1]   [ 1  1  1 ]   [Gh]
        // [G2] = [ 1  a²  a] * [Gd]
        // [G3]   [ 1  a  a²]   [Gi]
        Matrix mFortescue = matrixFactory.create(6, 6, 6);
        //column 1
        mFortescue.add(0, 0, 1.);
        mFortescue.add(1, 1, 1.);

        mFortescue.add(2, 0, 1.);
        mFortescue.add(3, 1, 1.);

        mFortescue.add(4, 0, 1.);
        mFortescue.add(5, 1, 1.);

        //column 2
        mFortescue.add(0, 2, 1.);
        mFortescue.add(1, 3, 1.);

        mFortescue.add(2, 2, -1. / 2.);
        mFortescue.add(2, 3, Math.sqrt(3.) / 2.);
        mFortescue.add(3, 2, -Math.sqrt(3.) / 2.);
        mFortescue.add(3, 3, -1. / 2.);

        mFortescue.add(4, 2, -1. / 2.);
        mFortescue.add(4, 3, -Math.sqrt(3.) / 2.);
        mFortescue.add(5, 2, Math.sqrt(3.) / 2.);
        mFortescue.add(5, 3, -1. / 2.);

        //column 3
        mFortescue.add(0, 4, 1.);
        mFortescue.add(1, 5, 1.);

        mFortescue.add(2, 4, -1. / 2.);
        mFortescue.add(2, 5, -Math.sqrt(3.) / 2.);
        mFortescue.add(3, 4, Math.sqrt(3.) / 2.);
        mFortescue.add(3, 5, -1. / 2.);

        mFortescue.add(4, 4, -1. / 2.);
        mFortescue.add(4, 5, Math.sqrt(3.) / 2.);
        mFortescue.add(5, 4, -Math.sqrt(3.) / 2.);
        mFortescue.add(5, 5, -1. / 2.);

        return mFortescue;
    }

}
