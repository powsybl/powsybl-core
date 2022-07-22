/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.loadflow.resultscompletion;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.commons.config.InMemoryPlatformConfig;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.StaticVarCompensator.RegulationMode;
import com.powsybl.iidm.network.ThreeWindingsTransformer.Leg;
import com.powsybl.loadflow.LoadFlowParameters;
import com.powsybl.loadflow.validation.CandidateComputation;
import com.powsybl.loadflow.validation.ValidationConfig;
import com.powsybl.loadflow.validation.ValidationType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Marcos De Miguel <demiguelm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
public class LoadFlowResultsCompletionZ0FlowsTest {

    @Test
    public void originalZ0FlowsCompletion() throws Exception {
        Network network = createNetwork();

        Line loop = network.getLine("L1-1");
        assertTrue(Double.isNaN(loop.getTerminal1().getP()));

        completeResults(network);

        assertEquals(0, loop.getTerminal1().getP(), 0);
        assertEquals(0, loop.getTerminal1().getQ(), 0);
        assertEquals(0, loop.getTerminal2().getP(), 0);
        assertEquals(0, loop.getTerminal2().getQ(), 0);

        assertTrue(validateBuses(network));
    }

    @Test
    public void disconnectZ0FlowsCompletion() throws Exception {
        Network network = createNetwork();
        disconnectLine(network);
        completeResults(network);
        assertTrue(validateBuses(network));
    }

    @Test
    public void splitZ0FlowsCompletion() throws Exception {
        Network network = createNetwork();
        splitNetwork(network);
        completeResults(network);
        assertTrue(validateBuses(network));
    }

    @Test
    public void distributeActivePowerGenerator() throws Exception {
        Network network = createNetwork();
        Generator generator = network.getGenerator("G2");
        double pgen = generator.getTerminal().getP();
        generator.getTerminal().setP(pgen + 25.0);

        completeResults(network);
        assertEquals(pgen, generator.getTerminal().getP(), 0.0001);
        assertTrue(validateBuses(network));

        generator.getTerminal().setP(pgen - 25.0);
        completeResults(network);
        assertEquals(pgen, generator.getTerminal().getP(), 0.0001);
        assertTrue(validateBuses(network));

        generator.setMinP(-5.0).setMaxP(20.0);
        Load load = network.getLoad("LD2");
        double pload = load.getTerminal().getP();

        load.getTerminal().setP(pload + 25.0);
        completeResults(network);
        assertEquals(-20.0, generator.getTerminal().getP(), 0.0);

        load.getTerminal().setP(pload + 50.0);
        completeResults(network);
        assertEquals(-20.0, generator.getTerminal().getP(), 0.0);

        load.getTerminal().setP(pload - 25.0);
        completeResults(network);
        assertEquals(5.0, generator.getTerminal().getP(), 0.0);

        load.getTerminal().setP(pload - 50.0);
        completeResults(network);
        assertEquals(5.0, generator.getTerminal().getP(), 0.0);
    }

    @Test
    public void distributeReactivePowerGenerator() throws Exception {
        Network network = createNetwork();
        Generator generator = network.getGenerator("G2");
        double qgen = generator.getTerminal().getQ();
        generator.getTerminal().setQ(qgen + 25.0);

        completeResults(network);
        assertEquals(qgen, generator.getTerminal().getQ(), 0.0001);
        assertTrue(validateBuses(network));

        generator.getTerminal().setQ(qgen - 25.0);
        completeResults(network);
        assertEquals(qgen, generator.getTerminal().getQ(), 0.0001);
        assertTrue(validateBuses(network));

        generator.newMinMaxReactiveLimits().setMinQ(-10.0).setMaxQ(15.0).add();
        Load load = network.getLoad("LD2");
        double qload = load.getTerminal().getQ();

        load.getTerminal().setQ(qload + 25.0);
        completeResults(network);
        assertEquals(-15.0, generator.getTerminal().getQ(), 0.0);

        load.getTerminal().setQ(qload + 50.0);
        completeResults(network);
        assertEquals(-15.0, generator.getTerminal().getQ(), 0.0);

        load.getTerminal().setQ(qload - 25.0);
        completeResults(network);
        assertEquals(10.0, generator.getTerminal().getQ(), 0.0);

        load.getTerminal().setQ(qload - 50.0);
        completeResults(network);
        assertEquals(10.0, generator.getTerminal().getQ(), 0.0);
    }

    @Test
    public void distributeActivePowerGeneratorZ0() throws Exception {
        Network network = createNetwork();
        Generator generator = network.getGenerator("G3.2");
        Load load = network.getLoad("LD3.3");
        double pgen = generator.getTerminal().getP();
        double pload = load.getTerminal().getP();

        generator.getTerminal().setP(pgen + 25.0);
        completeResults(network);
        assertEquals(pgen, generator.getTerminal().getP(), 0.0001);
        assertTrue(validateBuses(network));

        generator.getTerminal().setP(pgen - 25.0);
        completeResults(network);
        assertEquals(pgen, generator.getTerminal().getP(), 0.0001);
        assertTrue(validateBuses(network));

        load.getTerminal().setP(pload + 25.0);
        completeResults(network);
        assertEquals(pgen - 25.0, generator.getTerminal().getP(), 0.0001);
        assertTrue(validateBuses(network));

        load.getTerminal().setP(pload - 25.0);
        completeResults(network);
        assertEquals(pgen + 25.0, generator.getTerminal().getP(), 0.0001);
        assertTrue(validateBuses(network));

        generator.setMinP(-5.0).setMaxP(20.0);

        load.getTerminal().setP(pload + 50.0);
        completeResults(network);
        assertEquals(-20.0, generator.getTerminal().getP(), 0.0);

        load.getTerminal().setP(pload + 75.0);
        completeResults(network);
        assertEquals(-20.0, generator.getTerminal().getP(), 0.0);

        load.getTerminal().setP(pload - 50.0);
        completeResults(network);
        assertEquals(5.0, generator.getTerminal().getP(), 0.0);

        load.getTerminal().setP(pload - 75.0);
        completeResults(network);
        assertEquals(5.0, generator.getTerminal().getP(), 0.0);
    }

    @Test
    public void twoWindingsTransformerZ0() throws Exception {
        Network network = createT2wtZ0Network();

        TwoWindingsTransformer t2wt = network.getTwoWindingsTransformer("T2wt-3.1-3.2-1");
        TwoWindingsTransformer t2wtAntenna = network.getTwoWindingsTransformer("T2wt-3.1-3.2-2");
        Load load = network.getLoad("LD3.2");
        completeResults(network);
        assertTrue(validateBuses(network));
        assertEquals(load.getTerminal().getP(), t2wt.getTerminal1().getP(), 0.0);
        assertEquals(load.getTerminal().getQ(), t2wt.getTerminal1().getQ(), 0.0);
        assertEquals(-load.getTerminal().getP(), t2wt.getTerminal2().getP(), 0.0);
        assertEquals(-load.getTerminal().getQ(), t2wt.getTerminal2().getQ(), 0.0);

        assertEquals(0.0, t2wtAntenna.getTerminal1().getP(), 0.0);
        assertEquals(0.0, t2wtAntenna.getTerminal1().getQ(), 0.0);
        assertEquals(0.0, t2wtAntenna.getTerminal2().getP(), 0.0);
        assertEquals(0.0, t2wtAntenna.getTerminal2().getQ(), 0.0);
    }

