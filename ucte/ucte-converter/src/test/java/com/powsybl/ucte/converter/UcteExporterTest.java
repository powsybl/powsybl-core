/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.ucte.converter;

import com.google.common.collect.ImmutableList;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.datasource.MemDataSource;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.commons.datasource.ResourceDataSource;
import com.powsybl.commons.datasource.ResourceSet;
import com.powsybl.commons.test.AbstractSerDeTest;
import com.powsybl.iidm.network.*;
import org.apache.commons.io.FilenameUtils;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Properties;

import static com.powsybl.commons.test.ComparisonUtils.assertTxtEquals;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * @author Abdelsalem Hedhili {@literal <abdelsalem.hedhili at rte-france.com>}
 */

class UcteExporterTest extends AbstractSerDeTest {

    /**
     * Utility method to load a network file from resource directory without calling
     * @param filePath path of the file relative to resources directory
     * @return imported network
     */
    private static Network loadNetworkFromResourceFile(String filePath) {
        ReadOnlyDataSource dataSource = new ResourceDataSource(FilenameUtils.getBaseName(filePath), new ResourceSet(FilenameUtils.getPath(filePath), FilenameUtils.getName(filePath)));
        return new UcteImporter().importData(dataSource, NetworkFactory.findDefault(), null);
    }

    private static Network loadNetworkFromResourceFile(String filePath, Properties parameters) {
        ReadOnlyDataSource dataSource = new ResourceDataSource(FilenameUtils.getBaseName(filePath), new ResourceSet(FilenameUtils.getPath(filePath), FilenameUtils.getName(filePath)));
        return new UcteImporter().importData(dataSource, NetworkFactory.findDefault(), parameters);
    }

    private static void testExporter(Network network, String reference) throws IOException {
        testExporter(network, reference, new Properties());
    }

    private static void testExporter(Network network, String reference, Properties parameters) throws IOException {
        MemDataSource dataSource = new MemDataSource();

        UcteExporter exporter = new UcteExporter();
        exporter.export(network, parameters, dataSource);

        try (InputStream actual = dataSource.newInputStream(null, "uct");
             InputStream expected = UcteExporterTest.class.getResourceAsStream(reference)) {
            assertTxtEquals(expected, actual, Arrays.asList(1, 2));
        }
    }

    @Test
    void testExportBoundaryLineWithoutGeneration() throws IOException {
        Network network = NetworkFactory.findDefault().createNetwork("test", "test");
        Substation substation = network.newSubstation().setId("S").setCountry(Country.FR).add();
        VoltageLevel voltageLevel = substation.newVoltageLevel()
                .setId("VL")
                .setNominalV(380.0)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        voltageLevel.getBusBreakerView().newBus().setId("FFFFFF11").add();
        BoundaryLine boundaryLine = voltageLevel.newBoundaryLine()
                .setId("FFFFFF11 XXXXXX11 1")
                .setBus("FFFFFF11")
                .setR(1.0)
                .setX(10.0)
                .setG(0.0)
                .setB(0.0)
                .setP0(50.0)
                .setQ0(30.0)
                .setPairingKey("XXXXXX11")
                .add();
        assertNull(boundaryLine.getGeneration());

        Properties parameters = new Properties();
        parameters.put("ucte.export.naming-strategy", "Default");
        MemDataSource dataSource = new MemDataSource();
        new UcteExporter().export(network, parameters, dataSource);

        String xnodeLine;
        try (InputStream is = dataSource.newInputStream(null, "uct")) {
            xnodeLine = new String(is.readAllBytes(), StandardCharsets.UTF_8).lines()
                    .filter(line -> line.startsWith("XXXXXX11"))
                    .findFirst()
                    .orElseThrow(() -> new AssertionError("No line found for X-node XXXXXX11"));
        }
        // Active power generation (49-56) and reactive power generation (57-64): zero generation assumed.
        assertEquals("0.00000", xnodeLine.substring(49, 56));
        assertEquals("0.00000", xnodeLine.substring(57, 64));
    }

    @Test
    void testMerge() throws IOException {
        Network networkFR = loadNetworkFromResourceFile("/frTestGridForMerging.uct");
        testExporter(networkFR, "/frTestGridForMerging.uct");

        Network networkBE = loadNetworkFromResourceFile("/beTestGridForMerging.uct");
        testExporter(networkBE, "/beTestGridForMerging.uct");

        Network merge = Network.merge(networkBE, networkFR);
        testExporter(merge, "/uxTestGridForMerging.uct");
    }

