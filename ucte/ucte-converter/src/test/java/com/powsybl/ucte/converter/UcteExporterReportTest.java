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
import com.powsybl.iidm.network.regulation.RegulationMode;
import org.apache.commons.io.FilenameUtils;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Arthur Michaut {@literal <arthur.michaut at artelys.com>}
 */
class UcteExporterReportTest extends AbstractSerDeTest {

    private static Network loadNetworkFromResourceFile(String filePath) {
        ReadOnlyDataSource dataSource = new ResourceDataSource(FilenameUtils.getBaseName(filePath),
                new ResourceSet(FilenameUtils.getPath(filePath), FilenameUtils.getName(filePath)));
        return new UcteImporter().importData(dataSource, NetworkFactory.findDefault(), null);
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
     * Checks that a transformer with no nominal power (no {@code ratedS} and no legacy property) is reported
     * when exporting the network.
     */
    @Test
    void testNominalPowerMissingReported() {
        Network network = loadNetworkFromResourceFile("/expectedExport.uct");
        network.getTwoWindingsTransformer("F_SU1_11 F_SU1_21 1").setRatedS(Double.NaN);

        ReportNode rootReportNode = newTestRootReportNode();
        new UcteExporter().export(network, new Properties(), new MemDataSource(), rootReportNode);

        assertTrue(checkReportNode("""
                + Test exporting UCTE network
                   + Creating UCTE Network
                      Buses and Switches
                      Boundary Lines
                      Lines
                      Tie-Lines
                      + Transformers
                         Transformer F_SU1_11 F_SU1_21 1: No nominal power provided. Defaulting to 99999
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
     * Checks the entries reported when several generators or loads are aggregated on a bus. See
     * {@link UcteExporterTestNetworks#createWithMultipleInjections()} for the cases: only the out of bounds aggregated
     * maxP ({@code CASE6}) and the mixed power plant types ({@code CASE7}) are reported.
     */
    @Test
    void testMultipleInjectionsReports() {
        Network network = UcteExporterTestNetworks.createWithMultipleInjections();

        ReportNode rootReportNode = newTestRootReportNode();
        new UcteExporter().export(network, new Properties(), new MemDataSource(), rootReportNode);

        assertTrue(checkReportNode("""
                + Test exporting UCTE network
                   + Creating UCTE Network
                      + Buses and Switches
                         Bus FCASE671: aggregated maxP (12000.0) reaches or exceeds 9999 in absolute value ("no limit" value) and is left undefined
                         Bus FCASE771: generators have different power plant types, type F (further) is used
                      Boundary Lines
                      Lines
                      Tie-Lines
                      Transformers
                   Network exported to file .uct
                """, rootReportNode));
    }

    /**
     * Checks the entries reported when selecting the voltage reference of a bus. See
     * {@link UcteExporterTestNetworks#createWithMultipleVoltageTargets()} for the cases: conflicting targets
     * ({@code CASE1}, {@code CASE2}, {@code CASE6}) and rescaled remote targets ({@code CASE4}, {@code CASE8}) are
     * reported, a remote target on a bus with the same nominal voltage ({@code CASEA}) is not.
     * <p>
     * A regulating generator without any voltage target, only accepted at the {@link ValidationLevel#EQUIPMENT}
     * validation level, is added to {@code CASE9}: the missing voltage target is reported.
     */
    @Test
    void testVoltageTargetReports() {
        Network network = UcteExporterTestNetworks.createWithMultipleVoltageTargets();
        // EQUIPMENT to have FCASE971 pass validation (regulating node without generator carrying a voltage target)
        network.setMinimumAcceptableValidationLevel(ValidationLevel.EQUIPMENT);
        GeneratorAdder missingTargetAdder = network.getVoltageLevel("VL21").newGenerator()
                .setId("CASE9_GB")
                .setBus("FCASE971")
                .setConnectableBus("FCASE971")
                .setTargetP(10)
                .setMinP(0)
                .setMaxP(100)
                .setLocalTargetQ(0);
        missingTargetAdder.newVoltageRegulation().withMode(RegulationMode.VOLTAGE).withRegulating(true).add();
        missingTargetAdder.add();

        ReportNode rootReportNode = newTestRootReportNode();
        new UcteExporter().export(network, new Properties(), new MemDataSource(), rootReportNode);

        assertTrue(checkReportNode("""
                + Test exporting UCTE network
                   + Creating UCTE Network
                      + Buses and Switches
                         Bus FCASE171: generators have different voltage targets, 23.0 kV from generator CASE1_GB is used
                         Bus FCASE271: generators have different voltage targets, 22.0 kV from generator CASE2_GA is used
                         Bus FCASE471: generator CASE4_GA, connected to this bus, regulates the voltage of remote bus FREMOT11; its target 405.0 kV is rescaled to 22.38157894736842 kV
                         Bus FCASE671: generators have different voltage targets, 23.0 kV from generator CASE6_GB is used
                         Bus FCASE871: generator CASE8_GA, connected to this bus, regulates the voltage of remote bus FREMOT11; its target 405.0 kV is rescaled to 22.38157894736842 kV
                         Bus FCASE971: node regulates voltage but no generator provides a voltage target, the voltage reference is left undefined
                      Boundary Lines
                      Lines
                      Tie-Lines
                      Transformers
                   Network exported to file .uct
                """, rootReportNode));
    }
}
