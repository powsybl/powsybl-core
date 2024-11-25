/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network;

import com.google.common.base.Suppliers;
import com.powsybl.commons.config.PlatformConfig;

import java.util.*;
import java.util.function.Supplier;

/**
 *
 * Generic configuration for network importing. Specifically, this allows to configure
 * {@link ImportPostProcessor}s to be applied after networks are imported.
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class ImportConfig {

    public static final Supplier<ImportConfig> CACHE = Suppliers.memoize(ImportConfig::load);

    private static final List<String> DEFAULT_POST_PROCESSORS = new ArrayList<>();

    private final List<String> postProcessors;

    public static ImportConfig load() {
        return load(PlatformConfig.defaultConfig());
    }

    public static ImportConfig load(PlatformConfig platformConfig) {
        Objects.requireNonNull(platformConfig);
        List<String> postProcessors = platformConfig.getOptionalModuleConfig("import")
                .flatMap(config -> config.getOptionalStringListProperty("postProcessors"))
                .orElse(DEFAULT_POST_PROCESSORS);
        return new ImportConfig(postProcessors);
    }

    public ImportConfig() {
        this(new ArrayList<>());
    }

    public ImportConfig(String... postProcessors) {
        this(new ArrayList<>(Arrays.asList(postProcessors)));
    }

    public ImportConfig(List<String> postProcessors) {
        this.postProcessors = Objects.requireNonNull(postProcessors);
    }

    /**
     * The immutable list of enabled {@link ImportPostProcessor}, defined by their name.
     * @return the immutable list of enabled {@link ImportPostProcessor}, defined by their name.
     */
    public List<String> getPostProcessors() {
        return Collections.unmodifiableList(postProcessors);
    }

    public void addPostProcessors(List<String> postProcessors) {
        this.postProcessors.addAll(Objects.requireNonNull(postProcessors));
    }

    @Override
    public String toString() {
        return "{postProcessors=" + postProcessors
                + "}";
    }
}
