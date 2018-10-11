/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cim1.converter;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.commons.config.InMemoryPlatformConfig;
import com.powsybl.commons.config.MapModuleConfig;
import com.powsybl.commons.datasource.DataSource;
import com.powsybl.commons.datasource.FileDataSource;
import com.powsybl.iidm.network.Network;
import com.powsybl.loadflow.LoadFlowParameters;
import com.powsybl.loadflow.resultscompletion.LoadFlowResultsCompletion;
import com.powsybl.loadflow.resultscompletion.LoadFlowResultsCompletionParameters;
import com.powsybl.loadflow.mock.LoadFlowFactoryMock;
import com.powsybl.loadflow.validation.ValidationConfig;
import com.powsybl.loadflow.validation.ValidationType;

/**
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
public class CIM1ConverterRatioTapChangerSide2FixTest {

    private static final Logger LOG = LoggerFactory.getLogger(CIM1ConverterRatioTapChangerSide2FixTest.class);

    private FileSystem fileSystem;
    private DataSource dataSource;
    private CIM1Importer importer;

    @Before
    public void setUp() throws IOException {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());

        Path test = Files.createDirectory(fileSystem.getPath("test"));
        dataSource = new FileDataSource(test, "tx-from-microBE-adapted");
        resourceToDataSource("tx-from-microBE-adapted_EQ.xml", dataSource);
        resourceToDataSource("tx-from-microBE-adapted_TP.xml", dataSource);
        resourceToDataSource("tx-from-microBE-adapted_SV.xml", dataSource);
        resourceToDataSource("ENTSO-E_Boundary_Set_EU_EQ.xml", dataSource);
        resourceToDataSource("ENTSO-E_Boundary_Set_EU_TP.xml", dataSource);

        importer = new CIM1Importer();
    }

    @After
    public void tearDown() throws IOException {
        fileSystem.close();
    }

    @Test
    public void testBusBalance() throws IOException {
        Network network = importer.importData(dataSource, null);

        LoadFlowParameters loadFlowParameters = new LoadFlowParameters();
        loadFlowParameters.setSpecificCompatibility(true);
        computeMissingFlows(network, loadFlowParameters);
        ValidationConfig config = createValidationConfig(loadFlowParameters);

        Path working = Files.createDirectories(fileSystem.getPath("temp-validation"));
        boolean rb = ValidationType.BUSES.check(network, config, working);
        LOG.info("Bus balance validation for tx-from-microBE-adapted is [{}]", rb);
        assertTrue(rb);
    }

    private void resourceToDataSource(String name, DataSource dataSource) throws IOException {
        try (OutputStream stream = dataSource.newOutputStream(name, false)) {
            IOUtils.copy(getClass().getResourceAsStream("/" + name), stream);
        }
    }

    private ValidationConfig createValidationConfig(LoadFlowParameters loadFlowParameters) {
        InMemoryPlatformConfig platformConfig = new InMemoryPlatformConfig(fileSystem);
        MapModuleConfig defaultConfig = platformConfig.createModuleConfig("componentDefaultConfig");
        defaultConfig.setStringProperty("LoadFlowFactory",
                LoadFlowFactoryMock.class.getCanonicalName());
        ValidationConfig config = ValidationConfig.load(platformConfig);
        config.setVerbose(true);
        config.setLoadFlowParameters(loadFlowParameters);
        config.setThreshold(0.01f);
        config.setOkMissingValues(false);
        return config;
    }

    private void computeMissingFlows(Network network, LoadFlowParameters loadFlowParameters) {
        float epsilonX = 0f;
        boolean applyXCorrection = false;
        LoadFlowResultsCompletionParameters p = new LoadFlowResultsCompletionParameters(epsilonX, applyXCorrection);
        LoadFlowResultsCompletion lf = new LoadFlowResultsCompletion(p, loadFlowParameters);
        lf.run(network, null);
    }
}
