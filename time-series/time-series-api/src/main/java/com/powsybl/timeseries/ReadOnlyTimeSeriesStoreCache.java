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
        Objects.requireNonNull(timeSeriesName);
        DoubleTimeSeries timeSeries = doubleTimeSeriesMap.get(timeSeriesName);
        return timeSeries != null ? timeSeries.getMetadata() : null;
    }

    @Override
    public List<TimeSeriesMetadata> getTimeSeriesMetadata(Set<String> timeSeriesNames) {
        Objects.requireNonNull(timeSeriesNames);
        return timeSeriesNames.stream()
                .map(this::getTimeSeriesMetadata)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public Set<Integer> getTimeSeriesDataVersions() {
        return Collections.emptySet();
    }

    @Override
    public Set<Integer> getTimeSeriesDataVersions(String timeSeriesName) {
        return Collections.emptySet();
    }

    @Override
    public DoubleTimeSeries getDoubleTimeSeries(String timeSeriesName, int version) {
        return doubleTimeSeriesMap.get(timeSeriesName);
    }

    @Override
    public List<DoubleTimeSeries> getDoubleTimeSeries(Set<String> timeSeriesNames, int version) {
        return timeSeriesNames.stream()
                .map(timeSeriesName -> getDoubleTimeSeries(timeSeriesName, version))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public StringTimeSeries getStringTimeSeries(String timeSeriesName, int version) {
        return null;
    }

    @Override
    public List<StringTimeSeries> getStringTimeSeries(Set<String> timeSeriesNames, int version) {
        return Collections.emptyList();
    }

    private static UnsupportedOperationException createNotImplementedException() {
        return new UnsupportedOperationException("Not implemented");
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
