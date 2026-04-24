package com.powsybl.hybrid.security.analysis;

import com.google.auto.service.AutoService;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.contingency.ContingenciesProvider;
import com.powsybl.iidm.network.Network;
import com.powsybl.hybrid.security.analysis.parameters.HybridModeParametersExtension;
import com.powsybl.security.SecurityAnalysisProvider;
import com.powsybl.security.SecurityAnalysisReport;
import com.powsybl.security.SecurityAnalysisRunParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * {@link SecurityAnalysisProvider} orchestrating a first pass followed by a selective
 * second pass, discovered automatically via {@link java.util.ServiceLoader}.
 *
 * @author Riad Benradi {@literal <riad.benradi at rte-france.com>}
 */

@AutoService({SecurityAnalysisProvider.class})
public class HybridSecurityAnalysisProvider implements SecurityAnalysisProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(HybridSecurityAnalysisProvider.class);

    private static final String PROVIDER_NAME = "HybridSecurityAnalysis";

    /** @return {@code "HybridSecurityAnalysis"} */
    @Override
    public String getName() {
        return PROVIDER_NAME;
    }

    /** @return the provider version */
    @Override
    public String getVersion() {
        return getClass().getPackage().getImplementationVersion();
    }

    /**
     * Reads {@link HybridModeParametersExtension} from {@code runParameters} and delegates
     * to {@link HybridSecurityAnalysis}.
     *
     * @throws IllegalArgumentException if {@link HybridModeParametersExtension} is absent
     */
    @Override
    public CompletableFuture<SecurityAnalysisReport> run(Network network,
                                                         String workingVariantId,
                                                         ContingenciesProvider contingenciesProvider,
                                                         SecurityAnalysisRunParameters runParameters) {

        Objects.requireNonNull(network);
        Objects.requireNonNull(workingVariantId);
        Objects.requireNonNull(contingenciesProvider);
        Objects.requireNonNull(runParameters);
        LOGGER.info("Starting hybrid-mode security analysis for network: {}", network.getId());

        HybridModeParametersExtension extension = runParameters.getSecurityAnalysisParameters().getExtension(HybridModeParametersExtension.class);
        if (extension == null) {
            LOGGER.warn("HybridModeParametersExtension configuration is missing from SecurityAnalysisRunParameters, using defaults.");
            extension = (HybridModeParametersExtension) loadSpecificParameters(PlatformConfig.defaultConfig()).orElseThrow(() ->
                    new IllegalArgumentException("Failed to load HybridModeParametersExtension configuration"));
        }
        LOGGER.debug("Configuration loaded - First provider: {}, Second provider: {}",
                extension.getFirstProviderName(),
                extension.getSecondProviderName());

        HybridSecurityAnalysis analysis = new HybridSecurityAnalysis(
                network,
                workingVariantId,
                contingenciesProvider,
                runParameters,
                extension
        );

        return analysis.run();
    }
}
