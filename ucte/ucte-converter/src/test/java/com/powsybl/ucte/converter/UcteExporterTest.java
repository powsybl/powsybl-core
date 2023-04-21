/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.ucte.converter;

import com.google.common.collect.ImmutableList;
import com.powsybl.commons.test.AbstractConverterTest;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.datasource.MemDataSource;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.commons.datasource.ResourceDataSource;
import com.powsybl.commons.datasource.ResourceSet;
import com.powsybl.iidm.network.*;
import com.powsybl.ucte.converter.util.UcteConstants;
import org.apache.commons.io.FilenameUtils;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Properties;

import static com.powsybl.commons.test.ComparisonUtils.compareTxt;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Abdelsalem Hedhili <abdelsalem.hedhili at rte-france.com>
 */

class UcteExporterTest extends AbstractConverterTest {

    /**
     * Utility method to load a network file from resource directory without calling
     * @param filePath path of the file relative to resources directory
     * @return imported network
     */
    private static Network loadNetworkFromResourceFile(String filePath) {
        ReadOnlyDataSource dataSource = new ResourceDataSource(FilenameUtils.getBaseName(filePath), new ResourceSet(FilenameUtils.getPath(filePath), FilenameUtils.getName(filePath)));
        return new UcteImporter().importData(dataSource, NetworkFactory.findDefault(), null);
    }

    private static void testExporter(Network network, String reference) throws IOException {
        MemDataSource dataSource = new MemDataSource();

        UcteExporter exporter = new UcteExporter();
        exporter.export(network, new Properties(), dataSource);

        try (InputStream actual = dataSource.newInputStream(null, "uct");
             InputStream expected = UcteExporterTest.class.getResourceAsStream(reference)) {
            compareTxt(expected, actual, Arrays.asList(1, 2));
        }
    }

    @Test
    void testMerge() throws IOException {
        Network networkFR = loadNetworkFromResourceFile("/frTestGridForMerging.uct");
        testExporter(networkFR, "/frTestGridForMerging.uct");

        Network networkBE = loadNetworkFromResourceFile("/beTestGridForMerging.uct");
        testExporter(networkBE, "/beTestGridForMerging.uct");

        Network merge = Network.create("merge", "UCT");
        merge.merge(networkBE, networkFR);
        testExporter(merge, "/uxTestGridForMerging.uct");
    }

    @Test
    void testMergeProperties() throws IOException {
        Network networkFR = loadNetworkFromResourceFile("/frForMergeProperties.uct");
        testExporter(networkFR, "/frForMergeProperties.uct");

        Network networkBE = loadNetworkFromResourceFile("/beForMergeProperties.uct");
        testExporter(networkBE, "/beForMergeProperties.uct");

        Network mergedNetwork = Network.create("mergedNetwork", "UCT");
        mergedNetwork.merge(networkBE, networkFR);
        testExporter(mergedNetwork, "/uxForMergeProperties.uct");
    }

    @Test
    void testExport() throws IOException {
        Network network = loadNetworkFromResourceFile("/expectedExport.uct");
        testExporter(network, "/expectedExport.uct");
    }

