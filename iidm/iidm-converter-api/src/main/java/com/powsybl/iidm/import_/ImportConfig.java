/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.import_;

import com.powsybl.commons.config.ModuleConfig;
import com.powsybl.commons.config.PlatformConfig;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ImportConfig {

    private static final List<String> DEFAULT_POST_PROCESSORS = Collections.emptyList();

    private final List<String> postProcessors;

    public static ImportConfig load() {
        return load(PlatformConfig.defaultConfig());
    }

    public static ImportConfig load(PlatformConfig platformConfig) {
        Objects.requireNonNull(platformConfig);
        List<String> postProcessors;
        if (platformConfig.moduleExists("import")) {
            ModuleConfig config = platformConfig.getModuleConfig("import");
            postProcessors = config.getStringListProperty("postProcessors", DEFAULT_POST_PROCESSORS);
        } else {
            postProcessors = DEFAULT_POST_PROCESSORS;
        }
        return new ImportConfig(postProcessors);
    }

    public ImportConfig() {
        this(Collections.emptyList());
    }

    public ImportConfig(String... postProcessors) {
        this(Arrays.asList(postProcessors));
    }

    public ImportConfig(List<String> postProcessors) {
        this.postProcessors = Objects.requireNonNull(postProcessors);
    }

    public List<String> getPostProcessors() {
        return postProcessors;
    }

    @Override
    public String toString() {
        return "{postProcessors=" + postProcessors
                + "}";
    }
}
