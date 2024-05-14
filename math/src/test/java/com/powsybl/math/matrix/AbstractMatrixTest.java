/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.math.matrix;

import com.google.common.collect.ImmutableList;
import com.google.common.testing.EqualsTester;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
abstract class AbstractMatrixTest {

    protected static final double EPSILON = Math.pow(10, -15);

    protected abstract MatrixFactory getMatrixFactory();

    protected abstract MatrixFactory getOtherMatrixFactory();

    protected Matrix createA(MatrixFactory matrixFactory) {
        Matrix a = matrixFactory.create(3, 2, 3);
        a.set(0, 0, 1);
        a.set(2, 0, 2);
        a.set(1, 1, 3);
        return a;
    }

    @Test
    void checkBoundsTest() {
        MatrixFactory matrixFactory = getMatrixFactory();
        assertThrows(MatrixException.class, () -> matrixFactory.create(-1, 1, 1));
        assertThrows(MatrixException.class, () -> matrixFactory.create(1, -1, 1));

        Matrix a = matrixFactory.create(1, 1, 1);
        assertThrows(MatrixException.class, () -> a.set(-1, 0, 0));
        assertThrows(MatrixException.class, () -> a.set(0, -1, 0));
        assertThrows(MatrixException.class, () -> a.set(2, 0, 0));
        assertThrows(MatrixException.class, () -> a.set(0, 1, 0));
        assertThrows(MatrixException.class, () -> a.add(2, 0, 0));
        assertThrows(MatrixException.class, () -> a.add(0, 1, 0));
    }

    @Test
    void testMultiplication() {
        Matrix a = createA(getMatrixFactory());
        Matrix b = getMatrixFactory().create(2, 1, 2);
        b.set(0, 0, 4);
        b.set(1, 0, 5);

        Matrix cs = a.times(b);
        DenseMatrix c = cs.toDense();

        assertEquals(3, c.getRowCount());
        assertEquals(1, c.getColumnCount());
        assertEquals(4, c.get(0, 0), EPSILON);
        assertEquals(15, c.get(1, 0), EPSILON);
        assertEquals(8, c.get(2, 0), EPSILON);

        Matrix cs2 = a.times(b, 2);
        DenseMatrix c2 = cs2.toDense();

        assertEquals(3, c2.getRowCount());
        assertEquals(1, c2.getColumnCount());
        assertEquals(8, c2.get(0, 0), EPSILON);
        assertEquals(30, c2.get(1, 0), EPSILON);
        assertEquals(16, c2.get(2, 0), EPSILON);
    }

    @Test
    void testAddition() {
        /*
        1 0
        0 3
        2 0
         */
        Matrix a = createA(getMatrixFactory());
        /*
        4 0
        5 0
        0 0
         */
        Matrix b = getMatrixFactory().create(3, 2, 3);
        b.set(0, 0, 4);
        b.set(1, 0, 5);

        Matrix cs = a.add(b);
        DenseMatrix c = cs.toDense();

        assertEquals(3, c.getRowCount());
        assertEquals(2, c.getColumnCount());
        assertEquals(5, c.get(0, 0), EPSILON);
        assertEquals(5, c.get(1, 0), EPSILON);
        assertEquals(2, c.get(2, 0), EPSILON);
        assertEquals(0, c.get(0, 1), EPSILON);
        assertEquals(3, c.get(1, 1), EPSILON);
        assertEquals(0, c.get(2, 1), EPSILON);

        // in case of sparse matrix check, we only have 4 values
        if (cs instanceof SparseMatrix) {
            assertEquals(4, ((SparseMatrix) cs).getValues().length);
        }
    }

    @Test
    void testAdditionWithEmptyColumnInTheMiddle() {
        /*
        1 0 0
        0 0 3
        2 0 0
         */
        Matrix a = getMatrixFactory().create(3, 3, 3);
        a.set(0, 0, 1);
        a.set(2, 0, 2);
        a.set(1, 2, 3);

        /*
        4 0 6
        5 0 0
        0 0 0
         */
        Matrix b = getMatrixFactory().create(3, 3, 3);
        b.set(0, 0, 4);
        b.set(1, 0, 5);
        b.set(0, 2, 6);

        Matrix cs = a.add(b);
        DenseMatrix c = cs.toDense();

        assertEquals(3, c.getRowCount());
        assertEquals(3, c.getColumnCount());
        assertEquals(5, c.get(0, 0), EPSILON);
        assertEquals(5, c.get(1, 0), EPSILON);
        assertEquals(2, c.get(2, 0), EPSILON);
        assertEquals(0, c.get(0, 1), EPSILON);
        assertEquals(0, c.get(1, 1), EPSILON);
        assertEquals(0, c.get(2, 1), EPSILON);
        assertEquals(6, c.get(0, 2), EPSILON);
        assertEquals(3, c.get(1, 2), EPSILON);
        assertEquals(0, c.get(2, 2), EPSILON);
    }

