/**
 * Copyright (c) 2016-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security;

import com.powsybl.contingency.ContingenciesProvider;
import com.powsybl.security.interceptors.SecurityAnalysisInterceptor;

import java.util.concurrent.CompletableFuture;

/**
 *
 * A {@link SecurityAnalysis} is a power system computation which computes, for a {@link com.powsybl.iidm.network.Network Network},
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
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Teofil Calin BANC <teofil-calin.banc at rte-france.com>
 */
public interface SecurityAnalysis {

    void addInterceptor(SecurityAnalysisInterceptor interceptor);

    boolean removeInterceptor(SecurityAnalysisInterceptor interceptor);

    CompletableFuture<SecurityAnalysisResult> run(String workingVariantId, SecurityAnalysisParameters parameters, ContingenciesProvider contingenciesProvider);

    /**
     * To be consistent with {@link #run(String, SecurityAnalysisParameters, ContingenciesProvider)}, this method would also complete exceptionally
     * if there are exceptions thrown. But the original exception would be wrapped in {@link com.powsybl.computation.ComputationException}, and those .out/.err log file's contents
     * are be collected in the {@link com.powsybl.computation.ComputationException} too.
     *
     *
     * <pre> {@code
     * try {
     *       SecurityAnalysisResultWithLog resultWithLog = securityAnalysis.runWithLog(currentState, parameters, contingenciesProvider).join();
     *       result = resultWithLog.getResult();
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
     * @param workingVariantId
     * @param parameters
     * @param contingenciesProvider
     * @return
     */
    default CompletableFuture<SecurityAnalysisResultWithLog> runWithLog(String workingVariantId, SecurityAnalysisParameters parameters, ContingenciesProvider contingenciesProvider) {
        return run(workingVariantId, parameters, contingenciesProvider).thenApply(r -> new SecurityAnalysisResultWithLog(r, null));
    }

}
