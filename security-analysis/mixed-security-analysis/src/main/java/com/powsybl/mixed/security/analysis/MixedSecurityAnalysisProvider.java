package com.powsybl.mixed.security.analysis;

import com.google.auto.service.AutoService;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.contingency.ContingenciesProvider;
import com.powsybl.iidm.network.Network;
import com.powsybl.mixed.security.analysis.parameters.MixedModeParametersExtension;
import com.powsybl.security.SecurityAnalysisParameters;
import com.powsybl.security.SecurityAnalysisProvider;
import com.powsybl.security.SecurityAnalysisReport;
import com.powsybl.security.SecurityAnalysisRunParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * {@link SecurityAnalysisProvider} orchestrating a static pass followed by a selective
 * dynamic pass, discovered automatically via {@link java.util.ServiceLoader}.
 *
 * @author Riad Benradi {@literal <riad.benradi at rte-france.com>}
 */

@AutoService({SecurityAnalysisProvider.class})
public class MixedSecurityAnalysisProvider implements SecurityAnalysisProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(MixedSecurityAnalysisProvider.class);

    private static final String PROVIDER_NAME = "MixedSecurityAnalysis";

    /** @return {@code "MixedSecurityAnalysis"} */
    @Override
    public String getName() {
        return PROVIDER_NAME;
    }

    /** @return the provider version */
    @Override
    public String getVersion() {
        return "1.0.0-SNAPSHOT";
    }

    /**
     * Reads {@link MixedModeParametersExtension} from {@code runParameters} and delegates
     * to {@link MixedSecurityAnalysis}.
     *
     * @throws IllegalArgumentException if {@link MixedModeParametersExtension} is absent
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
        LOGGER.info("Starting mixed-mode security analysis for network: {}", network.getId());

        MixedModeParametersExtension extension = runParameters.getSecurityAnalysisParameters().getExtension(MixedModeParametersExtension.class);
        if(extension == null) {

            LOGGER.warn("MixedModeParametersExtension configuration is missing from SecurityAnalysisRunParameters, using defaults.");
            extension = (MixedModeParametersExtension) loadSpecificParameters(PlatformConfig.defaultConfig()).orElseThrow(() ->
                    new IllegalArgumentException("Failed to load MixedModeParametersExtension configuration"));
        }
        LOGGER.debug("Configuration loaded - Static simulator: {}, Dynamic simulator: {}, " +
                        "Switch criteria: {}",
                extension.getStaticSimulator(),
                extension.getDynamicSimulator(),
                extension.getSwitchCriteria());

        MixedSecurityAnalysis analysis = new MixedSecurityAnalysis(
                network,
                workingVariantId,
                contingenciesProvider,
                runParameters,
                extension
        );

        return analysis.run();
    }

    @Override
    public Optional<Extension<SecurityAnalysisParameters>> loadSpecificParameters(PlatformConfig config) {
        MixedModeParametersExtension ext = new MixedModeParametersExtension();

        // Default values — used if the key is absent from the config file
        ext.setStaticSimulator("load-flow");
        ext.setDynamicSimulator("dynaflow");
        ext.setSwitchCriteria(List.of("NOT_CONVERGED"));

        // Override with values from config file if the section exists
        // Reads from a YAML block named "mixed-mode-analysis"
        config.getOptionalModuleConfig(MixedModeParametersExtension.NAME).ifPresent(module -> {
            ext.setStaticSimulator(module.getStringProperty("static-simulator", ext.getStaticSimulator()));
            ext.setDynamicSimulator(module.getStringProperty("dynamic-simulator", ext.getDynamicSimulator()));
            ext.setSwitchCriteria(module.getStringListProperty("switch-criteria", ext.getSwitchCriteria()));
        });

        return Optional.of(ext);
    }
}
