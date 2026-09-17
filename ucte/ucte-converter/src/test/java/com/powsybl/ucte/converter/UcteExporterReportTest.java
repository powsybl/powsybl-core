/**
 * Copyright (c) 2026, TenneT (https://www.tennet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.ucte.converter;

import com.powsybl.commons.datasource.MemDataSource;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.commons.datasource.ResourceDataSource;
import com.powsybl.commons.datasource.ResourceSet;
import com.powsybl.commons.report.PowsyblCoreReportResourceBundle;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.commons.test.AbstractSerDeTest;
import com.powsybl.commons.test.PowsyblTestReportResourceBundle;
import com.powsybl.commons.test.TestUtil;
import com.powsybl.iidm.network.*;
import org.apache.commons.io.FilenameUtils;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Checks that the functional warnings raised while converting tap changers reach both the SLF4J logger and the
 * {@link ReportNode} tree, not only the former.
 *
 * @author Arthur Michaut {@literal <arthur.michaut at artelys.com>}
 */
class UcteExporterReportTest extends AbstractSerDeTest {

    private static Network loadNetworkFromResourceFile(String filePath) {
        ReadOnlyDataSource dataSource = new ResourceDataSource(FilenameUtils.getBaseName(filePath),
                new ResourceSet(FilenameUtils.getPath(filePath), FilenameUtils.getName(filePath)));
        return new UcteImporter().importData(dataSource, NetworkFactory.findDefault(), null);
    }

    // The default naming strategy doesn't generate UCTE codes for arbitrary IIDM IDs, it only checks that they
    // already conform to the UCTE-DEF format: 8-character node codes, and "<node1> <node2> <orderCode>" (19
    // characters) for the transformer/element id.
    private static TwoWindingsTransformer addTapChangerTestTransformer(Network network, String bus1Id, String bus2Id) {
        Substation substation = network.newSubstation().setId("S_" + bus1Id).setCountry(Country.FR).add();
        VoltageLevel voltageLevel1 = substation.newVoltageLevel()
                                               .setId("VL1_" + bus1Id).setNominalV(380).setTopologyKind(TopologyKind.BUS_BREAKER)
                                               .add();
        voltageLevel1.getBusBreakerView().newBus().setId(bus1Id).add();
        VoltageLevel voltageLevel2 = substation.newVoltageLevel()
                                               .setId("VL2_" + bus2Id).setNominalV(380).setTopologyKind(TopologyKind.BUS_BREAKER)
                                               .add();
        voltageLevel2.getBusBreakerView().newBus().setId(bus2Id).add();
        return substation.newTwoWindingsTransformer()
                         .setId(bus1Id + " " + bus2Id + " 1")
                         .setVoltageLevel1("VL1_" + bus1Id).setConnectableBus1(bus1Id).setBus1(bus1Id)
                         .setVoltageLevel2("VL2_" + bus2Id).setConnectableBus2(bus2Id).setBus2(bus2Id)
                         .setRatedU1(380.0).setRatedU2(380.0)
                         .setR(2.0).setX(100.0).setG(0.0).setB(0.0)
                         .add();
    }

    private static ReportNode newTestRootReportNode() {
        return ReportNode.newRootReportNode()
                         .withResourceBundles(PowsyblTestReportResourceBundle.TEST_BASE_NAME,
                                 PowsyblCoreReportResourceBundle.BASE_NAME)
                         .withMessageTemplate("testExportReportNode")
                         .build();
    }