    @Test
    void testIterateNonZeroValue() {
        Matrix a = createA(getMatrixFactory());
        a.iterateNonZeroValue((i, j, value) -> {
            if (i == 0 && j == 0) {
                assertEquals(1d, value, 0d);
            } else if (i == 1 && j == 1) {
                assertEquals(3d, value, 0d);
            } else if (i == 2 && j == 0) {
                assertEquals(2d, value, 0d);
            } else {
                fail();
            }
        });
    }

    @Test
    void testIterateNonZeroValueOfColumn() {
        Matrix a = createA(getMatrixFactory());
        List<Double> nonZeroValues = new ArrayList<>();
        a.iterateNonZeroValueOfColumn(0, (i, j, value) -> {
            nonZeroValues.add(value);
        });
        assertEquals(ImmutableList.of(1d, 2d), nonZeroValues);
    }

    protected String print(Matrix matrix, List<String> rowNames, List<String> columnNames) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            matrix.print(new PrintStream(bos), rowNames, columnNames);
        } finally {
            bos.close();
        }
        return bos.toString(StandardCharsets.UTF_8.name());
    }

    protected String print(Matrix matrix) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            matrix.print(new PrintStream(bos));
        } finally {
            bos.close();
        }
        return bos.toString(StandardCharsets.UTF_8.name());
    }

    @Test
    void testDecompose() {
        // 2  3  0  0  0
        // 3  0  4  0  6
        // 0 -1 -3  2  0
        // 0  0  1  0  0
        // 0  4  2  0  1
        Matrix matrix = getMatrixFactory().create(5, 5, 12);

        matrix.set(0, 0, 2);
        matrix.set(1, 0, 3);

        matrix.set(0, 1, 3);
        matrix.set(2, 1, -1);
        matrix.set(4, 1, 4);

        matrix.set(1, 2, 4);
        matrix.set(2, 2, -3);
        Matrix.Element e = matrix.addAndGetElement(3, 2, 1);
        matrix.set(4, 2, 2);

        matrix.set(2, 3, 2);

        matrix.set(1, 4, 6);
        matrix.set(4, 4, 1);

        try (LUDecomposition decomposition = matrix.decomposeLU()) {
            double[] x = {8, 45, -3, 3, 19};
            decomposition.solve(x);
            assertArrayEquals(new double[]{1, 2, 3, 4, 5}, x, EPSILON);

            DenseMatrix x2 = new DenseMatrix(5, 2);
            x2.set(0, 0, 8);
            x2.set(1, 0, 45);
            x2.set(2, 0, -3);
            x2.set(3, 0, 3);
            x2.set(4, 0, 19);
            x2.set(0, 1, 8);
            x2.set(1, 1, 45);
            x2.set(2, 1, -3);
            x2.set(3, 1, 3);
            x2.set(4, 1, 19);

            decomposition.solve(x2);
            assertEquals(1, x2.get(0, 0), EPSILON);
            assertEquals(2, x2.get(1, 0), EPSILON);
            assertEquals(3, x2.get(2, 0), EPSILON);
            assertEquals(4, x2.get(3, 0), EPSILON);
            assertEquals(5, x2.get(4, 0), EPSILON);
            assertEquals(1, x2.get(0, 1), EPSILON);
            assertEquals(2, x2.get(1, 1), EPSILON);
            assertEquals(3, x2.get(2, 1), EPSILON);
            assertEquals(4, x2.get(3, 1), EPSILON);
            assertEquals(5, x2.get(4, 1), EPSILON);

            e.set(4);
            e.add(1);
            decomposition.update();
            double[] x3 = {8, 45, -3, 3, 19};
            decomposition.solve(x3);
            assertArrayEquals(new double[]{-0.010526315789474902, 2.673684210526316, 0.6, 0.7368421052631579, 7.105263157894737}, x3, EPSILON);
        }
    }

    @Test
    void testDecompositionFailure() {
        Matrix matrix = getMatrixFactory().create(5, 5, 12);
        assertThrows(MatrixException.class, () -> {
            try (LUDecomposition decomposition = matrix.decomposeLU()) {
                double[] x = {0, 0, 0, 0, 0};
                decomposition.solve(x);
            }
        });
    }

    @Test
    void testTransposedDecompose() {
        // 2  3  0  0  0
        // 3  0 -1  0  4
        // 0  4 -3  1  2
        // 0  0  2  0  0
        // 0  6  0  0  1
        Matrix matrix = getMatrixFactory().create(5, 5, 12);

        matrix.set(0, 0, 2);
        matrix.set(1, 0, 3);

        matrix.set(0, 1, 3);
        matrix.set(2, 1, 4);
        matrix.set(4, 1, 6);

        matrix.set(1, 2, -1);
        matrix.set(2, 2, -3);
        matrix.set(3, 2, 2);

        matrix.set(2, 3, 1);

        matrix.set(1, 4, 4);
        matrix.set(2, 4, 2);
        matrix.set(4, 4, 1);

        try (LUDecomposition decomposition = matrix.decomposeLU()) {
            double[] x = {8, 45, -3, 3, 19};
            decomposition.solveTransposed(x);
            assertArrayEquals(new double[]{1, 2, 3, 4, 5}, x, EPSILON);

            DenseMatrix x2 = new DenseMatrix(5, 2);
            x2.set(0, 0, 8);
            x2.set(1, 0, 45);
            x2.set(2, 0, -3);
            x2.set(3, 0, 3);
            x2.set(4, 0, 19);
            x2.set(0, 1, 8);
            x2.set(1, 1, 45);
            x2.set(2, 1, -3);
            x2.set(3, 1, 3);
            x2.set(4, 1, 19);

            decomposition.solveTransposed(x2);
            assertEquals(1, x2.get(0, 0), EPSILON);
            assertEquals(2, x2.get(1, 0), EPSILON);
            assertEquals(3, x2.get(2, 0), EPSILON);
            assertEquals(4, x2.get(3, 0), EPSILON);
            assertEquals(5, x2.get(4, 0), EPSILON);
            assertEquals(1, x2.get(0, 1), EPSILON);
            assertEquals(2, x2.get(1, 1), EPSILON);
            assertEquals(3, x2.get(2, 1), EPSILON);
            assertEquals(4, x2.get(3, 1), EPSILON);
            assertEquals(5, x2.get(4, 1), EPSILON);
        }
    }

    private static void decomposeThenSolve(Matrix matrix, double[] b) {
        LUDecomposition luDecomposition = matrix.decomposeLU();
        luDecomposition.solve(b);
    }

    @Test
    void testDecomposeNonSquare() {
        Matrix matrix = getMatrixFactory().create(1, 2, 4);
        assertThrows(MatrixException.class, () -> decomposeThenSolve(matrix, new double[] {}));
    }

    @Test
    void testDenseEquals() {
        Matrix a1 = createA(getMatrixFactory());
        Matrix a2 = createA(getMatrixFactory());
        Matrix b1 = getMatrixFactory().create(5, 5, 0);
        Matrix b2 = getMatrixFactory().create(5, 5, 0);
        new EqualsTester()
                .addEqualityGroup(a1, a2)
                .addEqualityGroup(b1, b2)
                .testEquals();
    }

    @Test
    void toTest() {
        Matrix a = createA(getMatrixFactory());
        Matrix a2 = a.to(getOtherMatrixFactory());
        Matrix a3 = a2.to(getMatrixFactory());
        assertEquals(a, a3);
        assertSame(a.to(getMatrixFactory()), a);
        assertSame(a2.to(getOtherMatrixFactory()), a2);
    }

    @Test
    void testAddValue() {
        Matrix a = getMatrixFactory().create(2, 2, 2);
        a.add(0, 0, 1d);
        a.add(0, 0, 1d);
        a.add(1, 1, 1d);
        a.add(1, 1, 2d);

        DenseMatrix b = a.toDense();
        assertEquals(2d, b.get(0, 0), 0d);
        assertEquals(0d, b.get(1, 0), 0d);
        assertEquals(0d, b.get(0, 1), 0d);
        assertEquals(3d, b.get(1, 1), 0d);
    }

    @Test
    void testAddValue2() {
        Matrix a = getMatrixFactory().create(2, 2, 2);
        a.add(0, 0, 1d);
        a.add(0, 1, 1d);

        DenseMatrix b = a.toDense();
        assertEquals(1d, b.get(0, 0), 0d);
        assertEquals(0d, b.get(1, 0), 0d);
        assertEquals(1d, b.get(0, 1), 0d);
        assertEquals(0d, b.get(1, 1), 0d);
    }

    @Test
    void testIssueWithEmptyColumns() {
        Matrix a = getMatrixFactory().create(2, 2, 2);
        a.set(0, 0, 1d);
        // second column is empty
        assertEquals(1, a.toDense().get(0, 0), 0d);
    }

    @Test
    void testReset() {
        Matrix a = getMatrixFactory().create(3, 3, 3);
        // 1 0 4
        // 0 2 0
        // 0 3 0
        Matrix.Element e1 = a.addAndGetElement(0, 0, 1d);
        Matrix.Element e2 = a.addAndGetElement(1, 1, 2d);
        Matrix.Element e3 = a.addAndGetElement(2, 1, 3d);
        Matrix.Element e4 = a.addAndGetElement(0, 2, 4d);

        a.reset();

        assertEquals(0d, a.toDense().get(0, 0), 0d);
        assertEquals(0d, a.toDense().get(1, 1), 0d);
        assertEquals(0d, a.toDense().get(2, 1), 0d);
        assertEquals(0d, a.toDense().get(0, 2), 0d);

        e1.set(5d);
        e2.set(6d);
        e3.set(7d);
        e4.set(8d);

        assertEquals(5d, a.toDense().get(0, 0), 0d);
        assertEquals(6d, a.toDense().get(1, 1), 0d);
        assertEquals(7d, a.toDense().get(2, 1), 0d);
        assertEquals(8d, a.toDense().get(0, 2), 0d);
    }

    @Test
    void testDeprecated() {
        Matrix a = getMatrixFactory().create(2, 2, 2);
        assertEquals(a.getRowCount(), a.getM());
        assertEquals(a.getColumnCount(), a.getN());
        a.setValue(0, 0, 1d);
        a.setValue(0, 1, 1d);
        assertEquals(1d, a.toDense().get(0, 1), 0d);
        a.addValue(0, 1, 2d);
        assertEquals(3d, a.toDense().get(0, 1), 0d);
    }

    @Test
    void testAddAndGetIndex() {
        Matrix a = getMatrixFactory().create(3, 3, 3);
        // 1 0 4
        // 0 2 0
        // 0 3 0
        int index1 = a.addAndGetIndex(0, 0, 1d);
        int index2 = a.addAndGetIndex(1, 1, 2d);
        int index3 = a.addAndGetIndex(2, 1, 3d);
        int index4 = a.addAndGetIndex(0, 2, 4d);

        assertEquals(1d, a.toDense().get(0, 0), 0d);
        assertEquals(0d, a.toDense().get(1, 0), 0d);
        assertEquals(0d, a.toDense().get(2, 0), 0d);
        assertEquals(0d, a.toDense().get(0, 1), 0d);
        assertEquals(2d, a.toDense().get(1, 1), 0d);
        assertEquals(3d, a.toDense().get(2, 1), 0d);
        assertEquals(4d, a.toDense().get(0, 2), 0d);
        assertEquals(0d, a.toDense().get(1, 2), 0d);
        assertEquals(0d, a.toDense().get(2, 2), 0d);

        a.setAtIndex(index1, 9);
        a.addAtIndex(index2, 1);
        a.setAtIndex(index3, 10);
        a.addAtIndex(index4, 1);

        assertEquals(9d, a.toDense().get(0, 0), 0d);
        assertEquals(3d, a.toDense().get(1, 1), 0d);
        assertEquals(10d, a.toDense().get(2, 1), 0d);
        assertEquals(5d, a.toDense().get(0, 2), 0d);

        assertThrows(MatrixException.class, () -> a.setAtIndex(10, 0));
    }

    @Test
    void testTranspose() {
        Matrix a = createA(getMatrixFactory());
        DenseMatrix at = a.transpose().toDense();
        assertEquals(2, at.getRowCount());
        assertEquals(3, at.getColumnCount());
        assertEquals(1d, at.get(0, 0), 0d);
        assertEquals(0d, at.get(0, 1), 0d);
        assertEquals(2d, at.get(0, 2), 0d);
        assertEquals(0d, at.get(1, 0), 0d);
        assertEquals(3d, at.get(1, 1), 0d);
        assertEquals(0d, at.get(1, 2), 0d);
    }
}
