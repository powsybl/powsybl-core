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

import static com.powsybl.iidm.geodata.utils.NetworkGeoDataExtensionsAdder.fillNetworkLinesGeoData;
import static com.powsybl.iidm.geodata.utils.NetworkGeoDataExtensionsAdder.fillNetworkSubstationsGeoData;
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
        Coordinate coord1 = new Coordinate(1, 2);
        Coordinate coord2 = new Coordinate(3, 4);
        Map<String, Coordinate> substationsGeoData = Map.of("P1", coord1, "P2", coord2);

        fillNetworkSubstationsGeoData(network, substationsGeoData, false);

        Substation station1 = network.getSubstation("P1");
        assertSubstationCoordinates(station1, coord1);

        Substation station2 = network.getSubstation("P2");
        assertSubstationCoordinates(station2, coord2);
    }

    @Test
    void addLinesGeoData() {
        Coordinate coord1 = new Coordinate(1, 2);
        Coordinate coord2 = new Coordinate(3, 4);
        Map<String, List<Coordinate>> position = Map.of("NHV1_NHV2_2", List.of(coord1, coord2));

        fillNetworkLinesGeoData(network, position, false);

        Line line = network.getLine("NHV1_NHV2_2");
        LinePosition<Line> linePosition = line.getExtension(LinePosition.class);
        assertNotNull(linePosition);
        assertEquals(List.of(coord1, coord2), linePosition.getCoordinates());
    }

    @Test
    void replaceSubstationsPosition() {
        Coordinate coord1 = new Coordinate(1, 2);
        Coordinate coord2 = new Coordinate(3, 4);
        Map<String, Coordinate> substationsGeoData = Map.of("P1", coord1, "P2", coord2);
        fillNetworkSubstationsGeoData(network, substationsGeoData, false);

        Coordinate coord3 = new Coordinate(5, 6);
        Coordinate coord4 = new Coordinate(7, 8);
        Map<String, Coordinate> newSubstationsGeoData = Map.of("P1", coord3, "P2", coord4);
        fillNetworkSubstationsGeoData(network, newSubstationsGeoData, false);
        Substation substation = network.getSubstation("P1");
        assertSubstationCoordinates(substation, coord1);

        fillNetworkSubstationsGeoData(network, newSubstationsGeoData, true);
        assertSubstationCoordinates(substation, coord3);
    }

    private static void assertSubstationCoordinates(Substation substation, Coordinate expectedCoordinate) {
        SubstationPosition position = substation.getExtension(SubstationPosition.class);
        assertNotNull(position);
        assertEquals(expectedCoordinate, position.getCoordinate());
    }
}
