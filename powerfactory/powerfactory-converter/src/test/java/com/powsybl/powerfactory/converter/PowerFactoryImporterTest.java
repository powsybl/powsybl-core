/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.powerfactory.converter;

import com.powsybl.commons.AbstractConverterTest;
import com.powsybl.commons.datasource.FileDataSource;
import com.powsybl.commons.datasource.ResourceDataSource;
import com.powsybl.commons.datasource.ResourceSet;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.NetworkFactory;
import com.powsybl.iidm.xml.NetworkXml;
import com.powsybl.powerfactory.model.StudyCase;
import com.powsybl.powerfactory.model.StudyCaseLoader;
import org.joda.time.DateTime;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;

import static org.junit.Assert.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
public class PowerFactoryImporterTest extends AbstractConverterTest {

    @Test
    public void testBase() {
        PowerFactoryImporter importer = new PowerFactoryImporter();
        assertEquals("POWER-FACTORY", importer.getFormat());
        assertTrue(importer.getParameters().isEmpty());
        assertEquals("PowerFactory to IIDM converter", importer.getComment());
    }

    @Test
    public void testExistsAndCopy() throws IOException {
        InputStream is = Objects.requireNonNull(getClass().getResourceAsStream("/ieee14.dgs"));
        Path dgsFile = fileSystem.getPath("/work/ieee14.dgs");
        Files.copy(is, dgsFile);

        Optional<StudyCase> studyCase = StudyCaseLoader.load(dgsFile);
        assertTrue(studyCase.isPresent());

        PowerFactoryImporter importer = new PowerFactoryImporter();
        assertTrue(importer.exists(new FileDataSource(fileSystem.getPath("/work"), "ieee14")));
        assertFalse(importer.exists(new FileDataSource(fileSystem.getPath("/work"), "error")));

        importer.copy(new FileDataSource(fileSystem.getPath("/work"), "ieee14"),
                new FileDataSource(fileSystem.getPath("/work"), "ieee14-copy"));
        assertTrue(Files.exists(fileSystem.getPath("/work/ieee14-copy.dgs")));
    }

    private boolean importAndCompareXml(String id) {
        Network network = new PowerFactoryImporter()
                .importData(new ResourceDataSource(id, new ResourceSet("/", id + ".dgs")),
                        NetworkFactory.findDefault(),
                        null);
        Path file = fileSystem.getPath("/work/" + id + ".xiidm");
        network.setCaseDate(DateTime.parse("2021-01-01T10:00:00.000+02:00"));
        NetworkXml.write(network, file);
        try (InputStream is = Files.newInputStream(file)) {
            compareTxt(getClass().getResourceAsStream("/" + id + ".xiidm"), is);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return true;
    }

    @Test
    public void ieee14Test() {
        assertTrue(importAndCompareXml("ieee14"));
    }

    @Test
    public void twoBusesLineWithBTest() {
        assertTrue(importAndCompareXml("TwoBusesLineWithB"));
    }

    @Test
    public void twoBusesLineWithGandBTest() {
        assertTrue(importAndCompareXml("TwoBusesLineWithGandB"));
    }

    @Test
    public void twoBusesLineWithTandBTest() {
        assertTrue(importAndCompareXml("TwoBusesLineWithTandB"));
    }

    @Test
    public void twoBusesLineWithCTest() {
        assertTrue(importAndCompareXml("TwoBusesLineWithC"));
    }

    @Test
    public void twoBusesGeneratorTest() {
        assertTrue(importAndCompareXml("TwoBusesGenerator"));
    }

    @Test
    public void twoBusesGeneratorWithoutIvmodeTest() {
        assertTrue(importAndCompareXml("TwoBusesGeneratorWithoutIvmode"));
    }

    @Test
    public void twoBusesGeneratorAvmodeTest() {
        assertTrue(importAndCompareXml("TwoBusesGeneratorAvmode"));
    }

    @Test
    public void twoBusesGeneratorWithoutActiveLimitsTest() {
        assertTrue(importAndCompareXml("TwoBusesGeneratorWithoutActiveLimits"));
    }

    @Test
    public void twoBusesGeneratorIqtypeTest() {
        assertTrue(importAndCompareXml("TwoBusesGeneratorIqtype"));
    }

    @Test
    public void twoBusesGeneratorWithoutIqtypeTest() {
        assertTrue(importAndCompareXml("TwoBusesGeneratorWithoutIqtype"));
    }
}
