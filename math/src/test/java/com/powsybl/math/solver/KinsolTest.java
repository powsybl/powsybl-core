/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.math.solver;

import com.powsybl.math.matrix.SparseMatrix;
import com.powsybl.math.matrix.SparseMatrixFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class KinsolTest {

    @Test
    void test() {
        SparseMatrix j = new SparseMatrixFactory().create(2, 2, 4);
        j.add(0, 0, 0);
        j.add(1, 0, 0);
        j.add(0, 1, 0);
        j.add(1, 1, 0);
        Kinsol solver = new Kinsol(j, (x, f) -> {
            // 0 = 0.02 + v2 * 0.1 * sin(ph2)
            // 0 = 0.01 + v2 * 0.1 * (-cos(ph2) + v2)
            double v2 = x[0];
            double ph2 = x[1];
            f[0] = 0.02 + v2 * 0.1 * Math.sin(ph2);
            f[1] = 0.01 + v2 * 0.1 * (-Math.cos(ph2) + v2);
        }, (x, j2) -> {
            double v2 = x[0];
            double ph2 = x[1];
            double dp2dv2 = 0.1 * Math.sin(ph2);
            double dp2dph2 = v2 * 0.1 * Math.cos(ph2);
            double dq2dv2 = -0.1 * Math.cos(ph2) + 2 * v2 * 0.1;
            double dq2dph2 = v2 * 0.1 * Math.sin(ph2);
            j2.setAtIndex(0, dp2dv2);
            j2.setAtIndex(1, dp2dph2);
            j2.setAtIndex(2, dq2dv2);
            j2.setAtIndex(3, dq2dph2);
        });
        double[] x = new double[] {1, 0}; // initial guess
        KinsolParameters parameters = new KinsolParameters();
        KinsolResult result = solver.solve(x, parameters);
        assertSame(KinsolStatus.KIN_SUCCESS, result.getStatus());
        assertEquals(9, result.getIterations());
        assertArrayEquals(new double[] {0.85542, -0.235959}, x, 1e-6);
    }
}
