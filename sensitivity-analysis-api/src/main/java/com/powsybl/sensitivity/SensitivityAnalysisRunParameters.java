/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sensitivity;

import com.powsybl.action.Action;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.computation.ComputationManager;
import com.powsybl.computation.local.LocalComputationManager;
import com.powsybl.contingency.Contingency;
import com.powsybl.contingency.strategy.OperatorStrategy;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
public class SensitivityAnalysisRunParameters {

    private static final Supplier<ComputationManager> DEFAULT_COMPUTATION_MANAGER_SUPPLIER = LocalComputationManager::getDefault;
    private static final Supplier<SensitivityAnalysisParameters> DEFAULT_SENSITIVITY_ANALYSIS_PARAMETERS_SUPPLIER = SensitivityAnalysisParameters::load;

    private ComputationManager computationManager;
    private List<Contingency> contingencies = new ArrayList<>();
    private SensitivityAnalysisParameters parameters;
    private ReportNode reportNode = ReportNode.NO_OP;
    private List<SensitivityVariableSet> variableSets = new ArrayList<>();
    private List<OperatorStrategy> operatorStrategies = new ArrayList<>();
    private List<Action> actions = new ArrayList<>();

    /**
     * Returns a {@link SensitivityAnalysisRunParameters} instance with default value on each field.
     * @return the SensitivityAnalysisRunParameters instance.
     */
    public static SensitivityAnalysisRunParameters getDefault() {
        return new SensitivityAnalysisRunParameters()
            .setParameters(DEFAULT_SENSITIVITY_ANALYSIS_PARAMETERS_SUPPLIER.get())
            .setComputationManager(DEFAULT_COMPUTATION_MANAGER_SUPPLIER.get());
    }

    /**
     * {@link SensitivityAnalysisParameters} getter<br>
     * If null, sets the field to its default value with {@link #DEFAULT_SENSITIVITY_ANALYSIS_PARAMETERS_SUPPLIER} before returning it.
     */
    public SensitivityAnalysisParameters getSensitivityAnalysisParameters() {
        if (parameters == null) {
            setParameters(DEFAULT_SENSITIVITY_ANALYSIS_PARAMETERS_SUPPLIER.get());
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

    public List<Contingency> getContingencies() {
        return contingencies;
    }

    public ReportNode getReportNode() {
        return reportNode;
    }

    public List<SensitivityVariableSet> getVariableSets() {
        return variableSets;
    }

    public List<OperatorStrategy> getOperatorStrategies() {
        return operatorStrategies;
    }

    public List<Action> getActions() {
        return actions;
    }

    public SensitivityAnalysisRunParameters setParameters(SensitivityAnalysisParameters parameters) {
        Objects.requireNonNull(parameters, "LoadFlowRunParameters should not be null");
        this.parameters = parameters;
        return this;
    }

    /**
     * Sets the computationManager handling command execution.
     */
    public SensitivityAnalysisRunParameters setComputationManager(ComputationManager computationManager) {
        Objects.requireNonNull(computationManager, "ComputationManager should not be null");
        this.computationManager = computationManager;
        return this;
    }

    public SensitivityAnalysisRunParameters setContingencies(List<Contingency> contingencies) {
        Objects.requireNonNull(contingencies, "Contingencies should not be null");
        this.contingencies = contingencies;
        return this;
    }

    public SensitivityAnalysisRunParameters setReportNode(ReportNode reportNode) {
        Objects.requireNonNull(reportNode, "ReportNode should not be null");
        this.reportNode = reportNode;
        return this;
    }

    public SensitivityAnalysisRunParameters setVariableSets(List<SensitivityVariableSet> variableSets) {
        Objects.requireNonNull(variableSets, "VariableSets should not be null");
        this.variableSets = variableSets;
        return this;
    }

    public SensitivityAnalysisRunParameters setOperatorStrategies(List<OperatorStrategy> operatorStrategies) {
        this.operatorStrategies = Objects.requireNonNull(operatorStrategies, "operatorStrategies should not be null");
        return this;
    }

    public SensitivityAnalysisRunParameters setActions(List<Action> actions) {
        this.actions = Objects.requireNonNull(actions, "actions should not be null");
        return this;
    }
}