    private static void introduceNbVl(Network network) {
        network.getTwoWindingsTransformer("B_SU1_11 B_SU1_21 1").remove();
        network.getLine("XB__F_11 B_SU1_11 1 + XB__F_11 F_SU1_11 1").remove();
        network.getVoltageLevel("B_SU1_1").remove();
        VoltageLevel vl = network.getSubstation("B_SU1_").newVoltageLevel()
                .setId("B_SU1_1")
                .setNominalV(380.0)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();
        vl.getNodeBreakerView().newInternalConnection().setNode1(0).setNode2(1).add();
        vl.getNodeBreakerView().newInternalConnection().setNode1(0).setNode2(2).add();
        vl.newLoad().setId("B_SU1_11_load").setLoadType(LoadType.UNDEFINED).setP0(50.0).setQ0(0.0).setNode(2).add();
        TwoWindingsTransformer twt = network.getSubstation("B_SU1_").newTwoWindingsTransformer()
                .setId("B_SU1_11 B_SU1_21 1")
                .setR(0.55)
                .setX(1.68)
                .setG(0.0)
                .setB(1.325E-5)
                .setRatedU1(225.0)
                .setRatedU2(400.0)
                .setBus1("B_SU1_21")
                .setVoltageLevel1("B_SU1_2")
                .setNode2(0)
                .setVoltageLevel2("B_SU1_1")
                .add();
        twt.setProperty(UcteConstants.NOMINAL_POWER_KEY, "5000.0");
        twt.setProperty(UcteConstants.ELEMENT_NAME_PROPERTY_KEY, "Test 2WT 2");
        twt.newCurrentLimits2().setPermanentLimit(5000.0).add();
        Line tl = network.newTieLine()
                .setId("XB__F_11 B_SU1_11 1 + XB__F_11 F_SU1_11 1")
                .setUcteXnodeCode("XB__F_11")
                .setNode1(1)
                .setVoltageLevel1("B_SU1_1")
                .setNode1(1)
                .setVoltageLevel2("F_SU1_1")
                .setBus2("F_SU1_11")
                .newHalfLine1()
                .setId("XB__F_11 B_SU1_11 1")
                .setR(0.55)
                .setX(1.68)
                .setG1(0.0)
                .setB1(1.325E-5)
                .setG2(0.0)
                .setB2(0.0)
                .setFictitious(true)
                .add()
                .newHalfLine2()
                .setId("XB__F_11 F_SU1_11 1")
                .setR(0.55)
                .setX(1.68)
                .setG1(0.0)
                .setB1(0.0)
                .setG2(0.0)
                .setB2(1.325E-5)
                .setFictitious(true)
                .add()
                .add();
        tl.setProperty(UcteConstants.GEOGRAPHICAL_NAME_PROPERTY_KEY, "FR-BE Xnode1");
        tl.setProperty("elementName_1", "Test TL 1/2");
        tl.setProperty("elementName_2", "Test TL 1/1");
        tl.setProperty("status_XNode", "EQUIVALENT");
        tl.newCurrentLimits1().setPermanentLimit(5000.0).add();
        tl.newCurrentLimits2().setPermanentLimit(5000.0).add();
    }

    @Test
    void testNbExport() throws IOException {
        Network network = loadNetworkFromResourceFile("/expectedExport.uct");
        introduceNbVl(network);
        testExporter(network, "/expectedExportNb.uct"); // only difference is there is no bus name
    }

    @Test
    void testExporter() {
        var exporter = new UcteExporter();
        assertEquals("UCTE", exporter.getFormat());
        assertNotEquals("IIDM", exporter.getFormat());
        assertEquals("IIDM to UCTE converter", exporter.getComment());
        assertNotEquals("UCTE to IIDM converter", exporter.getComment());
        assertEquals(1, exporter.getParameters().size());
    }

    @Test
    void testCouplerToXnodeImport() throws IOException {
        Network network = loadNetworkFromResourceFile("/couplerToXnodeExample.uct");
        testExporter(network, "/couplerToXnodeExample.uct");
    }

    @Test
    void shouldNotUseScientificalNotationForExport() throws IOException {
        Network network = loadNetworkFromResourceFile("/testGridNoScientificNotation.uct");
        testExporter(network, "/testGridNoScientificNotation.uct");
    }

    @Test
    void testDefaultOneNamingStrategy() {
        NamingStrategy defaultNamingStrategy = UcteExporter.findNamingStrategy(null, ImmutableList.of(new DefaultNamingStrategy()));
        assertEquals("Default", defaultNamingStrategy.getName());
    }

