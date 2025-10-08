/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.loadflow;

import com.powsybl.commons.report.ReportNode;
import com.powsybl.computation.ComputationManager;
import com.powsybl.computation.local.LocalComputationManager;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * Parameters used in {@link LoadFlow#run} called in {@link LoadFlow} API
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
public class LoadFlowRunParameters {

    private static final Supplier<ComputationManager> DEFAULT_COMPUTATION_MANAGER_SUPPLIER = LocalComputationManager::getDefault;
    private static final Supplier<LoadFlowParameters> DEFAULT_LOAD_FLOW_PARAMETERS_SUPPLIER = LoadFlowParameters::load;

    private LoadFlowParameters parameters;
    private ComputationManager computationManager;
    private ReportNode reportNode = ReportNode.NO_OP;

    /**
     * Returns a {@link LoadFlowRunParameters} instance with default value on each field.
     * @return the LoadFlowRunParameters instance.
     */
    public static LoadFlowRunParameters getDefault() {
        return new LoadFlowRunParameters()
            .setParameters(DEFAULT_LOAD_FLOW_PARAMETERS_SUPPLIER.get())
            .setComputationManager(DEFAULT_COMPUTATION_MANAGER_SUPPLIER.get());
    }

    /**
     * {@link LoadFlowParameters} getter<br>
     * If null, sets the field to its default value with {@link #DEFAULT_LOAD_FLOW_PARAMETERS_SUPPLIER} before returning it.
     */
    public LoadFlowParameters getLoadFlowParameters() {
        if (parameters == null) {
            setParameters(DEFAULT_LOAD_FLOW_PARAMETERS_SUPPLIER.get());
        }
        return parameters;
    }

    /**
     * {@link ComputationManager} getter<br>
     * If null, sets the field to its default value with {@link #DEFAULT_COMPUTATION_MANAGER_SUPPLIER} before returning it.
     */
    public ComputationManager getComputationManager() {
        if (computationManager == null) {
            setComputationManager(DEFAULT_COMPUTATION_MANAGER_SUPPLIER.get());
        }
        return computationManager;
    }

    public ReportNode getReportNode() {
        return reportNode;
    }

    public LoadFlowRunParameters setParameters(LoadFlowParameters parameters) {
        Objects.requireNonNull(parameters, "LoadFlowRunParameters should not be null");
        this.parameters = parameters;
        return this;
    }

    /**
     * Sets the computationManager handling command execution.
     */
    public LoadFlowRunParameters setComputationManager(ComputationManager computationManager) {
        Objects.requireNonNull(computationManager, "ComputationManager should not be null");
        this.computationManager = computationManager;
        return this;
    }

    public LoadFlowRunParameters setReportNode(ReportNode reportNode) {
        Objects.requireNonNull(reportNode, "ReportNode should not be null");
        this.reportNode = reportNode;
        return this;
    }
}
