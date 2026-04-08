/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.preprocessor;

import com.google.common.io.ByteSource;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.commons.util.ServiceLoaderCache;
import com.powsybl.contingency.ContingenciesProvider;
import com.powsybl.contingency.ContingenciesProviderFactory;
import com.powsybl.security.SecurityAnalysisConfig;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

/**
 *
 * Provides instances of {@link SecurityAnalysisPreprocessor}s or factories.
 *
 * @author Sylvain Leclerc {@literal <sylvain.leclerc at rte-france.com>}
 */
public final class SecurityAnalysisPreprocessors {

    private SecurityAnalysisPreprocessors() {
    }

    /**
     * Look for a factory with specified name, among the factories loaded as services.
     * Throws if no matching factory is found.
     *
     * @param name the name identifying the requested factory.
     * @return the factory corresponding to the specified name.
     */
    public static SecurityAnalysisPreprocessorFactory factoryForName(String name) {
        requireNonNull(name);
        List<SecurityAnalysisPreprocessorFactory> factories = new ServiceLoaderCache<>(SecurityAnalysisPreprocessorFactory.class).getServices();
        return factories.stream()
                .filter(f -> f.getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new PowsyblException("Could not find any security analysis preprocessor for name " + name));
    }

    /**
     * The default preprocessor factory, based on default platform {@link com.powsybl.commons.config.PlatformConfig config}.
     *
     * @return The preprocessor factory as defined in default platform config.
     */
    public static Optional<SecurityAnalysisPreprocessorFactory> defaultConfiguredFactory() {
        return configuredFactory(PlatformConfig.defaultConfig());
    }

    /**
     * The preprocessor factory based on the specified platform {@link com.powsybl.commons.config.PlatformConfig config}.
     *
     * @return The preprocessor factory as defined in default platform config.
     */
    public static Optional<SecurityAnalysisPreprocessorFactory> configuredFactory(PlatformConfig platformConfig) {
        return configuredFactory(SecurityAnalysisConfig.load(platformConfig));
    }

    /**
     * The default preprocessor factory, based on default platform {@link com.powsybl.commons.config.PlatformConfig config}.
     *
     * <p>If a preprocessor is specified in {@literal security-analysis} config module, returns the
     * corresponding factory, else return the default factory which delegates data interpretation
     * to the configured {@link ContingenciesProviderFactory}.
     *
     * @return The preprocessor factory as defined in default platform config.
     */
    public static Optional<SecurityAnalysisPreprocessorFactory> configuredFactory(SecurityAnalysisConfig config) {
        return config.getPreprocessorName()
                .map(SecurityAnalysisPreprocessors::factoryForName);
    }

    /**
     * Create a preprocessor which will create and inject a {@link ContingenciesProvider} into
     * security analysis inputs, based on the specified factory and the specified
     * source of data (raw bytes to be interpreted by the factory).
     *
     * @param factory the factory in charge of creating the {@link ContingenciesProvider}.
     * @return a preprocessor which will create and inject a {@link ContingenciesProvider}.
     */
    public static SecurityAnalysisPreprocessorFactory wrap(ContingenciesProviderFactory factory) {
        return new ContingenciesProviderPreprocessorFactory(factory);
    }

    /**
     * Create a preprocessor which will create and inject a {@link ContingenciesProvider} into
     * security analysis inputs, based on the specified factory and the specified
     * source of data (raw bytes to be interpreted by the factory).
     *
     * @param factory the factory in charge of creating the {@link ContingenciesProvider}.
     * @param source the source of data to be used by the factory.
     * @return a preprocessor which will create and inject a {@link ContingenciesProvider}.
     */
    public static SecurityAnalysisPreprocessor contingenciesPreprocessor(ContingenciesProviderFactory factory,
                                                                   ByteSource source) {
        requireNonNull(factory);
        requireNonNull(source);
        return configuration -> configuration.setContingencies(newContingenciesProvider(factory, source));
    }

    private static ContingenciesProvider newContingenciesProvider(ContingenciesProviderFactory factory,
                                                                  ByteSource byteSource) {
        try (InputStream is = byteSource.openBufferedStream()) {
            return factory.create(is);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
