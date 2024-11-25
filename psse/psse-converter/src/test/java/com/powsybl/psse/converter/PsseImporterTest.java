/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.psse.converter;

import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.commons.datasource.ResourceDataSource;
import com.powsybl.commons.datasource.ResourceSet;
import com.powsybl.commons.test.AbstractSerDeTest;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.impl.NetworkFactoryImpl;
import com.powsybl.iidm.serde.NetworkSerDe;
import com.powsybl.psse.model.PsseException;
import com.powsybl.psse.model.PsseVersion;
import com.powsybl.psse.model.io.Context;
import com.powsybl.psse.model.pf.PssePowerFlowModel;
import com.powsybl.psse.model.pf.io.PowerFlowRawData33;
import java.time.ZonedDateTime;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;

import static com.powsybl.commons.test.ComparisonUtils.assertXmlEquals;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author JB Heyberger {@literal <jean-baptiste.heyberger at rte-france.com>}
 */
class PsseImporterTest extends AbstractSerDeTest {

    @Test
    void baseTest() {
        Importer importer = new PsseImporter();
        assertEquals("PSS/E", importer.getFormat());
        assertEquals("PSS/E Format to IIDM converter", importer.getComment());
        assertEquals(List.of("raw", "RAW", "rawx", "RAWX"), importer.getSupportedExtensions());
        assertEquals(1, importer.getParameters().size());
        assertEquals("psse.import.ignore-base-voltage", importer.getParameters().get(0).getName());
    }

    private void testNetwork(Network network) throws IOException {
        Path file = fileSystem.getPath("/work/" + network.getId() + ".xiidm");
        network.setCaseDate(ZonedDateTime.parse("2016-01-01T10:00:00.000+02:00"));
        NetworkSerDe.write(network, file);
        try (InputStream is = Files.newInputStream(file)) {
            assertXmlEquals(getClass().getResourceAsStream("/" + network.getId() + ".xiidm"), is);
        }
    }

    @Test
    void existsTest() {
        PsseImporter importer = new PsseImporter();

        // test with a valid raw/RAW file
        assertTrue(importer.exists(new ResourceDataSource("IEEE_14_bus", new ResourceSet("/", "IEEE_14_bus.raw"))));
        assertTrue(importer.exists(new ResourceDataSource("IEEE_30_bus", new ResourceSet("/", "IEEE_30_bus.raw"))));

        // test with an invalid extension
        assertFalse(importer.exists(new ResourceDataSource("IEEE_14_bus", new ResourceSet("/", "IEEE_14_bus.json"))));

        // test with not supported content
        ResourceDataSource dsCaseFlag = new ResourceDataSource("case-flag-not-supported", new ResourceSet("/", "case-flag-not-supported.raw"));
        assertFalse(importer.exists(dsCaseFlag));

        ResourceDataSource dsCaseVersion = new ResourceDataSource("version-not-supported", new ResourceSet("/", "version-not-supported.raw"));
        assertFalse(importer.exists(dsCaseVersion));

        // test with a valid extension and an invalid content
        ResourceDataSource dsCaseInvalid = new ResourceDataSource("fake", new ResourceSet("/", "fake.raw"));
        assertFalse(importer.exists(dsCaseInvalid));

        // test with a valid extension and an invalid content
        ResourceDataSource dsCaseInvalidx = new ResourceDataSource("fake", new ResourceSet("/", "fake.rawx"));
        assertFalse(importer.exists(dsCaseInvalidx));
    }

    private Network importTest(String basename, String filename, boolean ignoreBaseVoltage) throws IOException {
        Properties properties = new Properties();
        properties.put("psse.import.ignore-base-voltage", ignoreBaseVoltage);

        ReadOnlyDataSource dataSource = new ResourceDataSource(basename, new ResourceSet("/", filename));
        Network network = new PsseImporter().importData(dataSource, new NetworkFactoryImpl(), properties);
        testNetwork(network);
        return network;
    }

    @Test
    void importTest14() throws IOException {
        importTest("IEEE_14_bus", "IEEE_14_bus.raw", false);
    }

    @Test
    void importTest24() throws IOException {
        importTest("IEEE_24_bus", "IEEE_24_bus.raw", false);
    }

    @Test
    void importTest30() throws IOException {
        importTest("IEEE_30_bus", "IEEE_30_bus.raw", false);
    }

    @Test
    void importTest57() throws IOException {
        importTest("IEEE_57_bus", "IEEE_57_bus.raw", false);
    }

    @Test
    void importTest118() throws IOException {
        importTest("IEEE_118_bus", "IEEE_118_bus.raw", false);
    }

    @Test
    void importTestT3W() throws IOException {
        importTest("ThreeMIB_T3W_modified", "ThreeMIB_T3W_modified.raw", false);
    }

    @Test
    void importTestT3Wphase() throws IOException {
        importTest("ThreeMIB_T3W_phase", "ThreeMIB_T3W_phase.raw", false);
    }

