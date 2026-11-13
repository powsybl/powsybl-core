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
 * Parameters of one {@link StateEstimation} invocation, as opposed to
 * {@link StateEstimationParameters}, which describe the estimation itself.
 *
 * @author Šime Pavlić {@literal <sime.pavlic at kickstage.com>}
 */
public class StateEstimationRunParameters {

    private static final Supplier<ComputationManager> DEFAULT_COMPUTATION_MANAGER_SUPPLIER = LocalComputationManager::getDefault;
    private static final Supplier<StateEstimationParameters> DEFAULT_PARAMETERS_SUPPLIER = StateEstimationParameters::load;

    private StateEstimationParameters parameters;
    private ComputationManager computationManager;
    private ReportNode reportNode = ReportNode.NO_OP;

    /** Run parameters holding the configured estimation settings and the default computation manager. */
    public static StateEstimationRunParameters getDefault() {
        return new StateEstimationRunParameters()
                .setParameters(DEFAULT_PARAMETERS_SUPPLIER.get())
                .setComputationManager(DEFAULT_COMPUTATION_MANAGER_SUPPLIER.get());
    }

    /** The estimation settings, read from the platform configuration on first use if none were set. */
    public StateEstimationParameters getStateEstimationParameters() {
        if (parameters == null) {
            setParameters(DEFAULT_PARAMETERS_SUPPLIER.get());
        }
        return parameters;
    }

    /** The computation manager, defaulting to the local one on first use if none was set. */
    public ComputationManager getComputationManager() {
        if (computationManager == null) {
            setComputationManager(DEFAULT_COMPUTATION_MANAGER_SUPPLIER.get());
        }
        return computationManager;
    }

    /** Where the implementation writes its functional logs. {@link ReportNode#NO_OP} by default. */
    public ReportNode getReportNode() {
        return reportNode;
    }

    /**
     * Sets the estimation settings.
     *
     * @param parameters the estimation settings
     * @return this, so that calls can be chained
     */
    public StateEstimationRunParameters setParameters(StateEstimationParameters parameters) {
        this.parameters = Objects.requireNonNull(parameters, "StateEstimationParameters should not be null");
        return this;
    }

    /**
     * Sets the computation manager used for any external program execution.
     *
     * @param computationManager the computation manager used for any external program execution
     * @return this, so that calls can be chained
     */
    public StateEstimationRunParameters setComputationManager(ComputationManager computationManager) {
        this.computationManager = Objects.requireNonNull(computationManager, "ComputationManager should not be null");
        return this;
    }

    /**
     * Sets where the implementation writes its functional logs.
     *
     * @param reportNode where the implementation writes its functional logs
     * @return this, so that calls can be chained
     */
    public StateEstimationRunParameters setReportNode(ReportNode reportNode) {
        this.reportNode = Objects.requireNonNull(reportNode, "ReportNode should not be null");
        return this;
    }
}
