package com.powsybl.sensitivity;

import com.google.common.base.Suppliers;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.Versionable;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.commons.util.ServiceLoaderCache;
import com.powsybl.computation.ComputationManager;
import com.powsybl.computation.DefaultComputationManagerConfig;
import com.powsybl.contingency.ContingenciesProvider;
import com.powsybl.iidm.network.Network;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Sensitivity computation main API. It is a utility class (so with only static methods) used as an entry point for running
 * a sensitivity computation allowing to choose either a specific implementation or just to rely on default one.
 *
 * @author Joris Mancini {@literal <joris.mancini at rte-france.com>}
 */
public final class SensitivityComputation {

    private SensitivityComputation() {
        throw new AssertionError("Utility class should not been instantiated");
    }

    private static final Supplier<List<SensitivityComputationProvider>> SENSITIVITY_COMPUTATION_PROVIDERS
        = Suppliers.memoize(() -> new ServiceLoaderCache<>(SensitivityComputationProvider.class).getServices());

    /**
     * A sensitivity computation runner is responsible for providing convenient methods on top of {@link SensitivityComputationProvider}:
     * several variants of synchronous and asynchronous run with default parameters.
     */
    public static final class SensitivityComputationRunner implements Versionable {

        private final SensitivityComputationProvider provider;

        private SensitivityComputationRunner(SensitivityComputationProvider provider) {
            this.provider = Objects.requireNonNull(provider);
        }

        private void checkInputs(Network network,
                                 String workingStateId,
                                 SensitivityFactorsProvider factorsProvider,
                                 ContingenciesProvider contingenciesProvider,
                                 SensitivityComputationParameters parameters,
                                 ComputationManager computationManager) {
            checkInputs(network, workingStateId, factorsProvider, parameters, computationManager);
            Objects.requireNonNull(contingenciesProvider, "Contingencies provider should not be null");
        }

        private void checkInputs(Network network,
                                 String workingStateId,
                                 SensitivityFactorsProvider factorsProvider,
                                 SensitivityComputationParameters parameters,
                                 ComputationManager computationManager) {
            Objects.requireNonNull(network, "Network should not be null");
            Objects.requireNonNull(workingStateId, "Parameters should not be null");
            Objects.requireNonNull(factorsProvider, "Sensitivity factors provider should not be null");
            Objects.requireNonNull(parameters, "Sensitivity computation parameters should not be null");
            Objects.requireNonNull(computationManager, "Computation manager should not be null");
        }

        public CompletableFuture<SensitivityComputationResults> runAsync(Network network,
                                                                         String workingStateId,
                                                                         SensitivityFactorsProvider factorsProvider,
                                                                         ContingenciesProvider contingenciesProvider,
                                                                         SensitivityComputationParameters parameters,
                                                                         ComputationManager computationManager) {
            checkInputs(network, workingStateId, factorsProvider, contingenciesProvider, parameters, computationManager);
            return provider.run(network, workingStateId, factorsProvider, contingenciesProvider, parameters, computationManager);
        }

        public CompletableFuture<SensitivityComputationResults> runAsync(Network network,
                                                                         String workingStateId,
                                                                         SensitivityFactorsProvider factorsProvider,
                                                                         ContingenciesProvider contingenciesProvider,
                                                                         SensitivityComputationParameters parameters) {
            return runAsync(network, workingStateId, factorsProvider, contingenciesProvider, parameters, DefaultComputationManagerConfig.load().createLongTimeExecutionComputationManager());
        }

        public CompletableFuture<SensitivityComputationResults> runAsync(Network network,
                                                                         SensitivityFactorsProvider factorsProvider,
                                                                         ContingenciesProvider contingenciesProvider,
                                                                         SensitivityComputationParameters parameters) {
            return runAsync(network, network.getVariantManager().getWorkingVariantId(), factorsProvider, contingenciesProvider, parameters);
        }

        public CompletableFuture<SensitivityComputationResults> runAsync(Network network,
                                                                         SensitivityFactorsProvider factorsProvider,
                                                                         ContingenciesProvider contingenciesProvider) {
            return runAsync(network, factorsProvider, contingenciesProvider, SensitivityComputationParameters.load());
        }

