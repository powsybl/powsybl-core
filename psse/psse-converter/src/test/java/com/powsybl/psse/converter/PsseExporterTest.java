/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.psse.converter;

import com.google.common.collect.ImmutableList;
import com.powsybl.commons.test.AbstractConverterTest;
import com.powsybl.commons.datasource.DataSource;
import com.powsybl.commons.datasource.FileDataSource;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.commons.datasource.ResourceDataSource;
import com.powsybl.commons.datasource.ResourceSet;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.Load;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.ShuntCompensator;
import com.powsybl.iidm.network.TwoWindingsTransformer;
import com.powsybl.iidm.network.impl.NetworkFactoryImpl;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;

import static com.powsybl.commons.test.ComparisonUtils.compareTxt;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
class PsseExporterTest extends AbstractConverterTest {

    private Network importTest(String basename, String filename, boolean ignoreBaseVoltage) throws IOException {
        Properties properties = new Properties();
        properties.put("psse.import.ignore-base-voltage", ignoreBaseVoltage);

        ReadOnlyDataSource dataSource = new ResourceDataSource(basename, new ResourceSet("/", filename));
        Network network = new PsseImporter().importData(dataSource, new NetworkFactoryImpl(), properties);
        network.setCaseDate(DateTime.parse("2016-01-01T10:00:00.000+02:00"));
        return network;
    }

    private void exportTest(Network network, String baseName, String fileName) throws IOException {
        String pathName = "/work/";
        Path path = fileSystem.getPath(pathName);
        Path file = fileSystem.getPath(pathName + fileName);

        Properties properties = null;
        DataSource dataSource = new FileDataSource(path, baseName);
        new PsseExporter().export(network, properties, dataSource);

        try (InputStream is = Files.newInputStream(file)) {
            compareTxt(getClass().getResourceAsStream("/" + fileName), is);
        }
    }

    @Test
    void importExportTest14() throws IOException {
        Network network = importTest("IEEE_14_bus", "IEEE_14_bus.raw", false);
        exportTest(network, "IEEE_14_bus_exported", "IEEE_14_bus_exported.raw");
    }

    @Test
    void importExportTest14Completed() throws IOException {
        Network network = importTest("IEEE_14_bus_completed", "IEEE_14_bus_completed.raw", false);
        exportTest(network, "IEEE_14_bus_completed_exported", "IEEE_14_bus_completed_exported.raw");
    }

    @Test
    void importExportTest24() throws IOException {
        Network network = importTest("IEEE_24_bus", "IEEE_24_bus.raw", false);
        changeIEEE24BusNetwork(network);
        exportTest(network, "IEEE_24_bus_updated_exported", "IEEE_24_bus_updated_exported.raw");
    }

    private static void changeIEEE24BusNetwork(Network network) {
        network.getBusBreakerView().getBuses().forEach(bus -> {
            bus.setV(bus.getV() * 1.01);
            bus.setAngle(bus.getAngle() * 1.01);
        });

        Load load21 = network.getLoad("B2-L1 ");
        load21.getTerminal().disconnect();

        Load load31 = network.getLoad("B3-L1 ");
        load31.getTerminal().setP(Double.NaN);
        load31.getTerminal().setQ(Double.NaN);

        Generator gen11 = network.getGenerator("B1-G1 ");
        gen11.getTerminal().disconnect();

        Generator gen21 = network.getGenerator("B2-G1 ");
        gen21.getTerminal().setP(Double.NaN);
        gen21.getTerminal().setQ(Double.NaN);

        ShuntCompensator sh11 = network.getShuntCompensator("B1-SwSH1");
        sh11.getTerminal().disconnect();

        Line line = network.getLine("L-1-2-1 ");
        line.getTerminal1().disconnect();
        line.getTerminal2().disconnect();

        TwoWindingsTransformer tw2t = network.getTwoWindingsTransformer("T-24-3-1 ");
        tw2t.getTerminal1().disconnect();
        tw2t.getTerminal2().disconnect();
    }

    @Test
    void importExportTest30() throws IOException {
        Network network = importTest("IEEE_30_bus", "IEEE_30_bus.raw", false);
        exportTest(network, "IEEE_30_bus_updated_exported", "IEEE_30_bus_updated_exported.raw");
    }

    @Test
    void importExportTest57() throws IOException {
        Network network = importTest("IEEE_57_bus", "IEEE_57_bus.raw", false);
        exportTest(network, "IEEE_57_bus_updated_exported", "IEEE_57_bus_updated_exported.raw");
    }