    @Test
    void remoteControl() throws IOException {
        importTest("remoteControl", "remoteControl.raw", false);
    }

    @Test
    void exampleVersion32() throws IOException {
        importTest("ExampleVersion32", "ExampleVersion32.raw", false);
    }

    @Test
    void switchedShunt() throws IOException {
        importTest("SwitchedShunt", "SwitchedShunt.raw", false);
    }

    @Test
    void switchedShuntWithZeroVswlo() throws IOException {
        importTest("SwitchedShuntWithZeroVswlo", "SwitchedShuntWithZeroVswlo.raw", false);
    }

    @Test
    void importTest14IsolatedBuses() throws IOException {
        importTest("IEEE_14_isolated_buses", "IEEE_14_isolated_buses.raw", false);
    }

    @Test
    void twoTerminalDc() throws IOException {
        importTest("twoTerminalDc", "twoTerminalDc.raw", false);
    }

    @Test
    void twoTerminalDcWithNegativeSetvl() throws IOException {
        importTest("twoTerminalDc_with_negative_setvl", "twoTerminalDc_with_negative_setvl.raw", false);
    }

    @Test
    void parallelTwoTerminalDcBetweenSameAcBuses() throws IOException {
        importTest("parallelTwoTerminalDcBetweenSameAcBuses", "parallelTwoTerminalDcBetweenSameAcBuses.raw", false);
    }

    @Test
    void twoWindingsTransformerPhase() throws IOException {
        importTest("TwoWindingsTransformerPhase", "TwoWindingsTransformerPhase.raw", false);
    }

    @Test
    void isolatedSlackBus() throws IOException {
        importTest("IsolatedSlackBus", "IsolatedSlackBus.raw", false);
    }

    @Test
    void transformersWithZeroNomV() throws IOException {
        importTest("TransformersWithZeroNomV", "TransformersWithZeroNomV.raw", false);
    }

    @Test
    void rawCaseWithSpecialCharacters() throws IOException {
        importTest("RawCaseWithSpecialCharacters", "RawCaseWithSpecialCharacters.raw", false);
    }

    @Test
    void rawxCaseWithSpecialCharacters() throws IOException {
        importTest("RawxCaseWithSpecialCharacters", "RawxCaseWithSpecialCharacters.rawx", false);
    }

    @Test
    void importTest14BusesDuplicateIds() throws IOException {
        Network n = importTest("IEEE_14_buses_duplicate_ids", "IEEE_14_buses_duplicate_ids.raw", false);
        assertNotNull(n.getLoad("B2-L1 "));
        assertNotNull(n.getLoad("B2-L10"));
        assertNotNull(n.getGenerator("B1-G1 "));
        assertNotNull(n.getGenerator("B1-G10"));
        assertNotNull(n.getShuntCompensator("B9-SH 1"));
        assertNotNull(n.getShuntCompensator("B9-SH 0"));
        assertNotNull(n.getLine("L-13-14-1 "));
        assertNotNull(n.getLine("L-14-13-10"));
        assertNotNull(n.getLine("L-13-14-11"));
        assertNotNull(n.getTwoWindingsTransformer("T-4-7-1 "));
        assertNotNull(n.getTwoWindingsTransformer("T-7-4-10"));
        assertNotNull(n.getTwoWindingsTransformer("T-4-7-11"));
    }

    @Test
    void importTest14BusesDuplicateIdsV35() throws IOException {
        Network n = importTest("IEEE_14_buses_duplicate_ids_rev35", "IEEE_14_buses_duplicate_ids_rev35.raw", false);
        assertNotNull(n.getLoad("B2-L1 "));
        assertNotNull(n.getLoad("B2-L10"));
        assertNotNull(n.getGenerator("B1-G1 "));
        assertNotNull(n.getGenerator("B1-G10"));
        assertNotNull(n.getShuntCompensator("B9-SH 1"));
        assertNotNull(n.getShuntCompensator("B9-SH 0"));
        assertNotNull(n.getShuntCompensator("B1-SwSH1"));
        assertNotNull(n.getShuntCompensator("B1-SwSH10"));
        assertNotNull(n.getLine("L-1-2-1 "));
        assertNotNull(n.getLine("L-1-2-10"));
        assertNotNull(n.getLine("L-2-1-11"));
        assertNotNull(n.getTwoWindingsTransformer("T-4-7-1 "));
        assertNotNull(n.getTwoWindingsTransformer("T-4-7-10"));
    }

