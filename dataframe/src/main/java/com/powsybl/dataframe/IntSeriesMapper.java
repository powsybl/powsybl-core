/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.dataframe;

import java.util.List;
import java.util.function.ToIntFunction;

/**
 * @author Sylvain Leclerc <sylvain.leclerc at rte-france.com>
 */
public class IntSeriesMapper<T> implements SeriesMapper<T> {

    private final SeriesMetadata metadata;
    private final IntUpdater<T> updater;
    private final ToIntFunction<T> value;

    @FunctionalInterface
    public interface IntUpdater<U> {
        void update(U object, int value);
    }

    public IntSeriesMapper(String name, ToIntFunction<T> value) {
        this(name, false, value, null, true);
    }

    public IntSeriesMapper(String name, boolean index, ToIntFunction<T> value) {
        this(name, index, value, null, true);
    }

    public IntSeriesMapper(String name, ToIntFunction<T> value, IntUpdater<T> updater) {
        this(name, false, value, updater, true);
    }

    public IntSeriesMapper(String name, ToIntFunction<T> value, IntUpdater<T> updater, boolean defaultAttribute) {
        this(name, false, value, updater, defaultAttribute);
    }

    public IntSeriesMapper(String name, boolean index, ToIntFunction<T> value, IntUpdater<T> updater,
                           boolean defaultAttribute) {
        this.metadata = new SeriesMetadata(index, name, updater != null, SeriesDataType.INT, defaultAttribute);
        this.updater = updater;
        this.value = value;
    }

    @Override
    public SeriesMetadata getMetadata() {
        return metadata;
    }

    @Override
    public void createSeries(List<T> items, DataframeHandler handler) {
        boolean index = metadata.isIndex();
        String name = metadata.getName();
        DataframeHandler.IntSeriesWriter writer = index ? handler.newIntIndex(name,
            items.size()) : handler.newIntSeries(name, items.size());
        for (int i = 0; i < items.size(); i++) {
            writer.set(i, value.applyAsInt(items.get(i)));
        }
    }

    @Override
    public void updateInt(T object, int value) {
        if (updater == null) {
            throw new UnsupportedOperationException("Series '" + getMetadata().getName() + "' is not modifiable.");
        }
        updater.update(object, value);
    }
}
