package com.powsybl.iidm.export;

import com.powsybl.commons.config.ModuleConfig;
import com.powsybl.commons.config.PlatformConfig;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author Jérémy Labous <jlabous at silicom.fr>
 */
public class ExportConfig {

    private static final List<String> DEFAULT_POST_PROCESSORS = Collections.emptyList();

    private final List<String> postProcessors;

    public static ExportConfig load() {
        return load(PlatformConfig.defaultConfig());
    }

    public static ExportConfig load(PlatformConfig platformConfig) {
        Objects.requireNonNull(platformConfig);
        List<String> postProcessors;
        if (platformConfig.moduleExists("export")) {
            ModuleConfig config = platformConfig.getModuleConfig("export");
            postProcessors = config.getStringListProperty("postProcessors", DEFAULT_POST_PROCESSORS);
        } else {
            postProcessors = DEFAULT_POST_PROCESSORS;
        }
        return new ExportConfig(postProcessors);
    }

    public ExportConfig() {
        this(Collections.emptyList());
    }

    public ExportConfig(String... postProcessors) {
        this(Arrays.asList(postProcessors));
    }

    public ExportConfig(List<String> postProcessors) {
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
