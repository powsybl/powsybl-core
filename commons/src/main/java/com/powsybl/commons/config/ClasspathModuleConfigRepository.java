/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.config;

import com.powsybl.commons.PowsyblException;

import java.io.InputStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.util.Objects;
import java.util.Set;

/**
 * A {@link ModuleConfigRepository} reading a YAML configuration bundled as a classpath resource
 * (typically {@code /META-INF/powsybl/config.yml} inside a jar). This is the backing repository
 * for the {@link DefaultConfigProvider} mechanism.
 *
 * @author Gautier Bureau {@literal <gautier.bureau at gmail.com>}
 */
public class ClasspathModuleConfigRepository extends YamlModuleConfigRepository {

    public ClasspathModuleConfigRepository(String resourceName, ClassLoader classLoader) {
        this(resourceName, classLoader, FileSystems.getDefault());
    }

    public ClasspathModuleConfigRepository(String resourceName, ClassLoader classLoader, FileSystem fileSystem) {
        super(openResource(resourceName, classLoader), fileSystem);
    }

    private static InputStream openResource(String resourceName, ClassLoader classLoader) {
        Objects.requireNonNull(resourceName);
        Objects.requireNonNull(classLoader);
        // ClassLoader.getResourceAsStream expects a path without a leading slash
        String normalizedName = resourceName.startsWith("/") ? resourceName.substring(1) : resourceName;
        InputStream inputStream = classLoader.getResourceAsStream(normalizedName);
        if (inputStream == null) {
            throw new PowsyblException("Bundled default configuration resource not found in classpath: " + resourceName);
        }
        return inputStream;
    }

    /**
     * The set of module names defined by this bundled configuration. Used to detect overlapping
     * defaults across providers.
     */
    public Set<String> getModuleNames() {
        return configs.keySet();
    }
}