    @Test
    public void danglingLineZ0() throws Exception {
        Network network = createDlZ0Network();

        Line l = network.getLine("L2-3.1");
        Line lz0 = network.getLine("L3.1-3.2");
        DanglingLine dl = network.getDanglingLine("DL3.1");
        Load load = network.getLoad("LD3.2");
        completeResults(network, 0.001);
        assertTrue(validateBuses(network));

        assertEquals(load.getTerminal().getP(), lz0.getTerminal1().getP(), 0.0);
        assertEquals(load.getTerminal().getQ(), lz0.getTerminal1().getQ(), 0.0);
        assertEquals(-load.getTerminal().getP(), lz0.getTerminal2().getP(), 0.0);
        assertEquals(-load.getTerminal().getQ(), lz0.getTerminal2().getQ(), 0.0);

        assertEquals(l.getTerminal2().getP(), -(lz0.getTerminal1().getP() + dl.getTerminal().getP()), 0.0);
        assertEquals(l.getTerminal2().getQ(), -(lz0.getTerminal1().getQ() + dl.getTerminal().getQ()), 0.0);
    }

    @Test
    public void threeWindingsTransformerZ0() throws Exception {
        Network network = createT3wtZ0Network(1.0);

        Line l = network.getLine("L2-3.1");
        ThreeWindingsTransformer t = network.getThreeWindingsTransformer("T3wt3.1-3.2-3.3");
        Load load32 = network.getLoad("LD3.2");
        Load load33 = network.getLoad("LD3.3");
        completeResults(network, 0.001);
        assertTrue(validateBuses(network));

        assertEquals(load32.getTerminal().getP(), -t.getLeg2().getTerminal().getP(), 0.0);
        assertEquals(load32.getTerminal().getQ(), -t.getLeg2().getTerminal().getQ(), 0.0);

        assertEquals(load33.getTerminal().getP(), -t.getLeg3().getTerminal().getP(), 0.0);
        assertEquals(load33.getTerminal().getQ(), -t.getLeg3().getTerminal().getQ(), 0.0);

        assertEquals(l.getTerminal2().getP(), -t.getLeg1().getTerminal().getP(), 0.0009);
        assertEquals(l.getTerminal2().getQ(), -t.getLeg1().getTerminal().getQ(), 0.0009);
    }

    @Test
    public void threeWindingsTransformer12Z0() throws Exception {
        Network network = createT3wt12Z0Network();

        Line l = network.getLine("L2-3.1");
        ThreeWindingsTransformer t = network.getThreeWindingsTransformer("T3wt3.1-3.2-3.3");
        Load load32 = network.getLoad("LD3.2");
        Load load33 = network.getLoad("LD3.3");
        completeResults(network, 0.001);
        assertTrue(validateBuses(network));

        assertEquals(load32.getTerminal().getP(), -t.getLeg2().getTerminal().getP(), 0.0);
        assertEquals(load32.getTerminal().getQ(), -t.getLeg2().getTerminal().getQ(), 0.0);

        assertEquals(load33.getTerminal().getP(), -t.getLeg3().getTerminal().getP(), 0.0009);
        assertEquals(load33.getTerminal().getQ(), -t.getLeg3().getTerminal().getQ(), 0.0009);

        assertEquals(l.getTerminal2().getP(), -t.getLeg1().getTerminal().getP(), 0.0009);
        assertEquals(l.getTerminal2().getQ(), -t.getLeg1().getTerminal().getQ(), 0.0009);
    }

    @Test
    public void threeWindingsTransformer1Z0() throws Exception {
        Network network = createT3wt1Z0Network();

        Line l = network.getLine("L2-3.1");
        ThreeWindingsTransformer t = network.getThreeWindingsTransformer("T3wt3.1-3.2-3.3");
        Load load32 = network.getLoad("LD3.2");
        Load load33 = network.getLoad("LD3.3");
        completeResults(network, 0.001);
        assertTrue(validateBuses(network));

        assertEquals(load32.getTerminal().getP(), -t.getLeg2().getTerminal().getP(), 0.009);
        assertEquals(load32.getTerminal().getQ(), -t.getLeg2().getTerminal().getQ(), 0.009);

        assertEquals(load33.getTerminal().getP(), -t.getLeg3().getTerminal().getP(), 0.009);
        assertEquals(load33.getTerminal().getQ(), -t.getLeg3().getTerminal().getQ(), 0.009);

        assertEquals(l.getTerminal2().getP(), -t.getLeg1().getTerminal().getP(), 0.009);
        assertEquals(l.getTerminal2().getQ(), -t.getLeg1().getTerminal().getQ(), 0.009);
    }

    @Test
    public void threeWindingsTransformer12Z03Antenna() throws Exception {
        Network network = createT3wt12Z03AntennaNetwork();

        ThreeWindingsTransformer t3 = network.getThreeWindingsTransformer("T3wt3.1-3.2-3.3");
        TwoWindingsTransformer t2 = network.getTwoWindingsTransformer("T2wt3.1-3.3");
        Load load32 = network.getLoad("LD3.2");
        Load load33 = network.getLoad("LD3.3");
        completeResults(network, 0.001);
        assertTrue(validateBuses(network));

        assertEquals(load32.getTerminal().getP(), t3.getLeg1().getTerminal().getP(), 0.0);
        assertEquals(load32.getTerminal().getQ(), t3.getLeg1().getTerminal().getQ(), 0.0);
        assertEquals(load32.getTerminal().getP(), -t3.getLeg2().getTerminal().getP(), 0.0);
        assertEquals(load32.getTerminal().getQ(), -t3.getLeg2().getTerminal().getQ(), 0.0);
        assertEquals(0.0, t3.getLeg3().getTerminal().getP(), 0.0);
        assertEquals(0.0, t3.getLeg3().getTerminal().getQ(), 0.0);

        assertEquals(load33.getTerminal().getP(), t2.getTerminal1().getP(), 0.0);
        assertEquals(load33.getTerminal().getQ(), t2.getTerminal1().getQ(), 0.0);
        assertEquals(load33.getTerminal().getP(), -t2.getTerminal2().getP(), 0.0);
        assertEquals(load33.getTerminal().getQ(), -t2.getTerminal2().getQ(), 0.0);
    }

    @Test
    public void threeWindingsTransformer1Z03Antenna() throws Exception {
        Network network = createT3wt1Z03AntennaNetwork();

        ThreeWindingsTransformer t3 = network.getThreeWindingsTransformer("T3wt3.1-3.2-3.3");
        TwoWindingsTransformer t2 = network.getTwoWindingsTransformer("T2wt3.1-3.3");
        Load load32 = network.getLoad("LD3.2");
        Load load33 = network.getLoad("LD3.3");
        completeResults(network, 0.001);
        assertTrue(validateBuses(network));

        assertEquals(load32.getTerminal().getP(), t3.getLeg1().getTerminal().getP(), 0.0009);
        assertEquals(load32.getTerminal().getQ(), t3.getLeg1().getTerminal().getQ(), 0.0009);
        assertEquals(load32.getTerminal().getP(), -t3.getLeg2().getTerminal().getP(), 0.0009);
        assertEquals(load32.getTerminal().getQ(), -t3.getLeg2().getTerminal().getQ(), 0.0009);
        assertEquals(0.0, t3.getLeg3().getTerminal().getP(), 0.0);
        assertEquals(0.0, t3.getLeg3().getTerminal().getQ(), 0.0);

        assertEquals(load33.getTerminal().getP(), t2.getTerminal1().getP(), 0.0);
        assertEquals(load33.getTerminal().getQ(), t2.getTerminal1().getQ(), 0.0);
        assertEquals(load33.getTerminal().getP(), -t2.getTerminal2().getP(), 0.0);
        assertEquals(load33.getTerminal().getQ(), -t2.getTerminal2().getQ(), 0.0);
    }

