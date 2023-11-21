/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.matpower.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.ByteStreams;
import com.powsybl.cgmes.conformity.CgmesConformity1ModifiedCatalog;
import com.powsybl.commons.config.InMemoryPlatformConfig;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.commons.datasource.FileDataSource;
import com.powsybl.commons.datasource.MemDataSource;
import com.powsybl.commons.test.AbstractSerDeTest;
import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.NetworkFactory;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.network.test.FourSubstationsNodeBreakerFactory;
import com.powsybl.matpower.model.MatpowerModel;
import com.powsybl.matpower.model.MatpowerModelFactory;
import com.powsybl.matpower.model.MatpowerReader;
import com.powsybl.matpower.model.MatpowerWriter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class MatpowerExporterTest extends AbstractSerDeTest {

    private PlatformConfig platformConfig;

    @Override
    @BeforeEach
    public void setUp() throws IOException {
        super.setUp();
        platformConfig = new InMemoryPlatformConfig(fileSystem);
    }

    private void exportToMatAndCompareTo(Network network, String refJsonFile) throws IOException {
        Properties parameters = new Properties();
        parameters.setProperty(MatpowerExporter.WITH_BUS_NAMES, "true");
        exportToMatAndCompareTo(network, refJsonFile, parameters);
    }

    private void exportToMatAndCompareTo(Network network, String refJsonFile, Properties parameters) throws IOException {
        MemDataSource dataSource = new MemDataSource();
        new MatpowerExporter(platformConfig).export(network, parameters, dataSource);
        byte[] mat = dataSource.getData(null, "mat");
        MatpowerModel model = MatpowerReader.read(new ByteArrayInputStream(mat), network.getId());
        String json = new ObjectMapper()
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(model);
        assertEquals(new String(ByteStreams.toByteArray(Objects.requireNonNull(MatpowerExporterTest.class.getResourceAsStream(refJsonFile))), StandardCharsets.UTF_8),
                json);
    }

    @Test
    void testEsgTuto1() throws IOException {
        var network = EurostagTutorialExample1Factory.create();
        exportToMatAndCompareTo(network, "/sim1.json");
    }

    @Test
    void testEsgTuto1WithoutBusNames() throws IOException {
        var network = EurostagTutorialExample1Factory.create();
        Properties parameters = new Properties();
        parameters.setProperty(MatpowerExporter.WITH_BUS_NAMES, "false");
        exportToMatAndCompareTo(network, "/sim1-without-bus-names.json", parameters);
    }

    @Test
    void testMicroGridBe() throws IOException {
        Network network = Network.read(CgmesConformity1ModifiedCatalog.microGridBaseCaseBERatioPhaseTapChangerTabular().dataSource());
        exportToMatAndCompareTo(network, "/be.json");
    }

    @Test
    void testWithTieLines() throws IOException {
        var network = EurostagTutorialExample1Factory.createWithTieLine();
        exportToMatAndCompareTo(network, "/sim1-with-tie-lines.json");
    }

    @Test
    void testWithHvdcLines() throws IOException {
        var network = FourSubstationsNodeBreakerFactory.create();
        exportToMatAndCompareTo(network, "/fourSubstationFactory.json");
    }

    @Test
    void testCase30ConsideringBaseVoltage() throws IOException {
        MatpowerModel model = MatpowerModelFactory.create30();
        model.setCaseName("ieee30-considering-base-voltage");
        String caseId = model.getCaseName();
        Path matFile = tmpDir.resolve(caseId + ".mat");
        MatpowerWriter.write(model, matFile, true);

        Properties properties = new Properties();
        properties.put("matpower.import.ignore-base-voltage", false);
        Network network = new MatpowerImporter().importData(new FileDataSource(tmpDir, caseId), NetworkFactory.findDefault(), properties);

        exportToMatAndCompareTo(network, "/ieee30-considering-base-voltage.json");
    }

    @Test
    void testNonRegulatingGenOnPVBus() throws IOException {
        var network = EurostagTutorialExample1Factory.create();
        network.getVoltageLevel("VLGEN").newGenerator()
                .setId("GEN2")
                .setBus("NGEN")
                .setTargetP(10)
                .setTargetQ(5)
                .setMinP(0)
                .setMaxP(1000)
                .setVoltageRegulatorOn(false)
                .add();
        exportToMatAndCompareTo(network, "/sim1-with-non-regulating-gen.json");
    }

    @Test
    void testWithCurrentLimits() throws IOException {
        var network = EurostagTutorialExample1Factory.createWithFixedCurrentLimits();
        exportToMatAndCompareTo(network, "/sim1-with-current-limits.json");
    }

    @Test
    void testWithCurrentLimits2() throws IOException {
        var network = EurostagTutorialExample1Factory.create();
        Line line = network.getLine("NHV1_NHV2_1");
        line.newCurrentLimits1()
                .setPermanentLimit(1000)
                .beginTemporaryLimit()
                    .setName("20'")
                    .setAcceptableDuration(20 * 60)
                    .setValue(1100)
                .endTemporaryLimit()
                .beginTemporaryLimit()
                    .setName("10'")
                    .setAcceptableDuration(10 * 60)
                    .setValue(1200)
                .endTemporaryLimit()
                .beginTemporaryLimit()
                    .setName("1'")
                    .setAcceptableDuration(60)
                    .setValue(1300)
                .endTemporaryLimit()
                .beginTemporaryLimit()
                    .setName("N/A")
                    .setAcceptableDuration(0)
                    .setValue(Double.MAX_VALUE)
                .endTemporaryLimit()
                .add();
        exportToMatAndCompareTo(network, "/sim1-with-current-limits2.json");
    }

    @Test
    void testWithApparentPowerLimits() throws IOException {
        var network = EurostagTutorialExample1Factory.createWithFixedCurrentLimits();
        var l = network.getLine("NHV1_NHV2_1");
        l.newApparentPowerLimits1()
                .setPermanentLimit(1000)
                .beginTemporaryLimit()
                    .setName("1'")
                    .setAcceptableDuration(60)
                    .setValue(1500)
                .endTemporaryLimit()
                .add();
        l.newCurrentLimits2()
                .setPermanentLimit(1000)
                .add();
        exportToMatAndCompareTo(network, "/sim1-with-apparent-power-limits.json");
    }

    @Test
    void testNanTargetQIssue() throws IOException {
        var network = EurostagTutorialExample1Factory.create();
        network.getGenerator("GEN").setTargetQ(Double.NaN);
        exportToMatAndCompareTo(network, "/sim1-with-nan-target-q.json");
    }
}
