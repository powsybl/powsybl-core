/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.math.solver;

import com.powsybl.math.matrix.SparseMatrix;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class KinsolContext {

    private static final Logger LOGGER = LoggerFactory.getLogger(KinsolContext.class);

    private final double[] x;

    private final SparseMatrix j;

    private final Kinsol.FunctionUpdater functionUpdater;

    private final Kinsol.JacobianUpdater jacobianUpdater;

    private final Level logLevel;

    public KinsolContext(double[] x, SparseMatrix j, Kinsol.FunctionUpdater functionUpdater, Kinsol.JacobianUpdater jacobianUpdater,
                         Level logLevel) {
        this.x = Objects.requireNonNull(x);
        this.j = Objects.requireNonNull(j);
        this.functionUpdater = Objects.requireNonNull(functionUpdater);
        this.jacobianUpdater = Objects.requireNonNull(jacobianUpdater);
        this.logLevel = Objects.requireNonNull(logLevel);
    }

    public void logError(int errorCode, String module, String function, String message) {
        LOGGER.error("KinSol: code={}, module='{}', function='{}', message='{}'",
                errorCode, module, function, message);
    }

    public void logInfo(String module, String function, String message) {
        LOGGER.atLevel(logLevel).log("KinSol: module='{}', function='{}', message='{}'",
                module, function, message);
    }

    public void updateFunc(double[] f) {
        functionUpdater.update(x, f);
    }

    public void updateJac() {
        jacobianUpdater.update(x, j);
    }
}
