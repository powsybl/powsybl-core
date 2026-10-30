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
            read(reader, yamlConfigFile.getFileSystem());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Reads the configuration from an already opened reader, for sources which are not files,
     * classpath resources in particular. The given file system is the one used to resolve
     * path typed properties.
     *
     * <p>The caller retains ownership of {@code reader}: this constructor reads from it but
     * does not close it.
     */
    public YamlModuleConfigRepository(Reader reader, FileSystem fileSystem) {
        Objects.requireNonNull(reader);
        Objects.requireNonNull(fileSystem);

        read(reader, fileSystem);
    }

    private void read(Reader reader, FileSystem fileSystem) {
        Yaml yaml = new Yaml();
        Object data = yaml.load(reader);
        if (data == null) {
            // empty or comment-only document: no modules
            return;
        }
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