    @Test
    public void threeWindingsTransformer3Antenna() throws Exception {
        Network network = createT3wt3AntennaNetwork();

        ThreeWindingsTransformer t3 = network.getThreeWindingsTransformer("T3wt3.1-3.2-3.3");
        TwoWindingsTransformer t2 = network.getTwoWindingsTransformer("T2wt3.1-3.3");
        Load load32 = network.getLoad("LD3.2");
        Load load33 = network.getLoad("LD3.3");
        completeResults(network, 0.001);
        assertTrue(validateBuses(network));

        assertEquals(load32.getTerminal().getP(), t3.getLeg1().getTerminal().getP(), 0.0009);
        assertEquals(load32.getTerminal().getQ(), t3.getLeg1().getTerminal().getQ(), 0.0009);
        assertEquals(load32.getTerminal().getP(), -t3.getLeg2().getTerminal().getP(), 0.0009);
        assertEquals(load32.getTerminal().getQ(), -t3.getLeg2().getTerminal().getQ(), 0.0009);
        assertEquals(0.0, t3.getLeg3().getTerminal().getP(), 0.0);
        assertEquals(0.0, t3.getLeg3().getTerminal().getQ(), 0.0);

        assertEquals(load33.getTerminal().getP(), t2.getTerminal1().getP(), 0.0);
        assertEquals(load33.getTerminal().getQ(), t2.getTerminal1().getQ(), 0.0);
        assertEquals(load33.getTerminal().getP(), -t2.getTerminal2().getP(), 0.0);
        assertEquals(load33.getTerminal().getQ(), -t2.getTerminal2().getQ(), 0.0);
    }

    @Test
    public void threeWindingsTransformer1Z023Antenna() throws Exception {
        Network network = createT3wt1Z023AntennaNetwork();

        ThreeWindingsTransformer t3 = network.getThreeWindingsTransformer("T3wt3.1-3.2-3.3");
        TwoWindingsTransformer t22 = network.getTwoWindingsTransformer("T2wt3.1-3.2");
        TwoWindingsTransformer t23 = network.getTwoWindingsTransformer("T2wt3.1-3.3");
        Load load32 = network.getLoad("LD3.2");
        Load load33 = network.getLoad("LD3.3");
        completeResults(network, 0.001);
        assertTrue(validateBuses(network));

        assertEquals(0.0, t3.getLeg1().getTerminal().getP(), 0.0);
        assertEquals(0.0, t3.getLeg1().getTerminal().getQ(), 0.0);
        assertEquals(0.0, t3.getLeg2().getTerminal().getP(), 0.0);
        assertEquals(0.0, t3.getLeg2().getTerminal().getQ(), 0.0);
        assertEquals(0.0, t3.getLeg3().getTerminal().getP(), 0.0);
        assertEquals(0.0, t3.getLeg3().getTerminal().getQ(), 0.0);

        assertEquals(load32.getTerminal().getP(), t22.getTerminal1().getP(), 0.0);
        assertEquals(load32.getTerminal().getQ(), t22.getTerminal1().getQ(), 0.0);
        assertEquals(load32.getTerminal().getP(), -t22.getTerminal2().getP(), 0.0);
        assertEquals(load32.getTerminal().getQ(), -t22.getTerminal2().getQ(), 0.0);

        assertEquals(load33.getTerminal().getP(), t23.getTerminal1().getP(), 0.0);
        assertEquals(load33.getTerminal().getQ(), t23.getTerminal1().getQ(), 0.0);
        assertEquals(load33.getTerminal().getP(), -t23.getTerminal2().getP(), 0.0);
        assertEquals(load33.getTerminal().getQ(), -t23.getTerminal2().getQ(), 0.0);
    }

    @Test
    public void threeWindingsTransformer23Antenna() throws Exception {
        Network network = createT3wt23AntennaNetwork();

        ThreeWindingsTransformer t3 = network.getThreeWindingsTransformer("T3wt3.1-3.2-3.3");
        TwoWindingsTransformer t22 = network.getTwoWindingsTransformer("T2wt3.1-3.2");
        TwoWindingsTransformer t23 = network.getTwoWindingsTransformer("T2wt3.1-3.3");
        Load load32 = network.getLoad("LD3.2");
        Load load33 = network.getLoad("LD3.3");
        completeResults(network, 0.001);
        assertTrue(validateBuses(network));

        assertEquals(0.0, t3.getLeg1().getTerminal().getP(), 0.0009);
        assertEquals(0.0, t3.getLeg1().getTerminal().getQ(), 0.0009);
        assertEquals(0.0, t3.getLeg2().getTerminal().getP(), 0.0);
        assertEquals(0.0, t3.getLeg2().getTerminal().getQ(), 0.0);
        assertEquals(0.0, t3.getLeg3().getTerminal().getP(), 0.0);
        assertEquals(0.0, t3.getLeg3().getTerminal().getQ(), 0.0);

        assertEquals(load32.getTerminal().getP(), t22.getTerminal1().getP(), 0.0);
        assertEquals(load32.getTerminal().getQ(), t22.getTerminal1().getQ(), 0.0);
        assertEquals(load32.getTerminal().getP(), -t22.getTerminal2().getP(), 0.0);
        assertEquals(load32.getTerminal().getQ(), -t22.getTerminal2().getQ(), 0.0);

        assertEquals(load33.getTerminal().getP(), t23.getTerminal1().getP(), 0.0);
        assertEquals(load33.getTerminal().getQ(), t23.getTerminal1().getQ(), 0.0);
        assertEquals(load33.getTerminal().getP(), -t23.getTerminal2().getP(), 0.0);
        assertEquals(load33.getTerminal().getQ(), -t23.getTerminal2().getQ(), 0.0);
    }

    @Test
    public void distributeLccHvdc() throws Exception {
        Network network = createLccHvdcNetwork();
        completeResults(network, 0.001);
        assertTrue(validateBuses(network));

        HvdcLine hvdcLine = network.getHvdcLine("HVDC");
        assertEquals(30.0, hvdcLine.getConverterStation1().getTerminal().getP(), 0.000001);
        assertEquals(6.091757, hvdcLine.getConverterStation1().getTerminal().getQ(), 0.000001);
        assertEquals(-29.105353, hvdcLine.getConverterStation2().getTerminal().getP(), 0.000001);
        assertEquals(9.566468, hvdcLine.getConverterStation2().getTerminal().getQ(), 0.000001);
    }

    @Test
    public void distributeSvcHvdc() throws Exception {
        Network network = createSvcHvdcNetwork();
        completeResults(network, 0.001);
        assertTrue(validateBuses(network));

        HvdcLine hvdcLine = network.getHvdcLine("HVDC");
        assertEquals(-29.105346, hvdcLine.getConverterStation1().getTerminal().getP(), 0.000001);
        assertEquals(-21.333350, hvdcLine.getConverterStation1().getTerminal().getQ(), 0.000001);
        assertEquals(30.0, hvdcLine.getConverterStation2().getTerminal().getP(), 0.000001);
        assertEquals(0.0, hvdcLine.getConverterStation2().getTerminal().getQ(), 0.000001);
    }

    @Before
    public void setUp() throws IOException {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
    }

    @After
    public void tearDown() throws IOException {
        fileSystem.close();
    }

    private void completeResults(Network network) {
        CandidateComputation resultsCompletion = new LoadFlowResultsCompletion(
            new LoadFlowResultsCompletionParameters(),
            new LoadFlowParameters());
        assertEquals(LoadFlowResultsCompletion.NAME, resultsCompletion.getName());
        resultsCompletion.run(network, null);
    }

    private void completeResults(Network network, double distributeTolerance) {
        CandidateComputation resultsCompletion = new LoadFlowResultsCompletion(
            new LoadFlowResultsCompletionParameters(),
            new LoadFlowParameters(),
            distributeTolerance);
        assertEquals(LoadFlowResultsCompletion.NAME, resultsCompletion.getName());
        resultsCompletion.run(network, null);
    }

    private boolean validateBuses(Network network) throws IOException {
        ValidationConfig config = createValidationConfig();
        Path working = Files.createDirectories(fileSystem.getPath("temp-validation"));
        return ValidationType.BUSES.check(network, config, working);
    }

