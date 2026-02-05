/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.geodata.geojson;

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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
class GeoJsonAdderPostProcessorTest {

    private FileSystem fileSystem;
    private Network network;
    private PlatformConfig platformConfig;
    private GeoJsonAdderPostProcessor processor;
    private ComputationManager computationManager;

    @BeforeEach
    void setUp() {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
        network = EurostagTutorialExample1Factory.create();
        platformConfig = new InMemoryPlatformConfig(fileSystem);
        processor = new GeoJsonAdderPostProcessor(platformConfig);
        computationManager = LocalComputationManager.getDefault();
    }

    @AfterEach
    void tearDown() throws IOException {
        fileSystem.close();
    }

    @Test
    void test() {
        assertEquals("geoJsonImporter", processor.getName());

        GeoJsonAdderPostProcessor.DEFAULT_FILE_NAMES.forEach(
            (name, fileName) -> {
                Path path = platformConfig.getConfigDir().map(p -> p.resolve(fileName)).orElse(null);
                assertNotNull(path);
                try {
                    Files.copy(Objects.requireNonNull(getClass().getResourceAsStream("/eurostag-test/" + fileName)), path);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });

        try { // Launch process
            processor.process(network, computationManager);
        } catch (IOException e) {
            fail();
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
        assertEquals(List.of(new Coordinate(6, 6), new Coordinate(7, 7), new Coordinate(8, 8)),
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

    @Test
    void bothFilesMissing() throws IOException {
        // Process with two files missing: geographical data is unavailable in the network
        processor.process(network, computationManager);
        assertNull(network.getSubstation("P2").getExtension(SubstationPosition.class));
        LinePosition<Line> linePosition = network.getLine("NHV1_NHV2_1").getExtension(LinePosition.class);
        assertNull(linePosition);
    }

    @ParameterizedTest
    @ValueSource(strings = {"substations.geojson", "lines.geojson"})
    void oneFileMissing(String filename) throws IOException {
        // Process with one file missing: geographical data is unavailable in the network
        Path path = platformConfig.getConfigDir().map(p -> p.resolve(filename)).orElse(null);
        assertNotNull(path);
        Files.copy(Objects.requireNonNull(getClass().getResourceAsStream("/eurostag-test/" + filename)), path);
        processor.process(network, computationManager);
        assertNull(network.getSubstation("P2").getExtension(SubstationPosition.class));
        LinePosition<Line> linePosition = network.getLine("NHV1_NHV2_1").getExtension(LinePosition.class);
        assertNull(linePosition);
    }
}
