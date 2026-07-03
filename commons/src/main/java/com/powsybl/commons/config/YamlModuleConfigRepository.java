/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.config;

import com.powsybl.commons.PowsyblException;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;

/**
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class YamlModuleConfigRepository extends AbstractModuleConfigRepository {

    public YamlModuleConfigRepository(Path yamlConfigFile) {
        Objects.requireNonNull(yamlConfigFile);

        try (Reader reader = Files.newBufferedReader(yamlConfigFile, StandardCharsets.UTF_8)) {
            load(reader, yamlConfigFile.getFileSystem());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Reads the YAML configuration from an arbitrary input stream, typically a classpath
     * resource bundled inside a jar. The provided {@link FileSystem} is used to resolve
     * path-typed properties. The stream is closed by this constructor.
     */
    public YamlModuleConfigRepository(InputStream inputStream, FileSystem fileSystem) {
        Objects.requireNonNull(inputStream);
        Objects.requireNonNull(fileSystem);

        try (Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
            load(reader, fileSystem);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void load(Reader reader, FileSystem fileSystem) {
        Yaml yaml = new Yaml();
        Object data = yaml.load(reader);
        if (!(data instanceof Map)) {
            throw new PowsyblException("Named modules are expected at the first level of the YAML");
        }
        for (Map.Entry<String, Object> e : ((Map<String, Object>) data).entrySet()) {
            String moduleName = e.getKey();
            if (!(e.getValue() instanceof Map)) {
                throw new PowsyblException("Properties are expected at the second level of the YAML");
            }
            configs.put(moduleName, new MapModuleConfig((Map<Object, Object>) e.getValue(), fileSystem));
        }
    }
}
