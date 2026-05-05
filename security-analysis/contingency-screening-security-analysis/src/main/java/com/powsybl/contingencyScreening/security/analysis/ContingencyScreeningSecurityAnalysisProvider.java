/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.contingencyScreening.security.analysis;

import com.google.auto.service.AutoService;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.contingency.ContingenciesProvider;
import com.powsybl.contingencyScreening.security.analysis.parameters.ContingencyScreeningSecurityAnalysisParameters;
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
public class ContingencyScreeningSecurityAnalysisProvider implements SecurityAnalysisProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(ContingencyScreeningSecurityAnalysisProvider.class);

    private static final String PROVIDER_NAME = "contingency-screening-security-analysis";

    /** @return {@code "ContingencyScreening"} */
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
     * Reads {@link ContingencyScreeningSecurityAnalysisParameters} from {@code runParameters} and delegates
     * to {@link ContingencyScreeningSecurityAnalysisHandler}.
     *
     * @throws IllegalArgumentException if {@link ContingencyScreeningSecurityAnalysisParameters} is absent
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
        LOGGER.info("Starting contingency screening security analysis for network: {}", network.getId());

        ContingencyScreeningSecurityAnalysisParameters parameters = runParameters.getSecurityAnalysisParameters().getExtension(ContingencyScreeningSecurityAnalysisParameters.class);
        if (parameters == null) {
            LOGGER.warn("ContingencyScreeningSecurityAnalysisParameters configuration is missing from SecurityAnalysisRunParameters, using defaults.");
            parameters = (ContingencyScreeningSecurityAnalysisParameters) loadSpecificParameters(PlatformConfig.defaultConfig()).orElseThrow(() ->
                    new IllegalArgumentException("Failed to load ContingencyScreeningSecurityAnalysisParameters configuration"));
        }
        LOGGER.debug("Configuration loaded - First provider: {}, Second provider: {}",
                parameters.getFirstProviderName(),
                parameters.getSecondProviderName());

        ContingencyScreeningSecurityAnalysisHandler analysis = new ContingencyScreeningSecurityAnalysisHandler(
                network,
                workingVariantId,
                contingenciesProvider,
                runParameters,
                parameters
        );

        return analysis.run();
    }

    @Override
    public Optional<Extension<SecurityAnalysisParameters>> loadSpecificParameters(PlatformConfig platformConfig) {
        return Optional.of(ContingencyScreeningSecurityAnalysisParameters.load(platformConfig));
    }
}