    private Network createNetwork() {
        double sbase = 100.0;
        double vbase = 115.0;
        // Impedances are expressed in per-unit values,
        // we convert to engineering units when adding elements to IIDM
        double zpu = sbase / Math.pow(vbase, 2);

        Network network = Network.create("test", "test");
        Substation s = network.newSubstation()
                .setId("S")
                .setCountry(Country.FR)
                .add();
        VoltageLevel vl = s.newVoltageLevel()
                .setId("VL")
                .setNominalV(115.0)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        createBus(vl, "B1");
        createBus(vl, "B2");
        createBus(vl, "B4");
        createBus(vl, "B5");
        createBus(vl, "B3.1");
        createBus(vl, "B3.2");
        createBus(vl, "B3.3");
        createBus(vl, "B3.4");
        createBus(vl, "B3.5");

        // Add a loop (a line with same bus at both ends)
        createLine(network, "L1-1", "VL", "B1", "VL", "B1", 10.0, 100.0, 0.0, 0.0, 0.0, 0.0);
        Line l = createLine(network, "L1-2", "VL", "B1", "VL", "B2", 0.02 / zpu, 0.11 / zpu, 0.0, 0.015 / 2 * zpu, 0.0, 0.015 / 2 * zpu);
        setVoltageAndAngle(l.getTerminal1(), 1.0 * vbase, Math.toDegrees(0.0));
        setVoltageAndAngle(l.getTerminal2(), 1.0 * vbase, Math.toDegrees(-0.00015945));

        l = createLine(network, "L1-3.1", "VL", "B1", "VL", "B3.1", 0.01 / zpu, 0.09 / zpu, 0.0, 0.023 / 2 * zpu, 0.0, 0.023 / 2 * zpu);
        setVoltageAndAngle(l.getTerminal2(), 1.01333094 * vbase, Math.toDegrees(-0.00177645));

        l = createLine(network, "L2-3.2", "VL", "B2", "VL", "B3.2", 0.005 / zpu, 0.21 / zpu, 0.0, 0.046 / 2 * zpu, 0.0, 0.046 / 2 * zpu);
        setVoltageAndAngle(l.getTerminal2(), 1.01333094 * vbase, Math.toDegrees(-0.00177645));

        l = createLine(network, "L3.3-4", "VL", "B3.3", "VL", "B4", 0.05 / zpu, 0.13 / zpu, 0.0, 0.031 / 2 * zpu, 0.0, 0.031 / 2 * zpu);
        setVoltageAndAngle(l.getTerminal1(), 1.01333094 * vbase, Math.toDegrees(-0.00177645));
        setVoltageAndAngle(l.getTerminal2(), 1.01329069 * vbase, Math.toDegrees(-0.02239499));

        l = createLine(network, "L3.4-5", "VL", "B3.4", "VL", "B5", 0.001 / zpu, 0.023 / zpu, 0.0, 0.022 / 2 * zpu, 0.0, 0.022 / 2 * zpu);
        setVoltageAndAngle(l.getTerminal1(), 1.01333094 * vbase, Math.toDegrees(-0.00177645));
        setVoltageAndAngle(l.getTerminal2(), 1.0 * vbase, Math.toDegrees(0.00865175));

        l = createLine(network, "L4-5", "VL", "B4", "VL", "B5", 0.05 / zpu, 0.045 / zpu, 0.0, 0.051 / 2 * zpu, 0.0, 0.051 / 2 * zpu);

        createZeroImpedanceLine(network, "L3.1-3.2", "VL", "B3.1", "VL", "B3.2");
        createZeroImpedanceLine(network, "L3.1-3.3", "VL", "B3.1", "VL", "B3.3");
        createZeroImpedanceLine(network, "L3.2-3.3", "VL", "B3.2", "VL", "B3.3");
        createZeroImpedanceLine(network, "L3.2-3.4", "VL", "B3.2", "VL", "B3.4");
        l = createZeroImpedanceLine(network, "L3.2-3.5", "VL", "B3.2", "VL", "B3.5");
        setVoltageAndAngle(l.getTerminal2(), 1.01333094 * vbase, Math.toDegrees(-0.00177645));

        createZeroImpedanceLine(network, "L3.3-3.4", "VL", "B3.3", "VL", "B3.4");
        createZeroImpedanceLine(network, "L3.4-3.5", "VL", "B3.4", "VL", "B3.5");

        createLoadAndSetTerminalPQ(vl, "LD2", "B2", 15.0, 10.0);
        createLoadAndSetTerminalPQ(vl, "LD4", "B4", 30.0, -50.0);
        createLoadAndSetTerminalPQ(vl, "LD3.1", "B3.1", 35.0, 30.0);
        createLoadAndSetTerminalPQ(vl, "LD3.3", "B3.3", 10.0, 12.0);
        createLoadAndSetTerminalPQ(vl, "LD3.4", "B3.4", 10.0, -50.0);
        createLoadAndSetTerminalPQ(vl, "LD3.5", "B3.5", 5.0, -50.0);

        createGeneratorAndSetTerminalPQ(vl, "G1", "B1", true, 115.0, 0.490536, -16.774788);
        createGeneratorAndSetTerminalPQ(vl, "G2", "B2", true, 115.0, 15.488468, 0.613118);
        createGeneratorAndSetTerminalPQ(vl, "G5", "B5", true, 115.0, 60.486267, -110.743699);
        createGeneratorAndSetTerminalPQ(vl, "G3.2", "B3.2", false, 115.0, 30.0, -18.0);

        createShuntCompensator(vl, "SC3.1", "B3.1", 25.0 / Math.pow(vbase, 2));
        createShuntCompensator(vl, "SC3.4", "B3.4", 5.00 / Math.pow(vbase, 2));

        return network;
    }

    private void disconnectLine(Network network) {
        Line l = network.getLine("L3.1-3.2");
        l.getTerminal1().disconnect();
    }

    private void splitNetwork(Network network) {
        double vbase = 115.0;

        setGeneratorTargetPQAndTerminalPQ(network.getGenerator("G1"), 1.180568, 18.060835);
        setGeneratorTargetPQAndTerminalPQ(network.getGenerator("G2"), 16.128860, -4.462291);
        setGeneratorTargetPQAndTerminalPQ(network.getGenerator("G5"), 61.123773, -134.882226);

        setAngle(network.getLine("L1-2").getTerminal2(), Math.toDegrees(0.02318848));
        setVoltageAndAngle(network.getLine("L4-5").getTerminal1(), 0.99583652 * vbase, Math.toDegrees(0.01323237));
        setAngle(network.getLine("L4-5").getTerminal2(), Math.toDegrees(0.06442617));
        setVoltageAndAngle(network.getLine("L3.1-3.3").getTerminal1(), 0.98359818 * vbase, Math.toDegrees(-0.01808407));
        setVoltageAndAngle(network.getLine("L3.1-3.3").getTerminal2(), 0.98359818 * vbase, Math.toDegrees(-0.01808407));
        setVoltageAndAngle(network.getLine("L3.2-3.4").getTerminal1(), 1.01844096 * vbase, Math.toDegrees(0.06264234));
        setVoltageAndAngle(network.getLine("L3.3-3.4").getTerminal2(), 1.01844096 * vbase, Math.toDegrees(0.06264234));
        setVoltageAndAngle(network.getLine("L3.4-3.5").getTerminal2(), 1.01844096 * vbase, Math.toDegrees(0.06264234));

        network.getLine("L3.1-3.2").getTerminal1().disconnect();
        network.getLine("L3.1-3.2").getTerminal2().disconnect();
        network.getLine("L3.2-3.3").getTerminal1().disconnect();
        network.getLine("L3.2-3.3").getTerminal2().disconnect();
        network.getLine("L3.3-3.4").getTerminal1().disconnect();
        network.getLine("L3.3-3.4").getTerminal2().disconnect();
    }

