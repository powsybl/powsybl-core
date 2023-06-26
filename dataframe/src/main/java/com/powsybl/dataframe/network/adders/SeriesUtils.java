/**
 * Copyright (c) 2021-2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.dataframe.network.adders;

import com.powsybl.commons.PowsyblException;
import com.powsybl.dataframe.update.DoubleSeries;
import com.powsybl.dataframe.update.IntSeries;
import com.powsybl.dataframe.update.StringSeries;
import com.powsybl.dataframe.update.UpdatingDataframe;

import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.IntConsumer;

/**
 * @author Yichen TANG <yichen.tang at rte-france.com>
 */
public final class SeriesUtils {

    private SeriesUtils() {
    }

    public static void applyIfPresent(IntSeries series, int index, IntConsumer consumer) {
        if (series != null) {
            consumer.accept(series.get(index));
        }
    }

    @FunctionalInterface
    public interface BooleanConsumer {
        void accept(boolean value);
    }

    public static void applyBooleanIfPresent(IntSeries series, int index, BooleanConsumer consumer) {
        if (series != null) {
            consumer.accept(series.get(index) == 1);
        }
    }

    public static void applyIfPresent(DoubleSeries series, int index, DoubleConsumer consumer) {
        if (series != null) {
            consumer.accept(series.get(index));
        }
    }

    public static void applyIfPresent(StringSeries series, int index, Consumer<String> consumer) {
        if (series != null) {
            consumer.accept(series.get(index));
        }
    }

    public static <E extends Enum<E>> void applyIfPresent(StringSeries series, int index, Class<E> enumClass,
                                                          Consumer<E> consumer) {
        if (series != null) {
            consumer.accept(Enum.valueOf(enumClass, series.get(index)));
        }
    }

    public static StringSeries getRequiredStrings(UpdatingDataframe dataframe, String name) {
        StringSeries series = dataframe.getStrings(name);
        if (series == null) {
            throw new PowsyblException("Required column " + name + " is missing.");
        }
        return series;
    }

    public static IntSeries getRequiredInts(UpdatingDataframe dataframe, String name) {
        IntSeries series = dataframe.getInts(name);
        if (series == null) {
            throw new PowsyblException("Required column " + name + " is missing.");
        }
        return series;
    }

    public static DoubleSeries getRequiredDoubles(UpdatingDataframe dataframe, String name) {
        DoubleSeries series = dataframe.getDoubles(name);
        if (series == null) {
            throw new PowsyblException("Required column " + name + " is missing.");
        }
        return series;
    }
}
