/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.loadflow;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.Versionable;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.commons.util.ServiceLoaderCache;
import com.powsybl.computation.ComputationManager;
import com.powsybl.computation.local.LocalComputationManager;
import com.powsybl.iidm.network.Network;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class LoadFlow implements Versionable {

    private static final Supplier<List<LoadFlowProvider>> PROVIDERS_SUPPLIERS
            = Suppliers.memoize(() -> new ServiceLoaderCache<>(LoadFlowProvider.class).getServices());

    private final Network network;

    private final LoadFlowProvider provider;

    private final PlatformConfig platformConfig;

    public LoadFlow(Network network, LoadFlowProvider provider, PlatformConfig platformConfig) {
        this.network = Objects.requireNonNull(network);
        this.provider = Objects.requireNonNull(provider);
        this.platformConfig = Objects.requireNonNull(platformConfig);
    }

    public static LoadFlow on(Network network) {
        return on(network, PROVIDERS_SUPPLIERS.get(), PlatformConfig.defaultConfig());
    }

    static LoadFlow on(Network network, List<LoadFlowProvider> providers, PlatformConfig platformConfig) {
        if (providers.isEmpty()) {
            throw new PowsyblException("No loadflow providers found");
        }
        LoadFlowProvider provider;
        if (providers.size() == 1) {
            provider = providers.get(0);
        } else {
            // try to find a loadflow config to known which implementation has to be chosen
            String defaultLoadFlow = platformConfig.getOptionalModuleConfig("load-flow")
                    .flatMap(mc -> mc.getOptionalStringProperty("default"))
                    .orElseThrow(() -> new PowsyblException("Loadflow configuration not found"));
            provider = providers.stream()
                    .filter(p -> p.getName().equals(defaultLoadFlow))
                    .findFirst()
                    .orElseThrow(() -> new PowsyblException("Loadflow "  + defaultLoadFlow + " found"));
        }
        return new LoadFlow(network, provider, platformConfig);
    }

    public CompletableFuture<LoadFlowResult> runAsync(String workingStateId, ComputationManager computationManager, LoadFlowParameters parameters) {
        Objects.requireNonNull(workingStateId);
        Objects.requireNonNull(parameters);
        return provider.run(network, computationManager, workingStateId, parameters);
    }

    public CompletableFuture<LoadFlowResult> runAsync(ComputationManager computationManager, LoadFlowParameters parameters) {
        return runAsync(network.getVariantManager().getWorkingVariantId(), computationManager, parameters);
    }

    public CompletableFuture<LoadFlowResult> runAsync(LoadFlowParameters parameters) {
        return runAsync(LocalComputationManager.getDefault(), parameters);
    }

    public CompletableFuture<LoadFlowResult> runAsync() {
        return runAsync(LoadFlowParameters.load(platformConfig));
    }

    public LoadFlowResult run(String workingStateId, ComputationManager computationManager, LoadFlowParameters parameters) {
        Objects.requireNonNull(workingStateId);
        Objects.requireNonNull(parameters);
        return provider.run(network, computationManager, workingStateId, parameters).join();
    }

    public LoadFlowResult run(ComputationManager computationManager, LoadFlowParameters parameters) {
        return run(network.getVariantManager().getWorkingVariantId(), computationManager, parameters);
    }

    public LoadFlowResult run(LoadFlowParameters parameters) {
        return run(LocalComputationManager.getDefault(), parameters);
    }

    public LoadFlowResult run() {
        return run(LoadFlowParameters.load(platformConfig));
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