    private Network createT2wtZ0Network() {
        double sbase = 100.0;
        double vbase = 115.0;
        // Impedances are expressed in per-unit values,
        // we convert to engineering units when adding elements to IIDM
        double zpu = sbase / Math.pow(vbase, 2);

        Network network = Network.create("test", "test");
        Substation s = network.newSubstation()
                .setId("S")
                .setCountry(Country.FR)
                .add();
        VoltageLevel vl = s.newVoltageLevel()
                .setId("VL")
                .setNominalV(115.0)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        createBus(vl, "B1");
        createBus(vl, "B2");
        createBus(vl, "B3.1");
        createBus(vl, "B3.2");

        // Add a loop (a line with same bus at both ends)
        Line l = createLine(network, "L1-2", "VL", "B1", "VL", "B2", 0.0, 0.001 / zpu, 0.0, 0.0, 0.0, 0.0);
        setVoltageAndAngle(l.getTerminal1(), 115.0, 0.0);
        setVoltageAndAngle(l.getTerminal2(), 114.99770, -0.002864788976);

        l = createLine(network, "L2-3.1", "VL", "B2", "VL", "B3.1", 0.0, 0.001 / zpu, 0.0, 0.0, 0.0, 0.0);
        setVoltageAndAngle(l.getTerminal2(), 114.995400000000, -0.005729577951);

        TwoWindingsTransformer t = createZeroImpedanceTwoWindingsTransformer(network, s, "T2wt-3.1-3.2-1", "VL", "B3.1", "VL", "B3.2", 1.01, 2.0);
        setVoltageAndAngle(t.getTerminal2(), 116.145354000000, 1.994270422049);

        // Zero impedance twoWindingsTransformer only connected on side 1
        t = createZeroImpedanceTwoWindingsTransformer(network, s, "T2wt-3.1-3.2-2", "VL", "B3.1", "VL", "B3.2", 1.01, 2.0);
        t.getTerminal2().disconnect();

        createGeneratorAndSetTerminalPQ(vl, "G1", "B1", true, 115.0, 0.490536, -16.774788);
        createLoadAndSetTerminalPQ(vl, "LD3.2", "B3.2", 5.0, 2.0);

        return network;
    }

    private Network createDlZ0Network() {
        double sbase = 100.0;
        double vbase = 115.0;
        // Impedances are expressed in per-unit values,
        // we convert to engineering units when adding elements to IIDM
        double zpu = sbase / Math.pow(vbase, 2);

        Network network = Network.create("test", "test");
        Substation s = network.newSubstation()
                .setId("S")
                .setCountry(Country.FR)
                .add();
        VoltageLevel vl = s.newVoltageLevel()
                .setId("VL")
                .setNominalV(115.0)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        createBus(vl, "B1");
        createBus(vl, "B2");
        createBus(vl, "B3.1");
        createBus(vl, "B3.2");

        // Add a loop (a line with same bus at both ends)
        Line l = createLine(network, "L1-2", "VL", "B1", "VL", "B2", 0.0, 0.001 / zpu, 0.0, 0.0, 0.0, 0.0);
        setVoltageAndAngle(l.getTerminal1(), 115.0, 0.0);
        setVoltageAndAngle(l.getTerminal2(), 115.5, 0.0);

        l = createLine(network, "L2-3.1", "VL", "B2", "VL", "B3.1", 0.0, 0.001 / zpu, 0.0, 0.0, 0.0, 0.0);
        setVoltageAndAngle(l.getTerminal2(), 116.0, 0.0);

        l = createZeroImpedanceLine(network, "L3.1-3.2", "VL", "B3.1", "VL", "B3.2");
        setVoltageAndAngle(l.getTerminal2(), 116.0, 0.0);

        createZeroImpedanceDanglingLine(vl, "DL3.1", "B3.1", 2.0, 1.0, true, 116.0, 5.0, 1.0);

        createGeneratorAndSetTerminalPQ(vl, "G1", "B1", true, 115.0, 0.490536, -16.774788);
        createLoadAndSetTerminalPQ(vl, "LD3.2", "B3.2", 3.0, 1.0);

        return network;
    }

    // all threeWindings legs zero impedance
    private Network createT3wtZ0Network(double v0base) {
        double sbase = 100.0;
        double vbase = 115.0;
        // Impedances are expressed in per-unit values,
        // we convert to engineering units when adding elements to IIDM
        double zpu = sbase / Math.pow(vbase, 2);

        Network network = Network.create("test", "test");
        Substation s = network.newSubstation()
                .setId("S")
                .setCountry(Country.FR)
                .add();
        VoltageLevel vl = s.newVoltageLevel()
                .setId("VL")
                .setNominalV(115.0)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        createBus(vl, "B1");
        createBus(vl, "B2");
        createBus(vl, "B3.1");
        createBus(vl, "B3.2");
        createBus(vl, "B3.3");

        Line l = createLine(network, "L1-2", "VL", "B1", "VL", "B2", 0.0, 0.001 / zpu, 0.0, 0.0, 0.0, 0.0);
        setVoltageAndAngle(l.getTerminal1(), 115.0, 0.0);
        setVoltageAndAngle(l.getTerminal2(), 114.996550, -0.003437746771);

        l = createLine(network, "L2-3.1", "VL", "B2", "VL", "B3.1", 0.0, 0.001 / zpu, 0.0, 0.0, 0.0, 0.0);
        setVoltageAndAngle(l.getTerminal2(), 114.99310, -0.006875493542);

        ThreeWindingsTransformer t = createZeroImpedanceThreeWindingsTransformer(network, s, "T3wt3.1-3.2-3.3",
            "VL", "B3.1", "VL", "B3.2", "VL", "B3.3", v0base, 1.01, 0.0, 0.99, 2.0, 0.98, 3.0);
        setVoltageAndAngle(t.getLeg2().getTerminal(), 117.316192929293, -2.006875493542);
        setVoltageAndAngle(t.getLeg3().getTerminal(), 118.513296938775, -3.006875493542);

        createGeneratorAndSetTerminalPQ(vl, "G1", "B1", true, 115.0, 0.490536, -16.774788);
        createLoadAndSetTerminalPQ(vl, "LD3.2", "B3.2", 4.0, 2.0);
        createLoadAndSetTerminalPQ(vl, "LD3.3", "B3.3", 2.0, 1.0);

        return network;
    }

    // Leg1 and Leg2 zero impedance
    private Network createT3wt12Z0Network() {
        double sbase = 100.0;
        double v0base = 1.0;
        double zpu = sbase / Math.pow(v0base, 2);

        Network network = createT3wtZ0Network(v0base);
        ThreeWindingsTransformer t = network.getThreeWindingsTransformer("T3wt3.1-3.2-3.3");
        t.getLeg3().setX(0.001 / zpu);

        Line l = network.getLine("L1-2");
        setVoltageAndAngle(l.getTerminal1(), 115.0, 0.0);
        setVoltageAndAngle(l.getTerminal2(), 114.996549724539, -0.003437849912);

        setVoltageAndAngle(t.getLeg1().getTerminal(), 114.993099863116, -0.006875906093);
        setVoltageAndAngle(t.getLeg2().getTerminal(), 117.316192789643, -2.006875906093);
        setVoltageAndAngle(t.getLeg3().getTerminal(), 118.512135237431, -3.007999827811);

        return network;
    }

    // Leg1 zero impedance
    private Network createT3wt1Z0Network() {
        double sbase = 100.0;
        double v0base = 1.0;
        double zpu = sbase / Math.pow(v0base, 2);

        Network network = createT3wtZ0Network(v0base);
        ThreeWindingsTransformer t = network.getThreeWindingsTransformer("T3wt3.1-3.2-3.3");
        t.getLeg2().setX(0.001 / zpu);
        t.getLeg3().setX(0.001 / zpu);

        Line l = network.getLine("L1-2");
        setVoltageAndAngle(l.getTerminal1(), 115.0, 0.0);
        setVoltageAndAngle(l.getTerminal2(), 114.996550659518, -0.003437849875);

        setVoltageAndAngle(t.getLeg1().getTerminal(), 114.993101733073, -0.006875905925);
        setVoltageAndAngle(t.getLeg2().getTerminal(), 117.313894445243, -2.009123420460);
        setVoltageAndAngle(t.getLeg3().getTerminal(), 118.512137649341, -3.008000520958);

        return network;
    }

