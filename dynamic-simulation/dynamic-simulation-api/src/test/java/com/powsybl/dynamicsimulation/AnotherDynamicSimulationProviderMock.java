/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.dynamicsimulation;

import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.network.Network;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;

/**
 * @author Marcos de Miguel <demiguelm at aia.es>
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
        CurvesSupplier curvesSupplier, String workingVariantId, ComputationManager computationManager, DynamicSimulationParameters parameters) {
        return CompletableFuture.completedFuture(new DynamicSimulationResultImpl(true, null, Collections.emptyMap(), DynamicSimulationResult.emptyTimeLine()));
    }

}