    @Test
    void testRates() throws IOException {
        Context context = new Context();
        ReadOnlyDataSource ds = new ResourceDataSource("ThreeMIB_T3W_modified", new ResourceSet("/", "ThreeMIB_T3W_modified.raw"));
        PssePowerFlowModel model = new PowerFlowRawData33().read(ds, "raw", context);
        assertEquals(10451.0, model.getNonTransformerBranches().get(0).getRates().getRatea(), 0);
        assertEquals(10452.0, model.getNonTransformerBranches().get(0).getRates().getRateb(), 0);
        assertEquals(10453.0, model.getNonTransformerBranches().get(0).getRates().getRatec(), 0);
        assertEquals(10561.0, model.getNonTransformerBranches().get(1).getRates().getRatea(), 0);
        assertEquals(10562.0, model.getNonTransformerBranches().get(1).getRates().getRateb(), 0);
        assertEquals(10563.0, model.getNonTransformerBranches().get(1).getRates().getRatec(), 0);
        assertEquals(10140.0, model.getTransformers().get(0).getWinding1Rates().getRatea(), 0);
        assertEquals(10141.0, model.getTransformers().get(0).getWinding1Rates().getRateb(), 0);
        assertEquals(10142.0, model.getTransformers().get(0).getWinding1Rates().getRatec(), 0);
        assertEquals(101.0, model.getTransformers().get(1).getWinding1Rates().getRatea(), 0);
        assertEquals(102.0, model.getTransformers().get(1).getWinding1Rates().getRateb(), 0);
        assertEquals(103.0, model.getTransformers().get(1).getWinding1Rates().getRatec(), 0);
        assertEquals(201.0, model.getTransformers().get(1).getWinding2Rates().getRatea(), 0);
        assertEquals(202.0, model.getTransformers().get(1).getWinding2Rates().getRateb(), 0);
        assertEquals(203.0, model.getTransformers().get(1).getWinding2Rates().getRatec(), 0);
        assertEquals(301.0, model.getTransformers().get(1).getWinding3Rates().getRatea(), 0);
        assertEquals(302.0, model.getTransformers().get(1).getWinding3Rates().getRateb(), 0);
        assertEquals(303.0, model.getTransformers().get(1).getWinding3Rates().getRatec(), 0);
    }

    @Test()
    void badModeTest() {
        ReadOnlyDataSource dataSource = new ResourceDataSource("case-flag-not-supported", new ResourceSet("/", "case-flag-not-supported.raw"));
        PsseImporter psseImporter = new PsseImporter();
        NetworkFactory networkFactory = new NetworkFactoryImpl();
        assertThatExceptionOfType(PsseException.class)
                .isThrownBy(() -> psseImporter.importData(dataSource, networkFactory, null))
                .withMessage("Incremental load of data option (IC = 1) is not supported");
    }

    @Test
    void badVersionTest() {
        ReadOnlyDataSource dataSource = new ResourceDataSource("version-not-supported", new ResourceSet("/", "version-not-supported.raw"));
        PsseImporter psseImporter = new PsseImporter();
        NetworkFactory networkFactory = new NetworkFactoryImpl();
        assertThatExceptionOfType(PsseException.class)
                .isThrownBy(() -> psseImporter.importData(dataSource, networkFactory, null))
                .withMessage("Version 29 not supported. Supported versions are: " + PsseVersion.supportedVersions());
    }

    @Test
    void dataSourceExistsTest() {
        ReadOnlyDataSource dataSource;

        dataSource = new ResourceDataSource("version-not-supported", new ResourceSet("/", "version-not-supported.raw"));
        assertFalse(new PsseImporter().exists(dataSource));

        dataSource = new ResourceDataSource("IEEE_14_bus", new ResourceSet("/", "IEEE_14_bus.raw"));
        assertTrue(new PsseImporter().exists(dataSource));

        dataSource = new ResourceDataSource("IEEE_14_bus_rev35", new ResourceSet("/", "IEEE_14_bus_rev35.rawx"));
        assertTrue(new PsseImporter().exists(dataSource));
    }

    @Test
    void importTest14ZipLoad() throws IOException {
        importTest("IEEE_14_buses_zip_load", "IEEE_14_buses_zip_load.raw", false);
    }

    @Test
    void importTestTransformersWithVoltageControlAndNotDefinedControlledBusV33() {
        ReadOnlyDataSource dataSource = new ResourceDataSource("TransformersWithVoltageControlAndNotDefinedControlledBus", new ResourceSet("/", "TransformersWithVoltageControlAndNotDefinedControlledBus.raw"));
        Network network = new PsseImporter().importData(dataSource, new NetworkFactoryImpl(), new Properties());

        TwoWindingsTransformer t2w = network.getTwoWindingsTransformer("T-1-4-1 ");
        assertNotNull(t2w);
        assertNull(t2w.getRatioTapChanger());

        ThreeWindingsTransformer t3w = network.getThreeWindingsTransformer("T-4-2-7-1 ");
        assertNotNull(t3w);
        assertNull(t3w.getLeg1().getRatioTapChanger());
        assertNotNull(t3w.getLeg2().getRatioTapChanger());
        assertFalse(t3w.getLeg2().getRatioTapChanger().isRegulating());
        assertNotNull(t3w.getLeg3().getRatioTapChanger());
        assertFalse(t3w.getLeg3().getRatioTapChanger().isRegulating());
    }
}
