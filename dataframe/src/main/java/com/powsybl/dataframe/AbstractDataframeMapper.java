/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.dataframe;

import com.powsybl.commons.PowsyblException;
import com.powsybl.dataframe.update.DoubleSeries;
import com.powsybl.dataframe.update.IntSeries;
import com.powsybl.dataframe.update.StringSeries;
import com.powsybl.dataframe.update.UpdatingDataframe;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.google.common.collect.ImmutableMap.toImmutableMap;

/**
 * @author Sylvain Leclerc <sylvain.leclerc at rte-france.com>
 */
public abstract class AbstractDataframeMapper<T, U> implements DataframeMapper<T> {

    protected final Map<String, SeriesMapper<U>> seriesMappers;

    public AbstractDataframeMapper(List<SeriesMapper<U>> seriesMappers) {
        this.seriesMappers = seriesMappers.stream()
            .collect(toImmutableMap(mapper -> mapper.getMetadata().getName(), Function.identity()));
    }

    @Override
    public List<SeriesMetadata> getSeriesMetadata() {
        return seriesMappers.values().stream().map(SeriesMapper::getMetadata).collect(Collectors.toList());
    }

    @Override
    public SeriesMetadata getSeriesMetadata(String seriesName) {
        SeriesMapper<U> mapper = seriesMappers.get(seriesName);
        if (mapper == null) {
            throw new PowsyblException("No series named " + seriesName);
        }
        return mapper.getMetadata();
    }

    @Override
    public void createDataframe(T object, DataframeHandler dataframeHandler, DataframeFilter dataframeFilter) {
        Collection<SeriesMapper<U>> mappers = getSeriesMappers(dataframeFilter);
        dataframeHandler.allocate(mappers.size());
        List<U> items = getItems(object);
        mappers.stream().forEach(mapper -> mapper.createSeries(items, dataframeHandler));
    }

    interface ColumnUpdater<U> {
        void update(int index, U object);
    }

    private static final class IntColumnUpdater<U> implements ColumnUpdater<U> {
        private final IntSeries values;
        private final SeriesMapper<U> mapper;

        private IntColumnUpdater(IntSeries values, SeriesMapper<U> mapper) {
            this.values = values;
            this.mapper = mapper;
        }

        @Override
        public void update(int index, U object) {
            mapper.updateInt(object, values.get(index));
        }
    }

    private static final class DoubleColumnUpdater<U> implements ColumnUpdater<U> {
        private final DoubleSeries values;
        private final SeriesMapper<U> mapper;

        private DoubleColumnUpdater(DoubleSeries values, SeriesMapper<U> mapper) {
            this.values = values;
            this.mapper = mapper;
        }

        @Override
        public void update(int index, U object) {
            mapper.updateDouble(object, values.get(index));
        }
    }

    private static final class StringColumnUpdater<U> implements ColumnUpdater<U> {
        private final StringSeries values;
        private final SeriesMapper<U> mapper;

        private StringColumnUpdater(StringSeries values, SeriesMapper<U> mapper) {
            this.values = values;
            this.mapper = mapper;
        }

        @Override
        public void update(int index, U object) {
            mapper.updateString(object, values.get(index));
        }
    }

    @Override
    public void updateSeries(T object, UpdatingDataframe updatingDataframe) {

        //Setup links to minimize searches on column names
        List<ColumnUpdater<U>> updaters = new ArrayList<>();
        for (SeriesMetadata column : updatingDataframe.getSeriesMetadata()) {
            if (column.isIndex()) {
                continue;
            }
            String seriesName = column.getName();
            SeriesMapper<U> mapper = seriesMappers.get(seriesName);
            ColumnUpdater<U> updater;
            switch (column.getType()) {
                case STRING:
                    updater = new StringColumnUpdater<>(updatingDataframe.getStrings(seriesName), mapper);
                    break;
                case DOUBLE:
                    updater = new DoubleColumnUpdater<>(updatingDataframe.getDoubles(seriesName), mapper);
                    break;
                case INT:
                    updater = new IntColumnUpdater<>(updatingDataframe.getInts(seriesName), mapper);
                    break;
                default:
                    throw new IllegalStateException("Unexpected series type for update: " + column.getType());
            }
            updaters.add(updater);
        }

        for (int i = 0; i < updatingDataframe.getRowCount(); i++) {
            U item = getItem(object, updatingDataframe, i);
            int itemIndex = i;
            updaters.forEach(updater -> updater.update(itemIndex, item));
        }
    }

    @Override
    public boolean isSeriesMetaDataExists(String seriesName) {
        return seriesMappers.containsKey(seriesName);
    }

    public Collection<SeriesMapper<U>> getSeriesMappers(DataframeFilter dataframeFilter) {
        Collection<SeriesMapper<U>> mappers = seriesMappers.values();
        return mappers.stream()
            .filter(mapper -> filterMapper(mapper, dataframeFilter))
            .collect(Collectors.toList());
    }

    protected boolean filterMapper(SeriesMapper<U> mapper, DataframeFilter dataframeFilter) {
        switch (dataframeFilter.getAttributeFilterType()) {
            case DEFAULT_ATTRIBUTES:
                return mapper.getMetadata().isDefaultAttribute() || mapper.getMetadata().isIndex();
            case INPUT_ATTRIBUTES:
                return dataframeFilter.getInputAttributes()
                    .contains(mapper.getMetadata().getName()) || mapper.getMetadata().isIndex();
            case ALL_ATTRIBUTES:
                return true;
            default:
                throw new IllegalStateException(
                    "Unexpected attribute filter type: " + dataframeFilter.getAttributeFilterType());
        }
    }

    protected abstract List<U> getItems(T object);

    protected abstract U getItem(T object, UpdatingDataframe dataframe, int index);
}