        public CompletableFuture<SensitivityComputationResults> runAsync(Network network,
                                                                         String workingStateId,
                                                                         SensitivityFactorsProvider factorsProvider,
                                                                         SensitivityComputationParameters parameters,
                                                                         ComputationManager computationManager) {
            checkInputs(network, workingStateId, factorsProvider, parameters, computationManager);
            return provider.run(network, workingStateId, factorsProvider, parameters, computationManager);
        }

        public CompletableFuture<SensitivityComputationResults> runAsync(Network network,
                                                                         String workingStateId,
                                                                         SensitivityFactorsProvider factorsProvider,
                                                                         SensitivityComputationParameters parameters) {
            return runAsync(network, workingStateId, factorsProvider, parameters, DefaultComputationManagerConfig.load().createLongTimeExecutionComputationManager());
        }

        public CompletableFuture<SensitivityComputationResults> runAsync(Network network,
                                                                         SensitivityFactorsProvider factorsProvider,
                                                                         SensitivityComputationParameters parameters) {
            return runAsync(network, network.getVariantManager().getWorkingVariantId(), factorsProvider, parameters);
        }

        public CompletableFuture<SensitivityComputationResults> runAsync(Network network,
                                                                         SensitivityFactorsProvider factorsProvider) {
            return runAsync(network, factorsProvider, SensitivityComputationParameters.load());
        }

        public SensitivityComputationResults run(Network network,
                                                 String workingStateId,
                                                 SensitivityFactorsProvider factorsProvider,
                                                 ContingenciesProvider contingenciesProvider,
                                                 SensitivityComputationParameters parameters,
                                                 ComputationManager computationManager) {
            checkInputs(network, workingStateId, factorsProvider, contingenciesProvider, parameters, computationManager);
            return provider.run(network, workingStateId, factorsProvider, contingenciesProvider, parameters, computationManager).join();
        }

        public SensitivityComputationResults run(Network network,
                                                 String workingStateId,
                                                 SensitivityFactorsProvider factorsProvider,
                                                 ContingenciesProvider contingenciesProvider,
                                                 SensitivityComputationParameters parameters) {
            return run(network, workingStateId, factorsProvider, contingenciesProvider, parameters, DefaultComputationManagerConfig.load().createLongTimeExecutionComputationManager());
        }

        public SensitivityComputationResults run(Network network,
                                                 SensitivityFactorsProvider factorsProvider,
                                                 ContingenciesProvider contingenciesProvider,
                                                 SensitivityComputationParameters parameters) {
            return run(network, network.getVariantManager().getWorkingVariantId(), factorsProvider, contingenciesProvider, parameters);
        }

        public SensitivityComputationResults run(Network network,
                                                 SensitivityFactorsProvider factorsProvider,
                                                 ContingenciesProvider contingenciesProvider) {
            return run(network, factorsProvider, contingenciesProvider, SensitivityComputationParameters.load());
        }

        public SensitivityComputationResults run(Network network,
                                                 String workingStateId,
                                                 SensitivityFactorsProvider factorsProvider,
                                                 SensitivityComputationParameters parameters,
                                                 ComputationManager computationManager) {
            checkInputs(network, workingStateId, factorsProvider, parameters, computationManager);
            return provider.run(network, workingStateId, factorsProvider, parameters, computationManager).join();
        }

        public SensitivityComputationResults run(Network network,
                                                 String workingStateId,
                                                 SensitivityFactorsProvider factorsProvider,
                                                 SensitivityComputationParameters parameters) {
            return run(network, workingStateId, factorsProvider, parameters, DefaultComputationManagerConfig.load().createLongTimeExecutionComputationManager());
        }

        public SensitivityComputationResults run(Network network,
                                                 SensitivityFactorsProvider factorsProvider,
                                                 SensitivityComputationParameters parameters) {
            return run(network, network.getVariantManager().getWorkingVariantId(), factorsProvider, parameters);
        }

