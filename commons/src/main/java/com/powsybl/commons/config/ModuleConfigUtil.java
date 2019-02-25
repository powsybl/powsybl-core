/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.config;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

/**
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
public final class ModuleConfigUtil {

    private ModuleConfigUtil() {
    }

    public static Optional<Boolean> getOptionalBooleanProperty(ModuleConfig moduleConfig, List<String> names) {
        return getOptionalProperty(moduleConfig, names, moduleConfig::getOptionalBooleanProperty);
    }

    public static Optional<String> getOptionalStringProperty(ModuleConfig moduleConfig, List<String> names) {
        return getOptionalProperty(moduleConfig, names, moduleConfig::getOptionalStringProperty);
    }

    public static Optional<List<String>> getOptionalStringListProperty(ModuleConfig moduleConfig, List<String> names) {
        return getOptionalProperty(moduleConfig, names, moduleConfig::getOptionalStringListProperty);
    }

    private static <T> Optional<T> getOptionalProperty(ModuleConfig moduleConfig, List<String> names, Function<String, Optional<T>> supplier) {
        Objects.requireNonNull(moduleConfig);
        Objects.requireNonNull(names);

        Optional<T> res = Optional.empty();
        Iterator<String> it = names.iterator();
        while (!res.isPresent() && it.hasNext()) {
            res = supplier.apply(it.next());
        }

        return res;
    }
}
