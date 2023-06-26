/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.dataframe;

import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * @author Sylvain Leclerc <sylvain.leclerc at rte-france.com>
 */
public class EnumSeriesMapper<T, E extends Enum<E>> implements SeriesMapper<T> {

    private final SeriesMetadata metadata;
    private final Class<E> enumClass;
    private final BiConsumer<T, E> updater;
    private final Function<T, E> value;

    public EnumSeriesMapper(String name, Class<E> enumClass, Function<T, E> value) {
        this(name, enumClass, value, null, true);
    }

    public EnumSeriesMapper(String name, Class<E> enumClass, Function<T, E> value, BiConsumer<T, E> updater,
                            boolean defaultAttribute) {
        this.metadata = new SeriesMetadata(false, name, updater != null, SeriesDataType.STRING, defaultAttribute);
        this.enumClass = enumClass;
        this.updater = updater;
        this.value = value;
    }

    @Override
    public SeriesMetadata getMetadata() {
        return metadata;
    }

    @Override
    public void createSeries(List<T> items, DataframeHandler factory) {
        DataframeHandler.StringSeriesWriter writer = factory.newStringSeries(metadata.getName(), items.size());
        for (int i = 0; i < items.size(); i++) {
            writer.set(i, Objects.toString(value.apply(items.get(i)), ""));
        }
    }

    @Override
    public void updateString(T object, String stringValue) {
        if (updater == null) {
            throw new UnsupportedOperationException("Series '" + getMetadata().getName() + "' is not modifiable.");
        }
        E enumValue = Enum.valueOf(enumClass, stringValue.toUpperCase());
        updater.accept(object, enumValue);
    }
}
