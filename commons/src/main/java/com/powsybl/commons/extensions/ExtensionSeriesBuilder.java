/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.extensions;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;

/**
 * @author Yichen TANG <yichen.tang at rte-france.com>
 */
public interface ExtensionSeriesBuilder<B, T> {

    B addBooleanSeries(String seriesName, Predicate<T> booleanGetter);

    B addDoubleSeries(String seriesName, ToDoubleFunction<T> doubleGetter);

    B addStringSeries(String seriesName, Function<T, String> stringGetter);

    <U> B addIntSeries(String seriesName, Function<T, U> objectGetter, ToIntFunction<U> intGetter, int undefinedValue);
}
