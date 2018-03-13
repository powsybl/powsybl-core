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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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

        AmplNetworkReader reader = new AmplNetworkReader(memDataSource, network, mapper);
        testHvdc(network, reader);
        testShunts(network, reader);
    }
    @Test
    public void readSvc() throws IOException {
        Network network = SvcTestCaseFactory.create();
        StringToIntMapper<AmplSubset> mapper = AmplUtil.createMapper(network);

        MemDataSource memDataSource = new MemDataSource();
        importData(memDataSource, "_svc", "outputs/svc.txt");

        AmplNetworkReader reader = new AmplNetworkReader(memDataSource, network, mapper);
        testSvc(network, reader);
    }

    private void testGenerators(Network network, AmplNetworkReader reader) throws IOException {
        Generator generator = network.getGenerator("GEN");
        VoltageLevel voltageLevel = generator.getTerminal().getVoltageLevel();

        assertEquals(24.5, generator.getTargetV(), 0.0f);
        assertEquals(607.0f, generator.getTargetP(), 0.0f);
        assertTrue(Float.isNaN(generator.getTerminal().getP()));
        assertEquals(301.0f, generator.getTargetQ(), 0.0f);
        assertTrue(Float.isNaN(generator.getTerminal().getQ()));

        reader.readGenerators();

        assertEquals(voltageLevel.getNominalV(), generator.getTargetV(), 0.0f);
        assertEquals(300.0f, generator.getTargetP(), 0.0f);
        assertEquals(300.0f, generator.getTerminal().getP(), 0.0f);
        assertEquals(150.0f, generator.getTargetQ(), 0.0f);
        assertEquals(150.0f, generator.getTerminal().getQ(), 0.0f);
    }

    private void testLoads(Network network, AmplNetworkReader reader) throws IOException {
        Load load = network.getLoad("LOAD");

        assertEquals(600.0f, load.getP0(), 0.0);
        assertTrue(Float.isNaN(load.getTerminal().getP()));
        assertEquals(200.0f, load.getQ0(), 0.0);
        assertTrue(Float.isNaN(load.getTerminal().getQ()));

        reader.readLoads();

        assertEquals(300.0f, load.getP0(), 0.0);
        assertEquals(305.0f, load.getTerminal().getP(), 0.0);
        assertEquals(150.0f, load.getQ0(), 0.0);
        assertEquals(155.0f, load.getTerminal().getQ(), 0.0);
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
        for (Bus b : AmplUtil.getBuses(network)) {
            if (b.getId().equals("VLGEN_0")) {
                assertTrue(Float.isNaN(b.getAngle()));
                assertTrue(Float.isNaN(b.getV()));
            }
        }
        reader.readBuses();

        for (Bus b : AmplUtil.getBuses(network)) {
            if (b.getId().equals("VLGEN_0")) {
                assertEquals(20f, b.getAngle(), 0.0);
                assertEquals(180f, b.getV(), 0.0);
            }
        }
    }

    private void testBranches(Network network, AmplNetworkReader reader) throws IOException {
        Line l = network.getLine("NHV1_NHV2_1");
        assertTrue(Float.isNaN(l.getTerminal1().getP()));
        assertTrue(Float.isNaN(l.getTerminal1().getQ()));
        assertTrue(Float.isNaN(l.getTerminal2().getP()));
        assertTrue(Float.isNaN(l.getTerminal2().getQ()));

        //NHV2_NLOAD
        TwoWindingsTransformer twt = network.getTwoWindingsTransformer("NHV2_NLOAD");
        assertTrue(Float.isNaN(twt.getTerminal1().getP()));
        assertTrue(Float.isNaN(twt.getTerminal1().getQ()));
        assertTrue(Float.isNaN(twt.getTerminal2().getP()));
        assertTrue(Float.isNaN(twt.getTerminal2().getQ()));

        reader.readBranches();

        Line l2 = network.getLine("NHV1_NHV2_1");
        assertEquals(-100f, l2.getTerminal1().getP(), 0.0);
        assertEquals(-110f, l2.getTerminal1().getQ(), 0.0);
        assertEquals(-200f, l2.getTerminal2().getP(), 0.0);
        assertEquals(-120f, l2.getTerminal2().getQ(), 0.0);
        TwoWindingsTransformer twt2 = network.getTwoWindingsTransformer("NHV2_NLOAD");
        assertEquals(-100f, twt2.getTerminal1().getP(), 0.0);
        assertEquals(-110f, twt2.getTerminal1().getQ(), 0.0);
        assertEquals(-200f, twt2.getTerminal2().getP(), 0.0);
        assertEquals(-120f, twt2.getTerminal2().getQ(), 0.0);
    }

    private void testThreeWindingTransBranches(Network network, AmplNetworkReader reader) throws IOException {
        ThreeWindingsTransformer twt = network.getThreeWindingsTransformer("3WT");
        assertTrue(Float.isNaN(twt.getLeg1().getTerminal().getP()));
        assertTrue(Float.isNaN(twt.getLeg1().getTerminal().getQ()));
        assertTrue(Float.isNaN(twt.getLeg2().getTerminal().getP()));
        assertTrue(Float.isNaN(twt.getLeg2().getTerminal().getQ()));
        assertTrue(Float.isNaN(twt.getLeg3().getTerminal().getP()));
        assertTrue(Float.isNaN(twt.getLeg3().getTerminal().getQ()));

        reader.readBranches();

        ThreeWindingsTransformer twt2 = network.getThreeWindingsTransformer("3WT");
        assertEquals(-101f, twt2.getLeg1().getTerminal().getP(), 0.0);
        assertEquals(-111f, twt2.getLeg1().getTerminal().getQ(), 0.0);
        assertEquals(-102f, twt2.getLeg2().getTerminal().getP(), 0.0);
        assertEquals(-112f, twt2.getLeg2().getTerminal().getQ(), 0.0);
        assertEquals(-103f, twt2.getLeg3().getTerminal().getP(), 0.0);
        assertEquals(-113f, twt2.getLeg3().getTerminal().getQ(), 0.0);
    }

    private void testDLBranches(Network network, AmplNetworkReader reader) throws IOException {
        DanglingLine dl = network.getDanglingLine("DL");
        assertTrue(Float.isNaN(dl.getTerminal().getP()));
        assertTrue(Float.isNaN(dl.getTerminal().getQ()));

        reader.readBranches();

        DanglingLine dl2 = network.getDanglingLine("DL");
        assertEquals(-100f, dl2.getTerminal().getP(), 0.0);
        assertEquals(-110f, dl2.getTerminal().getQ(), 0.0);
    }

    private void testHvdc(Network network, AmplNetworkReader reader) throws IOException {
        HvdcLine hl = network.getHvdcLine("L");

        assertEquals(HvdcLine.ConvertersMode.SIDE_1_INVERTER_SIDE_2_RECTIFIER, hl.getConvertersMode());
        assertEquals(280f, hl.getActivePowerSetpoint(), 0.0);

        reader.readHvdcLines();

        HvdcLine hl2 = network.getHvdcLine("L");
        assertEquals(HvdcLine.ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER, hl2.getConvertersMode());
        assertEquals(300f, hl2.getActivePowerSetpoint(), 0.0);
    }

    private void testShunts(Network network, AmplNetworkReader reader) throws IOException {
        ShuntCompensator sc = network.getShunt("C1_Filter1");

        assertEquals(25.0f, sc.getTerminal().getQ(), 0.0);

        reader.readShunts();

        ShuntCompensator sc2 = network.getShunt("C1_Filter1");

        assertEquals(30f, sc2.getTerminal().getQ(), 0.0);
    }

    private void testSvc(Network network, AmplNetworkReader reader) throws IOException {
        StaticVarCompensator sv = network.getStaticVarCompensator("SVC2");

        assertEquals(RegulationMode.VOLTAGE, sv.getRegulationMode());
        assertEquals(390f, sv.getVoltageSetPoint(), 0.0);
        assertTrue(Float.isNaN(sv.getTerminal().getQ()));

        reader.readStaticVarcompensator();

        StaticVarCompensator sv2 = network.getStaticVarCompensator("SVC2");
        assertEquals(RegulationMode.REACTIVE_POWER, sv2.getRegulationMode());
        assertEquals(400f, sv2.getVoltageSetPoint(), 0.0);
        assertEquals(-30f, sv2.getReactivePowerSetPoint(), 0.0);
        assertEquals(0f, sv2.getTerminal().getP(), 0.0);
        assertEquals(30f, sv2.getTerminal().getQ(), 0.0);
    }
}
