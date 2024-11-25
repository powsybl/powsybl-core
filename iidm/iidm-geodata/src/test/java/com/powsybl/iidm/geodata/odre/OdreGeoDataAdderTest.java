/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.geodata.odre;

import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Substation;
import com.powsybl.iidm.network.extensions.Coordinate;
import com.powsybl.iidm.network.extensions.LinePosition;
import com.powsybl.iidm.network.extensions.SubstationPosition;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author Hugo Kulesza {@literal <hugo.kulesza at rte-france.com>}
 */
class OdreGeoDataAdderTest extends AbstractOdreTest {

    private Network network;

    @BeforeEach
    void setUp() {
        network = EurostagTutorialExample1Factory.create();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("provideTestArguments")
    void addSubstationsGeoDataFromFile(String descr, String directory, OdreConfig config) throws URISyntaxException, IOException {
        Path substationsPath = Paths.get(getClass()
                .getClassLoader().getResource(directory + "substations.csv").toURI());

        OdreGeoDataAdder.fillNetworkSubstationsGeoDataFromFile(network, substationsPath, config);

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

    @ParameterizedTest(name = "{0}")
    @MethodSource("provideTestArguments")
    void addLinesGeoDataFromFile(String descr, String directory, OdreConfig config) throws URISyntaxException, IOException {
        Path substationsPath = Paths.get(getClass()
                .getClassLoader().getResource(directory + "substations.csv").toURI());
        Path aerialLinesFile = Paths.get(getClass()
                .getClassLoader().getResource(directory + "aerial-lines.csv").toURI());
        Path undergroundLinesFile = Paths.get(getClass()
                .getClassLoader().getResource(directory + "underground-lines.csv").toURI());

        OdreGeoDataAdder.fillNetworkSubstationsGeoDataFromFile(network, substationsPath, config);
        OdreGeoDataAdder.fillNetworkLinesGeoDataFromFiles(network, aerialLinesFile, undergroundLinesFile, config);

        Line line = network.getLine("NHV1_NHV2_2");
        LinePosition<Line> linePosition = line.getExtension(LinePosition.class);
        assertNotNull(linePosition);
        assertEquals(List.of(new Coordinate(4, 4), new Coordinate(5, 5)),
                linePosition.getCoordinates());

        Line line2 = network.getLine("NHV1_NHV2_1");
        LinePosition<Line> linePosition2 = line2.getExtension(LinePosition.class);
        assertNotNull(linePosition2);
        assertEquals(List.of(new Coordinate(1, 1),
                        new Coordinate(2, 2),
                        new Coordinate(3, 3),
                        new Coordinate(4, 4),
                        new Coordinate(5, 5)),
                linePosition2.getCoordinates());
    }
}
