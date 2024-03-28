/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.extensions.util;

import com.powsybl.math.matrix.DenseMatrix;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.commons.math3.util.Pair;

/**
 * @author Jean-Baptiste Heyberger {@literal <jbheyberger at gmail.com>}
 */
public final class FortescueUtil {

    public enum SequenceType {
        POSITIVE,
        NEGATIVE,
        ZERO
    }

    public static DenseMatrix getFortescueMatrix() {

        // [GA]   [ 1  1  1 ]   [G0]
        // [GB] = [ 1  a²  a] * [G1]
        // [GC]   [ 1  a  a²]   [G2]
        return getFortescueOrInverseMatrix(false);
    }

    public static DenseMatrix getFortescueInverseMatrix() {

        // [G0]       [ 1  1  1 ]   [GA]
        // [G1] = 1/3 [ 1  a  a²] * [GB]
        // [G2]       [ 1  a² a ]   [GC]
        return getFortescueOrInverseMatrix(true);
    }

    public static DenseMatrix getFortescueOrInverseMatrix(boolean isInverse) {
        DenseMatrix mFortescueOrInverse = new DenseMatrix(6, 6);

        double t = 1;
        double signA = 1;
        if (isInverse) {
            t = 1. / 3.;
            signA = -1;
        }

        //column 1
        mFortescueOrInverse.add(0, 0, t);
        mFortescueOrInverse.add(1, 1, t);

        mFortescueOrInverse.add(2, 0, t);
        mFortescueOrInverse.add(3, 1, t);

        mFortescueOrInverse.add(4, 0, t);
        mFortescueOrInverse.add(5, 1, t);

        //column 2
        mFortescueOrInverse.add(0, 2, t);
        mFortescueOrInverse.add(1, 3, t);

        mFortescueOrInverse.add(2, 2, -t / 2.);
        mFortescueOrInverse.add(2, 3, signA * t * Math.sqrt(3.) / 2.);
        mFortescueOrInverse.add(3, 2, -signA * t * Math.sqrt(3.) / 2.);
        mFortescueOrInverse.add(3, 3, -t / 2.);

        mFortescueOrInverse.add(4, 2, -t / 2.);
        mFortescueOrInverse.add(4, 3, -signA * t * Math.sqrt(3.) / 2.);
        mFortescueOrInverse.add(5, 2, signA * t * Math.sqrt(3.) / 2.);
        mFortescueOrInverse.add(5, 3, -t / 2.);

        //column 3
        mFortescueOrInverse.add(0, 4, t);
        mFortescueOrInverse.add(1, 5, t);

        mFortescueOrInverse.add(2, 4, -t / 2.);
        mFortescueOrInverse.add(2, 5, -signA * t * Math.sqrt(3.) / 2.);
        mFortescueOrInverse.add(3, 4, signA * t * Math.sqrt(3.) / 2.);
        mFortescueOrInverse.add(3, 5, -t / 2.);

        mFortescueOrInverse.add(4, 4, -t / 2.);
        mFortescueOrInverse.add(4, 5, signA * t * Math.sqrt(3.) / 2.);
        mFortescueOrInverse.add(5, 4, -signA * t * Math.sqrt(3.) / 2.);
        mFortescueOrInverse.add(5, 5, -t / 2.);

        return mFortescueOrInverse;
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
