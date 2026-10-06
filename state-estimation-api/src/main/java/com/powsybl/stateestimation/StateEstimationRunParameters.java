/**
 * Copyright (c) 2026, Elia Group (https://www.eliagroup.eu)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.stateestimation;

import com.powsybl.commons.report.ReportNode;
import com.powsybl.computation.ComputationManager;
import com.powsybl.computation.local.LocalComputationManager;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * What belongs to one invocation of {@link StateEstimation#run} instead of to the estimation
 * itself.
 *
 * @author Šime Pavlić {@literal <sime.pavlic at kickstage.com>}
 */
public class StateEstimationRunParameters {

    private static final Supplier<ComputationManager> DEFAULT_COMPUTATION_MANAGER_SUPPLIER = LocalComputationManager::getDefault;
    private static final Supplier<StateEstimationParameters> DEFAULT_PARAMETERS_SUPPLIER = StateEstimationParameters::load;

    private StateEstimationParameters parameters;
    private ComputationManager computationManager;
    private ReportNode reportNode = ReportNode.NO_OP;

    public static StateEstimationRunParameters getDefault() {
        return new StateEstimationRunParameters()
                .setParameters(DEFAULT_PARAMETERS_SUPPLIER.get())
                .setComputationManager(DEFAULT_COMPUTATION_MANAGER_SUPPLIER.get());
    }

    public StateEstimationParameters getStateEstimationParameters() {
        if (parameters == null) {
            setParameters(DEFAULT_PARAMETERS_SUPPLIER.get());
        }
        return parameters;
    }

    public ComputationManager getComputationManager() {
        if (computationManager == null) {
            setComputationManager(DEFAULT_COMPUTATION_MANAGER_SUPPLIER.get());
        }
        return computationManager;
    }

    public ReportNode getReportNode() {
        return reportNode;
    }

    public StateEstimationRunParameters setParameters(StateEstimationParameters parameters) {
        this.parameters = Objects.requireNonNull(parameters, "StateEstimationParameters should not be null");
        return this;
    }

    public StateEstimationRunParameters setComputationManager(ComputationManager computationManager) {
        this.computationManager = Objects.requireNonNull(computationManager, "ComputationManager should not be null");
        return this;
    }

    public StateEstimationRunParameters setReportNode(ReportNode reportNode) {
        this.reportNode = Objects.requireNonNull(reportNode, "ReportNode should not be null");
        return this;
    }
}
