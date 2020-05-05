/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.matpower.converter;

import com.powsybl.commons.AbstractConverterTest;
import com.powsybl.commons.datasource.FileDataSource;
import com.powsybl.commons.datasource.ResourceDataSource;
import com.powsybl.commons.datasource.ResourceSet;
import com.powsybl.iidm.import_.Importer;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.NetworkFactory;
import com.powsybl.iidm.xml.NetworkXml;
import com.powsybl.matpower.model.MatpowerBinWriter;
import com.powsybl.matpower.model.MatpowerModel;
import com.powsybl.matpower.model.MatpowerReader;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Objects;

import static org.junit.Assert.*;

/**
 * @author Christian Biasuzzi <christian.biasuzzi@techrain.eu>
 */
public class MatpowerImporterTest extends AbstractConverterTest {

    private static final LocalDate DEFAULTDATEFORTESTS = LocalDate.of(2020, Month.JANUARY, 1);

    @Test
    public void baseTest() {
        Importer importer = new MatpowerImporter();
        assertEquals("MATPOWER", importer.getFormat());
        assertEquals("MATPOWER Format to IIDM converter", importer.getComment());
        assertEquals(1, importer.getParameters().size());
        assertEquals("matpower.import.ignore-base-voltage", importer.getParameters().get(0).getName());
    }

    @Test
    public void copyTest() {
        new MatpowerImporter().copy(new ResourceDataSource("case118", new ResourceSet("/", "case118.m")),
            new FileDataSource(fileSystem.getPath("/work"), "copy"));
        assertTrue(Files.exists(fileSystem.getPath("/work").resolve("copy.m")));
        assertFalse(Files.exists(fileSystem.getPath("/work").resolve("copy.mat")));
    }

    @Test
    public void existsTest() {
        assertTrue(new MatpowerImporter().exists(new ResourceDataSource("case118", new ResourceSet("/", "case118.m"))));
    }

    private void testNetwork(Network network, String id) throws IOException {
        //set the case date of the network to be tested to a default value to match the saved networks' date
        ZonedDateTime caseDateTime = DEFAULTDATEFORTESTS.atStartOfDay(ZoneOffset.UTC.normalized());
        network.setCaseDate(new DateTime(caseDateTime.toInstant().toEpochMilli(), DateTimeZone.UTC));

        Path file = fileSystem.getPath("/work/" + id + ".xiidm");
        NetworkXml.write(network, file);
        try (InputStream is = Files.newInputStream(file)) {
            compareTxt(getClass().getResourceAsStream("/" + id + ".xiidm"), is);
        }
    }

    private void testNetwork(Network network) throws IOException {
        testNetwork(network, network.getId());
    }

    private void testCaseBin(String caseId) throws IOException {
        createMatCaseFile(caseId + ".m", fileSystem.getPath("/work").resolve(caseId + ".mat"));
        testNetwork(new MatpowerImporter().importData(new FileDataSource(fileSystem.getPath("/work"), caseId), NetworkFactory.findDefault(), null), caseId);
    }

    private void createMatCaseFile(String sourceTextFile, Path destMatFile) throws IOException {
        MatpowerModel model = readModelFromResources(sourceTextFile);
        MatpowerBinWriter.write(model, Files.newOutputStream(destMatFile));
    }

    private MatpowerModel readModelFromResources(String fileName) throws IOException {
        Objects.requireNonNull(fileName);
        MatpowerModel model;
        try (InputStream iStream = getClass().getResourceAsStream("/" + fileName)) {
            model = MatpowerReader.read(iStream);
        }
        return model;
    }

    @Test
    public void testCase118() throws IOException {
        testNetwork(MatpowerNetworkFactory.create118());
    }

    @Test
    public void testCase14() throws IOException {
        testNetwork(MatpowerNetworkFactory.create14());
    }

    @Test
    public void testCase30() throws IOException {
        testNetwork(MatpowerNetworkFactory.create30());
    }

    @Test
    public void testCase300() throws IOException {
        testNetwork(MatpowerNetworkFactory.create300());
    }

    @Test
    public void testCase57() throws IOException {
        testNetwork(MatpowerNetworkFactory.create57());
    }

    @Test
    public void testCase9() throws IOException {
        testNetwork(MatpowerNetworkFactory.create9());
    }

    @Test
    public void testCaseBin9() throws IOException {
        testCaseBin("case9");
    }

    @Test
    public void testCaseBin14() throws IOException {
        testCaseBin("case14");
    }

    @Test
    public void testCaseBin30() throws IOException {
        testCaseBin("case30");
    }

    @Test
    public void testCaseBin57() throws IOException {
        testCaseBin("case57");
    }

    @Test
    public void testCaseBin118() throws IOException {
        testCaseBin("case118");
    }

    @Test
    public void testCaseBin300() throws IOException {
        testCaseBin("case300");
    }

    @Test(expected = MatpowerException.class)
    public void testNonexistentCase() throws IOException {
        testNetwork(new MatpowerImporter().importData(new FileDataSource(fileSystem.getPath("/"), "unknown"), NetworkFactory.findDefault(), null));
    }

}
