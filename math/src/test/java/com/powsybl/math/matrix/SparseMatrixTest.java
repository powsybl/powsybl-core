/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.math.matrix;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeTrue;

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

    @Override
    public void testMultiplication() throws Exception {
        assumeTrue(SparseMatrix.NATIVE_INIT);
        super.testMultiplication();
    }

    @Override
    public void testDecompose() throws Exception {
        assumeTrue(SparseMatrix.NATIVE_INIT);
        super.testDecompose();
    }

    @Test
    public void testSparsePrint() throws IOException {
        Matrix a = createA(matrixFactory);
        String expected = String.join(System.lineSeparator(),
                "m=3",
                "n=2",
                "columnStart=[0, 2, 3]",
                "rowIndices={0, 2, 1}",
                "values={1.0, 2.0, 3.0}")
                + System.lineSeparator();
        assertEquals(expected, print(a, null, null));
    }
}
