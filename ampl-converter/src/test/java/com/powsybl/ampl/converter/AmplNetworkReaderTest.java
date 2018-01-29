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
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.network.test.PhaseShifterTestCaseFactory;
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

        AmplNetworkReader reader = new AmplNetworkReader(memDataSource, network, mapper);
        testGenerators(network, reader);
        testLoads(network, reader);
        testRatioTapChanger(network, reader);
        testMetrics(reader);
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
        assertEquals(-300.0f, generator.getTerminal().getP(), 0.0f);
        assertEquals(150.0f, generator.getTargetQ(), 0.0f);
        assertEquals(-150.0f, generator.getTerminal().getQ(), 0.0f);
    }

    private void testLoads(Network network, AmplNetworkReader reader) throws IOException {
        Load load = network.getLoad("LOAD");

        assertEquals(600.0f, load.getP0(), 0.0);
        assertTrue(Float.isNaN(load.getTerminal().getP()));
        assertEquals(200.0f, load.getQ0(), 0.0);
        assertTrue(Float.isNaN(load.getTerminal().getQ()));

        reader.readLoads();

        assertEquals(300.0f, load.getP0(), 0.0);
        assertEquals(300.0f, load.getTerminal().getP(), 0.0);
        assertEquals(150.0f, load.getQ0(), 0.0);
        assertEquals(150.0f, load.getTerminal().getQ(), 0.0);
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
}
