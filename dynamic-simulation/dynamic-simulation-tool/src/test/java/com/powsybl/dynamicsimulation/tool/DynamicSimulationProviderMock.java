package com.powsybl.dynamicsimulation.tool;

import com.google.auto.service.AutoService;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.computation.ComputationManager;
import com.powsybl.dynamicsimulation.*;
import com.powsybl.iidm.network.Network;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;

@AutoService(DynamicSimulationProvider.class)
public class DynamicSimulationProviderMock implements DynamicSimulationProvider {

    @Override
    public CompletableFuture<DynamicSimulationResult> run(Network network, DynamicModelsSupplier dynamicModelsSupplier, EventModelsSupplier eventModelsSupplier, OutputVariablesSupplier outputVariablesSupplier, String workingVariantId, ComputationManager computationManager, DynamicSimulationParameters parameters, ReportNode reportNode) {
        return CompletableFuture.completedFuture(DynamicSimulationResultImpl.createSuccessResult(Collections.emptyMap(), DynamicSimulationResult.emptyTimeLine()));
    }

    @Override
    public CompletableFuture<DynamicSimulationResult> run(Network network, DynamicModelsSupplier dynamicModelsSupplier, EventModelsSupplier eventModelsSupplier, OutputVariablesSupplier outputVariablesSupplier, String workingVariantId, ComputationManager computationManager, DynamicSimulationParameters parameters, ReportNode reportNode, String debugDir) {
        return run(network, dynamicModelsSupplier, eventModelsSupplier, outputVariablesSupplier, workingVariantId, computationManager, parameters, reportNode);
    }

    @Override
    public String getName() {
        return "Mock";
    }

    @Override
    public String getVersion() {
        return null;
    }
}