    @Test
    void testDefaultTwoNamingStrategies() {
        try {
            UcteExporter.findNamingStrategy(null, ImmutableList.of(new DefaultNamingStrategy(), new OtherNamingStrategy()));
            fail();
        } catch (PowsyblException ignored) {
        }
    }

    @Test
    void testDefaultNoNamingStrategy() {
        try {
            UcteExporter.findNamingStrategy(null, ImmutableList.of());
            fail();
        } catch (PowsyblException ignored) {
        }
    }

    @Test
    void testChosenTwoNamingStrategies() {
        NamingStrategy namingStrategy = UcteExporter.findNamingStrategy("Default", ImmutableList.of(new DefaultNamingStrategy(), new OtherNamingStrategy()));
        assertEquals("Default", namingStrategy.getName());
        namingStrategy = UcteExporter.findNamingStrategy("OtherNamingStrategy", ImmutableList.of(new DefaultNamingStrategy(), new OtherNamingStrategy()));
        assertEquals("OtherNamingStrategy", namingStrategy.getName());
    }

    @Test
    void testWithIdDuplicationBetweenLineAndTransformer() throws IOException {
        Network network = loadNetworkFromResourceFile("/id_duplication_test.uct");
        testExporter(network, "/id_duplication_test.uct");
    }

    @Test
    void testElementStatusHandling() throws IOException {
        Network network = loadNetworkFromResourceFile("/multipleStatusTests.uct");
        testExporter(network, "/multipleStatusTests.uct");
    }

    @Test
    void testVoltageRegulatingXnode() throws IOException {
        Network network = loadNetworkFromResourceFile("/frVoltageRegulatingXnode.uct");
        testExporter(network, "/frVoltageRegulatingXnode.uct");
    }

    @Test
    void testMissingPermanentLimit() throws IOException {
        Network network = loadNetworkFromResourceFile("/expectedExport_withoutPermanentLimit.uct");
        testExporter(network, "/expectedExport_withoutPermanentLimit.uct");
    }

    @Test
    void testXnodeTransformer() throws IOException {
        Network network = loadNetworkFromResourceFile("/xNodeTransformer.uct");
        testExporter(network, "/xNodeTransformer.uct");
    }

    @Test
    void testValidationUtil() throws IOException {
        Network network = loadNetworkFromResourceFile("/expectedExport.uct");
        for (Bus bus : network.getBusView().getBuses()) {
            bus.setV(bus.getVoltageLevel().getNominalV() * 1.4);
        }
        for (Generator gen : network.getGenerators()) {
            if (gen.isVoltageRegulatorOn()) {
                gen.setTargetV(gen.getRegulatingTerminal().getVoltageLevel().getNominalV() * 1.4);
            }
        }
        for (TwoWindingsTransformer twt : network.getTwoWindingsTransformers()) {
            RatioTapChanger rtc = twt.getRatioTapChanger();
            if (rtc != null && rtc.isRegulating()) {
                rtc.setTargetV(rtc.getRegulationTerminal().getVoltageLevel().getNominalV() * 1.4);
            }
        }
        testExporter(network, "/invalidVoltageReference.uct");
    }

    @Test
    void roundTripOfNetworkWithXnodesConnectedToOneClosedLineMustSucceed() throws IOException {
        Network network = loadNetworkFromResourceFile("/xnodeOneClosedLine.uct");
        testExporter(network, "/xnodeOneClosedLine.uct");
    }

    @Test
    void roundTripOfNetworkWithXnodesConnectedToTwoClosedLineMustSucceed() throws IOException {
        Network network = loadNetworkFromResourceFile("/xnodeTwoClosedLine.uct");
        testExporter(network, "/xnodeTwoClosedLine.uct");
    }

    @Test
    void roundTripOfNetworkWithPstAngleRegulationMustSucceed() throws IOException {
        Network network = loadNetworkFromResourceFile("/phaseShifterActivePowerOn.uct");
        testExporter(network, "/phaseShifterActivePowerOn.uct");
    }

}
