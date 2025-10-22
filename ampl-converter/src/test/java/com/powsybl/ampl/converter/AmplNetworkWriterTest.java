/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.ampl.converter;

import com.powsybl.ampl.converter.util.AmplDatTableFormatter;
import com.powsybl.ampl.converter.version.AmplExportVersion;
import com.powsybl.ampl.converter.version.BasicAmplExporter;
import com.powsybl.commons.datasource.DataSource;
import com.powsybl.commons.datasource.MemDataSource;
import com.powsybl.commons.io.table.TableFormatter;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.test.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Properties;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class AmplNetworkWriterTest extends AbstractAmplExporterTest {

    @Override
    @BeforeEach
    public void setUp() throws IOException {
        super.setUp();
        properties.put("iidm.export.ampl.export-version", "1.0");
    }

    public String writeLinesToFormatter(String bus1, String bus2, String vl1, String vl2) throws IOException {
        Network network = NoEquipmentNetworkFactory.create();
        Line line = network.newLine()
                .setId("L1")
                .setName("LINE1")
                .setR(1.0)
                .setX(2.0)
                .setG1(3.0)
                .setG2(3.5)
                .setB1(4.0)
                .setB2(4.5)
                .setVoltageLevel1(vl1)
                .setVoltageLevel2(vl2)
                .setBus1(bus1)
                .setBus2(bus2)
                .setConnectableBus1(bus1)
                .setConnectableBus2(bus2)
                .add();

        AmplExportConfig amplExportConfig = new AmplExportConfig(AmplExportConfig.ExportScope.ALL, true,
                AmplExportConfig.ExportActionType.CURATIVE, false, false, AmplExportVersion.V1_1);
        BasicAmplExporter exporter = new BasicAmplExporter(amplExportConfig, network, AmplUtil.createMapper(network), 1, 0, 0);
        Writer writer = new StringWriter();
        TableFormatter formatter = new AmplDatTableFormatter(writer, AmplNetworkWriter.getTableTitle(network, "Branches"),
                AmplConstants.INVALID_FLOAT_VALUE, true, AmplConstants.LOCALE, exporter.getBranchesColumns());
        exporter.writeLinesToFormatter(formatter, line);
        return writer.toString();
    }

    public String writeTwoWindingsTransformerToFormatter(String bus1, String bus2, String vl1, String vl2) throws IOException {
        Network network = NoEquipmentNetworkFactory.create();
        TwoWindingsTransformer transformer = network.getSubstation("sub").newTwoWindingsTransformer()
                .setId("TR")
                .setVoltageLevel1(vl1)
                .setVoltageLevel2(vl2)
                .setR(0)
                .setX(1)
                .setB(1)
                .setG(0)
                .setRatedU1(1)
                .setRatedU2(1)
                .setBus1(bus1)
                .setBus2(bus2)
                .add();

        AmplExportConfig amplExportConfig = new AmplExportConfig(AmplExportConfig.ExportScope.ALL, true, AmplExportConfig.ExportActionType.CURATIVE,
                false, false, AmplExportVersion.V1_1);
        BasicAmplExporter exporter = new BasicAmplExporter(amplExportConfig, network, AmplUtil.createMapper(network), 1, 0, 0);
        Writer writer = new StringWriter();
        TableFormatter formatter = new AmplDatTableFormatter(writer, AmplNetworkWriter.getTableTitle(network, "Branches"), AmplConstants.INVALID_FLOAT_VALUE,
                true, AmplConstants.LOCALE, exporter.getBranchesColumns());
        exporter.writeTwoWindingsTranformerToFormatter(formatter, transformer);
        return writer.toString();
    }

    public String writeThreeWindingsTransformerToFormatter(int middleBus) throws IOException {
        Network network = NoEquipmentNetworkFactory.create();
        Substation substation = network.getSubstation("sub");
        ThreeWindingsTransformer twt3w = substation.newThreeWindingsTransformer()
                .setId("twt")
                .setName("transfo3")
                .newLeg1()
                .setR(1.3)
                .setX(1.4)
                .setG(1.6)
                .setB(1.7)
                .setRatedU(1.1)
                .setRatedS(1.2)
                .setVoltageLevel("vl1")
                .setConnectableBus("busA")
                .setBus("busA")
                .add()
                .newLeg2()
                .setR(2.03)
                .setX(2.04)
                .setG(0.0)
                .setB(0.0)
                .setRatedU(2.05)
                .setRatedS(2.06)
                .setVoltageLevel("vl2")
                .setConnectableBus("busB")
                .add()
                .newLeg3()
                .setR(3.3)
                .setX(3.4)
                .setG(0.0)
                .setB(0.0)
                .setRatedU(3.5)
                .setRatedS(3.6)
                .setVoltageLevel("vl2")
                .setConnectableBus("busB")
                .add()
                .add();

        AmplExportConfig amplExportConfig = new AmplExportConfig(AmplExportConfig.ExportScope.ALL, true, AmplExportConfig.ExportActionType.CURATIVE,
                false, false, AmplExportVersion.V1_1);
        BasicAmplExporter exporter = new BasicAmplExporter(amplExportConfig, network, AmplUtil.createMapper(network), 1, 0, 0);
        Writer writer = new StringWriter();
        TableFormatter formatter = new AmplDatTableFormatter(writer, AmplNetworkWriter.getTableTitle(network, "Branches"), AmplConstants.INVALID_FLOAT_VALUE,
                true, AmplConstants.LOCALE, exporter.getBranchesColumns());
        exporter.writeThreeWindingsTransformerLegToFormatter(formatter, twt3w, middleBus, 1, ThreeSides.ONE);
        return writer.toString();
    }

    public String writeTieLineToFormatter(String bus1, String bus2, String pairingKey, String vl1, String vl2) throws IOException {
        Network network = NoEquipmentNetworkFactory.create();
        VoltageLevel voltageLevelA = network.getVoltageLevel(vl1);
        VoltageLevel voltageLevelB = network.getVoltageLevel(vl2);

        double r = 10.0;
        double r2 = 1.0;
        double x = 20.0;
        double x2 = 2.0;
        double hl1g1 = 0.03;
        double hl1g2 = 0.035;
        double hl1b1 = 0.04;
        double hl1b2 = 0.045;
        double hl2g1 = 0.013;
        double hl2g2 = 0.0135;
        double hl2b1 = 0.014;
        double hl2b2 = 0.0145;

        DanglingLine dl1 = voltageLevelA.newDanglingLine()
                .setBus(bus1)
                .setId("dl1")
                .setEnsureIdUnicity(true)
                .setName("dl1_name")
                .setP0(0.0)
                .setQ0(0.0)
                .setR(r)
                .setX(x)
                .setB(hl1b1 + hl1b2)
                .setG(hl1g1 + hl1g2)
                .setPairingKey(pairingKey)
                .add();
        DanglingLine dl2 = voltageLevelB.newDanglingLine()
                .setBus(bus2)
                .setId("dl2")
                .setEnsureIdUnicity(true)
                .setP0(0.0)
                .setQ0(0.0)
                .setR(r2)
                .setX(x2)
                .setB(hl2b1 + hl2b2)
                .setG(hl2g1 + hl2g2)
                .add();

        TieLineAdder adder = network.newTieLine().setId("testTie")
                .setName("testNameTie")
                .setDanglingLine1(dl1.getId())
                .setDanglingLine2(dl2.getId());
        TieLine incorrectTieLine = adder.add();

        AmplExportConfig amplExportConfig = new AmplExportConfig(AmplExportConfig.ExportScope.ALL, true, AmplExportConfig.ExportActionType.CURATIVE,
                false, false, AmplExportVersion.V1_1);
        BasicAmplExporter exporter = new BasicAmplExporter(amplExportConfig, network, AmplUtil.createMapper(network), 1, 0, 0);
        Writer writer = new StringWriter();
        TableFormatter formatter = new AmplDatTableFormatter(writer, AmplNetworkWriter.getTableTitle(network, "Branches"), AmplConstants.INVALID_FLOAT_VALUE,
                true, AmplConstants.LOCALE, exporter.getBranchesColumns());
        exporter.writeTieLineToFormatter(formatter, incorrectTieLine);
        return writer.toString();
    }

    @Test
    void test() {
        AmplExporter exporter = new AmplExporter();
        assertEquals("AMPL", exporter.getFormat());
        assertEquals("IIDM to AMPL converter", exporter.getComment());
        assertEquals(7, exporter.getParameters().size());
    }

    @Test
    void writeEurostag() throws IOException {
        Network network = EurostagTutorialExample1Factory.createWithMoreGenerators();

        MemDataSource dataSource = new MemDataSource();
        export(network, properties, dataSource);

        assertEqualsToRef(dataSource, "_network_substations", "inputs/eurostag-tutorial-example1-substations.txt");
        assertEqualsToRef(dataSource, "_network_buses", "inputs/eurostag-tutorial-example1-buses.txt");
        assertEqualsToRef(dataSource, "_network_tct", "inputs/eurostag-tutorial-example1-tct.txt");
        assertEqualsToRef(dataSource, "_network_rtc", "inputs/eurostag-tutorial-example1-rtc.txt");
        assertEqualsToRef(dataSource, "_network_ptc", "inputs/eurostag-tutorial-example1-ptc.txt");
        assertEqualsToRef(dataSource, "_network_loads", "inputs/eurostag-tutorial-example1-loads.txt");
        assertEqualsToRef(dataSource, "_network_generators", "inputs/eurostag-tutorial-example1-generators.txt");
        assertEqualsToRef(dataSource, "_network_limits", "inputs/eurostag-tutorial-example1-limits.txt");
    }

    @Test
    void writeNetworkWithExtension() throws IOException {
        Network network = Network.create("sim1", "test");
        network.addExtension(FooNetworkExtension.class, new FooNetworkExtension());

        MemDataSource dataSource = new MemDataSource();
        export(network, properties, dataSource);

        assertEqualsToRef(dataSource, "foo-network-extension", "inputs/foo-network-extension.txt");
    }

    @Test
    void writeShunt() throws IOException {
        Network network = EurostagTutorialExample1Factory.createWithMultipleConnectedComponents();

        MemDataSource dataSource = new MemDataSource();
        export(network, properties, dataSource);

        assertEqualsToRef(dataSource, "_network_shunts", "inputs/eurostag-tutorial-example1-shunts.txt");
    }

    @Test
    void writeShunt2() throws IOException {
        Network network = ShuntTestCaseFactory.createNonLinear();
        ShuntCompensator sc = network.getShuntCompensator("SHUNT");
        sc.setSectionCount(2);

        MemDataSource dataSource = new MemDataSource();
        export(network, properties, dataSource);

        assertEqualsToRef(dataSource, "_network_shunts", "inputs/shunt-test-case-shunts.txt");
    }

    @Test
    void writeLcc() throws IOException {
        Network network = HvdcTestNetwork.createLcc();

        MemDataSource dataSource = new MemDataSource();
        export(network, properties, dataSource);

        assertEqualsToRef(dataSource, "_network_hvdc", "inputs/hvdc-lcc-test-case.txt");
        assertEqualsToRef(dataSource, "_network_lcc_converter_stations", "inputs/lcc-test-case.txt");

    }

    @Test
    void writePhaseTapChanger() throws IOException {
        Network network = PhaseShifterTestCaseFactory.create();

        MemDataSource dataSource = new MemDataSource();
        export(network, properties, dataSource);

        assertEqualsToRef(dataSource, "_network_ptc", "inputs/ptc-test-case.txt");
    }

    @Test
    void writeSVC() throws IOException {
        Network network = SvcTestCaseFactory.createWithMoreSVCs();
        MemDataSource dataSource = new MemDataSource();
        export(network, properties, dataSource);

        assertEqualsToRef(dataSource, "_network_static_var_compensators", "inputs/svc-test-case.txt");
    }

    @Test
    void writeBattery() throws IOException {
        Network network = BatteryNetworkFactory.create();

        MemDataSource dataSource = new MemDataSource();
        export(network, properties, dataSource);

        assertEqualsToRef(dataSource, "_network_batteries", "inputs/battery-test-batteries.txt");
    }

    @Test
    void writeThreeWindingsTransformer() throws IOException {
        Network network = ThreeWindingsTransformerNetworkFactory.createWithCurrentLimits();
        network.getThreeWindingsTransformer("3WT").getLeg1()
            .newPhaseTapChanger()
            .beginStep()
            .setRho(1)
            .setR(0.1)
            .setX(1.)
            .setB(0.)
            .setG(0.)
            .setAlpha(0)
            .endStep()
            .setTapPosition(0)
            .setLowTapPosition(0)
            .add();

        MemDataSource dataSource = new MemDataSource();
        export(network, properties, dataSource);

        assertEqualsToRef(dataSource, "_network_branches", "inputs/three-windings-transformers-branches.txt");
        assertEqualsToRef(dataSource, "_network_buses", "inputs/three-windings-transformers-buses.txt");
        assertEqualsToRef(dataSource, "_network_rtc", "inputs/three-windings-transformers-rtc.txt");
        assertEqualsToRef(dataSource, "_network_substations", "inputs/three-windings-transformers-substations.txt");
        assertEqualsToRef(dataSource, "_network_tct", "inputs/three-windings-transformers-tct.txt");
        assertEqualsToRef(dataSource, "_network_limits", "inputs/three-windings-transformers-limits.txt");
    }

    @Test
    void writeVsc() throws IOException {
        Network network = HvdcTestNetwork.createVsc();

        MemDataSource dataSource = new MemDataSource();
        export(network, properties, dataSource);

        assertEqualsToRef(dataSource, "_network_hvdc", "inputs/hvdc-vsc-test-case.txt");
        assertEqualsToRef(dataSource, "_network_vsc_converter_stations", "inputs/vsc-test-case.txt");

    }

    @Test
    void writeCurrentLimits() throws IOException {
        Network network = EurostagTutorialExample1Factory.createWithFixedCurrentLimits();

        MemDataSource dataSource = new MemDataSource();
        export(network, properties, dataSource);

        assertEqualsToRef(dataSource, "_network_limits", "inputs/current-limits-test-case.txt");
    }

    @Test
    void writeTieLine() throws IOException {
        Network network = EurostagTutorialExample1Factory.createWithTieLine();
        for (DanglingLine danglingLine : network.getDanglingLines()) {
            danglingLine.getOrCreateSelectedOperationalLimitsGroup().newCurrentLimits()
                .setPermanentLimit(100.0)
                .beginTemporaryLimit().setName("20'").setValue(120.0).setAcceptableDuration(20 * 60).endTemporaryLimit()
                .beginTemporaryLimit().setName("10'").setValue(140.0).setAcceptableDuration(10 * 60).endTemporaryLimit()
                .add();
        }

        properties.put("iidm.export.ampl.with-xnodes", "true");

        MemDataSource dataSource = new MemDataSource();
        export(network, properties, dataSource);

        assertEqualsToRef(dataSource, "_network_substations", "inputs/eurostag-tutorial-example1-substations-tl.txt");
        assertEqualsToRef(dataSource, "_network_buses", "inputs/eurostag-tutorial-example1-buses-tl.txt");
        assertEqualsToRef(dataSource, "_network_branches", "inputs/eurostag-tutorial-example1-branches-tl.txt");
        assertEqualsToRef(dataSource, "_network_limits", "inputs/eurostag-tutorial-example1-limits-tl.txt");
    }

    @Test
    void writeDanglingLines() throws IOException {
        Network network = DanglingLineNetworkFactory.create();

        MemDataSource dataSource = new MemDataSource();
        export(network, properties, dataSource);

        assertEqualsToRef(dataSource, "_network_branches", "inputs/dangling-line-branches.txt");
        assertEqualsToRef(dataSource, "_network_buses", "inputs/dangling-line-buses.txt");
        assertEqualsToRef(dataSource, "_network_limits", "inputs/dangling-line-limits.txt");
        assertEqualsToRef(dataSource, "_network_loads", "inputs/dangling-line-loads.txt");
        assertEqualsToRef(dataSource, "_network_substations", "inputs/dangling-line-substations.txt");
    }

    @Test
    void writeExtensions() throws IOException {
        Network network = HvdcTestNetwork.createLcc();
        HvdcLine l = network.getHvdcLine("L");
        l.addExtension(FooExtension.class, new FooExtension());
        MemDataSource dataSource = new MemDataSource();
        export(network, properties, dataSource);

        assertEqualsToRef(dataSource, "foo-extension", "inputs/foo-extension.txt");
    }

    private void export(Network network, Properties properties, DataSource dataSource) {
        AmplExporter exporter = new AmplExporter();
        exporter.export(network, properties, dataSource);
    }

    @Test
    void writeHeaders() throws IOException {
        Network network = Network.create("dummy_network", "test");
        MemDataSource dataSource = new MemDataSource();
        export(network, properties, dataSource);
        assertEqualsToRef(dataSource, "_headers", "inputs/headers.txt");
    }

    @Test
    void writeHeadersWithVersion10() throws IOException {
        Network network = Network.create("dummy_network", "test");
        MemDataSource dataSource = new MemDataSource();

        properties.put("iidm.export.ampl.export-version", "1.0");

        export(network, properties, dataSource);
        assertEqualsToRef(dataSource, "_headers", "inputs/headers.txt");
    }

    @Test
    void writeHeadersWithUnknownVersion() {
        Network network = Network.create("dummy_network", "test");
        MemDataSource dataSource = new MemDataSource();

        properties.put("iidm.export.ampl.export-version", "V1_0");

        Exception e = assertThrows(IllegalArgumentException.class, () -> export(network, properties, dataSource));

        assertTrue(e.getMessage().contains("Value V1_0 of parameter iidm.export.ampl.export-version is not contained in possible values [1.0, 1.1, 1.2]"));
    }

    @Test
    void writeHeadersWithVersion11() throws IOException {
        Network network = Network.create("dummy_network", "test");
        MemDataSource dataSource = new MemDataSource();

        properties.put("iidm.export.ampl.export-version", "1.1");

        export(network, properties, dataSource);
        assertEqualsToRef(dataSource, "_headers", "inputs/extended_exporter/headers.txt");
    }

    @Test
    void writeLineWithDifferentNominalVoltageAtEnds() throws IOException {
        Network network = SvcTestCaseFactory.create();
        network.getVoltageLevel("VL2").setNominalV(400);

        MemDataSource dataSource = new MemDataSource();
        export(network, properties, dataSource);

        assertEqualsToRef(dataSource, "_network_branches", "inputs/line-with-different-nominal-voltage-at-ends-test-case.txt");
    }

    @Test
    void writeZeroImpedanceLineWithDifferentNominalVoltageAtEnds() throws IOException {
        Network network = SvcTestCaseFactory.create();
        network.getVoltageLevel("VL2").setNominalV(400);
        network.getLine("L1").setR(0)
                                .setX(0);

        MemDataSource dataSource = new MemDataSource();
        export(network, properties, dataSource);

        assertEqualsToRef(dataSource, "_network_branches", "inputs/zero-impedance-line-with-different-nominal-voltage-at-ends-test-case.txt");
    }

    @Test
    void writeLinesToFormatterTest() throws IOException {
        String result1 = writeLinesToFormatter("busA", "busA", "vl1", "vl1");
        assertEquals("", result1);

        String result2 = writeLinesToFormatter("busA", "busB", "vl1", "vl2");
        assertEquals("#Branches (test/InitialState)" + System.lineSeparator() +
                "#\"variant\" \"num\" \"bus1\" \"bus2\" \"3wt num\" \"sub.1\" \"sub.2\" \"r (pu)\" \"x (pu)\" \"g1 (pu)\" \"g2 (pu)\" \"b1 (pu)\" \"b2 (pu)\" \"cst ratio (pu)\""
                + " \"ratio tc\" \"phase tc\" \"p1 (MW)\" \"p2 (MW)\" \"q1 (MVar)\" \"q2 (MVar)\" \"patl1 (A)\" \"patl2 (A)\" \"merged\" \"fault\" \"curative\" \"id\" \"description\""
                + System.lineSeparator() + "1 1 1 2 -1 1 2 0.00113636 0.00227273 6019.20 1304.00 7321.60 1992.00 1.00000 -1 -1 -99999.0 -99999.0 -99999.0 -99999.0 -99999.0 -99999.0 false"
                + " 0 0 \"L1\" \"LINE1\"" + System.lineSeparator(), result2);
    }

    @Test
    void writeTwoWindingsTransformerToFormatterTest() throws IOException {
        String result1 = writeTwoWindingsTransformerToFormatter("busA", "busA", "vl1", "vl1");
        assertEquals("", result1);

        String result2 = writeTwoWindingsTransformerToFormatter("busA", "busB", "vl1", "vl2");
        assertEquals("#Branches (test/InitialState)" + System.lineSeparator() +
                "#\"variant\" \"num\" \"bus1\" \"bus2\" \"3wt num\" \"sub.1\" \"sub.2\" \"r (pu)\" \"x (pu)\" \"g1 (pu)\" \"g2 (pu)\" \"b1 (pu)\" \"b2 (pu)\" \"cst ratio (pu)\""
                + " \"ratio tc\" \"phase tc\" \"p1 (MW)\" \"p2 (MW)\" \"q1 (MVar)\" \"q2 (MVar)\" \"patl1 (A)\" \"patl2 (A)\" \"merged\" \"fault\" \"curative\" \"id\" \"description\""
                + System.lineSeparator() + "1 1 1 2 -1 1 2 0.00000 0.00250000 0.00000 0.00000 400.000 0.00000 2.20000 -1 -1 -99999.0 -99999.0 -99999.0 -99999.0 -99999.0 -99999.0 false"
                + " 0 0 \"TR\" \"TR\"" + System.lineSeparator(), result2);
    }

    @Test
    void writeThreeWindingsTransformerToFormatterTest() throws IOException {
        String result1 = writeThreeWindingsTransformerToFormatter(1);
        assertEquals("", result1);

        String result2 = writeThreeWindingsTransformerToFormatter(2);
        assertEquals("#Branches (test/InitialState)" + System.lineSeparator() +
                "#\"variant\" \"num\" \"bus1\" \"bus2\" \"3wt num\" \"sub.1\" \"sub.2\" \"r (pu)\" \"x (pu)\" \"g1 (pu)\" \"g2 (pu)\" \"b1 (pu)\" \"b2 (pu)\" \"cst ratio (pu)\""
                + " \"ratio tc\" \"phase tc\" \"p1 (MW)\" \"p2 (MW)\" \"q1 (MVar)\" \"q2 (MVar)\" \"patl1 (A)\" \"patl2 (A)\" \"merged\" \"fault\" \"curative\" \"id\" \"description\""
                + System.lineSeparator() + "1 1 1 2 1 1 1 107.438 115.702 0.0193600 0.00000 0.0205700 0.00000 400.000 -1 -1 -99999.0 -99999.0 -99999.0 -99999.0 -99999.0 -99999.0 false"
                + " 0 0 \"twt_leg1\" \"\"" + System.lineSeparator(), result2);
    }

    @Test
    void writeTieLineToFormatterTest() throws IOException {
        String result1 = writeTieLineToFormatter("busA", "busB", "vl1_0", "vl1", "vl2");
        assertEquals("", result1);

        String result2 = writeTieLineToFormatter("busA", "busB", "other", "vl1", "vl2");
        assertEquals("#Branches (test/InitialState)" + System.lineSeparator() +
                "#\"variant\" \"num\" \"bus1\" \"bus2\" \"3wt num\" \"sub.1\" \"sub.2\" \"r (pu)\" \"x (pu)\" \"g1 (pu)\" \"g2 (pu)\" \"b1 (pu)\" \"b2 (pu)\" \"cst ratio (pu)\""
                + " \"ratio tc\" \"phase tc\" \"p1 (MW)\" \"p2 (MW)\" \"q1 (MVar)\" \"q2 (MVar)\" \"patl1 (A)\" \"patl2 (A)\" \"merged\" \"fault\" \"curative\" \"id\" \"description\""
                + System.lineSeparator() + "1 2 1 3 -1 1 3 0.00516529 0.0103306 62.9200 62.9200 82.2800 82.2800 1.00000 -1 -1 -99999.0 -99999.0 -99999.0 -99999.0 -99999.0 -99999.0 false"
                + " 0 0 \"dl1\" \"dl1_name\"" + System.lineSeparator() + "1 3 3 2 -1 3 2 0.000516529 0.00103306 62.9200 62.9200 82.2800 82.2800 1.00000 -1 -1 -99999.0 -99999.0 -99999.0"
                + " -99999.0 -99999.0 -99999.0 false 0 0 \"dl2\" \"dl2\"" + System.lineSeparator(), result2);

        String result3 = writeTieLineToFormatter("busA", "busA", "other", "vl1", "vl1");
        assertEquals("", result3);
    }

    @Test
    void testBasicAmplExporterGetConfig() {
        Network network = NoEquipmentNetworkFactory.create();
        AmplExportConfig amplExportConfig = new AmplExportConfig(AmplExportConfig.ExportScope.ALL, true, AmplExportConfig.ExportActionType.CURATIVE,
                false, false, AmplExportVersion.V1_1);
        BasicAmplExporter exporter = new BasicAmplExporter(amplExportConfig, network, AmplUtil.createMapper(network), 1, 0, 0);
        assertEquals(amplExportConfig, exporter.getConfig());
    }

    @Test
    void testBasicAmplExporterGetNetwork() {
        Network network = NoEquipmentNetworkFactory.create();
        AmplExportConfig amplExportConfig = new AmplExportConfig(AmplExportConfig.ExportScope.ALL, true, AmplExportConfig.ExportActionType.CURATIVE,
                false, false, AmplExportVersion.V1_1);
        BasicAmplExporter exporter = new BasicAmplExporter(amplExportConfig, network, AmplUtil.createMapper(network), 1, 0, 0);
        assertEquals(network, exporter.getNetwork());
    }
}
