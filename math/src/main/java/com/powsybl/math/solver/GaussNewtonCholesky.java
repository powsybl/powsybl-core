/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.math.solver;

import com.powsybl.math.AbstractMathNative;

import java.nio.DoubleBuffer;
import java.nio.IntBuffer;

/**
 * Sparse Cholesky decomposition using CHOLMOD for weighted least squares problems.
 * Used in Gauss-Newton algorithm to solve normal equations: (H'WH)Δx = H'Wr
 * <p>
 * <b>W must be diagonal</b>: {@link #init} takes a single weight per
 * observation, so what is minimised is {@code (r - HΔx)' W (r - HΔx)} with
 * {@code W = diag(w)}. A problem stated with a full covariance matrix Σ cannot
 * be passed as-is; whiten it first: factor {@code Σ = LL'}, solve
 * {@code L H̃ = H} and {@code L r̃ = r}, then hand over {@code H̃} and
 * {@code r̃} with unit weights. Be aware that whitening usually destroys the
 * sparsity of H, which is what this solver is built to exploit.
 * <p>
 * All state lives natively, keyed by the {@code id} passed to every method, so
 * instances of this class are stateless and interchangeable. A given id is
 * <em>not</em> thread-safe: its factor and cached Ht are mutated by
 * factorize/solve, so confine each id to one thread (or synchronize on it).
 * Distinct ids are independent and may be used concurrently. Always
 * {@link #release} an id when done - contexts are never reclaimed otherwise.
 *
 * @author Gautier Bureau {@literal <gautier.bureau at rte-france.com>}
 */
public class GaussNewtonCholesky extends AbstractMathNative {

    /**
     * Initialize the decomposition with the normal equations matrix structure.
     * <p>
     * {@code m} and {@code n} fix the problem size for this id: every later
     * {@link #factorize}, {@link #solve}, {@link #factorizeLM} or
     * {@link #solveLM} call must pass the same dimensions, otherwise it throws.
     * To solve a differently-sized problem, {@link #release} the id and init it
     * again.
     *
     * @param m number of rows in the Jacobian (observations)
     * @param n number of columns in the Jacobian (parameters)
     * @param sqrtWeights the elementwise <em>square root</em> of the diagonal
     *        weights W, not the weights themselves (length m). The native side
     *        squares them to recover W, so pass {@code Math.sqrt(w[i])}. Passing
     *        raw weights silently fits with W squared.
     */
    public native void init(String id, int m, int n, double[] sqrtWeights);

    /**
     * Assemble C = H'WH from Ht and factorize it. Caches the factor and the Ht
     * values so subsequent {@link #solveFactorized} calls can reuse them with
     * different residual vectors - useful when the same C must be solved against
     * several right-hand sides (factor once, solve repeatedly) or when only the
     * rank of C is needed (factor once, inspect the return value).
     *
     * @return n if the factorization succeeded (system is full-rank per the
     *         solver's pivot heuristic), or the column index k &lt; n where
     *         rank-deficiency was first detected. Heuristic: a well-scaled
     *         near-singular matrix can still report n, and a poorly-scaled
     *         full-rank matrix can report less. For guaranteed rank, use
     *         pivoted QR or SVD.
     */
    public native int factorize(String id, int m, int n,
                                IntBuffer ap, IntBuffer ai, DoubleBuffer ax);

    /**
     * Solve C Δx = H'W r using the factor cached by the most recent
     * {@link #factorize} call. May be called multiple times with different r.
     * Throws if no factor is cached or if the last factorize was rank-deficient.
     */
    public native void solveFactorized(String id, DoubleBuffer r, DoubleBuffer result);

    /**
     * Solve C x = b directly using the factor cached by the most recent
     * {@link #factorize} call. Unlike {@link #solveFactorized}, the right-hand
     * side is taken as-is from the {@code b} buffer (length n) and no
     * H'W r assembly is performed.
     * <p>
     * Useful for computing individual columns of {@code C^-1}: solving with
     * b = e_j (the j-th unit vector) returns column j of {@code C^-1}. Since
     * {@code C = H'WH}, that inverse is the parameter covariance of a weighted
     * least-squares fit (with W the inverse data covariance): the diagonal
     * entry {@code (C^-1)[j][j]} is the variance of parameter j and the rest of
     * the column its covariances with the other parameters - i.e. it gives the
     * parameter uncertainty of the estimated solution.
     * <p>
     * Throws if no factor is cached or if the last factorize was rank-deficient.
     */
    public native void solveFactorizedRaw(String id, DoubleBuffer b, DoubleBuffer result);

    /**
     * Convenience: factorize + solveFactorized in one call. Throws if the
     * factorization is rank-deficient. Equivalent to:
     * <pre>
     *   if (factorize(id, m, n, ap, ai, ax) != n) throw ...;
     *   solveFactorized(id, r, result);
     * </pre>
     *
     * All buffer arguments MUST be direct (ByteBuffer.allocateDirect + native order).
     *
     * @param r residual vector (size m)
     * @param m number of rows in Jacobian
     * @param n number of columns in Jacobian
     * @param ap column pointers for transpose of Jacobian Ht (size m+1)
     * @param ai row indices for Ht (size nnz)
     * @param ax values for Ht (size nnz)
     * @param result output for delta_x (size n)
     */
    public native void solve(String id, DoubleBuffer r, int m, int n,
                             IntBuffer ap, IntBuffer ai, DoubleBuffer ax, DoubleBuffer result);

    /** LM damping mode for {@link #factorizeLM}, {@link #refactorizeLM}, {@link #solveLM}. */
    public static final int LM_MODE_IDENTITY = 0;
    /** LM damping mode for {@link #factorizeLM}, {@link #refactorizeLM}, {@link #solveLM}. */
    public static final int LM_MODE_MARQUARDT = 1;

    /**
     * Levenberg-Marquardt factorize: assemble C = H'WH, add the LM damping
     * lambda*D to its diagonal, then factorize. Caches the factor and Ht
     * values so subsequent {@link #solveFactorized} or {@link #refactorizeLM}
     * calls can reuse them. With lambda == 0 this is equivalent to
     * {@link #factorize}.
     *
     * @param mode {@link #LM_MODE_IDENTITY} (D = I, classic Levenberg) or
     *             {@link #LM_MODE_MARQUARDT} (D = diag(H'WH), scale-invariant).
     * @return n if the damped factor is full rank, otherwise the column where
     *         rank deficiency was detected (same convention as
     *         {@link #factorize}).
     */
    public native int factorizeLM(String id, int m, int n,
                                  IntBuffer ap, IntBuffer ai, DoubleBuffer ax,
                                  double lambda, int mode);

    /**
     * Re-factorize the cached normal-equations matrix with a new damping. Useful in the
     * LM outer loop after a step is rejected: the Ht and contribution map are
     * unchanged, so we just rewrite the diagonals and factor again - no need
     * for the caller to pass Ht. Requires a prior {@link #factorize} or
     * {@link #factorizeLM} call to populate the pattern.
     *
     * @return same convention as {@link #factorizeLM}.
     */
    public native int refactorizeLM(String id, double lambda, int mode);

    /**
     * Convenience: factorizeLM + solveFactorized in one call. Throws if the
     * damped factor is still rank-deficient (typically a signal to increase
     * lambda).
     */
    public native void solveLM(String id, DoubleBuffer r, int m, int n,
                               IntBuffer ap, IntBuffer ai, DoubleBuffer ax,
                               double lambda, int mode, DoubleBuffer result);

    /**
     * Release all native resources.
     */
    public native void release(String id);

}
