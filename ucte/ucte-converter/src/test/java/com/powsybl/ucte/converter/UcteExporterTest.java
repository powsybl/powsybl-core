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
import com.powsybl.commons.report.ReportNode;
import com.powsybl.commons.test.AbstractSerDeTest;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.regulation.RegulationMode;
import com.powsybl.ucte.network.UcteNetwork;
import com.powsybl.ucte.network.io.UcteReader;
import org.apache.commons.io.FilenameUtils;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
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
        assertEquals(3, exporter.getParameters().size());
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
            if (gen.isRegulatingWithMode(RegulationMode.VOLTAGE)) {
                double targetV = gen.getRegulatingTerminal().getVoltageLevel().getNominalV() * 1.4;
                if (gen.hasRegulatingTerminal()) {
                    gen.getVoltageRegulation().setTargetValue(targetV);
                } else {
                    gen.setLocalTargetV(targetV);
                }
            }
        }
        for (TwoWindingsTransformer twt : network.getTwoWindingsTransformers()) {
            RatioTapChanger rtc = twt.getRatioTapChanger();
            if (rtc != null && rtc.isRegulating()) {
                double targetV = rtc.getRegulatingTerminal().getVoltageLevel().getNominalV() * 1.4;
                // Always with a Terminal
                rtc.getVoltageRegulation().setTargetValue(targetV);
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

    private static String exportWithPostProcessors(String postProcessors, ReportNode reportNode) throws IOException {
        Network network = loadNetworkFromResourceFile("/postProcessorsTestGrid.uct");
        network.setCaseDate(ZonedDateTime.of(2019, 9, 24, 19, 30, 0, 0, ZoneOffset.UTC));
        Properties parameters = new Properties();
        parameters.put(UcteExporter.POST_PROCESSORS, postProcessors);
        MemDataSource dataSource = new MemDataSource();
        new UcteExporter().export(network, parameters, dataSource, reportNode);
        try (InputStream is = dataSource.newInputStream(null, "uct")) {
            // the generation date is the current date
            return new String(is.readAllBytes(), StandardCharsets.UTF_8)
                    .replaceFirst("Generated by powsybl, .*", "Generated by powsybl, <date>");
        }
    }

    /**
     * The post-processor modifies the UCTE network, reports each modification and orders nodes in reverse order.
     */
    @Test
    void testModificationsPostProcessor() throws IOException {
        ReportNode reportNode = UcteExporterReportTest.newTestRootReportNode();
        assertTxtEquals("""
                ##C 2007.05.01
                Generated by powsybl, <date>
                Case date: 2019-09-24T19:30Z
                ##N
                ##ZBE
                BA____11 Belgium A1   0 2 400.00 10.0000 0.00000 -100.00 0.00000 0.00000 -200.00 100.000 -100.00                               H
                ##ZFR
                FB____11 France B1    0 0        10.0000 0.00000 0.00000 0.00000
                FA____21 France A3    0 0        10.0000 0.00000 0.00000 0.00000
                FA____12 France A2    0 0        10.0000 0.00000 0.00000 0.00000
                FA____11 France A1    0 2 400.00 10.0000 0.00000 -100.00 0.00000 0.00000 -200.00 100.000 -100.00                               G
                ##L
                BA____11 FA____11 1 0 0.5500 1.6800 13.25000   5000 Line BE-FR
                FA____11 FB____11 1 0 0.5500 1.6800 13.25000   5000 Line A1-B1
                FA____12 FB____11 1 8 0.5500 1.6800 13.25000   5000 Line A2-B1
                ##T
                FA____11 FA____21 1 0 400.0 225.0 5000. 0.5500 1.6800 13.25000 0.0000   5000 Transfo A
                ##R
                FA____11 FA____21 1 2.000  7  -3
                """, exportWithPostProcessors("test-modifications", reportNode));
        assertTrue(UcteExporterReportTest.checkReportNode("""
                + Test exporting UCTE network
                   + Creating UCTE Network
                      Buses and Switches
                      Boundary Lines
                      Lines
                      Tie-Lines
                      Transformers
                   + Post-processor test-modifications
                      Node FA____11: power plant type changed from F to G
                      Line FA____12 FB____11 1 set out of service
                   Network exported to file .uct
                """, reportNode));
    }

    /**
     * Nodes are first ordered by busbar by the first post-processor, then in reverse natural order by the second one.
     * Each post-processor gets its own report node. Comment blocks added in front of blocks can be read back.
     */
    @Test
    void testPostProcessorsChaining() throws IOException {
        ReportNode reportNode = UcteExporterReportTest.newTestRootReportNode();
        String exported = exportWithPostProcessors("test-comments,test-modifications", reportNode);
        assertTxtEquals("""
                ##C 2007.05.01
                Generated by powsybl, <date>
                Case date: 2019-09-24T19:30Z
                BA____11 <- BA____11
                FA____11 <- FA____11
                FA____12 <- FA____12
                FA____21 <- FA____21
                FB____11 <- FB____11
                ##C
                Nodes: 5
                ##N
                ##ZBE
                BA____11 Belgium A1   0 2 400.00 10.0000 0.00000 -100.00 0.00000 0.00000 -200.00 100.000 -100.00                               H
                ##ZFR
                FB____11 France B1    0 0        10.0000 0.00000 0.00000 0.00000
                FA____21 France A3    0 0        10.0000 0.00000 0.00000 0.00000
                FA____11 France A1    0 2 400.00 10.0000 0.00000 -100.00 0.00000 0.00000 -200.00 100.000 -100.00                               G
                FA____12 France A2    0 0        10.0000 0.00000 0.00000 0.00000
                ##C
                Lines: 3
                Second comment line
                ##L
                BA____11 FA____11 1 0 0.5500 1.6800 13.25000   5000 Line BE-FR
                FA____11 FB____11 1 0 0.5500 1.6800 13.25000   5000 Line A1-B1
                FA____12 FB____11 1 8 0.5500 1.6800 13.25000   5000 Line A2-B1
                ##T
                FA____11 FA____21 1 0 400.0 225.0 5000. 0.5500 1.6800 13.25000 0.0000   5000 Transfo A
                ##C
                Regulations: 1
                ##R
                FA____11 FA____21 1 2.000  7  -3
                """, exported);
        assertTrue(UcteExporterReportTest.checkReportNode("""
                + Test exporting UCTE network
                   + Creating UCTE Network
                      Buses and Switches
                      Boundary Lines
                      Lines
                      Tie-Lines
                      Transformers
                   + Post-processor test-comments
                      5 UCTE nodes mapped to IIDM buses
                   + Post-processor test-modifications
                      Node FA____11: power plant type changed from F to G
                      Line FA____12 FB____11 1 set out of service
                   Network exported to file .uct
                """, reportNode));

        UcteNetwork ucteNetwork = new UcteReader().read(new BufferedReader(new StringReader(exported)), ReportNode.NO_OP);
        assertEquals(5, ucteNetwork.getNodes().size());
        assertEquals(3, ucteNetwork.getLines().size());
        assertEquals(1, ucteNetwork.getTransformers().size());
        assertEquals(1, ucteNetwork.getRegulations().size());
    }

    /**
     * Same post-processors in the opposite order: the reverse natural order is total, so the busbar order is not used.
     */
    @Test
    void testPostProcessorsActivationOrder() throws IOException {
        ReportNode reportNode = UcteExporterReportTest.newTestRootReportNode();
        assertTxtEquals("""
                ##C 2007.05.01
                Generated by powsybl, <date>
                Case date: 2019-09-24T19:30Z
                BA____11 <- BA____11
                FA____11 <- FA____11
                FA____12 <- FA____12
                FA____21 <- FA____21
                FB____11 <- FB____11
                ##C
                Nodes: 5
                ##N
                ##ZBE
                BA____11 Belgium A1   0 2 400.00 10.0000 0.00000 -100.00 0.00000 0.00000 -200.00 100.000 -100.00                               H
                ##ZFR
                FB____11 France B1    0 0        10.0000 0.00000 0.00000 0.00000
                FA____21 France A3    0 0        10.0000 0.00000 0.00000 0.00000
                FA____12 France A2    0 0        10.0000 0.00000 0.00000 0.00000
                FA____11 France A1    0 2 400.00 10.0000 0.00000 -100.00 0.00000 0.00000 -200.00 100.000 -100.00                               G
                ##C
                Lines: 3
                Second comment line
                ##L
                BA____11 FA____11 1 0 0.5500 1.6800 13.25000   5000 Line BE-FR
                FA____11 FB____11 1 0 0.5500 1.6800 13.25000   5000 Line A1-B1
                FA____12 FB____11 1 8 0.5500 1.6800 13.25000   5000 Line A2-B1
                ##T
                FA____11 FA____21 1 0 400.0 225.0 5000. 0.5500 1.6800 13.25000 0.0000   5000 Transfo A
                ##C
                Regulations: 1
                ##R
                FA____11 FA____21 1 2.000  7  -3
                """, exportWithPostProcessors("test-modifications,test-comments", reportNode));
        assertTrue(UcteExporterReportTest.checkReportNode("""
                + Test exporting UCTE network
                   + Creating UCTE Network
                      Buses and Switches
                      Boundary Lines
                      Lines
                      Tie-Lines
                      Transformers
                   + Post-processor test-modifications
                      Node FA____11: power plant type changed from F to G
                      Line FA____12 FB____11 1 set out of service
                   + Post-processor test-comments
                      5 UCTE nodes mapped to IIDM buses
                   Network exported to file .uct
                """, reportNode));
    }

    @Test
    void testPostProcessorNotFound() {
        Network network = loadNetworkFromResourceFile("/postProcessorsTestGrid.uct");
        Properties parameters = new Properties();
        parameters.put(UcteExporter.POST_PROCESSORS, "unknown");
        MemDataSource dataSource = new MemDataSource();
        UcteExporter exporter = new UcteExporter();
        PowsyblException e = assertThrows(PowsyblException.class, () -> exporter.export(network, parameters, dataSource));
        assertEquals("UCTE export post-processor 'unknown' not found", e.getMessage());
    }
}
