/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.import_;

import com.powsybl.commons.config.ModuleConfig;
import com.powsybl.commons.config.PlatformConfig;

import java.util.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ImportConfig {

    private final List<String> postProcessors;

    public static ImportConfig load() {
        return load(PlatformConfig.defaultConfig());
    }

    public static ImportConfig load(PlatformConfig platformConfig) {
        Objects.requireNonNull(platformConfig);
        List<String> postProcessors;
        if (platformConfig.moduleExists("import")) {
            ModuleConfig config = platformConfig.getModuleConfig("import");
            postProcessors = config.getStringListProperty("postProcessors", new ArrayList<>());
        } else {
            postProcessors = new ArrayList<>();
        }
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

    public List<String> getPostProcessors() {
        return postProcessors;
    }

    /**
     * This method (as well as {@link #setPostProcessors(String...)} exists to anticipate evolutions of this class
     * (i.e. addition of other attributes): it allows to set custom post processors on an ImportConfig instance
     * where all the future potential other attributes have been loaded from the configuration file.
     */
    public ImportConfig setPostProcessors(List<String> postProcessors) {
        this.postProcessors.clear();
        this.postProcessors.addAll(Objects.requireNonNull(postProcessors));
        return this;
    }

    public ImportConfig setPostProcessors(String... postProcessors) {
        return setPostProcessors(Arrays.asList(postProcessors));
    }

    public ImportConfig addPostProcessor(String postProcessor) {
        postProcessors.add(Objects.requireNonNull(postProcessor));
        return this;
    }

    public ImportConfig removePostProcessor(String postProcessor) {
        postProcessors.remove(postProcessor);
        return this;
    }

    public ImportConfig clearPostProcessors() {
        postProcessors.clear();
        return this;
    }

    @Override
    public String toString() {
        return "{postProcessors=" + postProcessors
                + "}";
    }
}