        public SensitivityComputationResults run(Network network,
                                                 SensitivityFactorsProvider factorsProvider) {
            return run(network, factorsProvider, SensitivityComputationParameters.load());
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
     * Get a runner for a sensitivity computation named {@code name}. In the case of a null {@code name}, default
     * implementation is used.
     *
     * @param name name of the sensitivity computation implementation, null if we want to use default one
     * @return a runner for sensitivity computation implementation named {@code name}
     */
    private static SensitivityComputationRunner find(String name) {
        return find(name, SENSITIVITY_COMPUTATION_PROVIDERS.get(), PlatformConfig.defaultConfig());
    }

    /**
     * Get a runner for default sensitivity computation implementation.
     *
     * @throws PowsyblException in case we cannot find a default implementation
     * @return a runner for default sensitivity computation implementation
     */
    private static SensitivityComputationRunner find() {
        return find(null);
    }

    /**
     * A variant of {@link SensitivityComputation#find(String)} intended to be used for unit testing that allow passing
     * an explicit provider list instead of relying on service loader and an explicit {@link PlatformConfig}
     * instead of global one.
     *
     * @param name name of the sensitivity computation implementation, null if we want to use default one
     * @param providers sensitivity computation provider list
     * @param platformConfig platform config to look for default sensitivity computation implementation name
     * @return a runner for sensitivity computation implementation named {@code name}
     */
    static SensitivityComputationRunner find(String name, List<SensitivityComputationProvider> providers, PlatformConfig platformConfig) {
        Objects.requireNonNull(providers);
        Objects.requireNonNull(platformConfig);

        if (providers.isEmpty()) {
            throw new PowsyblException("No sensitivity computation provider found");
        }

        // if no sensitivity computation implementation name is provided through the API we look for information
        // in platform configuration
        String sensitivityComputationName = name != null ? name : platformConfig.getOptionalModuleConfig("sensitivity-computation")
            .flatMap(mc -> mc.getOptionalStringProperty("default"))
            .orElse(null);
        SensitivityComputationProvider provider;
        if (providers.size() == 1 && sensitivityComputationName == null) {
            // no information to select the implementation but only one provider, so we can use it by default
            // (that is be the most common use case)
            provider = providers.get(0);
        } else {
            if (providers.size() > 1 && sensitivityComputationName == null) {
                // several providers and no information to select which one to choose, we can only throw
                // an exception
                List<String> providerNames = providers.stream().map(SensitivityComputationProvider::getName).collect(Collectors.toList());
                throw new PowsyblException("Several sensitivity computation implementations found (" + providerNames
                    + "), you must add configuration to select the implementation");
            }
            provider = providers.stream()
                .filter(p -> p.getName().equals(sensitivityComputationName))
                .findFirst()
                .orElseThrow(() -> new PowsyblException("Sensitivity computation '" + sensitivityComputationName + "' not found"));
        }

        return new SensitivityComputationRunner(provider);
    }

    public static CompletableFuture<SensitivityComputationResults> runAsync(Network network,
                                                                            SensitivityFactorsProvider factorsProvider,
                                                                            ContingenciesProvider contingenciesProvider) {
        return find().runAsync(network, factorsProvider, contingenciesProvider);
    }

    public static CompletableFuture<SensitivityComputationResults> runAsync(Network network,
                                                                            SensitivityFactorsProvider factorsProvider,
                                                                            ContingenciesProvider contingenciesProvider,
                                                                            SensitivityComputationParameters parameters) {
        return find().runAsync(network, factorsProvider, contingenciesProvider, parameters);
    }

    public static CompletableFuture<SensitivityComputationResults> runAsync(Network network,
                                                                            SensitivityFactorsProvider factorsProvider) {
        return find().runAsync(network, factorsProvider);
    }

    public static CompletableFuture<SensitivityComputationResults> runAsync(Network network,
                                                                            SensitivityFactorsProvider factorsProvider,
                                                                            SensitivityComputationParameters parameters) {
        return find().runAsync(network, factorsProvider, parameters);
    }

    public static SensitivityComputationResults run(Network network,
                                                    SensitivityFactorsProvider factorsProvider,
                                                    ContingenciesProvider contingenciesProvider) {
        return find().run(network, factorsProvider, contingenciesProvider);
    }

    public static SensitivityComputationResults run(Network network,
                                                    SensitivityFactorsProvider factorsProvider,
                                                    ContingenciesProvider contingenciesProvider,
                                                    SensitivityComputationParameters parameters) {
        return find().run(network, factorsProvider, contingenciesProvider, parameters);
    }

    public static SensitivityComputationResults run(Network network,
                                                    SensitivityFactorsProvider factorsProvider) {
        return find().run(network, factorsProvider);
    }

    public static SensitivityComputationResults run(Network network,
                                                    SensitivityFactorsProvider factorsProvider,
                                                    SensitivityComputationParameters parameters) {
        return find().run(network, factorsProvider, parameters);
    }
}
