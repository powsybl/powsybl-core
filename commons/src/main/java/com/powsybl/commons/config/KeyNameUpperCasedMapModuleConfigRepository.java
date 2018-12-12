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
import java.util.*;
import java.util.function.UnaryOperator;

/**
 * @author Yichen TANG <yichen.tang at rte-france.com>
 */
public final class KeyNameUpperCasedMapModuleConfigRepository implements ModuleConfigRepository {

    private final Map<String, String> upperCasedMap = new HashMap<>();

    private final FileSystem fs;

    private final Map<String, KeyNameUpperCasedMapModuleConfig> cachedModuleConfigMap = new HashMap<>();

    private final Set<String> checkedNotExistsModuleName = new HashSet<>();

    static final String SEPARATOR = "__";

    static final UnaryOperator<String> ENV_VAR_FORMATTER = name -> {
        if (name.toLowerCase().equals(name)) {
            return CaseFormat.LOWER_HYPHEN.to(CaseFormat.UPPER_UNDERSCORE, name);
        } else {
            if (Character.isUpperCase(name.charAt(0))) {
                return CaseFormat.UPPER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, name);
            } else {
                return CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, name);
            }
        }
    };

    KeyNameUpperCasedMapModuleConfigRepository(Map<String, String> map, FileSystem fileSystem) {
        map.keySet().stream()
                .filter(k -> k.toUpperCase().equals(k))
                .filter(k -> k.contains(SEPARATOR))
                .forEach(k -> upperCasedMap.put(k, map.get(k)));
        fs = Objects.requireNonNull(fileSystem);
    }

    @Override
    public boolean moduleExists(String name) {
        if (Strings.isNullOrEmpty(name) || checkedNotExistsModuleName.contains(name)) {
            return false;
        }
        if (cachedModuleConfigMap.keySet().contains(name)) {
            return true;
        }

        boolean found = upperCasedMap.keySet().stream().anyMatch(k -> k.startsWith(ENV_VAR_FORMATTER.apply(name) + SEPARATOR));
        if (!found) {
            checkedNotExistsModuleName.add(name);
        }
        return found;
    }

    @Override
    public Optional<ModuleConfig> getModuleConfig(String name) {
        if (!moduleExists(name)) {
            return Optional.empty();
        }
        KeyNameUpperCasedMapModuleConfig cachedConfig = cachedModuleConfigMap.get(name);
        if (cachedConfig != null) {
            return Optional.of(cachedConfig);
        }

        // generate and cache
        cachedConfig = generateModuleConfig(name);
        cachedModuleConfigMap.put(name, cachedConfig);

        return Optional.of(cachedConfig);
    }

    private KeyNameUpperCasedMapModuleConfig generateModuleConfig(String name) {
        Map<Object, Object> map = new HashMap<>();
        upperCasedMap.keySet().stream().filter(k -> k.startsWith(ENV_VAR_FORMATTER.apply(name)))
                .forEach(k -> map.put((Object) k, (Object) upperCasedMap.get(k)));
        return new KeyNameUpperCasedMapModuleConfig(map, fs, name);
    }
}
