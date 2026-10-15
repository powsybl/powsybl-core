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
     * Checks the entries reported when several generators or loads are aggregated on a bus.
     * <p>
     * Network layout (21 kV buses in {@code VL21}, 400 kV bus in {@code VL400}):
     * <pre>
     *   FFFFFF71 --- two loads, generators GA1 (HYDRO) and GA2 (THERMAL)                -&gt; mixed power plant types
     *   FFFFFF72 --- regulating generators GB1 (P=100, 22 kV) and GB2 (P=200, 23 kV)    -&gt; conflicting voltage targets
     *   FFFFFF73 --- generator GC regulating FFFFFF11 at 420 kV                         -&gt; rescaled remote target
     *   FFFFFF74 --- generators GD1 and GD2, both with maxP = 6000                      -&gt; aggregated maxP out of bounds
     *   FFFFFF75 --- generator GE regulating FFFFFF71 (same nominal voltage) at 22 kV  -&gt; no rescaling, not reported
     *   FFFFFF76 --- generator GF regulating voltage, without any voltage target        -&gt; missing voltage target
     *   FFFFFF11 --- load L400 (regulating terminal of GC)
     * </pre>
     * A generator without voltage target such as GF is only accepted at the {@link ValidationLevel#EQUIPMENT}
     * validation level.
     */
    @Test
    void testAggregationReports() {
        Network network = NetworkFactory.findDefault().createNetwork("aggregation-test", "test");
        network.setMinimumAcceptableValidationLevel(ValidationLevel.EQUIPMENT);
        Substation substation = network.newSubstation().setId("S").setCountry(Country.FR).add();
        VoltageLevel vl400 = substation.newVoltageLevel().setId("VL400").setNominalV(400)
                .setTopologyKind(TopologyKind.BUS_BREAKER).add();
        vl400.getBusBreakerView().newBus().setId("FFFFFF11").add();
        vl400.newLoad().setId("L400").setBus("FFFFFF11").setConnectableBus("FFFFFF11").setP0(1).setQ0(1).add();
        VoltageLevel vl21 = substation.newVoltageLevel().setId("VL21").setNominalV(21)
                .setTopologyKind(TopologyKind.BUS_BREAKER).add();
        for (String busId : new String[] {"FFFFFF71", "FFFFFF72", "FFFFFF73", "FFFFFF74", "FFFFFF75", "FFFFFF76"}) {
            vl21.getBusBreakerView().newBus().setId(busId).add();
        }

        vl21.newLoad().setId("LA1").setBus("FFFFFF71").setConnectableBus("FFFFFF71").setP0(1).setQ0(1).add();
        vl21.newLoad().setId("LA2").setBus("FFFFFF71").setConnectableBus("FFFFFF71").setP0(1).setQ0(1).add();
        addGenerator(vl21, "GA1", "FFFFFF71", 10, 100, EnergySource.HYDRO).add();
        addGenerator(vl21, "GA2", "FFFFFF71", 10, 100, EnergySource.THERMAL).add();

        addRegulatingGenerator(vl21, "GB1", "FFFFFF72", 100, 22.0);
        addRegulatingGenerator(vl21, "GB2", "FFFFFF72", 200, 23.0);

        GeneratorAdder remoteAdder = addGenerator(vl21, "GC", "FFFFFF73", 100, 1000, EnergySource.OTHER);
        remoteAdder.newVoltageRegulation().withMode(RegulationMode.VOLTAGE).withRegulating(true)
                .withTerminal(network.getLoad("L400").getTerminal()).withTargetValue(420).add();
        remoteAdder.add();

        addGenerator(vl21, "GD1", "FFFFFF74", 10, 6000, EnergySource.OTHER).add();
        addGenerator(vl21, "GD2", "FFFFFF74", 10, 6000, EnergySource.OTHER).add();

        GeneratorAdder sameNominalVAdder = addGenerator(vl21, "GE", "FFFFFF75", 100, 1000, EnergySource.OTHER);
        sameNominalVAdder.newVoltageRegulation().withMode(RegulationMode.VOLTAGE).withRegulating(true)
                .withTerminal(network.getLoad("LA1").getTerminal()).withTargetValue(22).add();
        sameNominalVAdder.add();

        GeneratorAdder missingTargetAdder = addGenerator(vl21, "GF", "FFFFFF76", 100, 1000, EnergySource.OTHER);
        missingTargetAdder.newVoltageRegulation().withMode(RegulationMode.VOLTAGE).withRegulating(true).add();
        missingTargetAdder.add();

        ReportNode rootReportNode = newTestRootReportNode();
        new UcteExporter().export(network, new Properties(), new MemDataSource(), rootReportNode);

        assertTrue(checkReportNode("""
                + Test exporting UCTE network
                   + Creating UCTE Network
                      + Buses and Switches
                         Bus FFFFFF71: generators have different power plant types, type F (further) is used
                         Bus FFFFFF72: generators have different voltage targets, 23.0 kV from generator GB2 is used
                         Bus FFFFFF73: generator GC, connected to this bus, regulates the voltage of remote bus FFFFFF11; its target 420.0 kV is rescaled to 22.05 kV
                         Bus FFFFFF74: aggregated maxP (12000.0) reaches or exceeds 9999 in absolute value ("no limit" value) and is left undefined
                         Bus FFFFFF76: node regulates voltage but no generator provides a voltage target, the voltage reference is left undefined
                      Boundary Lines
                      Lines
                      Tie-Lines
                      Transformers
                   Network exported to file .uct
                """, rootReportNode));
    }

    private static GeneratorAdder addGenerator(VoltageLevel voltageLevel, String id, String busId, double targetP,
                                               double maxP, EnergySource energySource) {
        return voltageLevel.newGenerator()
                .setId(id)
                .setBus(busId)
                .setConnectableBus(busId)
                .setEnergySource(energySource)
                .setTargetP(targetP)
                .setMinP(0)
                .setMaxP(maxP)
                .setLocalTargetQ(0);
    }

    private static void addRegulatingGenerator(VoltageLevel voltageLevel, String id, String busId, double targetP, double targetV) {
        GeneratorAdder adder = addGenerator(voltageLevel, id, busId, targetP, 1000, EnergySource.OTHER).setLocalTargetV(targetV);
        adder.newVoltageRegulation().withMode(RegulationMode.VOLTAGE).withRegulating(true).add();
        adder.add();
    }

    /**
     * Checks the entries reported for tap changers that don't fit the UCTE-DEF model: tap range extended to stay
     * symmetric around the neutral tap, and tap steps deviating from the linear model. See
     * {@link UcteExporterTestNetworks#createWithTapChangers()} for the cases.
     */
    @Test
    void testTapChangerReports() {
        Network network = UcteExporterTestNetworks.createWithTapChangers();

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
                         FRSHAP11 FRSHAP12 1 [ratio tap changer] - tap position range extended on the high side to stay symmetric (low side spans 3 positions, high side spans 1)
                         FRSHAP11 FRSHAP12 1 [ratio tap changer] - differences found between original tap steps and the exported UCTE linear model
                         FRTIEB11 FRTIEB12 1 [ratio tap changer] - tap position range extended on the low side to stay symmetric (low side spans 0 positions, high side spans 1)
                         FRTIEB11 FRTIEB12 1 [ratio tap changer] - differences found between original tap steps and the exported UCTE linear model
                         FRLOWX11 FRLOWX12 1 [ratio tap changer] - tap position range extended on the low side to stay symmetric (low side spans 1 positions, high side spans 3)
                         FRDEVI11 FRDEVI12 1 [ratio tap changer] - differences found between original tap steps and the exported UCTE linear model
                         FPCLOS11 FPCLOS12 1 [phase tap changer] - tap position range extended on the high side to stay symmetric (low side spans 3 positions, high side spans 1)
                         FPCLOS11 FPCLOS12 1 [angle tap changer (SYMM)] - differences found between original tap steps and the exported UCTE linear model
                         FPTIEB11 FPTIEB12 1 [angle tap changer (ASYM)] - differences found between original tap steps and the exported UCTE linear model
                         FSDEVI11 FSDEVI12 1 [angle tap changer (SYMM)] - differences found between original tap steps and the exported UCTE linear model
                         FADEVI11 FADEVI12 1 [angle tap changer (ASYM)] - differences found between original tap steps and the exported UCTE linear model
                   Network exported to file .uct
                """, rootReportNode));
    }
}