    // all legs zero impedance, leg3 disconnected
    private Network createT3wt12Z03AntennaNetwork() {

        double v0base = 1.0;
        Network network = createT3wtZ0Network(v0base);
        ThreeWindingsTransformer t3 = network.getThreeWindingsTransformer("T3wt3.1-3.2-3.3");
        t3.getLeg3().getTerminal().disconnect();

        Substation s = network.getSubstation("S");
        TwoWindingsTransformer t2 = createZeroImpedanceTwoWindingsTransformer(network, s, "T2wt3.1-3.3", "VL", "B3.1", "VL", "B3.3", 0.98, 3.0);

        Line l = network.getLine("L1-2");
        setVoltageAndAngle(l.getTerminal1(), 115.0, 0.0);
        setVoltageAndAngle(l.getTerminal2(), 114.996550, -0.003437746771);

        setVoltageAndAngle(t3.getLeg1().getTerminal(), 114.99310, -0.006875493542);
        setVoltageAndAngle(t3.getLeg2().getTerminal(), 117.316192929293, -2.006875493542);
        setVoltageAndAngle(t2.getTerminal2(), 112.693238000000, 2.993124506458);

        return network;
    }

    // leg1 zero impedance, leg2 with impedance, leg3 zero impedance and disconnected
    private Network createT3wt1Z03AntennaNetwork() {
        double sbase = 100.0;
        double v0base = 1.0;
        double zpu = sbase / Math.pow(v0base, 2);

        Network network = createT3wtZ0Network(v0base);
        ThreeWindingsTransformer t3 = network.getThreeWindingsTransformer("T3wt3.1-3.2-3.3");
        t3.getLeg2().setX(0.001 / zpu);
        t3.getLeg3().getTerminal().disconnect();

        Substation s = network.getSubstation("S");
        TwoWindingsTransformer t2 = createZeroImpedanceTwoWindingsTransformer(network, s, "T2wt3.1-3.3", "VL", "B3.1", "VL", "B3.3", 0.98, 3.0);

        Line l = network.getLine("L1-2");
        setVoltageAndAngle(l.getTerminal1(), 115.0, 0.0);
        setVoltageAndAngle(l.getTerminal2(), 114.996548964655, -0.003437849939);

        setVoltageAndAngle(t3.getLeg1().getTerminal(), 114.993098343347, -0.006875906211);
        setVoltageAndAngle(t3.getLeg2().getTerminal(), 117.313890747101, -2.009122906330);
        setVoltageAndAngle(t2.getTerminal2(), 112.693236376480, 2.993124093789);

        return network;
    }

    // leg1 and leg2 with impedance, leg3 zero impedance and disconnected
    private Network createT3wt3AntennaNetwork() {
        double sbase = 100.0;
        double v0base = 1.0;
        double zpu = sbase / Math.pow(v0base, 2);

        Network network = createT3wtZ0Network(v0base);
        ThreeWindingsTransformer t3 = network.getThreeWindingsTransformer("T3wt3.1-3.2-3.3");
        t3.getLeg1().setX(0.001 / zpu);
        t3.getLeg2().setX(0.001 / zpu);
        t3.getLeg3().getTerminal().disconnect();

        Substation s = network.getSubstation("S");
        TwoWindingsTransformer t2 = createZeroImpedanceTwoWindingsTransformer(network, s, "T2wt3.1-3.3", "VL", "B3.1", "VL", "B3.3", 0.98, 3.0);

        Line l = network.getLine("L1-2");
        setVoltageAndAngle(l.getTerminal1(), 115.0, 0.0);
        setVoltageAndAngle(l.getTerminal2(), 114.996548778883, -0.003437849945);

        setVoltageAndAngle(t3.getLeg1().getTerminal(), 114.993097971804, -0.006875906232);
        setVoltageAndAngle(t3.getLeg2().getTerminal(), 117.311589675185, -2.011370018102);
        setVoltageAndAngle(t2.getTerminal2(), 112.693236012368, 2.993124093768);

        return network;
    }

    // leg1 zero impedance, leg2 and leg3 zero impedance and disconnected
    private Network createT3wt1Z023AntennaNetwork() {
        double v0base = 1.0;

        Network network = createT3wtZ0Network(v0base);
        ThreeWindingsTransformer t3 = network.getThreeWindingsTransformer("T3wt3.1-3.2-3.3");
        t3.getLeg2().getTerminal().disconnect();
        t3.getLeg3().getTerminal().disconnect();

        Substation s = network.getSubstation("S");
        TwoWindingsTransformer t23 = createZeroImpedanceTwoWindingsTransformer(network, s, "T2wt3.1-3.3", "VL", "B3.1", "VL", "B3.3", 0.98, 3.0);
        TwoWindingsTransformer t22 = createZeroImpedanceTwoWindingsTransformer(network, s, "T2wt3.1-3.2", "VL", "B3.1", "VL", "B3.2", 0.99, 2.0);

        Line l = network.getLine("L1-2");
        setVoltageAndAngle(l.getTerminal1(), 115.0, 0.0);
        setVoltageAndAngle(l.getTerminal2(), 114.996550, -0.003437746771);

        setVoltageAndAngle(t3.getLeg1().getTerminal(), 114.99310, -0.006875493542);
        setVoltageAndAngle(t22.getTerminal2(), 113.8431690, 1.993124506458);
        setVoltageAndAngle(t23.getTerminal2(), 112.6932380, 2.993124506458);

        return network;
    }

    // leg1 with impedance, leg2 and leg3 zero impedance and disconnected
    private Network createT3wt23AntennaNetwork() {
        double sbase = 100.0;
        double v0base = 1.0;
        double zpu = sbase / Math.pow(v0base, 2);

        Network network = createT3wtZ0Network(v0base);
        ThreeWindingsTransformer t3 = network.getThreeWindingsTransformer("T3wt3.1-3.2-3.3");
        t3.getLeg1().setX(0.001 / zpu);
        t3.getLeg2().getTerminal().disconnect();
        t3.getLeg3().getTerminal().disconnect();

        Substation s = network.getSubstation("S");
        TwoWindingsTransformer t23 = createZeroImpedanceTwoWindingsTransformer(network, s, "T2wt3.1-3.3", "VL", "B3.1", "VL", "B3.3", 0.98, 3.0);
        TwoWindingsTransformer t22 = createZeroImpedanceTwoWindingsTransformer(network, s, "T2wt3.1-3.2", "VL", "B3.1", "VL", "B3.2", 0.99, 2.0);

        Line l = network.getLine("L1-2");
        setVoltageAndAngle(l.getTerminal1(), 115.0, 0.0);
        setVoltageAndAngle(l.getTerminal2(), 114.996552773360, -0.003437849815);

        setVoltageAndAngle(t3.getLeg1().getTerminal(), 114.993105960715, -0.006875905722);
        setVoltageAndAngle(t22.getTerminal2(), 113.843174901108, 1.993124094278);
        setVoltageAndAngle(t23.getTerminal2(), 112.693243841501, 2.993124094278);

        return network;
    }

