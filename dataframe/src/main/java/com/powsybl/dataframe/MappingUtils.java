/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.dataframe;

import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;

/**
 * Utility methods to help mapping objects to values.
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 * @author Sylvain Leclerc <sylvain.leclerc at rte-france.com>
 */
public final class MappingUtils {

    private MappingUtils() {
    }

    public static final int INT_UNDEFINED_VALUE = -99999;

    /**
     * Maps T to another object U and returns the specified undefined value if U is {@code null}
     */
    public static <T, U> ToDoubleFunction<T> ifExistsDouble(Function<T, U> objectGetter,
                                                            ToDoubleFunction<U> valueGetter, double undefinedValue) {
        return item -> {
            U object = objectGetter.apply(item);
            return object != null ? valueGetter.applyAsDouble(object) : undefinedValue;
        };
    }

    /**
     * Maps T to another object U and returns {@link Double#NaN} if U is {@code null}
     */
    public static <T, U> ToDoubleFunction<T> ifExistsDouble(Function<T, U> objectGetter,
                                                            ToDoubleFunction<U> valueGetter) {
        return ifExistsDouble(objectGetter, valueGetter, Double.NaN);
    }

    /**
     * Maps T to another object U and returns the specified undefined value if U is {@code null}
     */
    public static <T, U> ToIntFunction<T> ifExistsInt(Function<T, U> objectGetter, ToIntFunction<U> valueGetter,
                                                      int undefinedValue) {
        return item -> {
            U object = objectGetter.apply(item);
            return object != null ? valueGetter.applyAsInt(object) : undefinedValue;
        };
    }

    /**
     * Maps T to another object U and returns {@link MappingUtils#INT_UNDEFINED_VALUE} if U is {@code null}
     */
    public static <T, U> ToIntFunction<T> ifExistsInt(Function<T, U> objectGetter, ToIntFunction<U> valueGetter) {
        return ifExistsInt(objectGetter, valueGetter, INT_UNDEFINED_VALUE);
    }
}
