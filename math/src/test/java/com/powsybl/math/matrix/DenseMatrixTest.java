/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.math.matrix;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class DenseMatrixTest extends AbstractMatrixTest {

    private final MatrixFactory matrixFactory = new DenseMatrixFactory();

    private final MatrixFactory otherMatrixFactory = new SparseMatrixFactory();

    @Override
    protected MatrixFactory getMatrixFactory() {
        return matrixFactory;
    }

    @Override
    protected MatrixFactory getOtherMatrixFactory() {
        return otherMatrixFactory;
    }

    @Test
    void invalidBufferCapacity() {
        assertThrows(MatrixException.class, () -> new DenseMatrix(2, 2, () -> ByteBuffer.allocate(3)));
    }

    @Test
    void testDensePrint() throws IOException {
        Matrix a = createA(matrixFactory);
        String expected = String.join(System.lineSeparator(),
                " 1.0 0.0",
                " 0.0 3.0",
                " 2.0 0.0")
                + System.lineSeparator();
        assertEquals(expected, print(a, null, null));
        assertEquals(expected, print(a));
    }

    @Test
    void testDensePrintWithNames() throws IOException {
        Matrix a = createA(matrixFactory);
        String expected = String.join(System.lineSeparator(),
                "     c1  c2",
                " r1 1.0 0.0",
                " r2 0.0 3.0",
                " r3 2.0 0.0")
                + System.lineSeparator();
        assertEquals(expected, print(a, ImmutableList.of("r1", "r2", "r3"), ImmutableList.of("c1", "c2")));
    }

    @Test
    void testCreateFromColumn() {
        DenseMatrix a = Matrix.createFromColumn(new double[] {1d, 2d, 3d}, matrixFactory).toDense();
        assertEquals(3, a.getRowCount());
        assertEquals(1, a.getColumnCount());
        assertEquals(1d, a.get(0, 0), 0d);
        assertEquals(2d, a.get(1, 0), 0d);
        assertEquals(3d, a.get(2, 0), 0d);
    }

    @Test
    void testCreateFromRow() {
        DenseMatrix a = Matrix.createFromRow(new double[] {1d, 2d, 3d}, matrixFactory).toDense();
        assertEquals(1, a.getRowCount());
        assertEquals(3, a.getColumnCount());
        assertEquals(1d, a.get(0, 0), 0d);
        assertEquals(2d, a.get(0, 1), 0d);
        assertEquals(3d, a.get(0, 2), 0d);
    }

    @Test
    void testToSparse() {
        DenseMatrix a = (DenseMatrix) createA(matrixFactory);
        SparseMatrix a2 = a.toSparse();
        assertNotNull(a2);
        assertSame(a2, a2.toSparse());
        DenseMatrix a3 = a2.toDense();
        assertEquals(a, a3);
    }

    @Test
    void testDenseMultiplication() {
        DenseMatrix a = new DenseMatrix(2, 1);
        a.set(0, 0, 4);
        a.set(1, 0, 5);
        DenseMatrix b = new DenseMatrix(1, 2);
        b.set(0, 0, 3);
        DenseMatrix c = a.times(b);
        assertEquals(2, c.getRowCount());
        assertEquals(2, c.getColumnCount());
        assertEquals(12, c.get(0, 0), EPSILON);
        assertEquals(15, c.get(1, 0), EPSILON);
        assertEquals(0, c.get(0, 1), EPSILON);
        assertEquals(0, c.get(1, 1), EPSILON);
    }

    @Test
    void testTooManyElementDenseMatrix() {
        MatrixException e = assertThrows(MatrixException.class, () -> new DenseMatrix(100000, 10000));
        assertEquals("Too many elements for a dense matrix, maximum allowed is 268435455", e.getMessage());
        assertEquals(268435455, DenseMatrix.MAX_ELEMENT_COUNT);
    }
}
