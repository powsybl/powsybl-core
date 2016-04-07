/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.dymola;

import com.google.common.collect.ImmutableMap;
import eu.itesla_project.computation.ComputationManager;
import eu.itesla_project.iidm.network.Network;
import eu.itesla_project.modules.ddb.DynamicDatabaseClient;
import eu.itesla_project.modules.ddb.DynamicDatabaseClientFactory;
import eu.itesla_project.modules.simulation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public class DymolaStabilization implements Stabilization {

    private static final Logger LOGGER = LoggerFactory.getLogger(DymolaStabilization.class);

    private final Network network;

    private final ComputationManager computationManager;

    private final DynamicDatabaseClient ddbClient;

    private final int priority;


    public DymolaStabilization(Network network, ComputationManager computationManager, int priority, DynamicDatabaseClientFactory ddbClientFactory) {
        Objects.requireNonNull(network, "network is null");
        Objects.requireNonNull(computationManager, "computation manager is null");
        Objects.requireNonNull(ddbClientFactory, "dynamic database client factory is null");
        this.network = network;
        this.computationManager = computationManager;
        this.priority = priority;
        this.ddbClient = ddbClientFactory.create(false);
    }

    @Override
    public void init(SimulationParameters parameters, Map<String, Object> context) throws Exception {

    }

    @Override
    public StabilizationResult run()  {
        LOGGER.info("Running Dymola stabilization");
        String baseStateId = network.getStateManager().getWorkingStateId();
        DymolaState state = new DymolaState(baseStateId);
        return new DymolaStabilizationResult(state);
    }

    @Override
    public CompletableFuture<StabilizationResult> runAsync(String workingStateId) {
        return CompletableFuture.supplyAsync(() -> {
            network.getStateManager().setWorkingState(workingStateId);
            return run();
        });
    }

    @Override
    public String getName() {
        return DymolaUtil.PRODUCT_NAME;
    }

    @Override
    public String getVersion() {
        return DymolaUtil.VERSION;
    }
}
