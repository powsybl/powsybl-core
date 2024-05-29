/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.dynamic;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.Versionable;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.commons.config.PlatformConfigNamedProvider;
import com.powsybl.contingency.ContingenciesProvider;
import com.powsybl.contingency.Contingency;
import com.powsybl.dynamicsimulation.DynamicModel;
import com.powsybl.dynamicsimulation.DynamicModelsSupplier;
import com.powsybl.dynamicsimulation.EventModel;
import com.powsybl.dynamicsimulation.EventModelsSupplier;
import com.powsybl.iidm.network.Network;
import com.powsybl.security.SecurityAnalysisReport;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * Dynamic Security analysis main API.
 * It is a utility class (so with only static methods) used as an entry point for running
 * a dynamic security analysis allowing to choose either a specific implementation or just to rely on the default one.
 *
 * @author Laurent Issertial {@literal <laurent.issertial at rte-france.com>}
 */
public final class DynamicSecurityAnalysis {

    private DynamicSecurityAnalysis() {
        throw new IllegalStateException("Utility class should not been instantiated");
    }

    /**
     * A dynamic security analysis runner is responsible for providing convenient methods on top of {@link DynamicSecurityAnalysisProvider}:
     * several variants of synchronous and asynchronous run with default parameters.
     */
    public static final class Runner implements Versionable {

        private final DynamicSecurityAnalysisProvider provider;

        public Runner(DynamicSecurityAnalysisProvider provider) {
            this.provider = Objects.requireNonNull(provider);
        }

        public CompletableFuture<SecurityAnalysisReport> runAsync(Network network,
                                                                  String workingVariantId,
                                                                  DynamicModelsSupplier dynamicModelsSupplier,
                                                                  EventModelsSupplier eventModelsSupplier,
                                                                  ContingenciesProvider contingenciesProvider,
                                                                  DynamicSecurityAnalysisRunParameters runParameters) {
            Objects.requireNonNull(network, "Network should not be null");
            Objects.requireNonNull(workingVariantId, "WorkingVariantId should not be null");
            Objects.requireNonNull(dynamicModelsSupplier, "Dynamic model supplier should not be null");
            Objects.requireNonNull(eventModelsSupplier, "Event models supplier should not be null");
            Objects.requireNonNull(contingenciesProvider, "Contingencies provider should not be null");
            return provider.run(network, workingVariantId, dynamicModelsSupplier, eventModelsSupplier, contingenciesProvider, runParameters);
        }

        public CompletableFuture<SecurityAnalysisReport> runAsync(Network network, List<DynamicModel> dynamicModels, List<EventModel> eventModels, List<Contingency> contingencies, DynamicSecurityAnalysisRunParameters runParameters) {
            return runAsync(network, network.getVariantManager().getWorkingVariantId(), (n, r) -> dynamicModels, (n, r) -> eventModels, n -> contingencies, runParameters);
        }

        public CompletableFuture<SecurityAnalysisReport> runAsync(Network network, List<DynamicModel> dynamicModels, List<EventModel> eventModels, List<Contingency> contingencies) {
            return runAsync(network, dynamicModels, eventModels, contingencies, DynamicSecurityAnalysisRunParameters.getDefault());
        }

        public CompletableFuture<SecurityAnalysisReport> runAsync(Network network, List<DynamicModel> dynamicModels, List<Contingency> contingencies) {
            return runAsync(network, dynamicModels, Collections.emptyList(), contingencies);
        }

        public SecurityAnalysisReport run(Network network,
                                                                  String workingVariantId,
                                                                  DynamicModelsSupplier dynamicModelsSupplier,
                                                                  EventModelsSupplier eventModelsSupplier,
                                                                  ContingenciesProvider contingenciesProvider,
                                                                  DynamicSecurityAnalysisRunParameters runParameters) {
            return runAsync(network, workingVariantId, dynamicModelsSupplier, eventModelsSupplier, contingenciesProvider, runParameters).join();
        }

