/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.geodata.geojson;

import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Substation;
import com.powsybl.iidm.network.extensions.Coordinate;
import com.powsybl.iidm.network.extensions.LinePosition;
import com.powsybl.iidm.network.extensions.SubstationPosition;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
class GeoJsonDataAdderTest {

    private Network network;

    @BeforeEach
    void setUp() {
        network = EurostagTutorialExample1Factory.create();
    }

    @Test
    void addSubstationsGeoDataFromFile() throws URISyntaxException, IOException {
        Path substationsPath = Paths.get(Objects.requireNonNull(getClass().getClassLoader().getResource("eurostag-test/substations.geojson")).toURI());

        // Add the data
        GeoJsonDataAdder.fillNetworkSubstationsGeoDataFromFile(network, substationsPath, false);

        Coordinate coord1 = new Coordinate(2, 1);
        Substation station1 = network.getSubstation("P1");
        SubstationPosition position1 = station1.getExtension(SubstationPosition.class);
        assertNotNull(position1);
        assertEquals(coord1, position1.getCoordinate());

        Coordinate coord2 = new Coordinate(4, 3);
        Substation station2 = network.getSubstation("P2");
        SubstationPosition position2 = station2.getExtension(SubstationPosition.class);
        assertNotNull(position2);
        assertEquals(coord2, position2.getCoordinate());
    }

    @Test
    void addLinesGeoDataFromFile() throws URISyntaxException, IOException {
        Path linesPath = Paths.get(Objects.requireNonNull(getClass().getClassLoader().getResource("eurostag-test/lines.geojson")).toURI());

        // Add the data
        GeoJsonDataAdder.fillNetworkLinesGeoDataFromFile(network, linesPath, false);

        Line line = network.getLine("NHV1_NHV2_2");
        LinePosition<Line> linePosition = line.getExtension(LinePosition.class);
        assertNotNull(linePosition);
        assertEquals(List.of(new Coordinate(6, 6),
                new Coordinate(7, 7),
                new Coordinate(8, 8)),
            linePosition.getCoordinates());

        Line line2 = network.getLine("NHV1_NHV2_1");
        LinePosition<Line> linePosition2 = line2.getExtension(LinePosition.class);
        assertNotNull(linePosition2);
        assertEquals(List.of(new Coordinate(1.5, 1),
                new Coordinate(2.5, 2),
                new Coordinate(3.5, 3),
                new Coordinate(4.5, 4),
                new Coordinate(5.5, 5)),
            linePosition2.getCoordinates());
    }
}
