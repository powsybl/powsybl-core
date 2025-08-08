/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.loadflow;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.Versionable;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.commons.config.PlatformConfigNamedProvider;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.computation.ComputationManager;
import com.powsybl.computation.local.LocalComputationManager;
import com.powsybl.iidm.network.Network;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * LoadFlow main API. It is a utility class (so with only static methods) used as an entry point for running
 * a loadflow allowing to choose either a specific find implementation or just to rely on default one.
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public final class LoadFlow {

    private LoadFlow() {
    }

    /**
     * A loadflow runner is responsible for providing convenient methods on top of {@link LoadFlowProvider}:
     * several variants of synchronous and asynchronous run with default parameters.
     */
    public static class Runner implements Versionable {

        private final LoadFlowProvider provider;
        private Network network = null;
        private LoadFlowParameters parameters = LoadFlowParameters.load();
        private ComputationManager computationManager = LocalComputationManager.getDefault();
        private ReportNode reportNode = ReportNode.NO_OP;
        private String variantId = null;

        public Runner(LoadFlowProvider provider) {
            this.provider = Objects.requireNonNull(provider);
        }

        public Runner setNetwork(Network network) {
            this.network = Objects.requireNonNull(network);
            setDefaultVariantId(network);
            return this;
        }

        public Runner setParameters(LoadFlowParameters parameters) {
            this.parameters = parameters;
            return this;
        }

        public Runner setComputationManager(ComputationManager computationManager) {
            this.computationManager = computationManager;
            return this;
        }

        public Runner setReportNode(ReportNode reportNode) {
            this.reportNode = reportNode;
            return this;
        }

        public Runner setVariantId(String variantId) {
            this.variantId = variantId;
            return this;
        }

        private void setDefaultVariantId(Network network) {
            if (this.variantId == null) {
                this.variantId = network.getVariantManager().getWorkingVariantId();
            }
        }

        public CompletableFuture<LoadFlowResult> runAsync(Network network, String workingStateId, ComputationManager computationManager, LoadFlowParameters parameters, ReportNode reportNode) {
            Objects.requireNonNull(workingStateId);
            Objects.requireNonNull(parameters);
            Objects.requireNonNull(reportNode);
            return provider.run(network, computationManager, workingStateId, parameters, reportNode);
        }

        public CompletableFuture<LoadFlowResult> runAsync(Network network, String workingStateId, ComputationManager computationManager, LoadFlowParameters parameters) {
            return runAsync(network, workingStateId, computationManager, parameters, reportNode);
        }

        public CompletableFuture<LoadFlowResult> runAsync(Network network, ComputationManager computationManager, LoadFlowParameters parameters) {
            setDefaultVariantId(network);
            return runAsync(network, variantId, computationManager, parameters);
        }

        public CompletableFuture<LoadFlowResult> runAsync(Network network, LoadFlowParameters parameters) {
            return runAsync(network, computationManager, parameters);
        }

        public CompletableFuture<LoadFlowResult> runAsync(Network network) {
            return runAsync(network, parameters);
        }

        public CompletableFuture<LoadFlowResult> runAsync() {
            return runAsync(network);
        }

        public LoadFlowResult run(Network network, String workingStateId, ComputationManager computationManager, LoadFlowParameters parameters, ReportNode reportNode) {
            Objects.requireNonNull(workingStateId);
            Objects.requireNonNull(parameters);
            Objects.requireNonNull(reportNode);
            return provider.run(network, computationManager, workingStateId, parameters, reportNode).join();
        }

        public LoadFlowResult run(Network network, String workingStateId, ComputationManager computationManager, LoadFlowParameters parameters) {
            return run(network, workingStateId, computationManager, parameters, reportNode);
        }

        public LoadFlowResult run(Network network, ComputationManager computationManager, LoadFlowParameters parameters) {
            setDefaultVariantId(network);
            return run(network, variantId, computationManager, parameters);
        }

        public LoadFlowResult run(Network network, LoadFlowParameters parameters) {
            return run(network, computationManager, parameters);
        }

        public LoadFlowResult run(Network network) {
            return run(network, parameters);
        }

        public LoadFlowResult run() {
            return run(network);
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
     * Get a runner for loadflow implementation named {@code name}. In the case of a null {@code name}, default
     * implementation is used.
     *
     * @param name name of the load flow implementation, null if we want to use default one
     * @return a runner for loadflow implementation named {@code name}
     */
    public static Runner find(String name) {
        return new Runner(PlatformConfigNamedProvider.Finder
                .find(name, "load-flow", LoadFlowProvider.class,
                PlatformConfig.defaultConfig()));
    }

    /**
     * Get a runner for default loadflow implementation.
     *
     * @throws PowsyblException in case we cannot find a default implementation
     * @return a runner for default loadflow implementation
     */
    public static Runner find() {
        return find(null);
    }

    public static CompletableFuture<LoadFlowResult> runAsync(Network network, String workingStateId, ComputationManager computationManager, LoadFlowParameters parameters, ReportNode reportNode) {
        return find().runAsync(network, workingStateId, computationManager, parameters, reportNode);
    }

    public static CompletableFuture<LoadFlowResult> runAsync(Network network, String workingStateId, ComputationManager computationManager, LoadFlowParameters parameters) {
        return find().runAsync(network, workingStateId, computationManager, parameters);
    }

    public static CompletableFuture<LoadFlowResult> runAsync(Network network, ComputationManager computationManager, LoadFlowParameters parameters) {
        return find().runAsync(network, computationManager, parameters);
    }

    public static CompletableFuture<LoadFlowResult> runAsync(Network network, LoadFlowParameters parameters) {
        return find().runAsync(network, parameters);
    }

    public static CompletableFuture<LoadFlowResult> runAsync(Network network) {
        return find().runAsync(network);
    }

    public static LoadFlowResult run(Network network, String workingStateId, ComputationManager computationManager, LoadFlowParameters parameters, ReportNode reportNode) {
        return find().run(network, workingStateId, computationManager, parameters, reportNode);
    }

    public static LoadFlowResult run(Network network, String workingStateId, ComputationManager computationManager, LoadFlowParameters parameters) {
        return find().run(network, workingStateId, computationManager, parameters);
    }

    public static LoadFlowResult run(Network network, ComputationManager computationManager, LoadFlowParameters parameters) {
        return find().run(network, computationManager, parameters);
    }

    public static LoadFlowResult run(Network network, LoadFlowParameters parameters) {
        return find().run(network, parameters);
    }

    public static LoadFlowResult run(Network network) {
        return find().run(network);
    }
}
