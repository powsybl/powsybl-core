/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.geodata.geojson;

import com.powsybl.iidm.network.extensions.Coordinate;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.powsybl.iidm.geodata.geojson.GeoJsonDataLoader.getLinesCoordinates;
import static com.powsybl.iidm.geodata.geojson.GeoJsonDataLoader.getSubstationsCoordinates;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
class GeoJsonDataLoaderTest {

    @Test
    void getSubstationsCoordinatesTest() throws URISyntaxException, IOException {
        Path substationsPath = Paths.get(Objects.requireNonNull(getClass().getClassLoader().getResource("eurostag-test/substations.geojson")).toURI());
        Map<String, Coordinate> expectedCoordinates = Map.of("P1", new Coordinate(2, 1),
            "P2", new Coordinate(4, 3));
        Map<String, Coordinate> substationsCoordinates = getSubstationsCoordinates(substationsPath);
        assertEquals(expectedCoordinates, substationsCoordinates);
    }

    @Test
    void getLinesCoordinatesTest() throws URISyntaxException, IOException {
        Path linesPath = Paths.get(Objects.requireNonNull(getClass().getClassLoader().getResource("eurostag-test/lines.geojson")).toURI());
        Map<String, List<Coordinate>> expectedCoordinates = Map.of(
            "NHV1_NHV2_2",
            List.of(new Coordinate(6, 6),
                new Coordinate(7, 7),
                new Coordinate(8, 8)),
            "NHV1_NHV2_1",
            List.of(new Coordinate(1.5, 1),
                new Coordinate(2.5, 2),
                new Coordinate(3.5, 3),
                new Coordinate(4.5, 4),
                new Coordinate(5.5, 5)));
        Map<String, List<Coordinate>> linesCoordinates = getLinesCoordinates(linesPath);
        assertEquals(expectedCoordinates, linesCoordinates);
    }

    @Test
    void unexpectedFeatureTypeTest() throws URISyntaxException, IOException {
        Path linesPath = Paths.get(Objects.requireNonNull(getClass().getClassLoader().getResource("eurostag-test/substations.geojson")).toURI());
        // Points are not supported for lines
        assertEquals(Map.of(), getLinesCoordinates(linesPath));
    }
}
