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
import com.powsybl.commons.config.ConfigVersion;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static com.powsybl.commons.config.ConfigVersion.DEFAULT_CONFIG_VERSION;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ImportConfig implements Versionable {

    private static final String CONFIG_MODULE_NAME = "import";

    private static final List<String> DEFAULT_POST_PROCESSORS = Collections.emptyList();

    private ConfigVersion version = new ConfigVersion(DEFAULT_CONFIG_VERSION);

    private final List<String> postProcessors;

    public static ImportConfig load() {
        return load(PlatformConfig.defaultConfig());
    }

    public static ImportConfig load(PlatformConfig platformConfig) {
        Objects.requireNonNull(platformConfig);
        return platformConfig.getOptionalModuleConfig(CONFIG_MODULE_NAME)
                .map(ImportConfig::load).orElseGet(() -> new ImportConfig(DEFAULT_POST_PROCESSORS));
    }

    private static ImportConfig load(ModuleConfig config) {
        ConfigVersion version = new ConfigVersion(config.getOptionalStringProperty("version").orElse(DEFAULT_CONFIG_VERSION));
        if (version.equalsOrIsNewerThan("1.1")) {
            return new ImportConfig(version, config.getStringListProperty("post-processors", DEFAULT_POST_PROCESSORS));
        } else {
            return new ImportConfig(version, config.getStringListProperty("postProcessors", DEFAULT_POST_PROCESSORS));
        }
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

    public ImportConfig(ConfigVersion version, List<String> postProcessors) {
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
        return version.toString();
    }
}
