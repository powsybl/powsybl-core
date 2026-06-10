/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.psse.model.io;

import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
public record PsseFieldDefinition<T, V>(String fieldName, Class<V> classType, Function<T, V> getter,
                                        BiConsumer<T, V> setter, V defaultValue, boolean hasDefaultValue,
                                        Function<V, String> formatter, BinaryOperator<String> suffixAdder) {
}