    private Network createLccHvdcNetwork() {
        double sbase = 100.0;
        double v0base = 1.0;
        double zpu = sbase / Math.pow(v0base, 2);

        Network network = Network.create("test", "test");
        Substation s = network.newSubstation()
                .setId("S")
                .setCountry(Country.FR)
                .add();
        VoltageLevel vl = s.newVoltageLevel()
                .setId("VL")
                .setNominalV(115.0)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        createBus(vl, "B1");
        createBus(vl, "B2");
        createBus(vl, "B3");
        createBus(vl, "B4");
        createBus(vl, "B5");

        Line l = createLine(network, "L1-2", "VL", "B1", "VL", "B2", 0.0, 0.001 / zpu, 0.0, 0.0, 0.0, 0.0);
        setVoltageAndAngle(l.getTerminal1(), 115.0, 0.0);
        setVoltageAndAngle(l.getTerminal2(), 115.000098275729, -0.000002551542);

        l = createLine(network, "L2-3", "VL", "B2", "VL", "B3", 0.0, 0.001 / zpu, 0.0, 0.0, 0.0, 0.0);
        setVoltageAndAngle(l.getTerminal2(), 115.000095175014, -0.000004756494);

        l = createLine(network, "L3-4", "VL", "B3", "VL", "B4", 0.0, 0.001 / zpu, 0.0, 0.0, 0.0, 0.0);
        setVoltageAndAngle(l.getTerminal2(), 115.000092604017, -0.000005661730);

        l = createLine(network, "L2-5", "VL", "B2", "VL", "B5", 0.0, 0.001 / zpu, 0.0, 0.0, 0.0, 0.0);
        setVoltageAndAngle(l.getTerminal2(), 115.00020, -0.000002551542);

        createGeneratorAndSetTerminalPQ(vl, "G1", "B1", true, 115.0, 0.490536, -16.774788);
        createLoadAndSetTerminalPQ(vl, "LD2", "B2", 8.0, 4.0);
        createLoadAndSetTerminalPQ(vl, "LD4", "B4", 50.0, 20.0);

        createLccHvdc(network, "HVDC", 30, HvdcLine.ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER, vl, "B3", 115.0001, true, vl, "B4");
        createStaticVarCompensator(vl, "SVC5", "B5", 115.0002, RegulationMode.VOLTAGE);

        return network;
    }

    private Network createSvcHvdcNetwork() {
        double sbase = 100.0;
        double v0base = 1.0;
        double zpu = sbase / Math.pow(v0base, 2);

        Network network = Network.create("test", "test");
        Substation s = network.newSubstation()
                .setId("S")
                .setCountry(Country.FR)
                .add();
        VoltageLevel vl = s.newVoltageLevel()
                .setId("VL")
                .setNominalV(115.0)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        createBus(vl, "B1");
        createBus(vl, "B2");
        createBus(vl, "B3");
        createBus(vl, "B4");
        createBus(vl, "B5");

        Line l = createLine(network, "L1-2", "VL", "B1", "VL", "B2", 0.0, 0.001 / zpu, 0.0, 0.0, 0.0, 0.0);
        setVoltageAndAngle(l.getTerminal1(), 115.0, 0.0);
        setVoltageAndAngle(l.getTerminal2(), 115.000099884058, -0.000002551543);

        l = createLine(network, "L2-3", "VL", "B2", "VL", "B3", 0.0, 0.001 / zpu, 0.0, 0.0, 0.0, 0.0);
        setVoltageAndAngle(l.getTerminal2(), 115.00010, -0.000004756494);

        l = createLine(network, "L3-4", "VL", "B3", "VL", "B4", 0.0, 0.001 / zpu, 0.0, 0.0, 0.0, 0.0);
        setVoltageAndAngle(l.getTerminal2(), 115.000098260870, -0.000008222402);

        l = createLine(network, "L2-5", "VL", "B2", "VL", "B5", 0.0, 0.001 / zpu, 0.0, 0.0, 0.0, 0.0);
        setVoltageAndAngle(l.getTerminal2(), 115.00020, -0.000002551543);

        createGeneratorAndSetTerminalPQ(vl, "G1", "B1", true, 115.0, 0.490536, -16.774788);
        createLoadAndSetTerminalPQ(vl, "LD2", "B2", 8.0, 4.0);
        createLoadAndSetTerminalPQ(vl, "LD4", "B4", 50.0, 20.0);

        createSvcHvdc(network, "HVDC", 30, HvdcLine.ConvertersMode.SIDE_1_INVERTER_SIDE_2_RECTIFIER, vl, "B3", 115.0001, true, vl, "B4");
        createStaticVarCompensator(vl, "SVC5", "B5", 115.0002, RegulationMode.VOLTAGE);

        return network;
    }

    private ValidationConfig createValidationConfig() {
        InMemoryPlatformConfig platformConfig = new InMemoryPlatformConfig(fileSystem);
        ValidationConfig config = ValidationConfig.load(platformConfig);
        config.setVerbose(true);
        config.setLoadFlowParameters(new LoadFlowParameters());
        config.setThreshold(0.01f);
        config.setOkMissingValues(false);
        return config;
    }

    private static Bus createBus(VoltageLevel vl, String id) {
        return vl.getBusBreakerView().newBus()
            .setId(id)
            .add();
    }

    private static void setVoltageAndAngle(Terminal terminal, double v, double angle) {
        terminal.getBusBreakerView().getBus()
            .setV(v)
            .setAngle(angle);
    }

    private static void setAngle(Terminal terminal, double angle) {
        terminal.getBusBreakerView().getBus().setAngle(angle);
    }

    private static void setGeneratorTargetPQAndTerminalPQ(Generator generator, double targetP, double targetQ) {
        generator.setTargetP(targetP).setTargetQ(targetQ);
        generator.getTerminal().setP(-targetP).setQ(-targetQ);
    }

