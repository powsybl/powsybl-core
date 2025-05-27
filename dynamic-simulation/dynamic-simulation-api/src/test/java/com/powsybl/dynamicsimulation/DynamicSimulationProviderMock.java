/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.dynamicsimulation;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;

import com.google.auto.service.AutoService;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.network.Network;

/**
 * @author Marcos de Miguel {@literal <demiguelm at aia.es>}
 */
@AutoService(DynamicSimulationProvider.class)
public class DynamicSimulationProviderMock implements DynamicSimulationProvider {

    @Override
    public String getName() {
        return "DynamicSimulationMock";
    }

    @Override
    public String getVersion() {
        return "1.0";
    }

    @Override
    public CompletableFuture<DynamicSimulationResult> run(Network network, DynamicModelsSupplier dynamicModelsSupplier, EventModelsSupplier eventModelsSupplier, OutputVariablesSupplier outputVariablesSupplier,
                                                        String workingVariantId, ComputationManager computationManager, DynamicSimulationParameters parameters, ReportNode reportNode) {
        return CompletableFuture.completedFuture(DynamicSimulationResultImpl.createSuccessResult(Collections.emptyMap(), DynamicSimulationResult.emptyTimeLine()));
    }

    @Override
    public CompletableFuture<DynamicSimulationResult> run(Network network, DynamicModelsSupplier dynamicModelsSupplier, EventModelsSupplier eventModelsSupplier, OutputVariablesSupplier outputVariablesSupplier, String workingVariantId, ComputationManager computationManager, DynamicSimulationParameters parameters, ReportNode reportNode, String debugDir) {
        return run(network, dynamicModelsSupplier, eventModelsSupplier, outputVariablesSupplier, workingVariantId, computationManager, parameters, reportNode);
    }

}
