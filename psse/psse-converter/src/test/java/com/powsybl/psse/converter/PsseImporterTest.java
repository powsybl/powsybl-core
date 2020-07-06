/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.psse.converter;

import com.powsybl.commons.AbstractConverterTest;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.commons.datasource.ResourceDataSource;
import com.powsybl.commons.datasource.ResourceSet;
import com.powsybl.iidm.import_.Importer;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.impl.NetworkFactoryImpl;
import com.powsybl.iidm.xml.NetworkXml;
import com.powsybl.psse.model.PsseException;
import org.joda.time.DateTime;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.*;

/**
 * @author JB Heyberger <jean-baptiste.heyberger at rte-france.com>
 */
public class PsseImporterTest extends AbstractConverterTest {

    @Test
    public void baseTest() {
        Importer importer = new PsseImporter();
        assertEquals("PSS/E", importer.getFormat());
        assertEquals("PSS/E Format to IIDM converter", importer.getComment());
        assertEquals(1, importer.getParameters().size());
        assertEquals("ignore-base-voltage", importer.getParameters().get(0).getName());
    }

    private void testNetwork(Network network) throws IOException {
        Path file = fileSystem.getPath("/work/" + network.getId() + ".xiidm");
        network.setCaseDate(DateTime.parse("2016-01-01T10:00:00.000+02:00"));
        NetworkXml.write(network, file);
        try (InputStream is = Files.newInputStream(file)) {
            compareTxt(getClass().getResourceAsStream("/" + network.getId() + ".xiidm"), is);
        }
    }

    @Test
    public void existsTest() {
        // test with a valid raw/RAW file
        assertTrue(new PsseImporter().exists(new ResourceDataSource("IEEE_14_bus", new ResourceSet("/", "IEEE_14_bus.raw"))));
        assertTrue(new PsseImporter().exists(new ResourceDataSource("IEEE_30_bus", new ResourceSet("/", "IEEE_30_bus.RAW"))));

        // test with an invalid extension
        assertFalse(new PsseImporter().exists(new ResourceDataSource("IEEE_14_bus", new ResourceSet("/", "IEEE_14_bus.json"))));

        // test with a valid extension and an invalid content
        assertFalse(new PsseImporter().exists(new ResourceDataSource("fake", new ResourceSet("/", "fake.raw"))));

        // test with not supported content
        assertFalse(new PsseImporter().exists(new ResourceDataSource("case-flag-not-supported", new ResourceSet("/", "case-flag-not-supported.raw"))));
        assertFalse(new PsseImporter().exists(new ResourceDataSource("version-not-supported", new ResourceSet("/", "version-not-supported.raw"))));
    }

    public void importTest(String basename, String filename) throws IOException {
        ReadOnlyDataSource dataSource = new ResourceDataSource(basename, new ResourceSet("/", filename));
        Network network = new PsseImporter().importData(dataSource, new NetworkFactoryImpl(), null);
        testNetwork(network);
    }

    @Test
    public void importTest14() throws IOException {
        importTest("IEEE_14_bus", "IEEE_14_bus.raw");
    }

    @Test
    public void importTest24() throws IOException {
        importTest("IEEE_24_bus", "IEEE_24_bus.RAW");
    }

    @Test
    public void importTest57() throws IOException {
        importTest("IEEE_57_bus", "IEEE_57_bus.RAW");
    }

    @Test
    public void importTest118() throws IOException {
        importTest("IEEE_118_bus", "IEEE_118_bus.RAW");
    }

    @Test
    public void importTestT3W() throws IOException {
        importTest("ThreeMIB_T3W_modified", "ThreeMIB_T3W_modified.RAW");
    }

    @Test(expected = PsseException.class)
    public void badVersionTest() {
        ReadOnlyDataSource dataSource = new ResourceDataSource("case-flag-not-supported", new ResourceSet("/", "case-flag-not-supported.raw"));
        new PsseImporter().importData(dataSource, new NetworkFactoryImpl(), null);
    }

    @Test(expected = PsseException.class)
    public void badModeTest() {
        ReadOnlyDataSource dataSource = new ResourceDataSource("version-not-supported", new ResourceSet("/", "version-not-supported.raw"));
        new PsseImporter().importData(dataSource, new NetworkFactoryImpl(), null);
    }
}
