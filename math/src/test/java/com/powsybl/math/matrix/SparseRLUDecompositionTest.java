/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.math.matrix;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests for {@link SparseRLUDecomposition} via {@link SparseMatrix#decomposeRLU()}.
 *
 * <p>The test matrix is the 5x5 system:
 * <pre>
 *   A = [ 2  3  0  0  0 ]
 *       [ 3  0  4  0  6 ]
 *       [ 0 -1 -3  2  0 ]
 *       [ 0  0  1  0  0 ]
 *       [ 0  4  2  0  1 ]
 * </pre>
 * with the known solution x = [1, 2, 3, 4, 5] for right-hand side b = A * x.
 * </p>
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class SparseRLUDecompositionTest {

    private static final double EPSILON = 1e-12;

    /**
     * Build the 5x5 test matrix in CSC format:
     * A = [ 2  3  0  0  0 ]
     *     [ 3  0  4  0  6 ]
     *     [ 0 -1 -3  2  0 ]
     *     [ 0  0  1  0  0 ]
     *     [ 0  4  2  0  1 ]
     */
    private SparseMatrix buildTestMatrix() {
        SparseMatrix m = new SparseMatrix(5, 5, 12);
        // column 0
        m.set(0, 0, 2.0);
        m.set(1, 0, 3.0);
        // column 1
        m.set(0, 1, 3.0);
        m.set(2, 1, -1.0);
        m.set(4, 1, 4.0);
        // column 2
        m.set(1, 2, 4.0);
        m.set(2, 2, -3.0);
        m.set(3, 2, 1.0);
        m.set(4, 2, 2.0);
        // column 3
        m.set(2, 3, 2.0);
        // column 4
        m.set(1, 4, 6.0);
        m.set(4, 4, 1.0);
        return m;
    }

    @Test
    void testSolve() {
        SparseMatrix m = buildTestMatrix();
        // b = A * [1, 2, 3, 4, 5] = [8, 45, -3, 3, 19]
        double[] b = {8, 45, -3, 3, 19};
        try (LUDecomposition lu = m.decomposeRLU()) {
            lu.solve(b);
            assertArrayEquals(new double[]{1, 2, 3, 4, 5}, b, EPSILON);
        }
    }

    @Test
    void testSolveTransposed() {
        SparseMatrix m = buildTestMatrix();
        // c = A^T * [1, 2, 3, 4, 5] = [8, 20, 15, 6, 17]
        double[] c = {8, 20, 15, 6, 17};
        try (LUDecomposition lu = m.decomposeRLU()) {
            lu.solveTransposed(c);
            assertArrayEquals(new double[]{1, 2, 3, 6, 5}, c, EPSILON);
        }
    }

    @Test
    void testSolveMatrix() {
        SparseMatrix m = buildTestMatrix();
        // Two RHS columns (column-major): col0 = b, col1 = 2*b => solutions [1..5] and [2,4,6,8,10]
        DenseMatrix b = new DenseMatrix(5, 2);
        double[] col0 = {8, 45, -3, 3, 19};
        double[] col1 = {16, 90, -6, 6, 38};
        for (int i = 0; i < 5; i++) {
            b.set(i, 0, col0[i]);
            b.set(i, 1, col1[i]);
        }
        try (LUDecomposition lu = m.decomposeRLU()) {
            lu.solve(b);
            assertArrayEquals(new double[]{1, 2, 3, 4, 5},
                    new double[]{b.get(0, 0), b.get(1, 0), b.get(2, 0), b.get(3, 0), b.get(4, 0)}, EPSILON);
            assertArrayEquals(new double[]{2, 4, 6, 8, 10},
                    new double[]{b.get(0, 1), b.get(1, 1), b.get(2, 1), b.get(3, 1), b.get(4, 1)}, EPSILON);
        }
    }

    @Test
    void testUpdate() {
        SparseMatrix m = buildTestMatrix();
        // Scale values by 2: A2 = 2*A, b2 = 2*b, solution is still [1,2,3,4,5]
        try (LUDecomposition lu = m.decomposeRLU()) {
            // scale all non-zero values in-place
            double[] values = m.getValues();
            for (int i = 0; i < values.length; i++) {
                values[i] *= 2;
            }
            lu.update();
            double[] b = {16, 90, -6, 6, 38};
            lu.solve(b);
            assertArrayEquals(new double[]{1, 2, 3, 4, 5}, b, EPSILON);
        }
    }

    @Test
    void testNonSquareMatrixThrows() {
        SparseMatrix m = new SparseMatrix(3, 2, 2);
        assertThrows(MatrixException.class, m::decomposeRLU);
    }

    @Test
    void testRedecomposeAfterAddingElementThrows() {
        SparseMatrix m = new SparseMatrix(2, 2, 2);
        m.set(0, 0, 3);
        m.set(1, 0, 4);
        m.set(0, 1, 1);
        try (LUDecomposition lu = m.decomposeRLU()) {
            lu.update();
            m.set(1, 1, 2);
            assertThrows(MatrixException.class, lu::update);
        }
    }
}
