/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.ieeecdf.converter;

import com.powsybl.commons.datasource.DataSourceUtil;
import com.powsybl.commons.datasource.ResourceDataSource;
import com.powsybl.commons.datasource.ResourceSet;
import com.powsybl.commons.test.AbstractSerDeTest;
import com.powsybl.iidm.network.Importer;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.serde.NetworkSerDe;
import com.powsybl.loadflow.LoadFlowParameters;
import com.powsybl.loadflow.resultscompletion.LoadFlowResultsCompletion;
import com.powsybl.loadflow.resultscompletion.LoadFlowResultsCompletionParameters;
import com.powsybl.loadflow.validation.ValidationConfig;
import com.powsybl.loadflow.validation.ValidationType;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.powsybl.commons.test.ComparisonUtils.compareXml;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class IeeeCdfImporterTest extends AbstractSerDeTest {

    @Test
    void baseTest() {
        Importer importer = new IeeeCdfImporter();
        assertEquals("IEEE-CDF", importer.getFormat());
        assertEquals("IEEE Common Data Format to IIDM converter", importer.getComment());
        assertEquals(1, importer.getParameters().size());
        assertEquals("ignore-base-voltage", importer.getParameters().get(0).getName());
    }

    @Test
    void copyTest() {
        new IeeeCdfImporter().copy(new ResourceDataSource("ieee14cdf", new ResourceSet("/", "ieee14cdf.txt")),
            DataSourceUtil.createDataSource(fileSystem.getPath("/work"), "", "copy"));
        assertTrue(Files.exists(fileSystem.getPath("/work").resolve("copy.txt")));
    }

    @Test
    void existsTest() {
        assertTrue(new IeeeCdfImporter().exists(new ResourceDataSource("ieee14cdf", new ResourceSet("/", "ieee14cdf.txt"))));
    }

    private void testNetwork(Network network) throws IOException {
        Path file = fileSystem.getPath("/work/" + network.getId() + ".xiidm");
        NetworkSerDe.write(network, file);
        try (InputStream is = Files.newInputStream(file)) {
            compareXml(getClass().getResourceAsStream("/" + network.getId() + ".xiidm"), is);
        }
    }

    private void testSolved(Network network) throws IOException {
        // Precision required on bus balances (MVA)
        double threshold = 1.05;
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

    static void computeMissingFlows(Network network, LoadFlowParameters lfparams) {

        LoadFlowResultsCompletionParameters p = new LoadFlowResultsCompletionParameters(
            LoadFlowResultsCompletionParameters.EPSILON_X_DEFAULT,
            LoadFlowResultsCompletionParameters.APPLY_REACTANCE_CORRECTION_DEFAULT,
            LoadFlowResultsCompletionParameters.Z0_THRESHOLD_DIFF_VOLTAGE_ANGLE);
        LoadFlowResultsCompletion lf = new LoadFlowResultsCompletion(p, lfparams);
        lf.run(network, null);
    }

    @Test
    void testIeee9() throws IOException {
        testNetwork(IeeeCdfNetworkFactory.create9());
    }

    @Test
    void testIeee14() throws IOException {
        testNetwork(IeeeCdfNetworkFactory.create14());
    }

    @Test
    void testIeee14Solved() throws IOException {
        Network network = IeeeCdfNetworkFactory.create14Solved();
        testSolved(network);
        testNetwork(network);
    }

    @Test
    void testIeee30() throws IOException {
        testNetwork(IeeeCdfNetworkFactory.create30());
    }

    @Test
    void testIeee57() throws IOException {
        testNetwork(IeeeCdfNetworkFactory.create57());
    }

    @Test
    void testIeee118() throws IOException {
        testNetwork(IeeeCdfNetworkFactory.create118());
    }

    @Test
    void testIeee300() throws IOException {
        testNetwork(IeeeCdfNetworkFactory.create300());
    }

    @Test
    void testIeeezeroimpedance9() throws IOException {
        testNetwork(IeeeCdfNetworkFactory.create9zeroimpedance());
    }

    @Test
    void testIeee33() throws IOException {
        testNetwork(IeeeCdfNetworkFactory.create33());
    }

    @Test
    void testIeee69() throws IOException {
        testNetwork(IeeeCdfNetworkFactory.create69());
    }
}