        public SecurityAnalysisReport run(Network network, List<DynamicModel> dynamicModels, List<EventModel> eventModels, List<Contingency> contingencies, DynamicSecurityAnalysisRunParameters runParameters) {
            return run(network, network.getVariantManager().getWorkingVariantId(), (n, r) -> dynamicModels, (n, r) -> eventModels, n -> contingencies, runParameters);
        }

        public SecurityAnalysisReport run(Network network, List<DynamicModel> dynamicModels, List<EventModel> eventModels, List<Contingency> contingencies) {
            return run(network, dynamicModels, eventModels, contingencies, DynamicSecurityAnalysisRunParameters.getDefault());
        }

        public SecurityAnalysisReport run(Network network, List<DynamicModel> dynamicModels, List<Contingency> contingencies) {
            return run(network, dynamicModels, Collections.emptyList(), contingencies);
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

    /**
     * Get a runner for security analysis implementation named {@code name}. In the case of a null {@code name}, default
     * implementation is used.
     *
     * @param name name of the security analysis implementation, null if we want to use default one
     * @return a runner for security analysis implementation named {@code name}
     */
    public static Runner find(String name) {
        return new Runner(PlatformConfigNamedProvider.Finder
            .find(name, "dynamic-security-analysis", DynamicSecurityAnalysisProvider.class,
                PlatformConfig.defaultConfig()));
    }

    /**
     * Get a runner for default security analysis implementation.
     *
     * @return a runner for default security analysis implementation
     * @throws PowsyblException in case we cannot find a default implementation
     */
    public static Runner find() {
        return find(null);
    }

    public static CompletableFuture<SecurityAnalysisReport> runAsync(Network network,
                                                              String workingVariantId,
                                                              DynamicModelsSupplier dynamicModelsSupplier,
                                                              EventModelsSupplier eventModelsSupplier,
                                                              ContingenciesProvider contingenciesProvider,
                                                              DynamicSecurityAnalysisRunParameters runParameters) {
        return find().runAsync(network, workingVariantId, dynamicModelsSupplier, eventModelsSupplier, contingenciesProvider, runParameters);
    }

    public static CompletableFuture<SecurityAnalysisReport> runAsync(Network network, List<DynamicModel> dynamicModels, List<EventModel> eventModels, List<Contingency> contingencies, DynamicSecurityAnalysisRunParameters runParameters) {
        return find().runAsync(network, dynamicModels, eventModels, contingencies, runParameters);
    }

    public static CompletableFuture<SecurityAnalysisReport> runAsync(Network network, List<DynamicModel> dynamicModels, List<EventModel> eventModels, List<Contingency> contingencies) {
        return find().runAsync(network, dynamicModels, eventModels, contingencies);
    }

    public static CompletableFuture<SecurityAnalysisReport> runAsync(Network network, List<DynamicModel> dynamicModels, List<Contingency> contingencies) {
        return find().runAsync(network, dynamicModels, contingencies);
    }

    public static SecurityAnalysisReport run(Network network,
                                      String workingVariantId,
                                      DynamicModelsSupplier dynamicModelsSupplier,
                                      EventModelsSupplier eventModelsSupplier,
                                      ContingenciesProvider contingenciesProvider,
                                      DynamicSecurityAnalysisRunParameters runParameters) {
        return find().run(network, workingVariantId, dynamicModelsSupplier, eventModelsSupplier, contingenciesProvider, runParameters);
    }

    public static SecurityAnalysisReport run(Network network, List<DynamicModel> dynamicModels, List<EventModel> eventModels, List<Contingency> contingencies, DynamicSecurityAnalysisRunParameters runParameters) {
        return find().run(network, dynamicModels, eventModels, contingencies, runParameters);
    }

    public static SecurityAnalysisReport run(Network network, List<DynamicModel> dynamicModels, List<EventModel> eventModels, List<Contingency> contingencies) {
        return find().run(network, dynamicModels, eventModels, contingencies);
    }

    public static SecurityAnalysisReport run(Network network, List<DynamicModel> dynamicModels, List<Contingency> contingencies) {
        return find().run(network, dynamicModels, contingencies);
    }
}
