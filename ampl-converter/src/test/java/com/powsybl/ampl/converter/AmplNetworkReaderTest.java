/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.ampl.converter;

import com.google.common.io.ByteStreams;
import com.powsybl.commons.datasource.MemDataSource;
import com.powsybl.commons.util.StringToIntMapper;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.StaticVarCompensator.RegulationMode;
import com.powsybl.iidm.network.test.DanglingLineNetworkFactory;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.network.test.HvdcTestNetwork;
import com.powsybl.iidm.network.test.PhaseShifterTestCaseFactory;
import com.powsybl.iidm.network.test.SvcTestCaseFactory;
import com.powsybl.iidm.network.test.ThreeWindingsTransformerNetworkFactory;

import org.junit.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author Mathieu Bague <mathieu.bague@rte-france.com>
 */
public class AmplNetworkReaderTest {

    private static void importData(MemDataSource dataSource, String suffix, String filename) throws IOException {
        try (OutputStream stream = dataSource.newOutputStream(suffix, "txt", false)) {
            ByteStreams.copy(AmplNetworkReaderTest.class.getResourceAsStream("/" + filename), stream);
        }
    }

    @Test
    public void readEurostag() throws IOException {
        Network network = EurostagTutorialExample1Factory.create();
        StringToIntMapper<AmplSubset> mapper = AmplUtil.createMapper(network);

        MemDataSource memDataSource = new MemDataSource();
        importData(memDataSource, "_generators", "outputs/eurostag-tutorial-example1-generators.txt");
        importData(memDataSource, "_loads", "outputs/eurostag-tutorial-example1-loads.txt");
        importData(memDataSource, "_rtc", "outputs/eurostag-tutorial-example1-rtc.txt");
        importData(memDataSource, "_indic", "outputs/eurostag-tutorial-example1-indic.txt");
        importData(memDataSource, "_buses", "outputs/eurostag-tutorial-example1-buses.txt");
        importData(memDataSource, "_branches", "outputs/eurostag-tutorial-example1-branches.txt");

        AmplNetworkReader reader = new AmplNetworkReader(memDataSource, network, mapper);
        testGenerators(network, reader);
        testLoads(network, reader);
        testRatioTapChanger(network, reader);
        testMetrics(reader);
        testBuses(network, reader);
        testBranches(network, reader);
    }

    @Test
    public void readThreeWindingTransformers() throws IOException {
        Network network = ThreeWindingsTransformerNetworkFactory.create();
        StringToIntMapper<AmplSubset> mapper = AmplUtil.createMapper(network);

        MemDataSource memDataSource = new MemDataSource();
        importData(memDataSource, "_branches", "outputs/3wt-branches.txt");

        AmplNetworkReader reader = new AmplNetworkReader(memDataSource, network, mapper);
        testThreeWindingTransBranches(network, reader);
    }

    @Test
    public void readDanglingLines() throws IOException {
        Network network = DanglingLineNetworkFactory.create();
        StringToIntMapper<AmplSubset> mapper = AmplUtil.createMapper(network);

        MemDataSource memDataSource = new MemDataSource();
        importData(memDataSource, "_branches", "outputs/dl-branches.txt");

        AmplNetworkReader reader = new AmplNetworkReader(memDataSource, network, mapper);
        testDLBranches(network, reader);
    }

    @Test
    public void readHvdcLines() throws IOException {
        Network network = HvdcTestNetwork.createLcc();
        StringToIntMapper<AmplSubset> mapper = AmplUtil.createMapper(network);

        MemDataSource memDataSource = new MemDataSource();
        importData(memDataSource, "_hvdc", "outputs/hvdc.txt");
        importData(memDataSource, "_shunts", "outputs/shunts.txt");
        importData(memDataSource, "_lcc_converter_stations", "outputs/lcc.txt");

        AmplNetworkReader reader = new AmplNetworkReader(memDataSource, network, mapper);
        testHvdc(network, reader);
        testShunts(network, reader);
        testLcc(network, reader);

        Network network2 = HvdcTestNetwork.createVsc();
        StringToIntMapper<AmplSubset> mapper2 = AmplUtil.createMapper(network2);

        MemDataSource memDataSource2 = new MemDataSource();
        importData(memDataSource2, "_vsc_converter_stations", "outputs/vsc.txt");
        AmplNetworkReader reader2 = new AmplNetworkReader(memDataSource2, network2, mapper2);
        testVsc(network2, reader2);

    }


