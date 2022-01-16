/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.matpower.converter;

import com.powsybl.commons.AbstractConverterTest;
import com.powsybl.commons.datasource.FileDataSource;
import com.powsybl.iidm.import_.Importer;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.NetworkFactory;
import com.powsybl.iidm.xml.NetworkXml;
import com.powsybl.matpower.model.MatpowerModelFactory;
import com.powsybl.matpower.model.MatpowerWriter;
import com.powsybl.matpower.model.MatpowerModel;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

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
    public void copyTest() throws IOException {
        MatpowerModel model = MatpowerModelFactory.create9();
        Path matpowerBinCase = tmpDir.resolve(model.getCaseName() + ".mat");
        MatpowerWriter.write(model, matpowerBinCase);
        new MatpowerImporter().copy(new FileDataSource(tmpDir, model.getCaseName()),
            new FileDataSource(tmpDir, "copy"));
        assertTrue(Files.exists(tmpDir.resolve("copy.mat")));
    }

    @Test
    public void existsTest() throws IOException {
        MatpowerModel model = MatpowerModelFactory.create118();
        Path matpowerBinCase = tmpDir.resolve(model.getCaseName() + ".mat");
        MatpowerWriter.write(model, matpowerBinCase);
        assertTrue(new MatpowerImporter().exists(new FileDataSource(tmpDir, model.getCaseName())));
        assertFalse(new MatpowerImporter().exists(new FileDataSource(tmpDir, "doesnotexist")));
    }

    @Test
    public void testCase9() throws IOException {
        testCase(MatpowerModelFactory.create9());
    }

    @Test
    public void testCase9limits() throws IOException {
        testCase(MatpowerModelFactory.create9limits());
    }

    @Test
    public void testCase14() throws IOException {
        testCase(MatpowerModelFactory.create14());
    }

    @Test
    public void testCase14WithPhaseShifter() throws IOException {
        testCase(MatpowerModelFactory.create14WithPhaseShifter());
    }

    @Test
    public void testCase30() throws IOException {
        testCase(MatpowerModelFactory.create30());
    }

    @Test
    public void testCase57() throws IOException {
        testCase(MatpowerModelFactory.create57());
    }

    @Test
    public void testCase118() throws IOException {
        testCase(MatpowerModelFactory.create118());
    }

    @Test
    public void testCase300() throws IOException {
        testCase(MatpowerModelFactory.create300());
    }

    @Test(expected = UncheckedIOException.class)
    public void testNonexistentCase() throws IOException {
        testNetwork(new MatpowerImporter().importData(new FileDataSource(tmpDir, "unknown"), NetworkFactory.findDefault(), null));
    }

    private void testCase(MatpowerModel model) throws IOException {
        String caseId = model.getCaseName();
        Path matFile = tmpDir.resolve(caseId + ".mat");
        MatpowerWriter.write(model, matFile);

        Network network = new MatpowerImporter().importData(new FileDataSource(tmpDir, caseId), NetworkFactory.findDefault(), null);
        testNetwork(network, caseId);
    }

    private void testNetwork(Network network, String id) throws IOException {
        //set the case date of the network to be tested to a default value to match the saved networks' date
        ZonedDateTime caseDateTime = DEFAULTDATEFORTESTS.atStartOfDay(ZoneOffset.UTC.normalized());
        network.setCaseDate(new DateTime(caseDateTime.toInstant().toEpochMilli(), DateTimeZone.UTC));

        String fileName = id + ".xiidm";
        Path file = tmpDir.resolve(fileName);
        NetworkXml.write(network, file);
        try (InputStream is = Files.newInputStream(file)) {
            compareTxt(getClass().getResourceAsStream("/" + fileName), is);
        }
    }

    private void testNetwork(Network network) throws IOException {
        testNetwork(network, network.getId());
    }
}
