/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.math.matrix;

import com.google.common.base.Stopwatch;
import org.scijava.nativelib.NativeLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Sparse matrix LU decomposition backed by NVIDIA cuDSS (GPU).
 *
 * <p>The native methods live in a separate {@code libmathcudss} library so the
 * CUDA dependency never touches the CPU-only {@code libmath}. The library is
 * loaded lazily; {@link #isAvailable()} reports whether it could be loaded, so a
 * caller can fall back to {@link SparseLUDecomposition} (KLU) when cuDSS or a GPU
 * is missing.
 *
 * <p>cuDSS factorizes the transpose of the matrix (the CSC arrays read as CSR),
 * so {@link #solveTransposed} maps to a plain cuDSS solve — the path used by the
 * Newton-Raphson load flow. cuDSS has no transpose solve, so the (unused on
 * sparse matrices) non-transposed {@link #solve} is not supported.
 *
 * @see SparseMatrix
 * @see CuDssMatrixFactory
 */
class CuDssLUDecomposition implements LUDecomposition {

    private static final Logger LOGGER = LoggerFactory.getLogger(CuDssLUDecomposition.class);

    private static final boolean AVAILABLE = load();

    private static boolean load() {
        try {
            NativeLoader.loadLibrary("mathcudss");
            return true;
        } catch (Throwable t) {
            LOGGER.debug("cuDSS native library not available: {}", t.getMessage());
            return false;
        }
    }

    /**
     * @return true if the cuDSS native library could be loaded.
     */
    static boolean isAvailable() {
        return AVAILABLE;
    }

    private final SparseMatrix matrix;

    private final String id;

    private final int valueCount;

    CuDssLUDecomposition(SparseMatrix matrix) {
        this.matrix = Objects.requireNonNull(matrix);
        if (matrix.getRowCount() != matrix.getColumnCount()) {
            throw new MatrixException("matrix is not square");
        }
        if (!AVAILABLE) {
            throw new MatrixException("cuDSS native library is not available");
        }
        this.id = UUID.randomUUID().toString();
        init(id, matrix);
        valueCount = getMatrixValueCount();
    }

    private void init(String id, SparseMatrix matrix) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        init(id, matrix.getColumnStart(), matrix.getRowIndices(), matrix.getValues());
        stopwatch.stop();
        LOGGER.debug("cuDSS LU decomposition done in {} us", stopwatch.elapsed(TimeUnit.MICROSECONDS));
    }

    private int getMatrixValueCount() {
        int[] columnStart = matrix.getColumnStart();
        return columnStart[columnStart.length - 1];
    }

    private native void init(String id, int[] ap, int[] ai, double[] ax);

    private native void release(String id);

    private native double update(String id, int[] ap, int[] ai, double[] ax, double rgrowthThreshold);

    private native void solve(String id, double[] b, boolean transpose);

    private native void solve2(String id, int m, int n, ByteBuffer b, boolean transpose);

    /**
     * Check no elements have been added since first decomposition
     */
    private void checkMatrixStructure() {
        if (getMatrixValueCount() != valueCount) {
            throw new MatrixException("Elements have been added to the sparse matrix since initial decomposition");
        }
    }

    /**
     * {@inheritDoc}
     *
     * The structure of the matrix is not supposed to have changed, only non zero values.
     * cuDSS always refactorizes (it has no incremental-update threshold), so
     * {@code allowIncrementalUpdate} is ignored.
     */
    @Override
    public void update(boolean allowIncrementalUpdate) {
        checkMatrixStructure();
        Stopwatch stopwatch = Stopwatch.createStarted();
        update(id, matrix.getColumnStart(), matrix.getRowIndices(), matrix.getValues(), 0);
        stopwatch.stop();
        LOGGER.debug("cuDSS LU decomposition updated in {} us", stopwatch.elapsed(TimeUnit.MICROSECONDS));
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
