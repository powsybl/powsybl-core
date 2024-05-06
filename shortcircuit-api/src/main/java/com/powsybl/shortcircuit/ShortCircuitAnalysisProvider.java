/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.shortcircuit;

import com.google.common.collect.Lists;
import com.powsybl.commons.Versionable;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.commons.config.PlatformConfigNamedProvider;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.commons.extensions.ExtensionJsonSerializer;
import com.powsybl.commons.parameters.Parameter;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.network.Network;

import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 *
 *  <p>Computation results are provided asynchronously as a {@link ShortCircuitAnalysisResult}.
 *
 *  <p>Implementations of that interface may typically rely on an external tool.
 *
 * @author Anne Tilloy {@literal <anne.tilloy at rte-france.com>}
 */
public interface ShortCircuitAnalysisProvider extends Versionable, PlatformConfigNamedProvider {

    static List<ShortCircuitAnalysisProvider> findAll() {
        return Lists.newArrayList(ServiceLoader.load(ShortCircuitAnalysisProvider.class, ShortCircuitAnalysisProvider.class.getClassLoader()));
    }

    /**
     * Run an asynchronous single short circuit analysis job.
     *
     * @param faultParameters parameters that define the fault about which information will be written after short circuit analysis
     */
    default CompletableFuture<ShortCircuitAnalysisResult> run(Network network,
                                                              List<Fault> faults,
                                                              ShortCircuitParameters parameters,
                                                              ComputationManager computationManager,
                                                              List<FaultParameters> faultParameters) {
        return ShortCircuitAnalysis.runAsync(network, faults, parameters, computationManager, faultParameters);
    }

    /**
     * Run an asynchronous single short circuit analysis job.
     */
    default CompletableFuture<ShortCircuitAnalysisResult> run(Network network, List<Fault> faults,
                                                              ShortCircuitParameters parameters,
                                                              ComputationManager computationManager,
                                                              List<FaultParameters> faultParameters,
                                                              ReportNode reportNode) {
        return ShortCircuitAnalysis.runAsync(network, faults, parameters, computationManager, faultParameters, reportNode);
    }

    /**
     * The serializer for implementation-specific parameters, or {@link Optional#empty()} if the implementation
     * does not have any specific parameters, or does not support JSON serialization.
     *
     * <p>Note that the actual serializer type should be {@code ExtensionJsonSerializer<ShortCircuitParameters, MyParametersExtension>}
     * where {@code MyParametersExtension} is the specific parameters class.
     */
    default Optional<ExtensionJsonSerializer> getSpecificParametersSerializer() {
        return Optional.empty();
    }

    /**
     * Reads implementation-specific parameters from platform config, or return {@link Optional#empty()}
     * if the implementation does not have any specific parameters, or does not support loading from config.
     */
    default Optional<Extension<ShortCircuitParameters>> loadSpecificParameters(PlatformConfig config) {
        return Optional.empty();
    }

    /**
     * Reads implementation-specific parameters from a Map, or return {@link Optional#empty()}
     * if the implementation does not have any specific parameters, or does not support loading from config.
     */
    default Optional<Extension<ShortCircuitParameters>> loadSpecificParameters(Map<String, String> properties) {
        return Optional.empty();
    }

    /**
     * Updates implementation-specific parameters from a Map.
     */
    default void updateSpecificParameters(Extension<ShortCircuitParameters> extension, Map<String, String> properties) {
    }

    /**
     * Get the list of the specific parameters.
     */
    default List<Parameter> getSpecificParameters() {
        return Collections.emptyList();
    }
}
