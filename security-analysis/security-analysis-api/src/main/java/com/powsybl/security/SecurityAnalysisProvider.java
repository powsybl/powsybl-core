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
import com.powsybl.commons.config.PlatformConfigNamedProvider;
import com.powsybl.commons.config.SpecificParametersProvider;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.contingency.ContingenciesProvider;
import com.powsybl.iidm.network.Network;
import com.powsybl.security.interceptors.SecurityAnalysisInterceptor;

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
public interface SecurityAnalysisProvider extends Versionable, PlatformConfigNamedProvider, SpecificParametersProvider<SecurityAnalysisParameters, Extension<SecurityAnalysisParameters>> {

    static List<SecurityAnalysisProvider> findAll() {
        return Lists.newArrayList(ServiceLoader.load(SecurityAnalysisProvider.class, SecurityAnalysisProvider.class.getClassLoader()));
    }

    /**
     * Run an asynchronous single security analysis job.
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
     *       SecurityAnalysisResult result = securityAnalysis.run(network, variantId, contingenciesProvider, runParameters).join();
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
     * @param contingenciesProvider provides list of contingencies
     * @param runParameters runner parameters
     * @return a {@link CompletableFuture} on {@link SecurityAnalysisResult} that gathers security factor values
     */
    CompletableFuture<SecurityAnalysisReport> run(Network network,
                                                  String workingVariantId,
                                                  ContingenciesProvider contingenciesProvider,
                                                  SecurityAnalysisRunParameters runParameters);

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
