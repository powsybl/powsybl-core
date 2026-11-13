/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.math.solver;

import org.junit.jupiter.api.Test;

import java.nio.DoubleBuffer;
import java.nio.IntBuffer;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

/**
 * Exercises the CHOLMOD-backed Gauss-Newton solver end to end: a small
 * weighted least-squares fit driven through the JNI handle.
 *
 * <p>The tests deliberately use the buffer API the way a real Gauss-Newton
 * outer loop should: the direct buffers are allocated <em>once</em> (dimensions
 * are stable across iterations) and only their contents are refreshed each
 * iteration via {@link NativeBuffers#copyFrom}, with the result read back via
 * {@link NativeBuffers#copyTo}. The sparsity structure of Ht is fixed, so
 * {@code ap}/{@code ai} are filled a single time before the loop.
 *
 * @author Gautier Bureau {@literal <gautier.bureau at rte-france.com>}
 */
class GaussNewtonCholeskyTest {

    /**
     * Fit a line y = a + b*x. Being linear, one Gauss-Newton step suffices, but
     * we iterate to convergence to exercise reuse of the pre-allocated buffers.
     */
    @Test
    void testLinearLeastSquares() {
        double[] xData = {0.0, 1.0, 2.0, 3.0, 4.0};
        double[] yData = {1.1, 2.9, 5.2, 6.8, 9.1}; // approximately y = 1 + 2*x with noise
        int m = xData.length;
        int n = 2; // parameters a, b
        int nnz = m * n;

        double[] params = {1.0, 1.0};

        double[] sqrtWeights = new double[m];
        for (int i = 0; i < m; i++) {
            sqrtWeights[i] = 1.0;
        }

        GaussNewtonCholesky cholesky = new GaussNewtonCholesky();
        cholesky.init("test", m, n, sqrtWeights);

        // Direct buffers allocated once and reused across every iteration.
        IntBuffer ap = NativeBuffers.allocInt(m + 1);
        IntBuffer ai = NativeBuffers.allocInt(nnz);
        DoubleBuffer ax = NativeBuffers.allocDouble(nnz);
        DoubleBuffer rBuf = NativeBuffers.allocDouble(m);
        DoubleBuffer deltaBuf = NativeBuffers.allocDouble(n);

        // Ht (n x m) sparsity is fixed: column i holds rows [a, b] = [0, 1].
        int[] apArr = new int[m + 1];
        int[] aiArr = new int[nnz];
        int idx = 0;
        for (int col = 0; col < m; col++) {
            apArr[col] = idx;
            aiArr[idx++] = 0;
            aiArr[idx++] = 1;
        }
        apArr[m] = idx;
        NativeBuffers.copyFrom(ap, apArr);
        NativeBuffers.copyFrom(ai, aiArr);

        double[] axArr = new double[nnz];
        double[] residuals = new double[m];
        double[] delta = new double[n];
        try {
            for (int iter = 0; iter < 10; iter++) {
                // Only the values change with params: residuals and Ht values.
                for (int i = 0; i < m; i++) {
                    residuals[i] = yData[i] - (params[0] + params[1] * xData[i]);
                }
                int k = 0;
                for (int col = 0; col < m; col++) {
                    axArr[k++] = 1.0;        // dr/da
                    axArr[k++] = xData[col]; // dr/db
                }
                NativeBuffers.copyFrom(rBuf, residuals);
                NativeBuffers.copyFrom(ax, axArr);

                cholesky.solve("test", rBuf, m, n, ap, ai, ax, deltaBuf);
                NativeBuffers.copyTo(deltaBuf, delta);

                double deltaNorm = 0.0;
                for (int i = 0; i < n; i++) {
                    params[i] += delta[i];
                    deltaNorm += delta[i] * delta[i];
                }
                if (Math.sqrt(deltaNorm) < 1e-6) {
                    break;
                }
            }

            assertArrayEquals(new double[]{1.02, 2.0}, params, 0.1,
                    "Parameters should fit the line y = 1 + 2*x");
        } finally {
            cholesky.release("test");
        }
    }

    /**
     * Fit the nonlinear decay model y = a * exp(b*x), which needs several
     * Gauss-Newton iterations to converge. The Jacobian values change every
     * iteration, so {@code ax} and the residual buffer are refilled in place
     * while their allocation and the fixed sparsity structure are not.
     */
    @Test
    void testNonlinearLeastSquares() {
        double[] xData = {0.0, 1.0, 2.0, 3.0, 4.0, 5.0};
        double[] yData = {5.0, 3.03, 1.84, 1.12, 0.68, 0.41}; // true model y = 5 * exp(-0.5*x)
        int m = xData.length;
        int n = 2; // parameters a, b
        int nnz = m * n;

        double[] params = {1.0, -0.1};

        double[] sqrtWeights = new double[m];
        for (int i = 0; i < m; i++) {
            sqrtWeights[i] = 1.0;
        }

        GaussNewtonCholesky cholesky = new GaussNewtonCholesky();
        cholesky.init("test", m, n, sqrtWeights);

        // Direct buffers allocated once and reused across every iteration.
        IntBuffer ap = NativeBuffers.allocInt(m + 1);
        IntBuffer ai = NativeBuffers.allocInt(nnz);
        DoubleBuffer ax = NativeBuffers.allocDouble(nnz);
        DoubleBuffer rBuf = NativeBuffers.allocDouble(m);
        DoubleBuffer deltaBuf = NativeBuffers.allocDouble(n);

        // Ht (n x m) sparsity is fixed: column i holds rows [a, b] = [0, 1].
        int[] apArr = new int[m + 1];
        int[] aiArr = new int[nnz];
        int idx = 0;
        for (int col = 0; col < m; col++) {
            apArr[col] = idx;
            aiArr[idx++] = 0;
            aiArr[idx++] = 1;
        }
        apArr[m] = idx;
        NativeBuffers.copyFrom(ap, apArr);
        NativeBuffers.copyFrom(ai, aiArr);

        double[] axArr = new double[nnz];
        double[] residuals = new double[m];
        double[] delta = new double[n];
        try {
            for (int iter = 0; iter < 20; iter++) {
                for (int i = 0; i < m; i++) {
                    residuals[i] = yData[i] - params[0] * Math.exp(params[1] * xData[i]);
                }
                int k = 0;
                for (int col = 0; col < m; col++) {
                    double expTerm = Math.exp(params[1] * xData[col]);
                    axArr[k++] = expTerm;                             // df/da
                    axArr[k++] = params[0] * xData[col] * expTerm;    // df/db
                }
                NativeBuffers.copyFrom(rBuf, residuals);
                NativeBuffers.copyFrom(ax, axArr);

                cholesky.solve("test", rBuf, m, n, ap, ai, ax, deltaBuf);
                NativeBuffers.copyTo(deltaBuf, delta);

                double deltaNorm = 0.0;
                for (int i = 0; i < n; i++) {
                    params[i] += delta[i];
                    deltaNorm += delta[i] * delta[i];
                }
                if (Math.sqrt(deltaNorm) < 1e-6) {
                    break;
                }
            }

            assertArrayEquals(new double[]{5.0, -0.5}, params, 0.1,
                    "Parameters should fit exponential decay model");
        } finally {
            cholesky.release("test");
        }
    }
}