    @Test
    public void readSvc() throws IOException {
        Network network = SvcTestCaseFactory.create();
        StringToIntMapper<AmplSubset> mapper = AmplUtil.createMapper(network);

        MemDataSource memDataSource = new MemDataSource();
        importData(memDataSource, "_static_var_compensators", "outputs/svc.txt");

        AmplNetworkReader reader = new AmplNetworkReader(memDataSource, network, mapper);
        testSvc(network, reader);
    }

    private void testGenerators(Network network, AmplNetworkReader reader) throws IOException {
        Generator generator = network.getGenerator("GEN");
        VoltageLevel voltageLevel = generator.getTerminal().getVoltageLevel();

        assertEquals(24.5, generator.getTargetV(), 0.0);
        assertEquals(607.0, generator.getTargetP(), 0.0);
        assertTrue(Double.isNaN(generator.getTerminal().getP()));
        assertEquals(301.0, generator.getTargetQ(), 0.0);
        assertTrue(Double.isNaN(generator.getTerminal().getQ()));

        reader.readGenerators();

        assertEquals(voltageLevel.getNominalV() * 1.01000, generator.getTargetV(), 0.0);
        assertEquals(300.0, generator.getTargetP(), 0.0);
        assertEquals(300.0, generator.getTerminal().getP(), 0.0);
        assertEquals(150.0, generator.getTargetQ(), 0.0);
        assertEquals(150.0, generator.getTerminal().getQ(), 0.0);
    }

    private void testLoads(Network network, AmplNetworkReader reader) throws IOException {
        Load load = network.getLoad("LOAD");

        assertEquals(600.0, load.getP0(), 0.0);
        assertTrue(Double.isNaN(load.getTerminal().getP()));
        assertEquals(200.0, load.getQ0(), 0.0);
        assertTrue(Double.isNaN(load.getTerminal().getQ()));

        reader.readLoads();

        assertEquals(300.0, load.getP0(), 0.0);
        assertEquals(305.0, load.getTerminal().getP(), 0.0);
        assertEquals(150.0, load.getQ0(), 0.0);
        assertEquals(155.0, load.getTerminal().getQ(), 0.0);
    }

    private void testRatioTapChanger(Network network, AmplNetworkReader reader) throws IOException {
        TwoWindingsTransformer twt = network.getTwoWindingsTransformer("NHV2_NLOAD");
        RatioTapChanger rtc = twt.getRatioTapChanger();

        assertEquals(1, rtc.getTapPosition());

        reader.readRatioTapChangers();

        assertEquals(2, rtc.getTapPosition());
    }

    private void testMetrics(AmplNetworkReader reader) throws IOException {
        Map<String, String> metrics = new HashMap<>();
        reader.readMetrics(metrics);

        assertEquals(3, metrics.size());
        assertTrue(metrics.containsKey("metric1"));
        assertEquals("value1", metrics.get("metric1"));
        assertTrue(metrics.containsKey("metric2"));
        assertEquals("value2", metrics.get("metric2"));
        assertTrue(metrics.containsKey("metric with space"));
        assertEquals("value 3", metrics.get("metric with space"));
    }

    @Test
    public void readPhaseTapChanger() throws IOException {
        Network network = PhaseShifterTestCaseFactory.create();
        StringToIntMapper<AmplSubset> mapper = AmplUtil.createMapper(network);

        TwoWindingsTransformer twt = network.getTwoWindingsTransformer("PS1");
        PhaseTapChanger ptc = twt.getPhaseTapChanger();
        assertEquals(1, ptc.getTapPosition());

        MemDataSource memDataSource = new MemDataSource();
        importData(memDataSource, "_ptc", "outputs/ptc-test-case.txt");

        AmplNetworkReader reader = new AmplNetworkReader(memDataSource, network, mapper);
        reader.readPhaseTapChangers();

        assertEquals(2, ptc.getTapPosition());
    }

