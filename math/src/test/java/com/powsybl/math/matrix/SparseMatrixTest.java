/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.math.matrix;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class SparseMatrixTest extends AbstractMatrixTest {

    private final MatrixFactory matrixFactory = new SparseMatrixFactory();

    private final MatrixFactory otherMatrixFactory = new DenseMatrixFactory();

    @Override
    protected MatrixFactory getMatrixFactory() {
        return matrixFactory;
    }

    @Override
    protected MatrixFactory getOtherMatrixFactory() {
        return otherMatrixFactory;
    }

    @Test
    void testSparsePrint() throws IOException {
        Matrix a = createA(matrixFactory);
        String expected = String.join(System.lineSeparator(),
                "rowCount=3",
                "columnCount=2",
                "columnStart=[0, 2, 3]",
                "columnValueCount=[2, 1]",
                "rowIndices={0, 2, 1}",
                "values={1.0, 2.0, 3.0}")
                + System.lineSeparator();
        assertEquals(expected, print(a, null, null));
        assertEquals(expected, print(a));
    }

    @Test
    void testWrongColumnOrder() {
        Matrix a = matrixFactory.create(2, 2, 2);
        a.set(0, 0, 1d);
        a.set(1, 0, 1d);
        a.set(0, 1, 1d);
        assertThrows(MatrixException.class, () -> a.set(1, 0, 1d));
    }

    @Test
    void testWrongColumnOrderWithAdd() {
        Matrix a = matrixFactory.create(2, 2, 2);
        a.add(0, 0, 1d);
        a.add(1, 0, 1d);
        a.add(0, 1, 1d);
        assertThrows(MatrixException.class, () -> a.add(1, 0, 1d));
    }

    @Test
    void testInitSparseMatrixFromCpp() {
        SparseMatrix m = new SparseMatrix(2, 5, new int[] {0, -1, 2, -1, 3, 4}, new int[] {0, 1, 0, 1}, new double[] {1d, 2d, 3d, 4d});
        assertArrayEquals(new int[] {2, 0, 1, 0, 1}, m.getColumnValueCount());
    }

    @Test
    void testRedecompose() {
        Matrix matrix = getMatrixFactory().create(2, 2, 2);
        matrix.set(0, 0, 3);
        matrix.set(1, 0, 4);
        matrix.set(0, 1, 1);

        try (LUDecomposition decomposition = matrix.decomposeLU()) {
            // fine
            decomposition.update();

            // error as an element has been added
            matrix.set(1, 1, 2);
            assertThrows(MatrixException.class, decomposition::update);
        }
    }

    @Test
    void initFromArrayIssue() {
        SparseMatrix a = new SparseMatrix(2, 2, 2);
        a.add(0, 0, 1d);
        a.add(1, 0, 1d);
        a.add(0, 1, 1d);
        try (LUDecomposition decomposition = a.decomposeLU()) {
            double[] x = new double[] {1, 0};
            decomposition.solve(x);
            assertArrayEquals(new double[] {0, 1}, x);
        }
        SparseMatrix m = new SparseMatrix(a.getRowCount(), a.getColumnCount(), a.getColumnStart(), a.getRowIndices(), a.getValues());
        try (LUDecomposition decomposition = m.decomposeLU()) {
            double[] x = new double[] {1, 0};
            decomposition.solve(x);
            assertArrayEquals(new double[] {0, 1}, x);
        }
    }

    @Test
    void testFromArrayConstructorErrors() {
        MatrixException e = assertThrows(MatrixException.class, () -> new SparseMatrix(2, 2, new int[]{0, 1}, new int[]{0, 1}, new double[]{0.5, 0.3}));
        assertEquals("columnStart array length has to be columnCount + 1", e.getMessage());
        e = assertThrows(MatrixException.class, () -> new SparseMatrix(2, 2, new int[]{0, 1, 1}, new int[]{0, 1, 1}, new double[]{0.5, 0.3}));
        assertEquals("rowIndices and values arrays must have the same length", e.getMessage());
    }
}
