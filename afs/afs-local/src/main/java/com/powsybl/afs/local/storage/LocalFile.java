/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.local.storage;

import com.powsybl.commons.datasource.DataSource;
import com.powsybl.math.timeseries.DoubleTimeSeries;
import com.powsybl.math.timeseries.StringTimeSeries;
import com.powsybl.math.timeseries.TimeSeriesMetadata;

import java.util.*;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface LocalFile extends LocalNode {

    String getPseudoClass();

    String getStringAttribute(String name);

    OptionalInt getIntAttribute(String name);

    OptionalDouble getDoubleAttribute(String name);

    Optional<Boolean> getBooleanAttribute(String name);

    DataSource getDataSourceAttribute(String name);

    Set<String> getTimeSeriesNames();

    List<TimeSeriesMetadata> getTimeSeriesMetadata(Set<String> timeSeriesNames);

    List<DoubleTimeSeries> getDoubleTimeSeries(Set<String> timeSeriesNames, int version);

    List<StringTimeSeries> getStringTimeSeries(Set<String> timeSeriesNames, int version);
}
