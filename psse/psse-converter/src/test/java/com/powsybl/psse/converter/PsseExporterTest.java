/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.psse.converter;

import com.powsybl.commons.AbstractConverterTest;
import com.powsybl.commons.datasource.DataSource;
import com.powsybl.commons.datasource.FileDataSource;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.commons.datasource.ResourceDataSource;
import com.powsybl.commons.datasource.ResourceSet;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.impl.NetworkFactoryImpl;
import org.joda.time.DateTime;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
public class PsseExporterTest extends AbstractConverterTest {

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
    public void importExportTest14() throws IOException {
        Network network = importTest("IEEE_14_bus", "IEEE_14_bus.raw", false);
        exportTest(network, "IEEE_14_bus_exported", "IEEE_14_bus_exported.raw");
    }

    @Test
    public void importExportTest14Completed() throws IOException {
        Network network = importTest("IEEE_14_bus_completed", "IEEE_14_bus_completed.raw", false);
        exportTest(network, "IEEE_14_bus_completed_exported", "IEEE_14_bus_completed_exported.raw");
    }

    @Test
    public void importExportTest24() throws IOException {
        Network network = importTest("IEEE_24_bus", "IEEE_24_bus.raw", false);
        exportTest(network, "IEEE_24_bus_updated_exported", "IEEE_24_bus_updated_exported.raw");
    }

    @Test
    public void importExportTest30() throws IOException {
        Network network = importTest("IEEE_30_bus", "IEEE_30_bus.raw", false);
        exportTest(network, "IEEE_30_bus_updated_exported", "IEEE_30_bus_updated_exported.raw");
    }

    @Test
    public void importExportTest57() throws IOException {
        Network network = importTest("IEEE_57_bus", "IEEE_57_bus.raw", false);
        exportTest(network, "IEEE_57_bus_updated_exported", "IEEE_57_bus_updated_exported.raw");
    }

    @Test
    public void importExportTest118() throws IOException {
        Network network = importTest("IEEE_118_bus", "IEEE_118_bus.raw", false);
        exportTest(network, "IEEE_118_bus_updated_exported", "IEEE_118_bus_updated_exported.raw");
    }

    @Test
    public void importExportTestT3W() throws IOException {
        Network network = importTest("ThreeMIB_T3W_modified", "ThreeMIB_T3W_modified.raw", false);
        exportTest(network, "ThreeMIB_T3W_modified_exported", "ThreeMIB_T3W_modified_exported.raw");
    }

    @Test
    public void importExportTestT3Wphase() throws IOException {
        Network network = importTest("ThreeMIB_T3W_phase", "ThreeMIB_T3W_phase.raw", false);
        exportTest(network, "ThreeMIB_T3W_phase_exported", "ThreeMIB_T3W_phase_exported.raw");
    }

    @Test
    public void importExportRemoteControl() throws IOException {
        Network network = importTest("remoteControl", "remoteControl.raw", false);
        exportTest(network, "remoteControl_updated_exported", "remoteControl_updated_exported.raw");
    }

    @Test
    public void importExportExampleVersion32() throws IOException {
        Network network = importTest("ExampleVersion32", "ExampleVersion32.raw", false);
        exportTest(network, "ExampleVersion32_exported", "ExampleVersion32_exported.raw");
    }

    @Test
    public void importExportSwitchedShunt() throws IOException {
        Network network = importTest("SwitchedShunt", "SwitchedShunt.raw", false);
        exportTest(network, "SwitchedShunt_exported", "SwitchedShunt_exported.raw");
    }

    @Test
    public void importExportTwoTerminalDc() throws IOException {
        Network network = importTest("twoTerminalDc", "twoTerminalDc.raw", false);
        exportTest(network, "twoTerminalDc_updated_exported", "twoTerminalDc_updated_exported.raw");
    }
}
