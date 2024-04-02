/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.geodata.utils;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * @author bendaamerahm <ahmed.bendaamer at rte-france.com>
 */
public class FileValidatorTest {

    @Test
    public void whenCallingValidate() throws IOException, URISyntaxException {
        Path file = Paths.get(getClass()
                .getClassLoader().getResource("postes-electriques-rte.csv").toURI());
        Path aerialLinesFile = Paths.get(getClass()
                .getClassLoader().getResource("lignes-aeriennes-rte-nv.csv").toURI());
        Path undergroundLinesFile = Paths.get(getClass()
                .getClassLoader().getResource("lignes-souterraines-rte-nv.csv").toURI());
        Path substationsFile = Paths.get(getClass()
                .getClassLoader().getResource("postes-electriques-rte.csv").toURI());
        Path invalidFile = Paths.get(getClass()
                .getClassLoader().getResource("postes-electriques-rte-erreur.csv").toURI());

        // test substations file validator with valid file
        Assertions.assertThat(FileValidator.validateSubstations(file)).isTrue();
        // test substations file validator with invalid file
        Assertions.assertThat(FileValidator.validateSubstations(invalidFile)).isFalse();
        // test lines file validator with valid files
        Assertions.assertThat(FileValidator.validateLines(List.of(substationsFile, aerialLinesFile, undergroundLinesFile))).hasSize(3);
        // test lines file validator with 1 invalid file
        Assertions.assertThat(FileValidator.validateLines(List.of(substationsFile, invalidFile, undergroundLinesFile))).hasSize(2);
        // test lines file validator with 2 invalid file
        Assertions.assertThat(FileValidator.validateLines(List.of(invalidFile, invalidFile, undergroundLinesFile))).hasSize(1);
        // test lines file validator with 3 invalid file
        Assertions.assertThat(FileValidator.validateLines(List.of(invalidFile, invalidFile, invalidFile))).isEmpty();
        // test lines file validator with 4 invalid file
        Assertions.assertThat(FileValidator.validateLines(List.of(invalidFile, invalidFile, invalidFile, invalidFile))).isEmpty();
    }

}
