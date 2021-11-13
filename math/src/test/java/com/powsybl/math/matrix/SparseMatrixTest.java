/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.math.matrix;

import com.powsybl.commons.PowsyblException;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class SparseMatrixTest extends AbstractMatrixTest {

    private final MatrixFactory matrixFactory = new SparseMatrixFactory();

    private final MatrixFactory otherMatrixFactory = new DenseMatrixFactory();

    @Override
    protected MatrixFactory getMatrixFactory() {
        return matrixFactory;
    }

    @Override
    public MatrixFactory getOtherMatrixFactory() {
        return otherMatrixFactory;
    }

    @Test
    public void testSparsePrint() throws IOException {
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

    @Test(expected = PowsyblException.class)
    public void testWrongColumnOrder() {
        Matrix a = matrixFactory.create(2, 2, 2);
        a.set(0, 0, 1d);
        a.set(1, 0, 1d);
        a.set(0, 1, 1d);
        a.set(1, 0, 1d);
    }

    @Test(expected = PowsyblException.class)
    public void testWrongColumnOrderWithAdd() {
        Matrix a = matrixFactory.create(2, 2, 2);
        a.add(0, 0, 1d);
        a.add(1, 0, 1d);
        a.add(0, 1, 1d);
        a.add(1, 0, 1d);
    }

    @Test
    public void testInitSparseMatrixFromCpp() {
        SparseMatrix m = new SparseMatrix(2, 5, new int[] {0, -1, 2, -1, 3, 4}, new int[] {0, 1, 0, 1}, new double[] {1d, 2d, 3d, 4d});
        assertArrayEquals(new int[] {2, 0, 1, 0, 1}, m.getColumnValueCount());
    }

    @Test
    public void testRedecompose() {
        Matrix matrix = getMatrixFactory().create(2, 2, 2);
        matrix.set(0, 0, 3);
        matrix.set(1, 0, 4);
        matrix.set(0, 1, 1);

        try (LUDecomposition decomposition = matrix.decomposeLU()) {
            // fine
            decomposition.update();

            // error as an element has been added
            matrix.set(1, 1, 2);
            try {
                decomposition.update();
                fail();
            } catch (PowsyblException ignored) {
            }
        }
    }
}
