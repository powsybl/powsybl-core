/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.hybrid.security.analysis;

import com.google.auto.service.AutoService;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.contingency.ContingenciesProvider;
import com.powsybl.hybrid.security.analysis.parameters.HybridSecurityAnalysisParameters;
import com.powsybl.iidm.network.Network;
import com.powsybl.security.SecurityAnalysisParameters;
import com.powsybl.security.SecurityAnalysisProvider;
import com.powsybl.security.SecurityAnalysisReport;
import com.powsybl.security.SecurityAnalysisRunParameters;
import com.powsybl.tools.PowsyblCoreVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * {@link SecurityAnalysisProvider} orchestrating a first pass followed by a selective
 * second pass, discovered automatically via {@link java.util.ServiceLoader}.
 *
 * @author Riad Benradi {@literal <riad.benradi_externe at rte-france.com>} */

@AutoService(SecurityAnalysisProvider.class)
public class HybridSecurityAnalysisProvider implements SecurityAnalysisProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(HybridSecurityAnalysisProvider.class);

    private static final String PROVIDER_NAME = "HybridSecurityAnalysisHandler";

    /** @return {@code "HybridSecurityAnalysisHandler"} */
    @Override
    public String getName() {
        return PROVIDER_NAME;
    }

    /** @return the provider version */
    @Override
    public String getVersion() {
        return new PowsyblCoreVersion().toString();
    }

    /**
     * Reads {@link HybridSecurityAnalysisParameters} from {@code runParameters} and delegates
     * to {@link HybridSecurityAnalysisHandler}.
     *
     * @throws IllegalArgumentException if {@link HybridSecurityAnalysisParameters} is absent
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

        HybridSecurityAnalysisParameters hybridSecurityAnalysisParameters = runParameters.getSecurityAnalysisParameters().getExtension(HybridSecurityAnalysisParameters.class);
        if (hybridSecurityAnalysisParameters == null) {
            LOGGER.warn("HybridSecurityAnalysisParameters configuration is missing from SecurityAnalysisRunParameters, using defaults.");
            hybridSecurityAnalysisParameters = (HybridSecurityAnalysisParameters) loadSpecificParameters(PlatformConfig.defaultConfig()).orElseThrow(() ->
                    new IllegalArgumentException("Failed to load HybridSecurityAnalysisParameters configuration"));
        }
        LOGGER.debug("Configuration loaded - First provider: {}, Second provider: {}",
                hybridSecurityAnalysisParameters.getFirstProviderName(),
                hybridSecurityAnalysisParameters.getSecondProviderName());

        HybridSecurityAnalysisHandler analysis = new HybridSecurityAnalysisHandler(
                network,
                workingVariantId,
                contingenciesProvider,
                runParameters,
                hybridSecurityAnalysisParameters
        );

        return analysis.run();
    }

    @Override
    public Optional<Extension<SecurityAnalysisParameters>> loadSpecificParameters(PlatformConfig platformConfig) {
        return Optional.of(HybridSecurityAnalysisParameters.load(platformConfig));
    }
}
