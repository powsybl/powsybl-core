/**
 * Copyright (c) 2016-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.commons.util.ServiceLoaderCache;
import com.powsybl.computation.ComputationManager;
import com.powsybl.contingency.ContingenciesProvider;
import com.powsybl.iidm.network.Network;
import com.powsybl.security.interceptors.SecurityAnalysisInterceptor;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

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
public final class SecurityAnalysis {

    private SecurityAnalysis() {
    }

    private static final Supplier<List<SecurityAnalysisProvider>> PROVIDERS_SUPPLIERS
            = Suppliers.memoize(() -> new ServiceLoaderCache<>(SecurityAnalysisProvider.class).getServices());

    public static class Runner {

        private final SecurityAnalysisProvider provider;

        private LimitViolationDetector detector;
        private LimitViolationFilter filter;
        private String workingVariantId;
        private List<SecurityAnalysisInterceptor> interceptors;

        public Runner(SecurityAnalysisProvider provider) {
            this.provider = Objects.requireNonNull(provider);
        }

        public Runner with(LimitViolationDetector detector) {
            this.detector = Objects.requireNonNull(detector);
            return this;
        }

        public Runner with(LimitViolationFilter filter) {
            this.filter = Objects.requireNonNull(filter);
            return this;
        }

        public Runner with(String workingVariantId) {
            this.workingVariantId = Objects.requireNonNull(workingVariantId);
            return this;
        }

        public Runner with(List<SecurityAnalysisInterceptor> interceptors) {
            this.interceptors = Objects.requireNonNull(interceptors);
            return this;
        }

        public CompletableFuture<SecurityAnalysisResult> run(Network network, ComputationManager computationManager,
                                                             SecurityAnalysisParameters parameters, ContingenciesProvider contingenciesProvider) {
            Objects.requireNonNull(network);
            Objects.requireNonNull(computationManager);
            Objects.requireNonNull(parameters);
            Objects.requireNonNull(contingenciesProvider);
            return provider.run(network, detector == null ? provider.getDefaultLimitViolationDetector() : detector,
                    filter == null ? provider.getDefaultLimitViolationFilter() : filter, computationManager,
                    workingVariantId == null ? network.getVariantManager().getWorkingVariantId() : workingVariantId,
                    parameters, contingenciesProvider, interceptors == null ? provider.getDefaultInterceptors() : interceptors);
        }

        /**
         * To be consistent with {@link #run(Network, ComputationManager, SecurityAnalysisParameters, ContingenciesProvider)}, this method would also complete exceptionally
         * if there are exceptions thrown. But the original exception would be wrapped in {@link com.powsybl.computation.ComputationException}, and those .out/.err log file's contents
         * are be collected in the {@link com.powsybl.computation.ComputationException} too.
         *
         *
         * <pre> {@code
         * try {
         *       SecurityAnalysisResultWithLog resultWithLog = SecurityAnalysis.find()
         *                  .runWithLog(network, computationManager, parameters, contingenciesProvider).join();
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
        public CompletableFuture<SecurityAnalysisResultWithLog> runWithLog(Network network, ComputationManager computationManager,
                                                             SecurityAnalysisParameters parameters, ContingenciesProvider contingenciesProvider) {
            Objects.requireNonNull(network);
            Objects.requireNonNull(computationManager);
            Objects.requireNonNull(parameters);
            Objects.requireNonNull(contingenciesProvider);
            return provider.runWithLog(network, detector == null ? provider.getDefaultLimitViolationDetector() : detector,
                    filter == null ? provider.getDefaultLimitViolationFilter() : filter, computationManager,
                    workingVariantId == null ? network.getVariantManager().getWorkingVariantId() : workingVariantId,
                    parameters, contingenciesProvider, interceptors == null ? provider.getDefaultInterceptors() : interceptors);
        }
    }

    /**
     * Get a runner for security-analysis implementation named {@code name}. In the case of a null {@code name}, default
     * implementation is used.
     *
     * @param name name of the security-analysis implementation, null if we want to use default one
     * @return a runner for security-analysis implementation named {@code name}
     */
    public static Runner find(String name) {
        PROVIDERS_SUPPLIERS.get();
        return find(name, PROVIDERS_SUPPLIERS.get(), PlatformConfig.defaultConfig());
    }

    public static Runner find() {
        return find(null);
    }

    // TODO generailize
    /**
     * A variant of {@link SecurityAnalysis#find(String)} intended to be used for unit testing that allow passing
     * an explicit provider list instead of relying on service loader and an explicit {@link PlatformConfig}
     * instead of global one.
     *
     * @param name name of the security-analysis implementation, null if we want to use default one
     * @param providers SecurityAnalysis provider list
     * @param platformConfig platform config to look for default SecurityAnalysis implementation name
     * @return a runner for SecurityAnalysis implementation named {@code name}
     */
    public static Runner find(String name, List<SecurityAnalysisProvider> providers, PlatformConfig platformConfig) {
        Objects.requireNonNull(providers);
        Objects.requireNonNull(platformConfig);

        if (providers.isEmpty()) {
            throw new PowsyblException("No SecurityAnalysis providers found");
        }

        // if no SecurityAnalysis implementation name is provided through the API we look for information
        // in platform configuration
        String securityAnalysisName = name != null ? name : platformConfig.getOptionalModuleConfig("security-analysis")
                .flatMap(mc -> mc.getOptionalStringProperty("default"))
                .orElse(null);
        SecurityAnalysisProvider provider;
        if (providers.size() == 1 && securityAnalysisName == null) {
            // no information to select the implementation but only one provider, so we can use it by default
            // (that is be the most common use case)
            provider = providers.get(0);
        } else {
            if (providers.size() > 1 && securityAnalysisName == null) {
                // several providers and no information to select which one to choose, we can only throw
                // an exception
                List<String> securityAnalysisNames = providers.stream().map(SecurityAnalysisProvider::getName).collect(Collectors.toList());
                throw new PowsyblException("Several SecurityAnalysis implementations found (" + securityAnalysisNames
                        + "), you must add configuration to select the implementation");
            }
            provider = providers.stream()
                    .filter(p -> p.getName().equals(securityAnalysisName))
                    .findFirst()
                    .orElseThrow(() -> new PowsyblException("SecurityAnalysis '" + securityAnalysisName + "' not found"));
        }

        return new Runner(provider);
    }

}