    @Test
    void importExportTest118() throws IOException {
        Network network = importTest("IEEE_118_bus", "IEEE_118_bus.raw", false);
        exportTest(network, "IEEE_118_bus_updated_exported", "IEEE_118_bus_updated_exported.raw");
    }

    @Test
    void importExportTestT3W() throws IOException {
        Network network = importTest("ThreeMIB_T3W_modified", "ThreeMIB_T3W_modified.raw", false);
        exportTest(network, "ThreeMIB_T3W_modified_exported", "ThreeMIB_T3W_modified_exported.raw");
    }

    @Test
    void importExportTestT3Wphase() throws IOException {
        Network network = importTest("ThreeMIB_T3W_phase", "ThreeMIB_T3W_phase.raw", false);
        exportTest(network, "ThreeMIB_T3W_phase_exported", "ThreeMIB_T3W_phase_exported.raw");
    }

    @Test
    void importExportRemoteControl() throws IOException {
        Network network = importTest("remoteControl", "remoteControl.raw", false);
        exportTest(network, "remoteControl_updated_exported", "remoteControl_updated_exported.raw");
    }

    @Test
    void importExportExampleVersion32() throws IOException {
        Network network = importTest("ExampleVersion32", "ExampleVersion32.raw", false);
        exportTest(network, "ExampleVersion32_exported", "ExampleVersion32_exported.raw");
    }

    @Test
    void importExportSwitchedShunt() throws IOException {
        Network network = importTest("SwitchedShunt", "SwitchedShunt.raw", false);
        exportTest(network, "SwitchedShunt_exported", "SwitchedShunt_exported.raw");
    }

    @Test
    void importExportTwoTerminalDc() throws IOException {
        Network network = importTest("twoTerminalDc", "twoTerminalDc.raw", false);
        exportTest(network, "twoTerminalDc_updated_exported", "twoTerminalDc_updated_exported.raw");
    }

    @Test
    void importExportParallelTwoTerminalDcBetweenSameAcBuses() throws IOException {
        Network network = importTest("parallelTwoTerminalDcBetweenSameAcBuses", "parallelTwoTerminalDcBetweenSameAcBuses.raw", false);
        exportTest(network, "parallelTwoTerminalDcBetweenSameAcBuses_updated_exported", "parallelTwoTerminalDcBetweenSameAcBuses_updated_exported.raw");
    }

    @Test
    void importExportIEEE14BusRev35() throws IOException {
        Network network = importTest("IEEE_14_bus_rev35", "IEEE_14_bus_rev35.raw", false);
        exportTest(network, "IEEE_14_bus_rev35_exported", "IEEE_14_bus_rev35_exported.raw");
    }

    @Test
    void importExportIEEE14BusRev35x() throws IOException {
        Network network = importTest("IEEE_14_bus_rev35", "IEEE_14_bus_rev35.rawx", false);
        exportTest(network, "IEEE_14_bus_rev35_exported", "IEEE_14_bus_rev35_exported.rawx");
    }

    @Test
    void importExportTwoWindingsTransformerPhase() throws IOException {
        Network network = importTest("TwoWindingsTransformerPhase", "TwoWindingsTransformerPhase.raw", false);
        exportTest(network, "TwoWindingsTransformerPhase_exported", "TwoWindingsTransformerPhase_exported.raw");
    }

    @Test
    void importExportRawCaseWithSpecialCharacters() throws IOException {
        Network network = importTest("RawCaseWithSpecialCharacters", "RawCaseWithSpecialCharacters.raw", false);
        exportTest(network, "RawCaseWithSpecialCharacters_exported", "RawCaseWithSpecialCharacters_exported.raw");
    }

    @Test
    void importExportRawxCaseWithSpecialCharacters() throws IOException {
        Network network = importTest("RawxCaseWithSpecialCharacters", "RawxCaseWithSpecialCharacters.rawx", false);
        exportTest(network, "RawxCaseWithSpecialCharacters_exported", "RawxCaseWithSpecialCharacters_exported.rawx");
    }

    @Test
    void exportDataTest() throws IOException {
        PsseExporter psseExporter = new PsseExporter();

        assertEquals("Update IIDM to PSS/E ", psseExporter.getComment());
        assertEquals("PSS/E", psseExporter.getFormat());
        assertEquals(ImmutableList.of(), psseExporter.getParameters());
    }
}
