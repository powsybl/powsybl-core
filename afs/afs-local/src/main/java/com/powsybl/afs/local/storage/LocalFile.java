/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.local.storage;

import com.powsybl.afs.storage.NodeGenericMetadata;
import com.powsybl.timeseries.*;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface LocalFile extends LocalNode {

    String getPseudoClass();

    String getDescription();

    NodeGenericMetadata getGenericMetadata();

    Optional<InputStream> readBinaryData(String name);

    boolean dataExists(String name);

    Set<String> getDataNames();

    Set<String> getTimeSeriesNames();

    boolean timeSeriesExists(String timeSeriesName);

    List<TimeSeriesMetadata> getTimeSeriesMetadata(Set<String> timeSeriesNames);

    Set<Integer> getTimeSeriesDataVersions();

    Set<Integer> getTimeSeriesDataVersions(String timeSeriesName);

    Map<String, List<DoubleDataChunk>> getDoubleTimeSeriesData(Set<String> timeSeriesNames, int version);

    Map<String, List<StringDataChunk>> getStringTimeSeriesData(Set<String> timeSeriesNames, int version);

}
