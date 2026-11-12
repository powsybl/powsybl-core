/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.dynamicsimulation;

import com.powsybl.commons.report.ReportNode;
import com.powsybl.computation.ComputationManager;
import com.powsybl.computation.local.LocalComputationManager;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * Parameters used in {@link DynamicSimulation#run} called in {@link DynamicSimulation} API
 * @author Laurent Issertial {@literal <laurent.issertial at rte-france.com>}
 */
public class DynamicSimulationRunParameters {

    private static final Supplier<ComputationManager> DEFAULT_COMPUTATION_MANAGER_SUPPLIER = LocalComputationManager::getDefault;
    private static final Supplier<DynamicSimulationParameters> DEFAULT_LOAD_FLOW_PARAMETERS_SUPPLIER = DynamicSimulationParameters::load;

    private EventModelsSupplier eventModelsSupplier = EventModelsSupplier.empty();
    private OutputVariablesSupplier outputVariablesSupplier = OutputVariablesSupplier.empty();
    private DynamicSimulationParameters parameters;
    private ComputationManager computationManager;
    private ReportNode reportNode = ReportNode.NO_OP;

    /**
     * Returns a {@link DynamicSimulationRunParameters} instance with default value on each field.
     * @return the DynamicSimulationRunParameters instance.
     */
    public static DynamicSimulationRunParameters getDefault() {
        return new DynamicSimulationRunParameters()
            .setParameters(DEFAULT_LOAD_FLOW_PARAMETERS_SUPPLIER.get())
            .setComputationManager(DEFAULT_COMPUTATION_MANAGER_SUPPLIER.get());
    }

    public EventModelsSupplier getEventModelsSupplier() {
        return eventModelsSupplier;
    }

    public OutputVariablesSupplier getOutputVariablesSupplier() {
        return outputVariablesSupplier;
    }

    /**
     * {@link DynamicSimulationParameters} getter<br>
     * If null, sets the field to its default value with {@link #DEFAULT_LOAD_FLOW_PARAMETERS_SUPPLIER} before returning it.
     */
    public DynamicSimulationParameters getDynamicSimulationParameters() {
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

    public DynamicSimulationRunParameters setEventModelsSupplier(EventModelsSupplier eventModelsSupplier) {
        Objects.requireNonNull(eventModelsSupplier, "EventModelsSupplier should not be null");
        this.eventModelsSupplier = eventModelsSupplier;
        return this;
    }

    public DynamicSimulationRunParameters setOutputVariablesSupplier(OutputVariablesSupplier outputVariablesSupplier) {
        Objects.requireNonNull(outputVariablesSupplier, "OutputVariablesSupplier should not be null");
        this.outputVariablesSupplier = outputVariablesSupplier;
        return this;
    }

    public DynamicSimulationRunParameters setParameters(DynamicSimulationParameters parameters) {
        Objects.requireNonNull(parameters, "DynamicSimulationRunParameters should not be null");
        this.parameters = parameters;
        return this;
    }

    public DynamicSimulationRunParameters setComputationManager(ComputationManager computationManager) {
        Objects.requireNonNull(computationManager, "ComputationManager should not be null");
        this.computationManager = computationManager;
        return this;
    }

    public DynamicSimulationRunParameters setReportNode(ReportNode reportNode) {
        Objects.requireNonNull(reportNode, "ReportNode should not be null");
        this.reportNode = reportNode;
        return this;
    }
}
