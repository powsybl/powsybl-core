/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.math.solver;

import com.powsybl.math.MathNative;
import com.powsybl.math.matrix.SparseMatrix;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class Kinsol {

    private static final Logger LOGGER = LoggerFactory.getLogger(Kinsol.class);

    static {
        MathNative.init();
    }

    public interface FunctionUpdater {

        void update(double[] x, double[] f);
    }

    public interface JacobianUpdater {

        void update(double[] x, SparseMatrix j);
    }

    private final SparseMatrix j;

    private final FunctionUpdater functionUpdater;

    private final JacobianUpdater jacobianUpdater;

    public Kinsol(SparseMatrix j, FunctionUpdater functionUpdater, JacobianUpdater jacobianUpdater) {
        this.j = Objects.requireNonNull(j);
        this.functionUpdater = Objects.requireNonNull(functionUpdater);
        this.jacobianUpdater = Objects.requireNonNull(jacobianUpdater);
    }

    public native int solve(double[] x, int[] ap, int[] ai, double[] ax, KinsolContext context,
                            int maxIter, boolean lineSearch, int level);

    private static int getPrintLevel() {
        if (LOGGER.isTraceEnabled()) {
            return 3;
        } else if (LOGGER.isDebugEnabled()) {
            return 2;
        } else if (LOGGER.isInfoEnabled()) {
            return 1;
        }
        return 0;
    }

    public void solve(double[] x, KinsolParameters parameters) {
        var solverContext = new KinsolContext(x, j, functionUpdater, jacobianUpdater);
        solve(x, j.getColumnStart(), j.getRowIndices(), j.getValues(), solverContext,
                parameters.getMaxIterations(), parameters.isLineSearch(), getPrintLevel());
    }
}
