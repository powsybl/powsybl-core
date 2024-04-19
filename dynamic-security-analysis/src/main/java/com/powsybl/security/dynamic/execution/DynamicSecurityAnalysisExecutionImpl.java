/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.dynamic.execution;

import com.powsybl.commons.report.ReportNode;
import com.powsybl.computation.ComputationManager;
import com.powsybl.dynamicsimulation.DynamicModelsSupplier;
import com.powsybl.dynamicsimulation.groovy.DynamicSimulationSupplierFactory;
import com.powsybl.security.SecurityAnalysisReport;
import com.powsybl.security.dynamic.DynamicSecurityAnalysis;
import com.powsybl.security.dynamic.DynamicSecurityAnalysisInput;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

import static java.util.Objects.requireNonNull;

/**
 * @author Laurent Issertial {@literal <laurent.issertial at rte-france.com>}
 */
public class DynamicSecurityAnalysisExecutionImpl implements DynamicSecurityAnalysisExecution {

    private final String providerName;
    private final DynamicSecurityAnalysisInputBuildStrategy inputBuildStrategy;

    public DynamicSecurityAnalysisExecutionImpl() {
        this(null, DynamicSecurityAnalysisExecutionImpl::buildDefault);
    }

    /**
     * The execution will use the {@literal providerName} implementation.
     */
    public DynamicSecurityAnalysisExecutionImpl(String providerName) {
        this(providerName, DynamicSecurityAnalysisExecutionImpl::buildDefault);
    }

    public DynamicSecurityAnalysisExecutionImpl(String providerName, DynamicSecurityAnalysisInputBuildStrategy inputBuildStrategy) {
        this.providerName = providerName;
        this.inputBuildStrategy = requireNonNull(inputBuildStrategy);
    }

    private static DynamicSecurityAnalysisInput buildDefault(DynamicSecurityAnalysisExecutionInput executionInput, String providerName) {
        DynamicModelsSupplier dynamicModelsSupplier;
        try (InputStream is = executionInput.getDynamicModelsSource().openBufferedStream()) {
            dynamicModelsSupplier = DynamicSimulationSupplierFactory.createDynamicModelsSupplier(is, providerName);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return new DynamicSecurityAnalysisInput(executionInput.getNetworkVariant(), dynamicModelsSupplier);
    }

    @Override
    public CompletableFuture<SecurityAnalysisReport> execute(ComputationManager computationManager, DynamicSecurityAnalysisExecutionInput data) {
        DynamicSecurityAnalysis.Runner runner = DynamicSecurityAnalysis.find(providerName);
        DynamicSecurityAnalysisInput input = inputBuildStrategy.buildFrom(data, runner.getName());
        return runner.runAsync(input.getNetworkVariant().getNetwork(),
                input.getDynamicModels(),
                input.getEventModels(),
                input.getNetworkVariant().getVariantId(),
                input.getContingenciesProvider(),
                input.getParameters(),
                computationManager,
                input.getFilter(),
                input.getLimitViolationDetector(),
                new ArrayList<>(input.getInterceptors()),
                data.getOperatorStrategies(),
                data.getActions(),
                data.getMonitors(),
                ReportNode.NO_OP);
    }
}
