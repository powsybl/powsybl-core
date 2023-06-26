/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.dataframe;

import java.util.List;
import java.util.function.Predicate;

/**
 * @author Sylvain Leclerc <sylvain.leclerc at rte-france.com>
 */
public class BooleanSeriesMapper<T> implements SeriesMapper<T> {

    private final SeriesMetadata metadata;
    private final BooleanUpdater<T> updater;
    private final Predicate<T> value;

    @FunctionalInterface
    public interface BooleanUpdater<U> {
        void update(U object, boolean value);
    }

    public BooleanSeriesMapper(String name, Predicate<T> value) {
        this(name, value, null, true);
    }

    public BooleanSeriesMapper(String name, Predicate<T> value, BooleanUpdater<T> updater, boolean defaultAttribute) {
        this.metadata = new SeriesMetadata(false, name, updater != null, SeriesDataType.BOOLEAN, defaultAttribute);
        this.updater = updater;
        this.value = value;
    }

    @Override
    public SeriesMetadata getMetadata() {
        return metadata;
    }

    @Override
    public void createSeries(List<T> items, DataframeHandler handler) {
        DataframeHandler.BooleanSeriesWriter writer = handler.newBooleanSeries(metadata.getName(), items.size());
        for (int i = 0; i < items.size(); i++) {
            writer.set(i, value.test(items.get(i)));
        }
    }

    @Override
    public void updateInt(T object, int value) {
        if (updater == null) {
            throw new UnsupportedOperationException("Series '" + getMetadata().getName() + "' is not modifiable.");
        }
        updater.update(object, value == 1);
    }
}
