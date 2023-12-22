/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.math.solver;

import com.powsybl.math.AbstractMathNative;
import com.powsybl.math.matrix.SparseMatrix;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class Kinsol extends AbstractMathNative {

    private static final Logger LOGGER = LoggerFactory.getLogger(Kinsol.class);

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

    public native KinsolResult solve(double[] x, int[] ap, int[] ai, double[] ax, KinsolContext context,
                                     boolean transpose, int maxIters, int msbset, int msbsetsub, double fnormtol,
                                     double scsteptol, boolean lineSearch, int level);

    private static Level getLogLevel() {
        if (LOGGER.isTraceEnabled()) {
            return Level.TRACE;
        } else if (LOGGER.isDebugEnabled()) {
            return Level.DEBUG;
        } else if (LOGGER.isInfoEnabled()) {
            return Level.INFO;
        }
        return Level.WARN;
    }

    private static int getPrintLevel(Level level) {
        return switch (level) {
            case INFO -> 1;
            case DEBUG -> 2;
            case TRACE -> 3;
            default -> 0;
        };
    }

    public KinsolResult solve(double[] x, KinsolParameters parameters) {
        return solve(x, parameters, false);
    }

    public KinsolResult solveTransposed(double[] x, KinsolParameters parameters) {
        return solve(x, parameters, true);
    }

    private KinsolResult solve(double[] x, KinsolParameters parameters, boolean transpose) {
        LOGGER.info("Running Kinsol using parameters: maxIters={}, msbset={}, msbsetsub={}, fnormtol={}, scsteptol={}, lineSearch={}",
                parameters.getMaxIters(), parameters.getMsbset(), parameters.getMsbsetsub(), parameters.getFnormtol(),
                parameters.getScsteptol(), parameters.isLineSearch());
        Level logLevel = getLogLevel();
        var context = new KinsolContext(x, j, functionUpdater, jacobianUpdater, logLevel);
        return solve(x, j.getColumnStart(), j.getRowIndices(), j.getValues(), context,
                transpose, parameters.getMaxIters(), parameters.getMsbset(), parameters.getMsbsetsub(),
                parameters.getFnormtol(), parameters.getScsteptol(), parameters.isLineSearch(),
                getPrintLevel(logLevel));
    }
}
