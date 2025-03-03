package com.powsybl.dynamicsimulation.tool;

import com.google.auto.service.AutoService;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.commons.parameters.Parameter;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.computation.ComputationManager;
import com.powsybl.dynamicsimulation.*;
import com.powsybl.iidm.network.Network;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@AutoService(DynamicSimulationProvider.class)
public class DynamicSimulationProviderMock implements DynamicSimulationProvider {

    @Override
    public CompletableFuture<DynamicSimulationResult> run(Network network, DynamicModelsSupplier dynamicModelsSupplier, EventModelsSupplier eventModelsSupplier, OutputVariablesSupplier outputVariablesSupplier, String workingVariantId, ComputationManager computationManager, DynamicSimulationParameters parameters, ReportNode reportNode) {
        return CompletableFuture.completedFuture(DynamicSimulationResultImpl.createSuccessResult(Collections.emptyMap(), DynamicSimulationResult.emptyTimeLine()));
    }

    @Override
    public Optional<Class<? extends Extension<DynamicSimulationParameters>>> getSpecificParametersClass() {
        return Optional.empty();
    }

    @Override
    public Optional<Extension<DynamicSimulationParameters>> loadSpecificParameters(PlatformConfig config) {
        return Optional.empty();
    }

    @Override
    public Optional<Extension<DynamicSimulationParameters>> loadSpecificParameters(Map<String, String> properties) {
        return Optional.empty();
    }

    @Override
    public Map<String, String> createMapFromSpecificParameters(Extension<DynamicSimulationParameters> extension) {
        return Map.of();
    }

    @Override
    public void updateSpecificParameters(Extension<DynamicSimulationParameters> extension, Map<String, String> properties) {
        // do nothing
    }

    @Override
    public List<Parameter> getSpecificParameters() {
        return List.of();
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
