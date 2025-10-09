/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.config;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * @author Mathieu Bague {@literal <mathieu.bague at rte-france.com>}
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

    public static OptionalDouble getOptionalDoubleProperty(ModuleConfig moduleConfig, List<String> names) {
        return getOptionalProperty(moduleConfig, names, moduleConfig::getOptionalDoubleProperty, OptionalDouble::empty, OptionalDouble::isPresent);
    }

    public static OptionalInt getOptionalIntProperty(ModuleConfig moduleConfig, List<String> names) {
        return getOptionalProperty(moduleConfig, names, moduleConfig::getOptionalIntProperty, OptionalInt::empty, OptionalInt::isPresent);
    }

    private static <T> Optional<T> getOptionalProperty(ModuleConfig moduleConfig, List<String> names, Function<String, Optional<T>> supplier) {
        return getOptionalProperty(moduleConfig, names, supplier, Optional::empty, Optional::isPresent);
    }

    private static <T> T getOptionalProperty(ModuleConfig moduleConfig, List<String> names, Function<String, T> supplier, Supplier<T> factory, Predicate<T> isPresent) {
        Objects.requireNonNull(moduleConfig);
        Objects.requireNonNull(names);
        T res = factory.get();
        Iterator<String> it = names.iterator();
        while (!isPresent.test(res) && it.hasNext()) {
            res = supplier.apply(it.next());
        }
        return res;
    }
}
