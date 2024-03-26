/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.timeseries;

import java.util.*;
import java.util.stream.Collectors;

/**
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class ReadOnlyTimeSeriesStoreAggregator implements ReadOnlyTimeSeriesStore {

    private final List<ReadOnlyTimeSeriesStore> stores;

    public ReadOnlyTimeSeriesStoreAggregator(ReadOnlyTimeSeriesStore... stores) {
        this(Arrays.asList(stores));
    }

    public ReadOnlyTimeSeriesStoreAggregator(List<ReadOnlyTimeSeriesStore> stores) {
        this.stores = Objects.requireNonNull(stores);
    }

    @Override
    public Set<String> getTimeSeriesNames(TimeSeriesFilter filter) {
        Set<String> timeSeriesNames = new HashSet<>();
        for (int i = 0; i < stores.size(); i++) {
            timeSeriesNames.addAll(stores.get(i).getTimeSeriesNames(filter));
        }
        return timeSeriesNames;
    }

    @Override
    public boolean timeSeriesExists(String timeSeriesName) {
        Objects.requireNonNull(timeSeriesName);
        for (ReadOnlyTimeSeriesStore store : stores) {
            if (store.timeSeriesExists(timeSeriesName)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Optional<TimeSeriesMetadata> getTimeSeriesMetadata(String timeSeriesName) {
        Objects.requireNonNull(timeSeriesName);
        for (ReadOnlyTimeSeriesStore store : stores) {
            Optional<TimeSeriesMetadata> metadata = store.getTimeSeriesMetadata(timeSeriesName);
            if (metadata.isPresent()) {
                return metadata;
            }
        }
        return Optional.empty();
    }

    @Override
    public List<TimeSeriesMetadata> getTimeSeriesMetadata(Set<String> timeSeriesNames) {
        Objects.requireNonNull(timeSeriesNames);
        List<TimeSeriesMetadata> aggregatedMetadataList = new ArrayList<>();
        Set<String> remainingTimeSeriesNames = new HashSet<>(timeSeriesNames);
        for (ReadOnlyTimeSeriesStore store : stores) {
            List<TimeSeriesMetadata> metadataList = store.getTimeSeriesMetadata(remainingTimeSeriesNames);
            remainingTimeSeriesNames.removeAll(metadataList.stream().map(TimeSeriesMetadata::getName).collect(Collectors.toSet()));
            aggregatedMetadataList.addAll(metadataList);
            if (remainingTimeSeriesNames.isEmpty()) {
                break;
            }
        }
        return aggregatedMetadataList;
    }

    @Override
    public Set<Integer> getTimeSeriesDataVersions() {
        Set<Integer> versions = new HashSet<>();
        for (ReadOnlyTimeSeriesStore store : stores) {
            versions.addAll(store.getTimeSeriesDataVersions());
        }
        return versions;
    }

    @Override
    public Set<Integer> getTimeSeriesDataVersions(String timeSeriesName) {
        Objects.requireNonNull(timeSeriesName);
        for (ReadOnlyTimeSeriesStore store : stores) {
            if (store.timeSeriesExists(timeSeriesName)) {
                return store.getTimeSeriesDataVersions(timeSeriesName);
            }
        }
        return Collections.emptySet();
    }

    @Override
    public Optional<DoubleTimeSeries> getDoubleTimeSeries(String timeSeriesName, int version) {
        Objects.requireNonNull(timeSeriesName);
        TimeSeriesVersions.check(version);
        for (ReadOnlyTimeSeriesStore store : stores) {
            Optional<DoubleTimeSeries> timeSeries = store.getDoubleTimeSeries(timeSeriesName, version);
            if (timeSeries.isPresent()) {
                return timeSeries;
            }
        }
        return Optional.empty();
    }

    @Override
    public List<DoubleTimeSeries> getDoubleTimeSeries(Set<String> timeSeriesNames, int version) {
        Objects.requireNonNull(timeSeriesNames);
        TimeSeriesVersions.check(version);
        List<DoubleTimeSeries> aggregatedTimeSeriesList = new ArrayList<>();
        Set<String> remainingTimeSeriesNames = new HashSet<>(timeSeriesNames);
        for (ReadOnlyTimeSeriesStore store : stores) {
            List<DoubleTimeSeries> timeSeriesList = store.getDoubleTimeSeries(remainingTimeSeriesNames, version);
            remainingTimeSeriesNames.removeAll(timeSeriesList.stream().map(TimeSeries::getMetadata)
                                                                      .map(TimeSeriesMetadata::getName)
                                                                      .collect(Collectors.toSet()));
            aggregatedTimeSeriesList.addAll(timeSeriesList);
            if (remainingTimeSeriesNames.isEmpty()) {
                break;
            }
        }
        return aggregatedTimeSeriesList;
    }

    @Override
    public List<DoubleTimeSeries> getDoubleTimeSeries(int version) {
        return stores.stream()
                .flatMap(store -> store.getDoubleTimeSeries(version).stream())
                .collect(Collectors.toList());
    }

    @Override
    public Optional<StringTimeSeries> getStringTimeSeries(String timeSeriesName, int version) {
        Objects.requireNonNull(timeSeriesName);
        TimeSeriesVersions.check(version);
        for (ReadOnlyTimeSeriesStore store : stores) {
            Optional<StringTimeSeries> timeSeries = store.getStringTimeSeries(timeSeriesName, version);
            if (timeSeries.isPresent()) {
                return timeSeries;
            }
        }
        return Optional.empty();
    }

    @Override
    public List<StringTimeSeries> getStringTimeSeries(Set<String> timeSeriesNames, int version) {
        Objects.requireNonNull(timeSeriesNames);
        TimeSeriesVersions.check(version);
        List<StringTimeSeries> aggregatedTimeSeriesList = new ArrayList<>();
        Set<String> remainingTimeSeriesNames = new HashSet<>(timeSeriesNames);
        for (ReadOnlyTimeSeriesStore store : stores) {
            List<StringTimeSeries> timeSeriesList = store.getStringTimeSeries(remainingTimeSeriesNames, version);
            remainingTimeSeriesNames.removeAll(timeSeriesList.stream().map(TimeSeries::getMetadata)
                    .map(TimeSeriesMetadata::getName)
                    .collect(Collectors.toSet()));
            aggregatedTimeSeriesList.addAll(timeSeriesList);
            if (remainingTimeSeriesNames.isEmpty()) {
                break;
            }
        }
        return aggregatedTimeSeriesList;

    }

    @Override
    public void addListener(TimeSeriesStoreListener listener) {
        Objects.requireNonNull(listener);
        for (ReadOnlyTimeSeriesStore store : stores) {
            store.addListener(listener);
        }
    }

    @Override
    public void removeListener(TimeSeriesStoreListener listener) {
        Objects.requireNonNull(listener);
        for (ReadOnlyTimeSeriesStore store : stores) {
            store.removeListener(listener);
        }
    }
}
