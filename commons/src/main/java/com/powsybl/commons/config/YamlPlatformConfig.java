/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.config;

import com.powsybl.commons.PowsyblException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class YamlPlatformConfig extends InMemoryPlatformConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(YamlPlatformConfig.class);

    protected YamlPlatformConfig(FileSystem fileSystem, Path configDir, Path cacheDir) {
        super(fileSystem, configDir, cacheDir);
    }

    public static Optional<PlatformConfig> create(FileSystem fileSystem, Path configDir, Path cacheDir, String configName) {
        Objects.requireNonNull(fileSystem);
        Objects.requireNonNull(configDir);
        Objects.requireNonNull(cacheDir);
        Objects.requireNonNull(configName);
        Path file = configDir.resolve(configName + ".yml");
        if (Files.exists(file)) {
            LOGGER.info("Platform configuration defined by YAML file {}", file);

            YamlPlatformConfig platformConfig = new YamlPlatformConfig(fileSystem, configDir, cacheDir);

            Yaml yaml = new Yaml();
            try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
                Object data = yaml.load(reader);
                if (!(data instanceof Map)) {
                    throw new PowsyblException("Named modules are expected at the first level of the YAML");
                }
                for (Map.Entry<String, Object> e : ((Map<String, Object>) data).entrySet()) {
                    String moduleName = e.getKey();
                    if (!(e.getValue() instanceof Map)) {
                        throw new PowsyblException("Properties are expected at the second level of the YAML");
                    }
                    ((InMemoryModuleConfigContainer) platformConfig.container).getConfigs()
                            .put(moduleName, new MapModuleConfig((Map<Object, Object>) e.getValue(), fileSystem));
                }
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }

            return Optional.of(platformConfig);
        } else {
            LOGGER.info("Platform configuration YAML file {} not found", file);

            return Optional.empty();
        }
    }
}