    private static Line createZeroImpedanceLine(Network network, String id, String vl1, String bus1, String vl2, String bus2) {
        return createLine(network, id, vl1, bus1, vl2, bus2, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
    }

    private static Line createLine(Network network, String id, String vl1, String bus1, String vl2, String bus2,
        double r, double x, double g1, double b1, double g2, double b2) {
        return network.newLine()
            .setId(id)
            .setVoltageLevel1(vl1)
            .setConnectableBus1(bus1)
            .setBus1(bus1)
            .setVoltageLevel2(vl2)
            .setConnectableBus2(bus2)
            .setBus2(bus2)
            .setR(r)
            .setX(x)
            .setG1(g1)
            .setB1(b1)
            .setG2(g2)
            .setB2(b2)
            .add();
    }

    private static TwoWindingsTransformer createZeroImpedanceTwoWindingsTransformer(Network network, Substation s,
        String id, String vl1, String bus1, String vl2, String bus2, double rho, double alpha) {

        TwoWindingsTransformer t2wt = s.newTwoWindingsTransformer()
            .setId(id)
            .setVoltageLevel1(vl1)
            .setConnectableBus1(bus1)
            .setBus1(bus1)
            .setRatedU1(network.getVoltageLevel(vl1).getNominalV())
            .setVoltageLevel2(vl2)
            .setConnectableBus2(bus2)
            .setBus2(bus2)
            .setRatedU2(network.getVoltageLevel(vl2).getNominalV())
            .setR(0.0)
            .setX(0.0)
            .setG(0.0)
            .setB(0.0)
            .add();

        if (alpha != 0.0) {
            t2wt.newPhaseTapChanger()
                .setLowTapPosition(0)
                .setTapPosition(0)
                .beginStep()
                .setRho(rho != 0.0 ? rho : 1.0)
                .setAlpha(alpha)
                .setR(0.0)
                .setX(0.0)
                .setG(0.0)
                .setB(0.0)
                .endStep()
                .add();
        } else if (rho != 0.0) {
            t2wt.newRatioTapChanger()
                .setLowTapPosition(0)
                .setTapPosition(0)
                .beginStep()
                .setRho(rho)
                .setR(0.0)
                .setX(0.0)
                .setG(0.0)
                .setB(0.0)
                .endStep()
                .add();
        }

        return t2wt;
    }

    private static ThreeWindingsTransformer createZeroImpedanceThreeWindingsTransformer(Network network, Substation s,
        String id, String vl1, String bus1, String vl2, String bus2, String vl3, String bus3, double v0base,
        double rho1, double alpha1, double rho2, double alpha2, double rho3, double alpha3) {

        ThreeWindingsTransformer t3wt = s.newThreeWindingsTransformer()
            .setId(id)
            .setRatedU0(v0base)
            .newLeg1()
            .setVoltageLevel(vl1)
            .setConnectableBus(bus1)
            .setBus(bus1)
            .setRatedU(network.getVoltageLevel(vl1).getNominalV())
            .setR(0.0)
            .setX(0.0)
            .setG(0.0)
            .setB(0.0)
            .add()
            .newLeg2()
            .setVoltageLevel(vl2)
            .setConnectableBus(bus2)
            .setBus(bus2)
            .setRatedU(network.getVoltageLevel(vl2).getNominalV())
            .setR(0.0)
            .setX(0.0)
            .setG(0.0)
            .setB(0.0)
            .add()
            .newLeg3()
            .setVoltageLevel(vl3)
            .setConnectableBus(bus3)
            .setBus(bus3)
            .setRatedU(network.getVoltageLevel(vl3).getNominalV())
            .setR(0.0)
            .setX(0.0)
            .setG(0.0)
            .setB(0.0)
            .add()
            .add();

        if (alpha1 != 0.0) {
            createLegPhaseTapChanger(t3wt.getLeg1(), rho1, alpha1);
        } else if (rho1 != 0.0) {
            createLegRatioTapChanger(t3wt.getLeg1(), rho1);
        }
        if (alpha2 != 0.0) {
            createLegPhaseTapChanger(t3wt.getLeg2(), rho2, alpha2);
        } else if (rho2 != 0.0) {
            createLegRatioTapChanger(t3wt.getLeg2(), rho2);
        }
        if (alpha3 != 0.0) {
            createLegPhaseTapChanger(t3wt.getLeg3(), rho3, alpha3);
        } else if (rho3 != 0.0) {
            createLegRatioTapChanger(t3wt.getLeg3(), rho3);
        }

        return t3wt;
    }

    private static void createLegRatioTapChanger(Leg leg, double rho) {
        leg.newRatioTapChanger()
            .setLowTapPosition(0)
            .setTapPosition(0)
            .beginStep()
            .setRho(rho)
            .setR(0.0)
            .setX(0.0)
            .setG(0.0)
            .setB(0.0)
            .endStep()
            .add();
    }

    private static void createLegPhaseTapChanger(Leg leg, double rho, double alpha) {
        leg.newPhaseTapChanger()
            .setLowTapPosition(0)
            .setTapPosition(0)
            .beginStep()
            .setRho(rho != 0.0 ? rho : 1.0)
            .setAlpha(alpha)
            .setR(0.0)
            .setX(0.0)
            .setG(0.0)
            .setB(0.0)
            .endStep()
            .add();
    }

    private static DanglingLine createZeroImpedanceDanglingLine(VoltageLevel vl, String id, String bus,
        double p0, double q0, boolean voltageRegulatorOn, double targetV, double targetP, double targetQ) {
        return vl.newDanglingLine()
            .setId(id)
            .setConnectableBus(bus)
            .setBus(bus)
            .setP0(p0)
            .setQ0(q0)
            .setR(0.0)
            .setX(0.0)
            .setG(0.0)
            .setB(0.0)
            .newGeneration()
            .setMinP(-50.0)
            .setMaxP(100.0)
            .setTargetV(targetV)
            .setTargetP(targetP)
            .setTargetQ(targetQ)
            .setVoltageRegulationOn(voltageRegulatorOn)
            .add()
            .add();
    }

    private static Load createLoadAndSetTerminalPQ(VoltageLevel vl, String id, String bus, double p, double q) {
        Load ld = vl.newLoad()
            .setId(id)
            .setConnectableBus(bus)
            .setBus(bus)
            .setP0(p)
            .setQ0(q)
            .add();
        ld.getTerminal().setP(p).setQ(q);
        return ld;
    }

    private static Generator createGeneratorAndSetTerminalPQ(VoltageLevel vl, String id, String bus,
        boolean voltageRegulatorOn, double targetV, double targetP, double targetQ) {
        Generator g = vl.newGenerator()
            .setId(id)
            .setConnectableBus(bus)
            .setBus(bus)
            .setVoltageRegulatorOn(voltageRegulatorOn)
            .setMinP(-9999.99)
            .setMaxP(9999.99)
            .setTargetV(targetV)
            .setTargetP(targetP)
            .setTargetQ(targetQ)
            .add();
        g.getTerminal().setP(-targetP).setQ(-targetQ);
        return g;
    }

    private static ShuntCompensator createShuntCompensator(VoltageLevel vl, String id, String bus, double bPerSection) {
        return vl.newShuntCompensator()
            .setId(id)
            .setConnectableBus(bus)
            .setBus(bus)
            .setSectionCount(1)
            .setVoltageRegulatorOn(false)
            .newLinearModel()
            .setBPerSection(bPerSection)
            .setMaximumSectionCount(1)
            .add()
            .add();
    }

    private static StaticVarCompensator createStaticVarCompensator(VoltageLevel vl, String id,
        String bus, double targetV, RegulationMode regulationMode) {
        return vl.newStaticVarCompensator()
            .setId(id)
            .setConnectableBus(bus)
            .setBus(bus)
            .setBmin(-0.1512)
            .setBmax(0.756)
            .setVoltageSetpoint(targetV)
            .setReactivePowerSetpoint(0.0)
            .setRegulationMode(regulationMode)
            .add();
    }

    private static HvdcLine createLccHvdc(Network network, String id, double targetP,
        HvdcLine.ConvertersMode convertersMode, VoltageLevel vl1, String bus1,
        double targetV, boolean voltageRegulatorOn, VoltageLevel vl2, String bus2) {

        LccConverterStation lcc1 = vl1.newLccConverterStation()
            .setId(id + "-lcc1")
            .setBus(bus1)
            .setConnectableBus(bus1)
            .setLossFactor(2)
            .setPowerFactor(0.98f)
            .add();

        LccConverterStation lcc2 = vl2.newLccConverterStation()
            .setId(id + "-lcc2")
            .setBus(bus2)
            .setConnectableBus(bus2)
            .setLossFactor(1)
            .setPowerFactor(0.95f)
            .add();

        return network.newHvdcLine()
            .setId(id)
            .setConvertersMode(convertersMode)
            .setConverterStationId1(lcc1.getId())
            .setConverterStationId2(lcc2.getId())
            .setNominalV(vl1.getNominalV())
            .setActivePowerSetpoint(targetP)
            .setMaxP(2 * targetP)
            .setR(0.01)
            .add();
    }

    private static HvdcLine createSvcHvdc(Network network, String id, double targetP,
        HvdcLine.ConvertersMode convertersMode, VoltageLevel vl1, String bus1,
        double targetV, boolean voltageRegulatorOn, VoltageLevel vl2, String bus2) {

        VscConverterStation vsc1 = vl1.newVscConverterStation()
            .setId(id + "-vsc1")
            .setBus(bus1)
            .setConnectableBus(bus1)
            .setLossFactor(2)
            .setVoltageSetpoint(targetV)
            .setReactivePowerSetpoint(0.0)
            .setVoltageRegulatorOn(voltageRegulatorOn)
            .add();

        VscConverterStation vsc2 = vl2.newVscConverterStation()
            .setId(id + "-vsc2")
            .setBus(bus2)
            .setConnectableBus(bus2)
            .setLossFactor(1)
            .setReactivePowerSetpoint(0.0)
            .setVoltageRegulatorOn(false)
            .add();

        return network.newHvdcLine()
            .setId(id)
            .setConvertersMode(convertersMode)
            .setConverterStationId1(vsc1.getId())
            .setConverterStationId2(vsc2.getId())
            .setNominalV(vl1.getNominalV())
            .setActivePowerSetpoint(targetP)
            .setMaxP(2 * targetP)
            .setR(0.01)
            .add();
    }

    private FileSystem fileSystem;
}
