/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.extensions;

import com.powsybl.iidm.network.extensions.util.FortescueUtil;
import com.powsybl.math.matrix.DenseMatrix;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.commons.math3.util.Pair;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Jean-Baptiste Heyberger {@literal <jbheyberger at gmail.com>}
 */
public class FortescueTest {

    @Test
    void testFortescueTransformation() {

        double pi = Math.PI;
        DenseMatrix mGfortescue = new DenseMatrix(6, 1);

        Vector2D positiveSequence = FortescueUtil.getCartesianFromPolar(86.8086319, 1.83823431 * pi / 180);
        Vector2D zeroSequence = FortescueUtil.getCartesianFromPolar(0., 0.);
        Vector2D negativeSequence = FortescueUtil.getCartesianFromPolar(0., 0.);
        mGfortescue.add(0, 0, zeroSequence.getX());
        mGfortescue.add(1, 0, zeroSequence.getY());
        mGfortescue.add(2, 0, positiveSequence.getX());
        mGfortescue.add(3, 0, positiveSequence.getY());
        mGfortescue.add(4, 0, negativeSequence.getX());
        mGfortescue.add(5, 0, negativeSequence.getY());

        DenseMatrix mGphase = FortescueUtil.getFortescueMatrix().times(mGfortescue).toDense();
        // test based on a result given in degrees for both fortescue and phase

        Pair<Double, Double> phaseA = FortescueUtil.getPolarFromCartesian(mGphase.get(0, 0), mGphase.get(1, 0));
        Pair<Double, Double> phaseB = FortescueUtil.getPolarFromCartesian(mGphase.get(2, 0), mGphase.get(3, 0));
        Pair<Double, Double> phaseC = FortescueUtil.getPolarFromCartesian(mGphase.get(4, 0), mGphase.get(5, 0));

        assertEquals(50.118988, phaseA.getFirst() / Math.sqrt(3), 0.00001);
        assertEquals(1.83823431 * pi / 180, phaseA.getSecond(), 0.00001);
        assertEquals(-118.161751 * pi / 180, phaseB.getSecond(), 0.00001);
        assertEquals(121.838219 * pi / 180, phaseC.getSecond(), 0.00001);
    }

    @Test
    void testFortescueInversion() {

        DenseMatrix mGfortescue = new DenseMatrix(6, 1);

        mGfortescue.add(0, 0, 1.);
        mGfortescue.add(1, 0, 2.);
        mGfortescue.add(2, 0, 3.);
        mGfortescue.add(3, 0, 4.);
        mGfortescue.add(4, 0, 5.);
        mGfortescue.add(5, 0, 6.);

        DenseMatrix mGphase = FortescueUtil.getFortescueMatrix().times(mGfortescue).toDense();
        DenseMatrix mGfortescueCalculated = FortescueUtil.getFortescueInverseMatrix().times(mGphase).toDense();
        // test based on a result given in degrees for both fortescue and phase

        assertEquals(1, mGfortescueCalculated.get(0, 0), 0.00000001);
        assertEquals(2, mGfortescueCalculated.get(1, 0), 0.00000001);
        assertEquals(3, mGfortescueCalculated.get(2, 0), 0.00000001);
        assertEquals(4, mGfortescueCalculated.get(3, 0), 0.00000001);
        assertEquals(5, mGfortescueCalculated.get(4, 0), 0.00000001);
        assertEquals(6, mGfortescueCalculated.get(5, 0), 0.00000001);
    }
}
