/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.import_;

import com.powsybl.commons.Versionable;
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
public class ImportConfig implements Versionable {

    private static final String CONFIG_MODULE_NAME = "import";

    private static final List<String> DEFAULT_POST_PROCESSORS = Collections.emptyList();

    private static final String DEFAULT_CONFIG_VERSION = "1.0";

    private VersionConfig version = new VersionConfig(DEFAULT_CONFIG_VERSION);

    private final List<String> postProcessors;

    public static ImportConfig load() {
        return load(PlatformConfig.defaultConfig());
    }

    public static ImportConfig load(PlatformConfig platformConfig) {
        Objects.requireNonNull(platformConfig);
        VersionConfig version = new VersionConfig(DEFAULT_CONFIG_VERSION);
        List<String> postProcessors = DEFAULT_POST_PROCESSORS;
        if (platformConfig.moduleExists(CONFIG_MODULE_NAME)) {
            ModuleConfig config = platformConfig.getModuleConfig(CONFIG_MODULE_NAME);
            version = config.hasProperty("version") ? new VersionConfig(config.getStringProperty("version")) : version;
            if (version.equalsOrIsNewerThan("1.1")) {
                postProcessors = config.getStringListProperty("post-processors", DEFAULT_POST_PROCESSORS);
            } else {
                postProcessors = config.getStringListProperty("postProcessors", DEFAULT_POST_PROCESSORS);
            }
        }
        return new ImportConfig(version, postProcessors);
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

    public ImportConfig(VersionConfig version, List<String> postProcessors) {
        this(postProcessors);
        this.version = version;
    }

    public List<String> getPostProcessors() {
        return postProcessors;
    }

    @Override
    public String toString() {
        return "{postProcessors=" + postProcessors
                + "}";
    }

    @Override
    public String getName() {
        return CONFIG_MODULE_NAME;
    }

    @Override
    public String getVersion() {
        return this.version.toString();
    }
}
