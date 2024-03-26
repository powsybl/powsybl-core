/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security;

import com.google.common.collect.Lists;
import com.powsybl.commons.Versionable;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.commons.config.PlatformConfigNamedProvider;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.commons.extensions.ExtensionJsonSerializer;
import com.powsybl.computation.ComputationManager;
import com.powsybl.contingency.ContingenciesProvider;
import com.powsybl.iidm.network.Network;
import com.powsybl.action.Action;
import com.powsybl.security.interceptors.SecurityAnalysisInterceptor;
import com.powsybl.security.limitreduction.LimitReduction;
import com.powsybl.security.monitor.StateMonitor;
import com.powsybl.security.strategy.OperatorStrategy;

import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * A {@link SecurityAnalysisProvider} is a power system computation which computes, for a {@link com.powsybl.iidm.network.Network Network},
 * the {@link LimitViolation LimitViolations} on N-situation
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
 * @author Thomas Adam {@literal <tadam at silicom.fr>}
 */
public interface SecurityAnalysisProvider extends Versionable, PlatformConfigNamedProvider {

    static List<SecurityAnalysisProvider> findAll() {
        return Lists.newArrayList(ServiceLoader.load(SecurityAnalysisProvider.class, SecurityAnalysisProvider.class.getClassLoader()));
    }

    /**
     * Run an asynchronous single security analysis job.
     * <p>
     * if there are exceptions thrown. But the original exception would be wrapped in {@link com.powsybl.computation.ComputationException}, and those .out/.err log file's contents
     * are be collected in the {@link com.powsybl.computation.ComputationException} too.
     *
     * <pre> {@code
     * try {
     *       SecurityAnalysisResult result = securityAnalysis.run(network, variantId, detector, filter, computationManager, parameters, contingenciesProvider, interceptors).join();
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
     * @param detector
     * @param filter
     * @param computationManager
     * @param parameters specific security analysis parameters
     * @param contingenciesProvider provides list of contingencies
     * @param interceptors
     * @param monitors stateMonitor that defines the branch bus and threeWindingsTransformer about which informations will be written after security analysis
     * @param limitReductions list of the limit reductions to apply
     * @param reportNode the reportNode used for functional logs
     * @return a {@link CompletableFuture} on {@link SecurityAnalysisResult} that gathers security factor values
     */
    CompletableFuture<SecurityAnalysisReport> run(Network network,
                                                  String workingVariantId,
                                                  LimitViolationDetector detector,
                                                  LimitViolationFilter filter,
                                                  ComputationManager computationManager,
                                                  SecurityAnalysisParameters parameters,
                                                  ContingenciesProvider contingenciesProvider,
                                                  List<SecurityAnalysisInterceptor> interceptors,
                                                  List<OperatorStrategy> operatorStrategies,
                                                  List<Action> actions,
                                                  List<StateMonitor> monitors,
                                                  List<LimitReduction> limitReductions,
                                                  ReportNode reportNode);

    /**
     * The serializer for implementation-specific parameters, or {@link Optional#empty()} if the implementation
     * does not have any specific parameters, or does not support JSON serialization.
     *
     * <p>Note that the actual serializer type should be {@code ExtensionJsonSerializer<SecurityAnalysisParameters, MyParametersExtension>}
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
    default Optional<Extension<SecurityAnalysisParameters>> loadSpecificParameters(PlatformConfig config) {
        return Optional.empty();
    }

    /**
     * Reads implementation-specific parameters from a Map, or return {@link Optional#empty()}
     * if the implementation does not have any specific parameters, or does not support loading from config.
     *
     * @return The specific parameters read from Map.
     */
    default Optional<Extension<SecurityAnalysisParameters>> loadSpecificParameters(Map<String, String> properties) {
        return Optional.empty();
    }

    /**
     * Updates implementation-specific parameters from a Map.
     */
    default void updateSpecificParameters(Extension<SecurityAnalysisParameters> extension, Map<String, String> properties) {
    }

    /**
     *
     * @return The name of the loadflow used for the security analysis.
     */
    default Optional<String> getLoadFlowProviderName() {
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
