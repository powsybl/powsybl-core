/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.math.matrix;

import java.nio.ByteBuffer;
import java.util.UUID;

/**
 * Sparse matrix LU decomposition.
 *
 * @see SparseMatrix
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class SparseLUDecomposition implements LUDecomposition {

    private final String id;

    SparseLUDecomposition(SparseMatrix matrix) {
        if (matrix.getM() != matrix.getN()) {
            throw new IllegalArgumentException("matrix is not square");
        }
        this.id = UUID.randomUUID().toString();
        init(id, matrix.getColumnStart(), matrix.getRowIndices(), matrix.getValues());
    }

    private native void init(String id, int[] ap, int[] ai, double[] ax);

    private native void release(String id);

    private native void solve(String id, double[] b);

    private native void solve2(String id, int m, int n, ByteBuffer b);

    /**
     * {@inheritDoc}
     */
    @Override
    public void solve(double[] b) {
        solve(id, b);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void solve(DenseMatrix b) {
        solve2(id, b.getM(), b.getN(), b.getBuffer());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
        release(id);
    }
}
