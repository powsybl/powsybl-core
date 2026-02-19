/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.geodata.geojson;

import com.powsybl.iidm.geodata.utils.InputUtils;
import com.powsybl.iidm.network.Network;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;

import static com.powsybl.iidm.geodata.geojson.GeoJsonDataParser.parseLines;
import static com.powsybl.iidm.geodata.geojson.GeoJsonDataParser.parseSubstations;
import static com.powsybl.iidm.geodata.utils.NetworkGeoDataExtensionsAdder.fillLineGeoData;
import static com.powsybl.iidm.geodata.utils.NetworkGeoDataExtensionsAdder.fillSubstationGeoData;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
public class GeoJsonDataAdder {

    private static final Logger LOGGER = LoggerFactory.getLogger(GeoJsonDataAdder.class);

    protected GeoJsonDataAdder() {
    }

    public static void fillNetworkSubstationsGeoDataFromFile(Network network, Path path, boolean forceGeoDataComputation) throws IOException {
        AtomicInteger substationsWithNewData = new AtomicInteger();
        AtomicInteger substationsWithUpdatedData = new AtomicInteger();
        AtomicInteger unknownSubstations = new AtomicInteger();
        try (Reader reader = InputUtils.toReader(path)) {
            parseSubstations(reader, (id, coordinates) ->
                fillSubstationGeoData(network, id, coordinates, forceGeoDataComputation, substationsWithNewData, substationsWithUpdatedData, unknownSubstations));
        }
        LOGGER.info("{} substations with data added - {} substations with data updated - {} unknown substations", substationsWithNewData.get(), substationsWithUpdatedData.get(), unknownSubstations.get());
    }

    public static void fillNetworkLinesGeoDataFromFile(Network network, Path path, boolean forceGeoDataComputation) throws IOException {
        AtomicInteger linesWithNewData = new AtomicInteger();
        AtomicInteger linesWithUpdatedData = new AtomicInteger();
        AtomicInteger unknownLines = new AtomicInteger();
        try (Reader reader = InputUtils.toReader(path)) {
            parseLines(reader, (id, coordinates) ->
                fillLineGeoData(network, id, coordinates, forceGeoDataComputation, linesWithNewData, linesWithUpdatedData, unknownLines));
        }
        LOGGER.info("{} lines with data added - {} lines with data updated - {} unknown lines", linesWithNewData.get(), linesWithUpdatedData.get(), unknownLines.get());
    }
}
