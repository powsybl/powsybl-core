/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.extensions.util;

import com.powsybl.math.matrix.ComplexMatrix;
import com.powsybl.math.matrix.DenseMatrix;
import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.commons.math3.util.FastMath;
import org.apache.commons.math3.util.Pair;

/**
 * @author Jean-Baptiste Heyberger {@literal <jbheyberger at gmail.com>}
 */
public final class FortescueUtil {

    public enum SequenceType {
        POSITIVE(1),
        NEGATIVE(2),
        ZERO(0);

        private final int num;

        SequenceType(int num) {
            this.num = num;
        }

        public int getNum() {
            return num;
        }
    }

    public static DenseMatrix getFortescueMatrix() {

        // [GA]   [ 1  1  1 ]   [G0]
        // [GB] = [ 1  a²  a] * [G1]
        // [GC]   [ 1  a  a²]   [G2]
        return createComplexMatrix(false).toRealCartesianMatrix();
    }

    public static DenseMatrix getFortescueInverseMatrix() {

        // [G0]       [ 1  1  1 ]   [GA]
        // [G1] = 1/3 [ 1  a  a²] * [GB]
        // [G2]       [ 1  a² a ]   [GC]
        return createComplexMatrix(true).toRealCartesianMatrix();
    }

    public static ComplexMatrix createComplexMatrix(boolean isInverse) {
        // [G1]   [ 1  1  1 ]   [Gh]
        // [G2] = [ 1  a²  a] * [Gd]
        // [G3]   [ 1  a  a²]   [Gi]

        Complex a = new Complex(-0.5, FastMath.sqrt(3.) / 2);
        Complex a2 = a.multiply(a);

        double t = 1.;
        Complex c1 = a;
        Complex c2 = a2;
        if (isInverse) {
            t = 1. / 3.;
            c1 = a2.multiply(t);
            c2 = a.multiply(t);
        }
        Complex unit = new Complex(t, 0);

        ComplexMatrix complexMatrix = new ComplexMatrix(3, 3);
        complexMatrix.set(0, 0, unit);
        complexMatrix.set(0, 1, unit);
        complexMatrix.set(0, 2, unit);

        complexMatrix.set(1, 0, unit);
        complexMatrix.set(1, 1, c2);
        complexMatrix.set(1, 2, c1);

        complexMatrix.set(2, 0, unit);
        complexMatrix.set(2, 1, c1);
        complexMatrix.set(2, 2, c2);

        return complexMatrix;
    }

    public static Vector2D getCartesianFromPolar(double magnitude, double angle) {
        return new Vector2D(magnitude * Math.cos(angle), magnitude * Math.sin(angle));
    }

    public static Pair<Double, Double> getPolarFromCartesian(double xValue, double yValue) {
        return new Pair<>(Math.sqrt(xValue * xValue + yValue * yValue), Math.atan2(yValue, xValue));
    }

    private FortescueUtil() {
    }
}
