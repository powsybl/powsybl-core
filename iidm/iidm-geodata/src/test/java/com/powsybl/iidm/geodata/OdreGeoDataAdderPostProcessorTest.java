/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.geodata;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.commons.config.InMemoryPlatformConfig;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.computation.ComputationManager;
import com.powsybl.computation.local.LocalComputationManager;
import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Substation;
import com.powsybl.iidm.network.extensions.Coordinate;
import com.powsybl.iidm.network.extensions.LinePosition;
import com.powsybl.iidm.network.extensions.SubstationPosition;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Hugo Kulesza {@literal <hugo.kulesza at rte-france.com>}
 */
class OdreGeoDataAdderPostProcessorTest {

    private FileSystem fileSystem;
    private Network network;
    private PlatformConfig platformConfig;
    private OdreGeoDataAdderPostProcessor processor;
    private ComputationManager computationManager;

    @BeforeEach
    void setUp() throws IOException {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
        network = EurostagTutorialExample1Factory.create();
        platformConfig = new InMemoryPlatformConfig(fileSystem);
        processor = new OdreGeoDataAdderPostProcessor(platformConfig);
        computationManager = LocalComputationManager.getDefault();
    }

    @AfterEach
    void tearDown() throws IOException {
        fileSystem.close();
    }

    @Test
    void test() {
        OdreGeoDataAdderPostProcessor.DEFAULT_FILE_NAMES.forEach(
                (name, fileName) -> {
                    Path path = platformConfig.getConfigDir().map(p -> p.resolve(fileName)).orElse(null);
                    assertNotNull(path);
                    try {
                        Files.copy(getClass().getResourceAsStream("/eurostag-test/" + fileName), path);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });

        try { // Launch process
            processor.process(network, computationManager);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

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

    @Test
    void missingFiles() throws IOException {
        processor.process(network, computationManager);
        assertNull(network.getSubstation("P2").getExtension(SubstationPosition.class));

        Path path = platformConfig.getConfigDir().map(p -> p.resolve("postes-electriques-rte.csv")).orElse(null);
        Files.copy(getClass().getResourceAsStream("/eurostag-test/postes-electriques-rte.csv"), path);
        processor.process(network, computationManager);

        assertNotNull(network.getSubstation("P2").getExtension(SubstationPosition.class));
        assertNull(network.getLine("NHV1_NHV2_1").getExtension(LinePosition.class));

        Path aerialLinePath = platformConfig.getConfigDir().map(p -> p.resolve("lignes-aeriennes-rte-nv.csv")).orElse(null);
        Files.copy(getClass().getResourceAsStream("/eurostag-test/lignes-aeriennes-rte-nv.csv"), aerialLinePath);
        processor.process(network, computationManager);

        assertNull(network.getLine("NHV1_NHV2_1").getExtension(LinePosition.class));

        Path undergroundLinePath = platformConfig.getConfigDir().map(p -> p.resolve("lignes-souterraines-rte-nv.csv")).orElse(null);
        Files.copy(getClass().getResourceAsStream("/eurostag-test/lignes-souterraines-rte-nv.csv"), undergroundLinePath);
        processor.process(network, computationManager);

        assertNotNull(network.getLine("NHV1_NHV2_1").getExtension(LinePosition.class));
    }

}
