/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
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
 * A {@link ModuleConfigRepository} designed to read property values
 * from the map of environment variables.
 *
 * For a configuration property named "property-name" in module "module-name",
 * the expected environment variables name is MODULE_NAME__PROPERTY_NAME.
 * CamelCase names are also translated to underscore-separated names.
 *
 * @author Yichen TANG {@literal <yichen.tang at rte-france.com>}
 */
public final class EnvironmentModuleConfigRepository implements ModuleConfigRepository {

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

    private final Map<String, String> filteredEnvVarMap = new HashMap<>();

    private final FileSystem fs;

    public EnvironmentModuleConfigRepository(Map<String, String> map, FileSystem fileSystem) {
        fs = Objects.requireNonNull(fileSystem);

        Objects.requireNonNull(map);
        map.keySet().stream()
                .filter(k -> k.toUpperCase().equals(k))
                .filter(k -> k.contains(SEPARATOR))
                .forEach(k -> filteredEnvVarMap.put(k, map.get(k)));
    }

    @Override
    public Optional<ModuleConfig> getModuleConfig(String name) {
        if (Strings.isNullOrEmpty(name)) {
            return Optional.empty();
        }

        Map<Object, Object> map = new HashMap<>();
        filteredEnvVarMap.keySet().stream().filter(k -> k.startsWith(UPPER_UNDERSCORE_FORMATTER.apply(name) + SEPARATOR))
                .forEach(k -> map.put(k, filteredEnvVarMap.get(k)));
        return map.isEmpty() ? Optional.empty() : Optional.of(new EnvironmentMapModuleConfig(map, fs, name));
    }
}
