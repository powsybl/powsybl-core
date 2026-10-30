/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.config;

import com.google.auto.service.AutoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Reads the runtime configuration from {@value #RESOURCE_PATH} resources of the class path,
 * so that an application can ship its default values in a jar.
 *
 * <p>Several jars may carry the resource. Each one may rank itself against the others in a
 * reserved {@code powsybl-runtime-config} module:
 *
 * <pre>
 * powsybl-runtime-config:
 *     priority: 100
 * </pre>
 *
 * <p>The default priority is 0, higher wins, and resources of equal priority are ordered by
 * URL so the result never depends on the order of the class path. That priority ranks these
 * resources against each other only: this provider itself takes part in the ordering of the
 * runtime configuration providers with the default priority of 0.
 *
 * @author Fabrice Buscaylet {@literal <fabrice.buscaylet at artelys.com>}
 */
@AutoService(RuntimeConfigProvider.class)
public class ClasspathRuntimeConfigProvider implements RuntimeConfigProvider {

    public static final String RESOURCE_PATH = "com/powsybl/config/config.yml";

    /**
     * Name of the module that a {@value #RESOURCE_PATH} resource may use to declare its own
     * priority. It is reserved: removed from the exposed configuration and cannot be read back
     * as an ordinary module.
     */
    public static final String RESERVED_MODULE = "powsybl-runtime-config";

    private static final Logger LOGGER = LoggerFactory.getLogger(ClasspathRuntimeConfigProvider.class);

    private static final String NAME = "classpath";

    private static final String PRIORITY_PROPERTY = "priority";

    private final ClassLoader classLoader;

    public ClasspathRuntimeConfigProvider() {
        this(ClasspathRuntimeConfigProvider.class.getClassLoader());
    }

    ClasspathRuntimeConfigProvider(ClassLoader classLoader) {
        this.classLoader = Objects.requireNonNull(classLoader);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Optional<ModuleConfigRepository> getModuleConfigRepository() {
        List<URL> urls = findResources();
        if (urls.isEmpty()) {
            LOGGER.debug("No {} resource found on the class path", RESOURCE_PATH);
            return Optional.empty();
        }

        List<RankedRepository> ranked = new ArrayList<>();
        for (URL url : urls) {
            ranked.add(read(url));
        }
        ranked.sort(Comparator.comparingInt(RankedRepository::priority).reversed()
                .thenComparing(RankedRepository::url));
        LOGGER.info("Runtime configuration read from class path resources, in decreasing order of precedence: {}",
                ranked.stream().map(RankedRepository::url).toList());

        // Done here, while the resources are still separate: once stacked, an overlap between two jars is invisible.
        RuntimeConfigRepositories.warnOverlappingProperties(
                ranked.stream().map(RankedRepository::url).toList(),
                ranked.stream().map(r -> (ModuleConfigRepository) new WithoutReservedModule(r.repository())).toList());

        ModuleConfigRepository stacked = new StackedModuleConfigRepository(
                ranked.stream().map(RankedRepository::repository).toList());
        return Optional.of(new WithoutReservedModule(stacked));
    }

    private List<URL> findResources() {
        try {
            return Collections.list(classLoader.getResources(RESOURCE_PATH));
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to scan the class path for " + RESOURCE_PATH, e);
        }
    }

    private static RankedRepository read(URL url) {
        try (Reader reader = new InputStreamReader(url.openStream(), StandardCharsets.UTF_8)) {
            ModuleConfigRepository repository = new YamlModuleConfigRepository(reader, FileSystems.getDefault());
            return new RankedRepository(url.toString(), readPriority(repository), repository);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read the runtime configuration resource " + url, e);
        } catch (Exception e) {
            // Catches a malformed structure (PowsyblException from YamlModuleConfigRepository), a YAML
            // syntax error (SnakeYAML's YAMLException) and an invalid priority, so that whichever jar carries the broken
            // resource is named in the failure, not just the "classpath" provider as a whole.
            throw new ConfigurationException("Failed to parse the runtime configuration resource " + url, e);
        }
    }

    private static int readPriority(ModuleConfigRepository repository) {
        return repository.getModuleConfig(RESERVED_MODULE)
                .map(config -> config.getOptionalIntProperty(PRIORITY_PROPERTY).orElse(0))
                .orElse(0);
    }

    /**
     * Hides the reserved module from the stacked resources. A named class rather than a lambda so
     * that the modules stay enumerable for the overlap diagnostic of {@link RuntimeConfigRepositories}.
     */
    private record WithoutReservedModule(ModuleConfigRepository delegate) implements ModuleConfigRepository {

        @Override
        public Optional<ModuleConfig> getModuleConfig(String name) {
            return RESERVED_MODULE.equals(name) ? Optional.empty() : delegate.getModuleConfig(name);
        }

        @Override
        public Set<String> getModuleNames() {
            Set<String> names = new LinkedHashSet<>(delegate.getModuleNames());
            names.remove(RESERVED_MODULE);
            return names;
        }
    }

    private record RankedRepository(String url, int priority, ModuleConfigRepository repository) {
    }
}
