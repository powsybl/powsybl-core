/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.import_;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.config.ModuleConfig;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.commons.config.VersionConfig;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ImportConfig {

    private static final VersionConfig DEFAULT_VERSION = VersionConfig.LATEST_VERSION;

    private static final List<String> DEFAULT_POST_PROCESSORS = Collections.emptyList();

    private final VersionConfig version;

    private final List<String> postProcessors;

    public static ImportConfig load() {
        return load(PlatformConfig.defaultConfig());
    }

    public static ImportConfig load(PlatformConfig platformConfig) {
        Objects.requireNonNull(platformConfig);
        List<String> postProcessors;
        VersionConfig version = platformConfig.getVersion();
        if (platformConfig.moduleExists("import")) {
            ModuleConfig config = platformConfig.getModuleConfig("import");
            version = config.hasProperty("version") ? VersionConfig.valueOfByString(config.getStringProperty("version")) : version;
            switch (version) {
                case VERSION_1_0:
                    postProcessors = config.getStringListProperty("postProcessors", DEFAULT_POST_PROCESSORS);
                    break;
                case LATEST_VERSION:
                    postProcessors = config.getStringListProperty("post-processors", DEFAULT_POST_PROCESSORS);
                    break;
                default:
                    throw new PowsyblException("Unexpected module version : this version is not supported");
            }
        } else {
            postProcessors = DEFAULT_POST_PROCESSORS;
        }
        return new ImportConfig(version, postProcessors);
    }

    public ImportConfig() {
        this(DEFAULT_VERSION);
    }

    public ImportConfig(VersionConfig version) {
        this(version, Collections.emptyList());
    }

    public ImportConfig(VersionConfig version, String... postProcessors) {
        this(version, Arrays.asList(postProcessors));
    }

    public ImportConfig(VersionConfig version, List<String> postProcessors) {
        this.version = version;
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
