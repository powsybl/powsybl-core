/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.math.matrix;

import org.apache.commons.math3.complex.Complex;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Jean-Baptiste Heyberger {@literal <jbheyberger at gmail.com>}
 */
class ComplexMatrixTest {

    @Test
    void complexMatrixTest() {

        ComplexMatrix cm = new ComplexMatrix(2, 2);
        cm.set(0, 0, new Complex(1., 2.));
        cm.set(0, 1, new Complex(3., 4.));
        cm.set(1, 0, new Complex(5., 6.));
        cm.set(1, 1, new Complex(7., 8.));

        // create the real equivalent matrix of complex matrix
        DenseMatrix rm = cm.toRealCartesianMatrix();

        assertEquals(cm.get(0, 0).getReal(), rm.get(0, 0));
        assertEquals(cm.get(1, 1).getImaginary(), -rm.get(2, 3));

        ComplexMatrix cmFromReal = ComplexMatrix.fromRealCartesian(rm);

        for (int i = 0; i < cm.getRowCount(); i++) {
            for (int j = 0; j < cm.getColumnCount(); j++) {
                assertEquals(cm.get(i, j), cmFromReal.get(i, j));
            }
        }

        ComplexMatrix tm = cm.transpose();
        assertEquals(cm.get(0, 1), tm.get(1, 0));

        ComplexMatrix im = ComplexMatrix.createIdentity(2);
        assertEquals(im.get(0, 1), new Complex(0, 0));
        assertEquals(im.get(1, 1), new Complex(1, 0));

        ComplexMatrix sm = cm.scale(new Complex(2., 1.)); //ComplexMatrix.getMatrixScaled(cm, new Complex(2., 1.));
        assertEquals(sm.get(0, 0), new Complex(0., 5.));

        sm = cm.scale(3.);
        assertEquals(sm.get(0, 0), new Complex(3., 6.));

    }
}
