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
}
