/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.math.matrix;

import com.google.common.collect.ImmutableList;
import com.google.common.testing.EqualsTester;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public abstract class AbstractMatrixTest {

    protected static final double EPSILON = Math.pow(10, -15);

    protected abstract MatrixFactory getMatrixFactory();

    protected abstract MatrixFactory getOtherMatrixFactory();

    protected Matrix createA(MatrixFactory matrixFactory) {
        Matrix a = matrixFactory.create(3, 2, 3);
        a.setValue(0, 0, 1);
        a.setValue(2, 0, 2);
        a.setValue(1, 1, 3);
        return a;
    }

    @Test
    public void testMultiplication() throws Exception {
        Matrix a = createA(getMatrixFactory());
        Matrix b = getMatrixFactory().create(2, 1, 2);
        b.setValue(0, 0, 4);
        b.setValue(1, 0, 5);

        Matrix cs = a.times(b);
        DenseMatrix c = cs.toDense();

        assertEquals(3, c.getM());
        assertEquals(1, c.getN());
        assertEquals(4, c.getValue(0, 0), EPSILON);
        assertEquals(15, c.getValue(1, 0), EPSILON);
        assertEquals(8, c.getValue(2, 0), EPSILON);
    }

    @Test
    public void testIterateNonZeroValue() {
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
    public void testIterateNonZeroValueOfColumn() {
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

    @Test
    public void testDecompose() throws Exception {
        Matrix matrix = getMatrixFactory().create(5, 5, 12);

        matrix.setValue(0, 0, 2);
        matrix.setValue(1, 0, 3);

        matrix.setValue(0, 1, 3);
        matrix.setValue(2, 1, -1);
        matrix.setValue(4, 1, 4);

        matrix.setValue(1, 2, 4);
        matrix.setValue(2, 2, -3);
        matrix.setValue(3, 2, 1);
        matrix.setValue(4, 2, 2);

        matrix.setValue(2, 3, 2);

        matrix.setValue(1, 4, 6);
        matrix.setValue(4, 4, 1);

        try (LUDecomposition decomposition = matrix.decomposeLU()) {
            double[] x = {8, 45, -3, 3, 19};
            decomposition.solve(x);
            assertArrayEquals(new double[]{1, 2, 3, 4, 5}, x, EPSILON);

            DenseMatrix x2 = new DenseMatrix(5, 2);
            x2.setValue(0, 0, 8);
            x2.setValue(1, 0, 45);
            x2.setValue(2, 0, -3);
            x2.setValue(3, 0, 3);
            x2.setValue(4, 0, 19);
            x2.setValue(0, 1, 8);
            x2.setValue(1, 1, 45);
            x2.setValue(2, 1, -3);
            x2.setValue(3, 1, 3);
            x2.setValue(4, 1, 19);

            decomposition.solve(x2);
            assertEquals(1, x2.getValue(0, 0), EPSILON);
            assertEquals(2, x2.getValue(1, 0), EPSILON);
            assertEquals(3, x2.getValue(2, 0), EPSILON);
            assertEquals(4, x2.getValue(3, 0), EPSILON);
            assertEquals(5, x2.getValue(4, 0), EPSILON);
            assertEquals(1, x2.getValue(0, 1), EPSILON);
            assertEquals(2, x2.getValue(1, 1), EPSILON);
            assertEquals(3, x2.getValue(2, 1), EPSILON);
            assertEquals(4, x2.getValue(3, 1), EPSILON);
            assertEquals(5, x2.getValue(4, 1), EPSILON);
        }
    }

    @Test
    public void testDenseEquals() {
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
    public void toTest() {
        Matrix a = createA(getMatrixFactory());
        Matrix a2 = a.to(getOtherMatrixFactory());
        Matrix a3 = a2.to(getMatrixFactory());
        assertEquals(a, a3);
        assertTrue(a.to(getMatrixFactory()) == a);
        assertTrue(a2.to(getOtherMatrixFactory()) == a2);
    }
}
