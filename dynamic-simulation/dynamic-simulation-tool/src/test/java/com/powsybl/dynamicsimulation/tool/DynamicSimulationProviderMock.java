package com.powsybl.dynamicsimulation.tool;

import com.google.auto.service.AutoService;
import com.powsybl.commons.reporter.Reporter;
import com.powsybl.computation.ComputationManager;
import com.powsybl.dynamicsimulation.*;
import com.powsybl.iidm.network.Network;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;

@AutoService(DynamicSimulationProvider.class)
public class DynamicSimulationProviderMock implements DynamicSimulationProvider {

    @Override
    public CompletableFuture<DynamicSimulationResult> run(Network network, DynamicModelsSupplier dynamicModelsSupplier, EventModelsSupplier eventModelsSupplier, CurvesSupplier curvesSupplier, String workingVariantId, ComputationManager computationManager, DynamicSimulationParameters parameters, Reporter reporter) {
        return CompletableFuture.completedFuture(new DynamicSimulationResultImpl(true, null, Collections.emptyMap(), DynamicSimulationResult.emptyTimeLine()));
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
