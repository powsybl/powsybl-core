/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.timeseries;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public interface ReadOnlyTimeSeriesStore {

    Set<String> getTimeSeriesNames(TimeSeriesFilter filter);

    boolean timeSeriesExists(String timeSeriesName);

    Optional<TimeSeriesMetadata> getTimeSeriesMetadata(String timeSeriesName);

    List<TimeSeriesMetadata> getTimeSeriesMetadata(Set<String> timeSeriesNames);

    Set<Integer> getTimeSeriesDataVersions();

    Set<Integer> getTimeSeriesDataVersions(String timeSeriesName);

    Optional<DoubleTimeSeries> getDoubleTimeSeries(String timeSeriesName, int version);

    List<DoubleTimeSeries> getDoubleTimeSeries(Set<String> timeSeriesNames, int version);

    List<DoubleTimeSeries> getDoubleTimeSeries(int version);

    Optional<StringTimeSeries> getStringTimeSeries(String timeSeriesName, int version);

    List<StringTimeSeries> getStringTimeSeries(Set<String> timeSeriesNames, int version);

    void addListener(TimeSeriesStoreListener listener);

    void removeListener(TimeSeriesStoreListener listener);
}
