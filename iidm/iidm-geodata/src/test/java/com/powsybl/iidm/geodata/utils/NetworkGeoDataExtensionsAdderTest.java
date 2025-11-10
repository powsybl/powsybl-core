/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.geodata.utils;

import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Substation;
import com.powsybl.iidm.network.extensions.Coordinate;
import com.powsybl.iidm.network.extensions.LinePosition;
import com.powsybl.iidm.network.extensions.SubstationPosition;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static com.powsybl.iidm.geodata.utils.NetworkGeoDataExtensionsAdder.fillLineGeoData;
import static com.powsybl.iidm.geodata.utils.NetworkGeoDataExtensionsAdder.fillSubstationGeoData;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author Hugo Kulesza {@literal <hugo.kulesza at rte-france.com>}
 */
class NetworkGeoDataExtensionsAdderTest {

    private Network network;

    @BeforeEach
    void setUp() {
        network = EurostagTutorialExample1Factory.create();
    }

    @Test
    void addSubstationsPosition() {
        AtomicInteger substationsWithNewData = new AtomicInteger();
        AtomicInteger substationsWithOldData = new AtomicInteger();
        AtomicInteger unknownSubstations = new AtomicInteger();
        Coordinate coord1 = new Coordinate(1, 2);
        Coordinate coord2 = new Coordinate(3, 4);
        fillSubstationGeoData(network, "P1", coord1, false, substationsWithNewData, substationsWithOldData, unknownSubstations);
        fillSubstationGeoData(network, "P2", coord2, false, substationsWithNewData, substationsWithOldData, unknownSubstations);

        assertEquals(2, substationsWithNewData.get());
        assertEquals(0, substationsWithOldData.get());
        assertEquals(0, unknownSubstations.get());

        Substation station1 = network.getSubstation("P1");
        assertSubstationCoordinates(station1, coord1);

        Substation station2 = network.getSubstation("P2");
        assertSubstationCoordinates(station2, coord2);
    }

    @Test
    void addLinesGeoData() {
        AtomicInteger linesWithNewData = new AtomicInteger();
        AtomicInteger linesWithOldData = new AtomicInteger();
        AtomicInteger unknownLines = new AtomicInteger();
        Coordinate coord1 = new Coordinate(1, 2);
        Coordinate coord2 = new Coordinate(3, 4);
        Map<String, List<Coordinate>> position = Map.of("NHV1_NHV2_2", List.of(coord1, coord2));
        fillLineGeoData(network, "NHV1_NHV2_2", List.of(coord1, coord2), false, linesWithNewData, linesWithOldData, unknownLines);

        assertEquals(1, linesWithNewData.get());
        assertEquals(0, linesWithOldData.get());
        assertEquals(0, unknownLines.get());

        Line line = network.getLine("NHV1_NHV2_2");
        LinePosition<Line> linePosition = line.getExtension(LinePosition.class);
        assertNotNull(linePosition);
        assertEquals(List.of(coord1, coord2), linePosition.getCoordinates());
    }

    @Test
    void replaceSubstationsPosition() {
        AtomicInteger substationsWithNewData = new AtomicInteger();
        AtomicInteger substationsWithOldData = new AtomicInteger();
        AtomicInteger unknownSubstations = new AtomicInteger();

        Coordinate coord1 = new Coordinate(1, 2);
        Coordinate coord2 = new Coordinate(3, 4);
        fillSubstationGeoData(network, "P1", coord1, false, substationsWithNewData, substationsWithOldData, unknownSubstations);
        fillSubstationGeoData(network, "P2", coord2, false, substationsWithNewData, substationsWithOldData, unknownSubstations);

        assertEquals(2, substationsWithNewData.get());
        assertEquals(0, substationsWithOldData.get());
        assertEquals(0, unknownSubstations.get());

        Coordinate coord3 = new Coordinate(5, 6);
        fillSubstationGeoData(network, "P1", coord3, false, substationsWithNewData, substationsWithOldData, unknownSubstations);
        Substation substation = network.getSubstation("P1");
        assertSubstationCoordinates(substation, coord1);

        assertEquals(2, substationsWithNewData.get());
        assertEquals(0, substationsWithOldData.get());
        assertEquals(0, unknownSubstations.get());

        fillSubstationGeoData(network, "P1", coord3, true, substationsWithNewData, substationsWithOldData, unknownSubstations);
        assertSubstationCoordinates(substation, coord3);

        assertEquals(2, substationsWithNewData.get());
        assertEquals(1, substationsWithOldData.get());
        assertEquals(0, unknownSubstations.get());
    }

    private static void assertSubstationCoordinates(Substation substation, Coordinate expectedCoordinate) {
        SubstationPosition position = substation.getExtension(SubstationPosition.class);
        assertNotNull(position);
        assertEquals(expectedCoordinate, position.getCoordinate());
    }
}
