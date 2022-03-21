/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.math.matrix;

import com.google.common.collect.ImmutableList;
import org.junit.Test;

import java.io.IOException;
import java.nio.ByteBuffer;

import static org.junit.Assert.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class DenseMatrixTest extends AbstractMatrixTest {

    private final MatrixFactory matrixFactory = new DenseMatrixFactory();

    private final MatrixFactory otherMatrixFactory = new SparseMatrixFactory();

    @Override
    protected MatrixFactory getMatrixFactory() {
        return matrixFactory;
    }

    @Override
    public MatrixFactory getOtherMatrixFactory() {
        return otherMatrixFactory;
    }

    @Test
    public void invalidBufferCapacity() {
        assertThrows(MatrixException.class, () -> new DenseMatrix(2, 2, () -> ByteBuffer.allocate(3)));
    }

    @Test
    public void testDensePrint() throws IOException {
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
    public void testDensePrintWithNames() throws IOException {
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
    public void testCreateFromColumn() {
        DenseMatrix a = Matrix.createFromColumn(new double[] {1d, 2d, 3d}, matrixFactory).toDense();
        assertEquals(3, a.getRowCount());
        assertEquals(1, a.getColumnCount());
        assertEquals(1d, a.get(0, 0), 0d);
        assertEquals(2d, a.get(1, 0), 0d);
        assertEquals(3d, a.get(2, 0), 0d);
    }

    @Test
    public void testCreateFromRow() {
        DenseMatrix a = Matrix.createFromRow(new double[] {1d, 2d, 3d}, matrixFactory).toDense();
        assertEquals(1, a.getRowCount());
        assertEquals(3, a.getColumnCount());
        assertEquals(1d, a.get(0, 0), 0d);
        assertEquals(2d, a.get(0, 1), 0d);
        assertEquals(3d, a.get(0, 2), 0d);
    }

    @Test
    public void testToSparse() {
        DenseMatrix a = (DenseMatrix) createA(matrixFactory);
        SparseMatrix a2 = a.toSparse();
        assertNotNull(a2);
        assertSame(a2, a2.toSparse());
        DenseMatrix a3 = a2.toDense();
        assertEquals(a, a3);
    }
}
