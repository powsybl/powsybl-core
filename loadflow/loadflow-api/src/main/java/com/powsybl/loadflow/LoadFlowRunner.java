/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.loadflow;

import com.powsybl.commons.Versionable;
import com.powsybl.computation.ComputationManager;
import com.powsybl.computation.local.LocalComputationManager;
import com.powsybl.iidm.network.Network;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * Loadflow runner API. This class is responsible for providing convenient methods on top of {@link LoadFlowProvider}:
 * several variants of synchronous and asynchronous run with default parameters.
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class LoadFlowRunner implements Versionable {

    private final LoadFlowProvider provider;

    public LoadFlowRunner(LoadFlowProvider provider) {
        this.provider = Objects.requireNonNull(provider);
    }

    public CompletableFuture<LoadFlowResult> runAsync(Network network, String workingStateId, ComputationManager computationManager, LoadFlowParameters parameters) {
        Objects.requireNonNull(workingStateId);
        Objects.requireNonNull(parameters);
        return provider.run(network, computationManager, workingStateId, parameters);
    }

    public CompletableFuture<LoadFlowResult> runAsync(Network network, ComputationManager computationManager, LoadFlowParameters parameters) {
        return runAsync(network, network.getVariantManager().getWorkingVariantId(), computationManager, parameters);
    }

    public CompletableFuture<LoadFlowResult> runAsync(Network network, LoadFlowParameters parameters) {
        return runAsync(network, LocalComputationManager.getDefault(), parameters);
    }

    public CompletableFuture<LoadFlowResult> runAsync(Network network) {
        return runAsync(network, LoadFlowParameters.load());
    }

    public LoadFlowResult run(Network network, String workingStateId, ComputationManager computationManager, LoadFlowParameters parameters) {
        Objects.requireNonNull(workingStateId);
        Objects.requireNonNull(parameters);
        return provider.run(network, computationManager, workingStateId, parameters).join();
    }

    public LoadFlowResult run(Network network, ComputationManager computationManager, LoadFlowParameters parameters) {
        return run(network, network.getVariantManager().getWorkingVariantId(), computationManager, parameters);
    }

    public LoadFlowResult run(Network network, LoadFlowParameters parameters) {
        return run(network, LocalComputationManager.getDefault(), parameters);
    }

    public LoadFlowResult run(Network network) {
        return run(network, LoadFlowParameters.load());
    }

    @Override
    public String getName() {
        return provider.getName();
    }

    @Override
    public String getVersion() {
        return provider.getVersion();
    }
}