    @Test
    void testMergeProperties() throws IOException {
        Network networkFR = loadNetworkFromResourceFile("/frForMergeProperties.uct");
        testExporter(networkFR, "/frForMergeProperties.uct");

        Network networkBE = loadNetworkFromResourceFile("/beForMergeProperties.uct");
        testExporter(networkBE, "/beForMergeProperties.uct");

        Network mergedNetwork = Network.merge(networkBE, networkFR);
        testExporter(mergedNetwork, "/uxForMergeProperties.uct");
    }

    @Test
    void testExport() throws IOException {
        Network network = loadNetworkFromResourceFile("/expectedExport.uct");
        testExporter(network, "/expectedExport.uct");
    }

    @Test
    void testExporter() {
        var exporter = new UcteExporter();
        assertEquals("UCTE", exporter.getFormat());
        assertNotEquals("IIDM", exporter.getFormat());
        assertEquals("IIDM to UCTE converter", exporter.getComment());
        assertNotEquals("UCTE to IIDM converter", exporter.getComment());
        assertEquals(2, exporter.getParameters().size());
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

    @Test
    void roundTripOfNetworkWithTapChangers() throws IOException {
        Network network = loadNetworkFromResourceFile("/expectedExport2.uct");
        testExporter(network, "/expectedExport3.uct"); // because of asymmetrical phase shifter
    }

    @Test
    void roundTripOfNetworkWithTapChangers2() throws IOException {
        Network network = loadNetworkFromResourceFile("/expectedExport4.uct");
        testExporter(network, "/expectedExport4.uct");
    }

    @Test
    void roundTripOfNetworkWithTapChangers3() throws IOException {
        Network network = loadNetworkFromResourceFile("/expectedExport5.uct");
        testExporter(network, "/expectedExport5.uct");
    }

    @Test
    void testTapChangers() {
        Network network = loadNetworkFromResourceFile("/expectedExport2.uct");
        Network exportedNetwork = loadNetworkFromResourceFile("/expectedExport3.uct"); // because of asymmetrical phase shifter
        String rtcId = "0BBBBB5  0AAAAA2  1";
        assertEquals(network.getTwoWindingsTransformer(rtcId).getRatioTapChanger().getCurrentStep().getRho(),
                exportedNetwork.getTwoWindingsTransformer(rtcId).getRatioTapChanger().getCurrentStep().getRho());
        String ptcId = "HDDDDD2  HCCCCC1  1";
        assertEquals(network.getTwoWindingsTransformer(ptcId).getPhaseTapChanger().getCurrentStep().getRho(),
                exportedNetwork.getTwoWindingsTransformer(ptcId).getPhaseTapChanger().getCurrentStep().getRho(), 0.0001);
        assertEquals(network.getTwoWindingsTransformer(ptcId).getPhaseTapChanger().getCurrentStep().getAlpha(),
                exportedNetwork.getTwoWindingsTransformer(ptcId).getPhaseTapChanger().getCurrentStep().getAlpha(), 0.0001);
        String ptcId2 = "ZABCD221 ZEFGH221 1";
        assertEquals(network.getTwoWindingsTransformer(ptcId2).getPhaseTapChanger().getCurrentStep().getRho(),
                exportedNetwork.getTwoWindingsTransformer(ptcId2).getPhaseTapChanger().getCurrentStep().getRho());
        assertEquals(network.getTwoWindingsTransformer(ptcId2).getPhaseTapChanger().getCurrentStep().getAlpha(),
                exportedNetwork.getTwoWindingsTransformer(ptcId2).getPhaseTapChanger().getCurrentStep().getAlpha(), 0.0001);
    }

    @Test
    void roundTripOfCombineRtcAndPtc() throws IOException {
        Properties parameters = new Properties();
        parameters.put("ucte.import.combine-phase-angle-regulation", "true");
        parameters.put("ucte.export.combine-phase-angle-regulation", "true");
        Network network = loadNetworkFromResourceFile("/expectedExport5.uct", parameters);
        testExporter(network, "/expectedExport5.uct", parameters);
    }
}
