/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.timeseries;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ReadOnlyTimeSeriesStoreCache implements ReadOnlyTimeSeriesStore {

    private final Map<String, DoubleTimeSeries> doubleTimeSeriesMap;

    public ReadOnlyTimeSeriesStoreCache(DoubleTimeSeries... doubleTimeSeriesList) {
        this(Arrays.asList(doubleTimeSeriesList));
    }

    public ReadOnlyTimeSeriesStoreCache(List<DoubleTimeSeries> doubleTimeSeriesList) {
        Objects.requireNonNull(doubleTimeSeriesList);
        doubleTimeSeriesMap = new HashMap<>(doubleTimeSeriesList.size());
        for (DoubleTimeSeries doubleTimeSeries : doubleTimeSeriesList) {
            doubleTimeSeriesMap.put(doubleTimeSeries.getMetadata().getName(), doubleTimeSeries);
        }
    }

    @Override
    public Set<String> getTimeSeriesNames(TimeSeriesFilter filter) {
        return doubleTimeSeriesMap.keySet();
    }

    @Override
    public boolean timeSeriesExists(String timeSeriesName) {
        return doubleTimeSeriesMap.containsKey(timeSeriesName);
    }

    @Override
    public TimeSeriesMetadata getTimeSeriesMetadata(String timeSeriesName) {
        return getDoubleTimeSeries(timeSeriesName, 1).getMetadata();
    }

    @Override
    public List<TimeSeriesMetadata> getTimeSeriesMetadata(Set<String> timeSeriesNames) {
        return getDoubleTimeSeries(timeSeriesNames, 1).stream().map(TimeSeries::getMetadata).collect(Collectors.toList());
    }

    private static UnsupportedOperationException createNotImplementedException() {
        return new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Set<Integer> getTimeSeriesDataVersions() {
        throw createNotImplementedException();
    }

    @Override
    public Set<Integer> getTimeSeriesDataVersions(String timeSeriesName) {
        throw createNotImplementedException();
    }

    @Override
    public DoubleTimeSeries getDoubleTimeSeries(String timeSeriesName, int version) {
        DoubleTimeSeries doubleTimeSeries = doubleTimeSeriesMap.get(timeSeriesName);
        if (doubleTimeSeries == null) {
            throw new TimeSeriesException("Time series '" + timeSeriesName + "' not found");
        }
        return doubleTimeSeries;
    }

    @Override
    public List<DoubleTimeSeries> getDoubleTimeSeries(Set<String> timeSeriesNames, int version) {
        return timeSeriesNames.stream().map(timeSeriesName -> getDoubleTimeSeries(timeSeriesName, version)).collect(Collectors.toList());
    }

    @Override
    public StringTimeSeries getStringTimeSeries(String timeSeriesName, int version) {
        throw createNotImplementedException();
    }

    @Override
    public List<StringTimeSeries> getStringTimeSeries(Set<String> timeSeriesNames, int version) {
        throw createNotImplementedException();
    }

    @Override
    public void addListener(TimeSeriesStoreListener listener) {
        throw createNotImplementedException();
    }

    @Override
    public void removeListener(TimeSeriesStoreListener listener) {
        throw createNotImplementedException();
    }
}
