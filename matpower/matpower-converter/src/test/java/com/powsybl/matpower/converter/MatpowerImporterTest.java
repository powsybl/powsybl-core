/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.matpower.converter;

import com.powsybl.commons.datasource.FileDataSource;
import com.powsybl.commons.test.AbstractSerDeTest;
import com.powsybl.iidm.network.Importer;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.NetworkFactory;
import com.powsybl.iidm.serde.NetworkSerDe;
import com.powsybl.loadflow.LoadFlowParameters;
import com.powsybl.loadflow.resultscompletion.LoadFlowResultsCompletion;
import com.powsybl.loadflow.resultscompletion.LoadFlowResultsCompletionParameters;
import com.powsybl.loadflow.validation.ValidationConfig;
import com.powsybl.loadflow.validation.ValidationType;
import com.powsybl.matpower.model.MBus;
import com.powsybl.matpower.model.MatpowerModel;
import com.powsybl.matpower.model.MatpowerModelFactory;
import com.powsybl.matpower.model.MatpowerWriter;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Properties;

import static com.powsybl.commons.test.ComparisonUtils.compareXml;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Christian Biasuzzi {@literal <christian.biasuzzi@techrain.eu>}
 */
class MatpowerImporterTest extends AbstractSerDeTest {

    private static final LocalDate DEFAULTDATEFORTESTS = LocalDate.of(2020, Month.JANUARY, 1);

    @Test
    void baseTest() {
        Importer importer = new MatpowerImporter();
        assertEquals("MATPOWER", importer.getFormat());
        assertEquals("MATPOWER Format to IIDM converter", importer.getComment());
        assertEquals(1, importer.getParameters().size());
        assertEquals("matpower.import.ignore-base-voltage", importer.getParameters().get(0).getName());
    }

    @Test
    void copyTest() throws IOException {
        MatpowerModel model = MatpowerModelFactory.create9();
        Path matpowerBinCase = tmpDir.resolve(model.getCaseName() + ".mat");
        MatpowerWriter.write(model, matpowerBinCase, true);
        new MatpowerImporter().copy(new FileDataSource(tmpDir, model.getCaseName()),
            new FileDataSource(tmpDir, "copy"));
        assertTrue(Files.exists(tmpDir.resolve("copy.mat")));
    }

    @Test
    void existsTest() throws IOException {
        MatpowerModel model = MatpowerModelFactory.create118();
        Path matpowerBinCase = tmpDir.resolve(model.getCaseName() + ".mat");
        MatpowerWriter.write(model, matpowerBinCase, true);
        assertTrue(new MatpowerImporter().exists(new FileDataSource(tmpDir, model.getCaseName())));
        assertFalse(new MatpowerImporter().exists(new FileDataSource(tmpDir, "doesnotexist")));
    }

    @Test
    void testCase9() throws IOException {
        testCase(MatpowerModelFactory.create9());
    }

    @Test
    void testCase9limits() throws IOException {
        testCase(MatpowerModelFactory.create9limits());
    }

    @Test
    void testCase14() throws IOException {
        testCase(MatpowerModelFactory.create14());
    }

    @Test
    void testCase14WithPhaseShifter() throws IOException {
        testCase(MatpowerModelFactory.create14WithPhaseShifter());
    }

    @Test
    void testCase14WithPhaseShifterSolved() throws IOException {
        testCaseSolved(MatpowerModelFactory.create14WithPhaseShifter());
    }

    @Test
    void testCase14WithInvertedVoltageLimits() throws IOException {
        MatpowerModel model14 = MatpowerModelFactory.create14();
        model14.setCaseName("ieee14-inverted-voltage-limits");
        MBus bus1 = model14.getBusByNum(1);
        bus1.setMinimumVoltageMagnitude(1.1);
        bus1.setMaximumVoltageMagnitude(0.9);
        testCase(model14);
    }

    @Test
    void testCase30() throws IOException {
        testCase(MatpowerModelFactory.create30());
    }

    @Test
    void testCase30ConsideringBaseVoltage() throws IOException {
        MatpowerModel model = MatpowerModelFactory.create30();
        model.setCaseName("ieee30-considering-base-voltage");

        Properties properties = new Properties();
        properties.put("matpower.import.ignore-base-voltage", false);
        testCase(model, properties);
    }

