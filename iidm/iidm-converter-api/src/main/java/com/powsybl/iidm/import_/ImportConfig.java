/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.import_;

import com.google.common.base.Suppliers;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.iidm.network.ValidationLevel;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/**
 *
 * Generic configuration for network importing. Specifically, this allows to configure
 * {@link ImportPostProcessor}s to be applied after networks are imported.
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ImportConfig {

    public static final Supplier<ImportConfig> CACHE = Suppliers.memoize(ImportConfig::load);

    private static final List<String> DEFAULT_POST_PROCESSORS = Collections.emptyList();
    private static final ValidationLevel DEFAULT_VALIDATION_LEVEL = ValidationLevel.STEADY_STATE_HYPOTHESIS;

    private final List<String> postProcessors;
    private final ValidationLevel validationLevel;

    public static ImportConfig load() {
        return load(PlatformConfig.defaultConfig());
    }

    public static ImportConfig load(PlatformConfig platformConfig) {
        Objects.requireNonNull(platformConfig);
        List<String> postProcessors = platformConfig.getOptionalModuleConfig("import")
                .flatMap(config -> config.getOptionalStringListProperty("postProcessors"))
                .orElse(DEFAULT_POST_PROCESSORS);
        ValidationLevel validationLevel = platformConfig.getOptionalModuleConfig("import")
                .flatMap(config -> config.getOptionalStringProperty("validationLevel"))
                .map(ValidationLevel::valueOf)
                .orElse(DEFAULT_VALIDATION_LEVEL);
        return new ImportConfig(postProcessors, validationLevel);
    }

    public ImportConfig() {
        this(Collections.emptyList(), DEFAULT_VALIDATION_LEVEL);
    }

    public ImportConfig(String... postProcessors) {
        this(Arrays.asList(postProcessors), DEFAULT_VALIDATION_LEVEL);
    }

    public ImportConfig(List<String> postProcessors, ValidationLevel validationLevel) {
        this.postProcessors = Objects.requireNonNull(postProcessors);
        this.validationLevel = Objects.requireNonNull(validationLevel);
    }

    /**
     * The list of enabled {@link ImportPostProcessor}, defined by their name.
     * @return the list of enabled {@link ImportPostProcessor}, defined by their name.
     */
    public List<String> getPostProcessors() {
        return postProcessors;
    }

    /**
     * The minimum validation level for the network being imported
     * @return the minimum validation level for the network to be imported.
     */
    public ValidationLevel getValidationLevel() {
        return validationLevel;
    }

    @Override
    public String toString() {
        return "{postProcessors=" + postProcessors + ", "
                + "validationLevel=" + validationLevel
                + "}";
    }
}