    private void testBuses(Network network, AmplNetworkReader reader) throws IOException {

        Optional<Bus> bx = network.getVoltageLevelStream().map(vl -> vl.getBusView().getBus("VLGEN_0")).filter(Objects::nonNull).findFirst();
        if (bx.isPresent()) {
            Bus b = bx.get();
            assertTrue(Double.isNaN(b.getAngle()));
            assertTrue(Double.isNaN(b.getV()));
        } else {
            fail("Bus not found");
        }

        reader.readBuses();

        Optional<Bus> bx2 = network.getVoltageLevelStream().map(vl -> vl.getBusView().getBus("VLGEN_0")).filter(Objects::nonNull).findFirst();
        if (bx2.isPresent()) {
            Bus b = bx2.get();
            assertEquals(Math.toDegrees(2d), b.getAngle(), 0.0);
            assertEquals(180f * b.getVoltageLevel().getNominalV(), b.getV(), 0.0);
        } else {
            fail("Bus not found");
        }

    }

    private void testBranches(Network network, AmplNetworkReader reader) throws IOException {
        Line l = network.getLine("NHV1_NHV2_1");
        assertTrue(Double.isNaN(l.getTerminal1().getP()));
        assertTrue(Double.isNaN(l.getTerminal1().getQ()));
        assertTrue(Double.isNaN(l.getTerminal2().getP()));
        assertTrue(Double.isNaN(l.getTerminal2().getQ()));

        //NHV2_NLOAD
        TwoWindingsTransformer twt = network.getTwoWindingsTransformer("NHV2_NLOAD");
        assertTrue(Double.isNaN(twt.getTerminal1().getP()));
        assertTrue(Double.isNaN(twt.getTerminal1().getQ()));
        assertTrue(Double.isNaN(twt.getTerminal2().getP()));
        assertTrue(Double.isNaN(twt.getTerminal2().getQ()));

        reader.readBranches();

        Line l2 = network.getLine("NHV1_NHV2_1");
        assertEquals(-100, l2.getTerminal1().getP(), 0.0);
        assertEquals(-110, l2.getTerminal1().getQ(), 0.0);
        assertEquals(-200, l2.getTerminal2().getP(), 0.0);
        assertEquals(-120, l2.getTerminal2().getQ(), 0.0);
        TwoWindingsTransformer twt2 = network.getTwoWindingsTransformer("NHV2_NLOAD");
        assertEquals(-100, twt2.getTerminal1().getP(), 0.0);
        assertEquals(-110, twt2.getTerminal1().getQ(), 0.0);
        assertEquals(-200, twt2.getTerminal2().getP(), 0.0);
        assertEquals(-120, twt2.getTerminal2().getQ(), 0.0);
    }

    private void testThreeWindingTransBranches(Network network, AmplNetworkReader reader) throws IOException {
        ThreeWindingsTransformer twt = network.getThreeWindingsTransformer("3WT");
        assertTrue(Double.isNaN(twt.getLeg1().getTerminal().getP()));
        assertTrue(Double.isNaN(twt.getLeg1().getTerminal().getQ()));
        assertTrue(Double.isNaN(twt.getLeg2().getTerminal().getP()));
        assertTrue(Double.isNaN(twt.getLeg2().getTerminal().getQ()));
        assertTrue(Double.isNaN(twt.getLeg3().getTerminal().getP()));
        assertTrue(Double.isNaN(twt.getLeg3().getTerminal().getQ()));

        reader.readBranches();

        ThreeWindingsTransformer twt2 = network.getThreeWindingsTransformer("3WT");
        assertEquals(-101, twt2.getLeg1().getTerminal().getP(), 0.0);
        assertEquals(-111, twt2.getLeg1().getTerminal().getQ(), 0.0);
        assertEquals(-102, twt2.getLeg2().getTerminal().getP(), 0.0);
        assertEquals(-112, twt2.getLeg2().getTerminal().getQ(), 0.0);
        assertEquals(-103, twt2.getLeg3().getTerminal().getP(), 0.0);
        assertEquals(-113, twt2.getLeg3().getTerminal().getQ(), 0.0);
    }

