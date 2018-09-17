/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.timeseries;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface TimeSeriesNameResolver {

    TimeSeriesNameResolver EMPTY = new TimeSeriesNameResolver() {

        @Override
        public List<TimeSeriesMetadata> getTimeSeriesMetadata(Set<String> timeSeriesNames) {
            return Collections.emptyList();
        }

        @Override
        public Set<Integer> getTimeSeriesDataVersions(String timeSeriesName) {
            return Collections.emptySet();
        }

        @Override
        public List<DoubleTimeSeries> getDoubleTimeSeries(Set<String> timeSeriesNames) {
            return Collections.emptyList();
        }
    };

    List<TimeSeriesMetadata> getTimeSeriesMetadata(Set<String> timeSeriesNames);

    Set<Integer> getTimeSeriesDataVersions(String timeSeriesName);

    List<DoubleTimeSeries> getDoubleTimeSeries(Set<String> timeSeriesNames);
}