    @Test
    void testCase57() throws IOException {
        testCase(MatpowerModelFactory.create57());
    }

    @Test
    void testCase118() throws IOException {
        testCase(MatpowerModelFactory.create118());
    }

    @Test
    void testCase300() throws IOException {
        testCase(MatpowerModelFactory.create300());
    }

    @Test
    void testCase9zeroimpedance() throws IOException {
        testCase(MatpowerModelFactory.create9zeroimpedance());
    }

    @Test
    void testCase9DcLine() throws IOException {
        testCase(MatpowerModelFactory.create9Dcline());
    }

    @Test
    void testNonexistentCase() {
        assertThrows(UncheckedIOException.class, () -> testNetwork(new MatpowerImporter().importData(new FileDataSource(tmpDir, "unknown"), NetworkFactory.findDefault(), null)));
    }

    private void testCase(MatpowerModel model) throws IOException {
        testCase(model, null);
    }

    private void testCase(MatpowerModel model, Properties properties) throws IOException {
        String caseId = model.getCaseName();
        Path matFile = tmpDir.resolve(caseId + ".mat");
        MatpowerWriter.write(model, matFile, true);

        Network network = new MatpowerImporter().importData(new FileDataSource(tmpDir, caseId), NetworkFactory.findDefault(), properties);
        testNetwork(network, caseId);
    }

    private void testNetwork(Network network, String id) throws IOException {
        //set the case date of the network to be tested to a default value to match the saved networks' date
        ZonedDateTime caseDateTime = DEFAULTDATEFORTESTS.atStartOfDay(ZoneOffset.UTC.normalized());
        network.setCaseDate(ZonedDateTime.ofInstant(caseDateTime.toInstant(), ZoneOffset.UTC));

        String fileName = id + ".xiidm";
        Path file = tmpDir.resolve(fileName);
        NetworkSerDe.write(network, file);
        try (InputStream is = Files.newInputStream(file)) {
            compareXml(getClass().getResourceAsStream("/" + fileName), is);
        }
    }

    private void testNetwork(Network network) throws IOException {
        testNetwork(network, network.getId());
    }

    private void testCaseSolved(MatpowerModel model) throws IOException {
        String caseId = model.getCaseName();
        Path matFile = tmpDir.resolve(caseId + ".mat");
        MatpowerWriter.write(model, matFile, true);

        Network network = new MatpowerImporter().importData(new FileDataSource(tmpDir, caseId), NetworkFactory.findDefault(), null);
        testSolved(network);
    }

    private void testSolved(Network network) throws IOException {
        // Precision required on bus balances (MVA)
        double threshold = 0.0000001;
        ValidationConfig config = loadFlowValidationConfig(threshold);
        Path work = Files.createDirectories(fileSystem.getPath("/lf-validation" + network.getId()));
        computeMissingFlows(network, config.getLoadFlowParameters());
        assertTrue(ValidationType.BUSES.check(network, config, work));
    }

    private static ValidationConfig loadFlowValidationConfig(double threshold) {
        ValidationConfig config = ValidationConfig.load();
        config.setVerbose(true);
        config.setThreshold(threshold);
        config.setOkMissingValues(false);
        LoadFlowParameters lf = new LoadFlowParameters();
        lf.setTwtSplitShuntAdmittance(true);
        config.setLoadFlowParameters(lf);
        return config;
    }

    private static void computeMissingFlows(Network network, LoadFlowParameters lfparams) {

        LoadFlowResultsCompletionParameters p = new LoadFlowResultsCompletionParameters(
            LoadFlowResultsCompletionParameters.EPSILON_X_DEFAULT,
            LoadFlowResultsCompletionParameters.APPLY_REACTANCE_CORRECTION_DEFAULT,
            LoadFlowResultsCompletionParameters.Z0_THRESHOLD_DIFF_VOLTAGE_ANGLE);
        LoadFlowResultsCompletion lf = new LoadFlowResultsCompletion(p, lfparams);
        lf.run(network, null);
    }
}