    private void testDLBranches(Network network, AmplNetworkReader reader) throws IOException {
        DanglingLine dl = network.getDanglingLine("DL");
        assertTrue(Double.isNaN(dl.getTerminal().getP()));
        assertTrue(Double.isNaN(dl.getTerminal().getQ()));

        reader.readBranches();

        DanglingLine dl2 = network.getDanglingLine("DL");
        assertEquals(-100, dl2.getTerminal().getP(), 0.0);
        assertEquals(-110, dl2.getTerminal().getQ(), 0.0);
    }

    private void testHvdc(Network network, AmplNetworkReader reader) throws IOException {
        HvdcLine hl = network.getHvdcLine("L");

        assertEquals(HvdcLine.ConvertersMode.SIDE_1_INVERTER_SIDE_2_RECTIFIER, hl.getConvertersMode());
        assertEquals(280, hl.getActivePowerSetpoint(), 0.0);

        reader.readHvdcLines();

        HvdcLine hl2 = network.getHvdcLine("L");
        assertEquals(HvdcLine.ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER, hl2.getConvertersMode());
        assertEquals(300, hl2.getActivePowerSetpoint(), 0.0);
    }

    private void testLcc(Network network, AmplNetworkReader reader) throws IOException {
        LccConverterStation lc = network.getLccConverterStation("C1");
        assertEquals(100, lc.getTerminal().getP(), 0.0);
        assertEquals(50, lc.getTerminal().getQ(), 0.0);

        reader.readLccConverterStations();

        assertEquals(200, lc.getTerminal().getP(), 0.0);
        assertEquals(75, lc.getTerminal().getQ(), 0.0);

    }

    private void testVsc(Network network, AmplNetworkReader reader) throws IOException {
        VscConverterStation vc = network.getVscConverterStation("C1");
        assertEquals(100, vc.getTerminal().getP(), 0.0);
        assertEquals(50, vc.getTerminal().getQ(), 0.0);
        assertTrue(vc.isVoltageRegulatorOn());
        assertTrue(Double.isNaN(vc.getReactivePowerSetpoint()));
        assertEquals(405, vc.getVoltageSetpoint(), 0.0);

        reader.readVscConverterStations();

        assertEquals(200, vc.getTerminal().getP(), 0.0);
        assertEquals(75, vc.getTerminal().getQ(), 0.0);
        assertTrue(vc.isVoltageRegulatorOn());
        assertEquals(30, vc.getReactivePowerSetpoint(), 0.0);
        assertEquals(400 * 1.01000, vc.getVoltageSetpoint(), 0.0);
    }

    private void testShunts(Network network, AmplNetworkReader reader) throws IOException {
        ShuntCompensator sc = network.getShuntCompensator("C1_Filter1");

        assertEquals(25.0, sc.getTerminal().getQ(), 0.0);

        reader.readShunts();

        ShuntCompensator sc2 = network.getShuntCompensator("C1_Filter1");

        assertEquals(30.0, sc2.getTerminal().getQ(), 0.0);
    }

    private void testSvc(Network network, AmplNetworkReader reader) throws IOException {
        StaticVarCompensator sv = network.getStaticVarCompensator("SVC2");

        assertEquals(RegulationMode.VOLTAGE, sv.getRegulationMode());
        assertEquals(390.0, sv.getVoltageSetPoint(), 0.0);
        assertTrue(Double.isNaN(sv.getTerminal().getQ()));

        reader.readStaticVarcompensator();

        StaticVarCompensator sv2 = network.getStaticVarCompensator("SVC2");
        assertEquals(RegulationMode.REACTIVE_POWER, sv2.getRegulationMode());
        assertEquals(1.080000 * sv.getTerminal().getVoltageLevel().getNominalV(), sv2.getVoltageSetPoint(), 0.0);
        assertEquals(-30.0, sv2.getReactivePowerSetPoint(), 0.0);
        assertEquals(30.0, sv2.getTerminal().getQ(), 0.0);
    }

}
