/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.config;

import com.google.common.base.CaseFormat;
import com.google.common.base.Strings;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.util.*;
import java.util.function.Function;

/**
 * @author Yichen TANG <yichen.tang at rte-france.com>
 */
public final class EnvironmentModuleConfigRepository implements ModuleConfigRepository {


    private static EnvironmentModuleConfigRepository instance = null;

    private static final Map<String, String> UPPER_CASE_ENV = new HashMap<>();

    private final FileSystem fs;

    private static final Map<String, KeyNameUpperCasedMapModuleConfig> CACHED_MODULE_CONFIG = new HashMap<>();

    private static final Set<String> CHECKED_NOT_EXISTS_MODULES = new HashSet<>();


    static final Function<String, String> ENV_VAR_FORMATTER = name -> {
        if (name.toLowerCase().equals(name)) {
            return CaseFormat.LOWER_HYPHEN.to(CaseFormat.UPPER_UNDERSCORE, name);
        } else {
            return CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, name);
        }
    };



    private EnvironmentModuleConfigRepository(Map<String, String> map, FileSystem fileSystem) {
        map.keySet().stream()
                .filter(k -> k.toUpperCase().equals(k))
                .filter(k -> k.contains("_")) // should contains at least one underscore
                .forEach(k -> UPPER_CASE_ENV.put(k, map.get(k)));
        fs = Objects.requireNonNull(fileSystem);
    }

    static EnvironmentModuleConfigRepository getInstance() {
        return init(System.getenv(), FileSystems.getDefault());
    }

    static EnvironmentModuleConfigRepository getInstanceForTest(Map<String, String> map, FileSystem fileSystem) {
        return init(map, fileSystem);
    }

    private static EnvironmentModuleConfigRepository init(Map<String, String> map, FileSystem fileSystem) {
        if (instance == null) {
            synchronized (EnvironmentModuleConfigRepository.class) {
                if (instance == null) {
                    instance = new EnvironmentModuleConfigRepository(map, fileSystem);
                }
            }
        }
        return instance;
    }

    @Override
    public boolean moduleExists(String name) {
        if (Strings.isNullOrEmpty(name) || CACHED_MODULE_CONFIG.keySet().contains(name)) {
            return true;
        }
        if (CHECKED_NOT_EXISTS_MODULES.contains(name)) {
            return false;
        }
        boolean found = UPPER_CASE_ENV.keySet().stream().anyMatch(k -> k.startsWith(ENV_VAR_FORMATTER.apply(name)));
        if (!found) {
            CHECKED_NOT_EXISTS_MODULES.add(name);
        }
        return found;
    }

    @Override
    public Optional<ModuleConfig> getModuleConfig(String name) {
        if (!moduleExists(name)) {
            return Optional.empty();
        }
        KeyNameUpperCasedMapModuleConfig cachedConfig = CACHED_MODULE_CONFIG.get(name);
        if (cachedConfig != null) {
            return Optional.of(cachedConfig);
        }

        // generate and cache
        cachedConfig = generateModuleConfig(name);
        CACHED_MODULE_CONFIG.put(name, cachedConfig);

        return Optional.of(cachedConfig);
    }

    private KeyNameUpperCasedMapModuleConfig generateModuleConfig(String name) {
        Map<Object, Object> map = new HashMap<>();
        UPPER_CASE_ENV.keySet().stream().filter(k -> k.startsWith(ENV_VAR_FORMATTER.apply(name)))
                .forEach(k -> map.put((Object) k, (Object) UPPER_CASE_ENV.get(k)));
        return new KeyNameUpperCasedMapModuleConfig(map, fs, name);
    }
}
