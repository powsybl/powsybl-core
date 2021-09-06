/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.math.matrix;

import com.powsybl.commons.PowsyblException;

import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.UUID;

/**
 * Sparse matrix LU decomposition.
 *
 * @see SparseMatrix
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class SparseLUDecomposition implements LUDecomposition {

    static final double DEFAULT_RGROWTH_THRESHOLD = 1e-10;

    private final SparseMatrix matrix;

    private final String id;

    private int valueCount;

    SparseLUDecomposition(SparseMatrix matrix) {
        this.matrix = Objects.requireNonNull(matrix);
        if (matrix.getRowCount() != matrix.getColumnCount()) {
            throw new IllegalArgumentException("matrix is not square");
        }
        this.id = UUID.randomUUID().toString();
        init(id, matrix.getColumnStart(), matrix.getRowIndices(), matrix.getValues());
        valueCount = getMatrixValueCount();
    }

    private int getMatrixValueCount() {
        int[] columnStart = matrix.getColumnStart();
        return columnStart[columnStart.length - 1];
    }

    private native void init(String id, int[] ap, int[] ai, double[] ax);

    private native void release(String id);

    private native void update(String id, int[] ap, int[] ai, double[] ax, double rgrowthThreshold);

    private native void solve(String id, double[] b, boolean transpose);

    private native void solve2(String id, int m, int n, ByteBuffer b, boolean transpose);

    /**
     * Check no elements have been added since first decomposition
     */
    private void checkMatrixStructure() {
        if (getMatrixValueCount() != valueCount) {
            throw new PowsyblException("Elements have been added to the sparse matrix since initial decomposition");
        }
    }

    /**
     * {@inheritDoc}
     *
     * The structure of the matrix is not supposed to have changed, only non zero values.
     */
    @Override
    public void update() {
        checkMatrixStructure();
        update(id, matrix.getColumnStart(), matrix.getRowIndices(), matrix.getValues(), matrix.getRgrowthThreshold());
    }

    @Override
    public void solve(double[] b) {
        solve(id, b, false);
    }

    @Override
    public void solveTransposed(double[] b) {
        solve(id, b, true);
    }

    @Override
    public void solve(DenseMatrix b) {
        solve2(id, b.getRowCount(), b.getColumnCount(), b.getBuffer(), false);
    }

    @Override
    public void solveTransposed(DenseMatrix b) {
        solve2(id, b.getRowCount(), b.getColumnCount(), b.getBuffer(), true);
    }

    @Override
    public void close() {
        release(id);
    }
}