    private static boolean checkReportNode(String expected, ReportNode reportNode) {
        StringWriter sw = new StringWriter();
        try {
            reportNode.print(sw);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        assertEquals(expected, TestUtil.normalizeLineSeparator(sw.toString()));
        return true;
    }

    private static String captureStderr(Runnable action) {
        PrintStream originalErr = System.err;
        ByteArrayOutputStream captured = new ByteArrayOutputStream();
        System.setErr(new PrintStream(captured, true, StandardCharsets.UTF_8));
        try {
            action.run();
        } finally {
            System.setErr(originalErr);
        }
        return captured.toString(StandardCharsets.UTF_8);
    }

    /**
     * Checks the full shape of the report tree produced when exporting a network (loaded from
     * {@code /expectedExport.uct}): the {@code networkCreation} node with its five conversion-step children,
     * in order, followed by the top-level {@code fileWriting} node.
     */
    @Test
    void testExportReportsNetworkCreationAndFileWriting() {
        Network network = loadNetworkFromResourceFile("/expectedExport.uct");
        ReportNode rootReportNode = newTestRootReportNode();

        new UcteExporter().export(network, new Properties(), new MemDataSource(), rootReportNode);

        assertTrue(checkReportNode("""
                + Test exporting UCTE network
                   + Creating UCTE Network
                      Buses and Switches
                      Boundary Lines
                      Lines
                      Tie-Lines
                      Transformers
                   Network exported to file .uct
                """, rootReportNode));
    }

    /**
     * Checks that a closed switch with no current limit is reported when exporting the network.
     * <p>
     * Network layout:
     * <pre>
     *   VL
     *   FFFFFF11 --- FFFFFF11 FFFFFF12 1 --- FFFFFF12
     * </pre>
     * The switch {@code FFFFFF11 FFFFFF12 1} is closed and has no current limit set.
     */
    @Test
    void testSwitchCurrentLimitMissingReported() {
        Network network = NetworkFactory.findDefault().createNetwork("switch-test", "test");
        Substation substation = network.newSubstation().setId("S").setCountry(Country.FR).add();
        VoltageLevel voltageLevel = substation.newVoltageLevel()
                                              .setId("VL")
                                              .setNominalV(380)
                                              .setTopologyKind(TopologyKind.BUS_BREAKER)
                                              .add();
        voltageLevel.getBusBreakerView().newBus().setId("FFFFFF11").add();
        voltageLevel.getBusBreakerView().newBus().setId("FFFFFF12").add();
        voltageLevel.getBusBreakerView().newSwitch()
                    .setId("FFFFFF11 FFFFFF12 1")
                    .setBus1("FFFFFF11")
                    .setBus2("FFFFFF12")
                    .setOpen(false)
                    .add();

        ReportNode rootReportNode = newTestRootReportNode();
        new UcteExporter().export(network, new Properties(), new MemDataSource(), rootReportNode);

        assertTrue(checkReportNode("""
                + Test exporting UCTE network
                   + Creating UCTE Network
                      + Buses and Switches
                         Switch FFFFFF11 FFFFFF12 1: No current limit provided
                      Boundary Lines
                      Lines
                      Tie-Lines
                      Transformers
                   Network exported to file .uct
                """, rootReportNode));
    }

    /**
     * Checks that every tap-changer functional warning (range extension, and the three flavours of model
     * deviation) is both logged and reported, on a network gathering one transformer per case, plus one normal
     * transformer that should trigger neither.
     */
    @Test
    void tapChangerWarningsAreLoggedAndReported() {
        Network network = NetworkFactory.findDefault().createNetwork("test", "test");

        TwoWindingsTransformer rangeExtensionTwt = addTapChangerTestTransformer(network, "FTEST111", "FTEST211");
        rangeExtensionTwt.newRatioTapChanger()
                         .setLoadTapChangingCapabilities(false)
                         .setLowTapPosition(-1)
                         .setTapPosition(0)
                         .setRegulating(false)
                         .beginStep().setRho(1.0204081632653061).setR(0.0).setX(40.0).setG(0.0).setB(0.0).endStep() // -1
                         .beginStep().setRho(1.0).setR(0.0).setX(40.0).setG(0.0).setB(0.0).endStep() // 0 (neutral)
                         .beginStep().setRho(0.9803921568627451).setR(0.0).setX(40.0).setG(0.0).setB(0.0).endStep() // 1
                         .beginStep().setRho(0.9615384615384615).setR(0.0).setX(40.0).setG(0.0).setB(0.0).endStep() // 2
                         .beginStep().setRho(0.9433962264150942).setR(0.0).setX(40.0).setG(0.0).setB(0.0).endStep() // 3
                         .add();

        TwoWindingsTransformer ratioDeviationTwt = addTapChangerTestTransformer(network, "FTEST311", "FTEST411");
        ratioDeviationTwt.newRatioTapChanger()
                         .setLoadTapChangingCapabilities(false)
                         .setLowTapPosition(-2)
                         .setTapPosition(0)
                         .setRegulating(false)
                         .beginStep().setRho(1.0416666666666667).setR(0.0).setX(40.0).setG(0.0).setB(0.0).endStep() // -2
                         .beginStep().setRho(1.0204081632653061).setR(0.0).setX(40.0).setG(0.0).setB(0.0).endStep() // -1
                         .beginStep().setRho(1.0).setR(0.0).setX(40.0).setG(0.0).setB(0.0).endStep() // 0
                         .beginStep().setRho(1.5).setR(0.0).setX(40.0).setG(0.0).setB(0.0).endStep() // 1 -- deviates
                         .beginStep().setRho(0.9615384615384616).setR(0.0).setX(40.0).setG(0.0).setB(0.0).endStep() // 2
                         .add();

        TwoWindingsTransformer symmDeviationTwt = addTapChangerTestTransformer(network, "FTEST711", "FTEST811");
        symmDeviationTwt.newPhaseTapChanger()
                        .setLowTapPosition(-1)
                        .setTapPosition(0)
                        .setRegulating(false)
                        .setRegulationMode(PhaseTapChanger.RegulationMode.CURRENT_LIMITER)
                        .setRegulationValue(200)
                        .setRegulationTerminal(symmDeviationTwt.getTerminal2())
                        .beginStep().setAlpha(90.0).setRho(1.0).setR(0.0).setX(0.0).setG(0.0).setB(0.0).endStep() // -1
                        .beginStep().setAlpha(0.0).setRho(1.0).setR(0.0).setX(0.0).setG(0.0).setB(0.0).endStep() // 0
                        .beginStep().setAlpha(-50.0).setRho(1.0).setR(0.0).setX(0.0).setG(0.0).setB(0.0).endStep() // 1 -- deviates from -90
                        .add();

        TwoWindingsTransformer asymmDeviationTwt = addTapChangerTestTransformer(network, "FTEST911", "FTESTA11");
        asymmDeviationTwt.newPhaseTapChanger()
                         .setLowTapPosition(-2)
                         .setTapPosition(0)
                         .setRegulating(false)
                         .setRegulationMode(PhaseTapChanger.RegulationMode.CURRENT_LIMITER)
                         .setRegulationValue(200)
                         .setRegulationTerminal(asymmDeviationTwt.getTerminal2())
                         .beginStep().setAlpha(0.0).setRho(2.0).setR(0.0).setX(0.0).setG(0.0).setB(0.0).endStep() // -2 (low anchor)
                         .beginStep().setAlpha(0.0).setRho(1.3333333333333333).setR(0.0).setX(0.0).setG(0.0).setB(0.0).endStep() // -1
                         .beginStep().setAlpha(0.0).setRho(1.0).setR(0.0).setX(0.0).setG(0.0).setB(0.0).endStep() // 0 (neutral: rho = 1, alpha = 0)
                         .beginStep().setAlpha(0.0).setRho(0.7).setR(0.0).setX(0.0).setG(0.0).setB(0.0).endStep() // 1 -- deviates from 0.8
                         .beginStep().setAlpha(0.0).setRho(0.6666666666666666).setR(0.0).setX(0.0).setG(0.0).setB(0.0).endStep() // 2 (high anchor)
                         .add();

        TwoWindingsTransformer normalTwt = addTapChangerTestTransformer(network, "FTEST511", "FTEST611");
        normalTwt.newRatioTapChanger()
                 .setLoadTapChangingCapabilities(false)
                 .setLowTapPosition(-2)
                 .setTapPosition(0)
                 .setRegulating(false)
                 .beginStep().setRho(1.0416666666666667).setR(0.0).setX(40.0).setG(0.0).setB(0.0).endStep() // -2
                 .beginStep().setRho(1.0204081632653061).setR(0.0).setX(40.0).setG(0.0).setB(0.0).endStep() // -1
                 .beginStep().setRho(1.0).setR(0.0).setX(40.0).setG(0.0).setB(0.0).endStep() // 0
                 .beginStep().setRho(0.9803921568627451).setR(0.0).setX(40.0).setG(0.0).setB(0.0).endStep() // 1
                 .beginStep().setRho(0.9615384615384616).setR(0.0).setX(40.0).setG(0.0).setB(0.0).endStep() // 2
                 .add();

        ReportNode reportNode = newTestRootReportNode();
        MemDataSource dataSource = new MemDataSource();
        String warnings = captureStderr(() -> new UcteExporter().export(network, new Properties(), dataSource, reportNode));

        assertTrue(warnings.contains(rangeExtensionTwt.getId()) && warnings.contains("extended on the low side"));
        assertEquals(1, warnings.lines().filter(line -> line.contains(ratioDeviationTwt.getId())).count());
        assertEquals(1, warnings.lines().filter(line -> line.contains(symmDeviationTwt.getId())).count());
        assertEquals(1, warnings.lines().filter(line -> line.contains(asymmDeviationTwt.getId())).count());
        assertFalse(warnings.contains(normalTwt.getId()));

        assertTrue(checkReportNode("""
                + Test exporting UCTE network
                   + Creating UCTE Network
                      Buses and Switches
                      Boundary Lines
                      Lines
                      Tie-Lines
                      + Transformers
                         FTEST111 FTEST211 1 [ratio tap changer] - tap position range extended on the low side to stay symmetric (low side spans 1 positions, high side spans 3)
                         FTEST311 FTEST411 1 [ratio tap changer] - differences found between original tap steps and the exported UCTE linear model
                         FTEST711 FTEST811 1 [angle tap changer (SYMM)] - differences found between original tap steps and the exported UCTE linear model
                         FTEST911 FTESTA11 1 [angle tap changer (ASSYM)] - differences found between original tap steps and the exported UCTE linear model
                   Network exported to file .uct
                """, reportNode));
    }
}
