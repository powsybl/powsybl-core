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

    private final LoadFlowProvider provider;

    private final PlatformConfig platformConfig;

    public LoadFlow(LoadFlowProvider provider, PlatformConfig platformConfig) {
        this.provider = Objects.requireNonNull(provider);
        this.platformConfig = Objects.requireNonNull(platformConfig);
    }

    public static LoadFlow find(String name) {
        return find(name, PROVIDERS_SUPPLIERS.get(), PlatformConfig.defaultConfig());
    }

    static LoadFlow find(String name, List<LoadFlowProvider> providers, PlatformConfig platformConfig) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(providers);

        LoadFlowProvider provider = providers.stream()
                .filter(p -> p.getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new PowsyblException("Loadflow '" + name + "' not found"));

        return new LoadFlow(provider, platformConfig);
    }

    public static LoadFlow findDefault() {
        return findDefault(PROVIDERS_SUPPLIERS.get(), PlatformConfig.defaultConfig());
    }

    static LoadFlow findDefault(List<LoadFlowProvider> providers, PlatformConfig platformConfig) {
        Objects.requireNonNull(providers);
        Objects.requireNonNull(platformConfig);

        if (providers.isEmpty()) {
            throw new PowsyblException("No loadflow providers found");
        }
        String name = platformConfig.getOptionalModuleConfig("load-flow")
                .flatMap(mc -> mc.getOptionalStringProperty("default"))
                .orElse(null);
        if (providers.size() == 1) {
            LoadFlowProvider provider = providers.get(0);
            if (name != null && !provider.getName().equals(name)) {
                throw new PowsyblException("Loadflow '" + name + "' not found");
            }
            return new LoadFlow(provider, platformConfig);
        } else {
            // try to find a loadflow config to known which implementation has to be chosen
            if (name == null) {
                throw new PowsyblException("Loadflow configuration not found");
            }
            return find(name, providers, platformConfig);
        }
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
        return runAsync(network, LoadFlowParameters.load(platformConfig));
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
        return run(network, LoadFlowParameters.load(platformConfig));
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
