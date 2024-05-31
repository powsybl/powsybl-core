/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.geodata.odre;

import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Ahmed Bendaamer {@literal <ahmed.bendaamer at rte-france.com>}
 * @author Hugo Kulesza {@literal <hugo.kulesza at rte-france.com>}
 */
class FileValidatorTest {

    @Test
    void whenCallingValidate() throws URISyntaxException {
        Path file = Paths.get(getClass()
                .getClassLoader().getResource("eurostag-test/substations.csv").toURI());
        Path aerialLinesFile = Paths.get(getClass()
                .getClassLoader().getResource("eurostag-test/aerial-lines.csv").toURI());
        Path undergroundLinesFile = Paths.get(getClass()
                .getClassLoader().getResource("eurostag-test/underground-lines.csv").toURI());
        Path substationsFile = Paths.get(getClass()
                .getClassLoader().getResource("eurostag-test/substations.csv").toURI());
        Path invalidFile = Paths.get(getClass()
                .getClassLoader().getResource("eurostag-test/substations-error.csv").toURI());

        // test substations file validator with valid file
        assertTrue(FileValidator.validateSubstations(file));
        // test substations file validator with invalid file
        assertFalse(FileValidator.validateSubstations(invalidFile));
        // test lines file validator with valid files
        assertEquals(3, FileValidator.validateLines(List.of(substationsFile, aerialLinesFile, undergroundLinesFile)).size());
        // test lines file validator with 1 invalid file
        assertEquals(2, FileValidator.validateLines(List.of(substationsFile, invalidFile, undergroundLinesFile)).size());
        // test lines file validator with 2 invalid file
        assertEquals(1, FileValidator.validateLines(List.of(invalidFile, invalidFile, undergroundLinesFile)).size());
        // test lines file validator with 3 invalid file
        assertEquals(0, FileValidator.validateLines(List.of(invalidFile, invalidFile, invalidFile)).size());
        // test lines file validator with 4 invalid file
        assertEquals(0, FileValidator.validateLines(List.of(invalidFile, invalidFile, invalidFile, invalidFile)).size());
    }

}
