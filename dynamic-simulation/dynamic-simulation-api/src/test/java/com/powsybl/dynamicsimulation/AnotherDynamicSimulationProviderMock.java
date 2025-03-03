/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.dynamicsimulation;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.commons.parameters.Parameter;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.network.Network;

/**
 * @author Marcos de Miguel {@literal <demiguelm at aia.es>}
 */
public class AnotherDynamicSimulationProviderMock implements DynamicSimulationProvider {

    @Override
    public String getName() {
        return "AnotherDynamicSimulationMock";
    }

    @Override
    public String getVersion() {
        return "1.0";
    }

    @Override
    public CompletableFuture<DynamicSimulationResult> run(Network network, DynamicModelsSupplier dynamicModelsSupplier, EventModelsSupplier eventModelsSupplier,
                                                          OutputVariablesSupplier outputVariablesSupplier, String workingVariantId, ComputationManager computationManager, DynamicSimulationParameters parameters, ReportNode reportNode) {
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
        // Do nothing
    }

    @Override
    public List<Parameter> getSpecificParameters() {
        return List.of();
    }
}
