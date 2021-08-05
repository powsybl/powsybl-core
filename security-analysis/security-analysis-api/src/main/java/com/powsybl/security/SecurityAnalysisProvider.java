/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security;

import com.powsybl.commons.Versionable;
import com.powsybl.commons.config.PlatformConfigNamedProvider;
import com.powsybl.computation.ComputationManager;
import com.powsybl.contingency.ContingenciesProvider;
import com.powsybl.iidm.network.Network;
import com.powsybl.security.interceptors.SecurityAnalysisInterceptor;
import com.powsybl.security.monitor.StateMonitor;

import java.util.Collections;
import java.util.List;
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
 * @author Thomas Adam <tadam at silicom.fr>
 */
public interface SecurityAnalysisProvider extends Versionable, PlatformConfigNamedProvider {

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
     * @param network               IIDM network on which the security analysis will be performed
     * @param workingVariantId      network variant ID on which the analysis will be performed
     * @param detector
     * @param filter
     * @param computationManager
     * @param parameters            specific security analysis parameters
     * @param contingenciesProvider provides list of contingencies
     * @param interceptors
     * @return a {@link CompletableFuture} on {@link SecurityAnalysisResult} that gathers security factor values
     */
    default CompletableFuture<SecurityAnalysisReport> run(Network network,
                                                          String workingVariantId,
                                                          LimitViolationDetector detector,
                                                          LimitViolationFilter filter,
                                                          ComputationManager computationManager,
                                                          SecurityAnalysisParameters parameters,
                                                          ContingenciesProvider contingenciesProvider,
                                                          List<SecurityAnalysisInterceptor> interceptors) {
        return run(network, workingVariantId, detector, filter, computationManager, parameters, contingenciesProvider, interceptors, Collections.emptyList());
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
     * @param network               IIDM network on which the security analysis will be performed
     * @param workingVariantId      network variant ID on which the analysis will be performed
     * @param detector
     * @param filter
     * @param computationManager
     * @param parameters            specific security analysis parameters
     * @param contingenciesProvider provides list of contingencies
     * @param interceptors
     * @param monitors stateMonitor that defines the branch bus and threeWindingsTransformer about which informations will be written after security analysis
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
                                                          List<StateMonitor> monitors);
}
