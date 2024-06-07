/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.dynamic;

import com.google.common.collect.Lists;
import com.powsybl.commons.Versionable;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.commons.config.PlatformConfigNamedProvider;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.commons.extensions.ExtensionJsonSerializer;
import com.powsybl.contingency.ContingenciesProvider;
import com.powsybl.dynamicsimulation.DynamicModelsSupplier;
import com.powsybl.dynamicsimulation.EventModelsSupplier;
import com.powsybl.iidm.network.Network;
import com.powsybl.security.SecurityAnalysisReport;
import com.powsybl.security.SecurityAnalysisResult;
import com.powsybl.security.interceptors.SecurityAnalysisInterceptor;

import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * A {@link com.powsybl.security.dynamic.DynamicSecurityAnalysisProvider} is a power system computation which computes,
 * for a {@link com.powsybl.iidm.network.Network Network} on which a {@link com.powsybl.dynamicsimulation.DynamicSimulation} is run,
 * the {@link com.powsybl.security.LimitViolation LimitViolations} on N-situation
 * and the ones caused by a specified list of {@link com.powsybl.contingency.Contingency Contingencies}.
 *
 * <p>Computation results are provided asynchronously as a {@link SecurityAnalysisResult}.
 *
 * <p>Implementations of that interface may typically rely on an external tool.
 *
 * <p>{@link SecurityAnalysisInterceptor Interceptors} might be used to execute client user-specific code
 * on events such as the availability of N-situation results, for example to further customize the results content
 * through {@link com.powsybl.commons.extensions.Extension Extensions}.
 *
 * @author Laurent Issertial {@literal <laurent.issertial at rte-france.com>}
 */
public interface DynamicSecurityAnalysisProvider extends Versionable, PlatformConfigNamedProvider {

    static List<DynamicSecurityAnalysisProvider> findAll() {
        return Lists.newArrayList(ServiceLoader.load(DynamicSecurityAnalysisProvider.class, DynamicSecurityAnalysisProvider.class.getClassLoader()));
    }

    /**
     * Run an asynchronous single dynamic security analysis job.
     * <p>
     * This method should complete with an exception if there is an exception thrown in the security analysis, in order
     * to be able to collect execution's logs in that case.
     * The original exception should be wrapped in {@link com.powsybl.computation.ComputationException}, together with
     * the out and err logs, within a {@link java.util.concurrent.CompletionException}.
     *
     * <p>
     * Example of use:
     * <pre> {@code
     * try {
     *       SecurityAnalysisResult result = dynamicSecurityAnalysis.run(network, variantId, dynamicModelsSupplier, eventModelsSupplier, contingenciesProvider, runParameters).join();
     *   } catch (CompletionException e) {
     *       if (e.getCause() instanceof ComputationException) {
     *           ComputationException computationException = (ComputationException) e.getCause();
     *           System.out.println("Consume exception...");
     *           computationException.getOutLogs().forEach((name, content) -> {
     *               System.out.println("-----" + name + "----");
     *               System.out.println(content);
     *           });
     *           computationException.getErrLogs().forEach((name, content) -> {
     *               System.out.println("-----" + name + "----");
     *               System.out.println(content);
     *           });
     *       }
     *       throw e;
     *   }
     * }</pre>
     *
     * @param network IIDM network on which the security analysis will be performed
     * @param workingVariantId network variant ID on which the analysis will be performed
     * @param dynamicModelsSupplier supplies list of dynamic models for the dynamic simulation part
     * @param eventModelsSupplier supplies list of event models for the dynamic simulation part
     * @param contingenciesProvider provides list of contingencies
     * @param runParameters runner parameters
     * @return a {@link CompletableFuture} on {@link SecurityAnalysisResult} that gathers security factor values
     */
    CompletableFuture<SecurityAnalysisReport> run(Network network,
                                                  String workingVariantId,
                                                  DynamicModelsSupplier dynamicModelsSupplier,
                                                  EventModelsSupplier eventModelsSupplier,
                                                  ContingenciesProvider contingenciesProvider,
                                                  DynamicSecurityAnalysisRunParameters runParameters);

    /**
     * The serializer for implementation-specific parameters, or {@link Optional#empty()} if the implementation
     * does not have any specific parameters, or does not support JSON serialization.
     *
     * <p>Note that the actual serializer type should be {@code ExtensionJsonSerializer<DynamicSecurityAnalysisParameters, MyParametersExtension>}
     * where {@code MyParametersExtension} is the specific parameters class.
     *
     * @return The serializer for implementation-specific parameters.
     */
    default Optional<ExtensionJsonSerializer> getSpecificParametersSerializer() {
        return Optional.empty();
    }

    /**
     * Reads implementation-specific parameters from platform config, or return {@link Optional#empty()}
     * if the implementation does not have any specific parameters, or does not support loading from config.
     *
     * @return The specific parameters read from platform config.
     */
    default Optional<Extension<DynamicSecurityAnalysisParameters>> loadSpecificParameters(PlatformConfig config) {
        return Optional.empty();
    }

    /**
     * Reads implementation-specific parameters from a Map, or return {@link Optional#empty()}
     * if the implementation does not have any specific parameters, or does not support loading from config.
     *
     * @return The specific parameters read from Map.
     */
    default Optional<Extension<DynamicSecurityAnalysisParameters>> loadSpecificParameters(Map<String, String> properties) {
        return Optional.empty();
    }

    /**
     * Updates implementation-specific parameters from a Map.
     */
    default void updateSpecificParameters(Extension<DynamicSecurityAnalysisParameters> extension, Map<String, String> properties) {
    }

    /**
     *
     * @return The name of the loadflow used for the security analysis.
     */
    default Optional<String> getDynamicSimulationProviderName() {
        return Optional.empty();
    }

    /**
     * get the list of the specific parameters names.
     *
     * @return the list of the specific parameters names.
     */
    default List<String> getSpecificParametersNames() {
        return Collections.emptyList();
    }
}
