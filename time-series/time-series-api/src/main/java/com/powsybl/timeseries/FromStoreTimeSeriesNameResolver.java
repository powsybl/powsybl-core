/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.timeseries;

import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class FromStoreTimeSeriesNameResolver implements TimeSeriesNameResolver {

    private final ReadOnlyTimeSeriesStore store;

    private final int version;

    public FromStoreTimeSeriesNameResolver(ReadOnlyTimeSeriesStore store, int version) {
        this.store = Objects.requireNonNull(store);
        this.version = version;
    }

    @Override
    public List<TimeSeriesMetadata> getTimeSeriesMetadata(Set<String> timeSeriesNames) {
        return store.getTimeSeriesMetadata(timeSeriesNames);
    }

    @Override
    public Set<Integer> getTimeSeriesDataVersions(String timeSeriesName) {
        return store.getTimeSeriesDataVersions(timeSeriesName);
    }

    @Override
    public List<DoubleTimeSeries> getDoubleTimeSeries(Set<String> timeSeriesNames) {
        return store.getDoubleTimeSeries(timeSeriesNames, version);
    }
}
