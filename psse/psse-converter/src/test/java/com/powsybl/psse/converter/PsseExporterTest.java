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

    //Test
    public void importExportTest14Completed() throws IOException {
        Network network = importTest("IEEE_14_bus_completed", "IEEE_14_bus_completed.raw", false);
        exportTest(network, "IEEE_14_bus_completed_exported", "IEEE_14_bus_completed_exported.raw");
    }

    //@Test
    public void importTest24() throws IOException {
        Network network = importTest("IEEE_24_bus", "IEEE_24_bus.raw", false);
        exportTest(network, "IEEE_24_bus_exported", "IEEE_24_bus_exported.raw");
    }

    @Test
    public void importTest30() throws IOException {
        importTest("IEEE_30_bus", "IEEE_30_bus.raw", false);
    }

    @Test
    public void importTest57() throws IOException {
        importTest("IEEE_57_bus", "IEEE_57_bus.raw", false);
    }

    @Test
    public void importTest118() throws IOException {
        importTest("IEEE_118_bus", "IEEE_118_bus.raw", false);
    }

    @Test
    public void importTestT3W() throws IOException {
        importTest("ThreeMIB_T3W_modified", "ThreeMIB_T3W_modified.raw", false);
    }

    @Test
    public void importTestT3Wphase() throws IOException {
        importTest("ThreeMIB_T3W_phase", "ThreeMIB_T3W_phase.raw", false);
    }

    @Test
    public void remoteControl() throws IOException {
        importTest("remoteControl", "remoteControl.raw", false);
    }

    @Test
    public void exampleVersion32() throws IOException {
        importTest("ExampleVersion32", "ExampleVersion32.raw", false);
    }

    @Test
    public void switchedShunt() throws IOException {
        importTest("SwitchedShunt", "SwitchedShunt.raw", false);
    }

    @Test
    public void importTest14IsolatedBuses() throws IOException {
        importTest("IEEE_14_isolated_buses", "IEEE_14_isolated_buses.raw", false);
    }

    @Test
    public void twoTerminalDc() throws IOException {
        importTest("twoTerminalDc", "twoTerminalDc.raw", false);
    }
}
