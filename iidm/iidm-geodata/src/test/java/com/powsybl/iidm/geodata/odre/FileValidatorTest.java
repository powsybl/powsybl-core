/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.geodata.odre;

import com.powsybl.iidm.geodata.utils.InputUtils;
import org.apache.commons.csv.CSVParser;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Ahmed Bendaamer {@literal <ahmed.bendaamer at rte-france.com>}
 * @author Hugo Kulesza {@literal <hugo.kulesza at rte-france.com>}
 */
class FileValidatorTest extends AbstractOdreTest {

    @ParameterizedTest(name = "{0}")
    @MethodSource("provideTestArguments")
    void whenCallingValidate(String testName, String directory, OdreConfig config) throws URISyntaxException, IOException {
        Path aerialLinesFile = Paths.get(getClass()
                .getClassLoader().getResource(directory + "aerial-lines.csv").toURI());
        Path undergroundLinesFile = Paths.get(getClass()
                .getClassLoader().getResource(directory + "underground-lines.csv").toURI());
        Path substationsFile = Paths.get(getClass()
                .getClassLoader().getResource(directory + "substations.csv").toURI());
        Path invalidFile = Paths.get(getClass()
                .getClassLoader().getResource(directory + "substations-error.csv").toURI());

        // test substations file validator with valid file
        assertTrue(FileValidator.validateSubstationsHeaders(getCsvParser(substationsFile), config));
        // test substations file validator with invalid file
        assertFalse(FileValidator.validateSubstationsHeaders(getCsvParser(invalidFile), config));
        // test lines file validator with valid files
        assertTrue(FileValidator.validateAerialLinesHeaders(getCsvParser(aerialLinesFile), config));
        assertTrue(FileValidator.validateUndergroundHeaders(getCsvParser(undergroundLinesFile), config));
        // test lines file validator with an invalid file
        assertFalse(FileValidator.validateAerialLinesHeaders(getCsvParser(invalidFile), config));
        assertFalse(FileValidator.validateUndergroundHeaders(getCsvParser(invalidFile), config));
    }

    private static CSVParser getCsvParser(Path substationsFile) throws IOException {
        return CSVParser.parse(InputUtils.toReader(substationsFile), FileValidator.CSV_FORMAT);
    }

}
