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
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.UnaryOperator;

/**
 * @author Yichen TANG <yichen.tang at rte-france.com>
 */
public final class KeyNameUpperCasedMapModuleConfigRepository implements ModuleConfigRepository {

    private final Map<String, String> upperCasedMap = new HashMap<>();

    private final FileSystem fs;

    static final String SEPARATOR = "__";

    static final UnaryOperator<String> UPPER_UNDERSCORE_FORMATTER = name -> {
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
        if (Strings.isNullOrEmpty(name)) {
            return false;
        }

        return upperCasedMap.keySet().stream().anyMatch(k -> k.startsWith(UPPER_UNDERSCORE_FORMATTER.apply(name) + SEPARATOR));
    }

    @Override
    public Optional<ModuleConfig> getModuleConfig(String name) {
        if (!moduleExists(name)) {
            return Optional.empty();
        }

        Map<Object, Object> map = new HashMap<>();
        upperCasedMap.keySet().stream().filter(k -> k.startsWith(UPPER_UNDERSCORE_FORMATTER.apply(name)))
                .forEach(k -> map.put((Object) k, (Object) upperCasedMap.get(k)));
        return Optional.of(new KeyNameUpperCasedMapModuleConfig(map, fs, name));
    }
}
