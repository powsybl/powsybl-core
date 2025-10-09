/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.loadflow.resultscompletion;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.commons.config.InMemoryPlatformConfig;
import com.powsybl.iidm.network.*;
import com.powsybl.loadflow.LoadFlowParameters;
import com.powsybl.loadflow.validation.CandidateComputation;
import com.powsybl.loadflow.validation.ValidationConfig;
import com.powsybl.loadflow.validation.ValidationType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Marcos De Miguel {@literal <demiguelm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
class LoadFlowResultsCompletionZ0FlowsTest {

    @Test
    void originalZ0FlowsCompletion() throws Exception {
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
    void disconnectZ0FlowsCompletion() throws Exception {
        Network network = createNetwork();
        disconnectLine(network);
        completeResults(network);
        assertTrue(validateBuses(network));
    }

    @Test
    void splitZ0FlowsCompletion() throws Exception {
        Network network = createNetwork();
        splitNetwork(network);
        completeResults(network);
        assertTrue(validateBuses(network));
    }

    @BeforeEach
    void setUp() throws IOException {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
    }

    @AfterEach
    void tearDown() throws IOException {
        fileSystem.close();
    }

    private void completeResults(Network network) {
        CandidateComputation resultsCompletion = new LoadFlowResultsCompletion(
                new LoadFlowResultsCompletionParameters(),
                new LoadFlowParameters());
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
        vl.getBusBreakerView().newBus()
                .setId("B1")
                .add();
        vl.getBusBreakerView().newBus()
                .setId("B2")
                .add();
        vl.getBusBreakerView().newBus()
                .setId("B4")
                .add();
        vl.getBusBreakerView().newBus()
                .setId("B5")
                .add();
        vl.getBusBreakerView().newBus()
                .setId("B3.1")
                .add();
        vl.getBusBreakerView().newBus()
                .setId("B3.2")
                .add();
        vl.getBusBreakerView().newBus()
                .setId("B3.3")
                .add();
        vl.getBusBreakerView().newBus()
                .setId("B3.4")
                .add();
        vl.getBusBreakerView().newBus()
                .setId("B3.5")
                .add();
        // Add a loop (a line with same bus at both ends)
        network.newLine()
                .setId("L1-1")
                .setVoltageLevel1("VL")
                .setBus1("B1")
                .setConnectableBus1("B1")
                .setVoltageLevel2("VL")
                .setBus2("B1")
                .setConnectableBus2("B1")
                .setR(10.0)
                .setX(100.0)
                .setG1(0.0)
                .setB1(0.0)
                .setG2(0.0)
                .setB2(0.0)
                .add();
        Line l = network.newLine()
                .setId("L1-2")
                .setVoltageLevel1("VL")
                .setConnectableBus1("B1")
                .setBus1("B1")
                .setVoltageLevel2("VL")
                .setConnectableBus2("B2")
                .setBus2("B2")
                .setR(0.02 / zpu)
                .setX(0.11 / zpu)
                .setG1(0.0 * zpu)
                .setG2(0.0 * zpu)
                .setB1(0.015 / 2 * zpu)
                .setB2(0.015 / 2 * zpu)
                .add();
        l.getTerminal1().getBusBreakerView().getBus()
                .setV(1.0 * vbase)
                .setAngle(Math.toDegrees(0.0));
        l.getTerminal2().getBusBreakerView().getBus()
                .setV(1.0 * vbase)
                .setAngle(Math.toDegrees(-0.00015945));
        l = network.newLine()
                .setId("L1-3.1")
                .setVoltageLevel1("VL")
                .setConnectableBus1("B1")
                .setBus1("B1")
                .setVoltageLevel2("VL")
                .setConnectableBus2("B3.1")
                .setBus2("B3.1")
                .setR(0.01 / zpu)
                .setX(0.09 / zpu)
                .setG1(0.0 * zpu)
                .setG2(0.0 * zpu)
                .setB1(0.023 / 2 * zpu)
                .setB2(0.023 / 2 * zpu)
                .add();
        l.getTerminal2().getBusBreakerView().getBus()
                .setV(1.01333094 * vbase)
                .setAngle(Math.toDegrees(-0.00177645));
        l = network.newLine()
                .setId("L2-3.2")
                .setVoltageLevel1("VL")
                .setConnectableBus1("B2")
                .setBus1("B2")
                .setVoltageLevel2("VL")
                .setConnectableBus2("B3.2")
                .setBus2("B3.2")
                .setR(0.005 / zpu)
                .setX(0.21 / zpu)
                .setG1(0.0 * zpu)
                .setG2(0.0 * zpu)
                .setB1(0.046 / 2 * zpu)
                .setB2(0.046 / 2 * zpu)
                .add();
        l.getTerminal2().getBusBreakerView().getBus()
                .setV(1.01333094 * vbase)
                .setAngle(Math.toDegrees(-0.00177645));
        l = network.newLine()
                .setId("L3.3-4")
                .setVoltageLevel1("VL")
                .setConnectableBus1("B3.3")
                .setBus1("B3.3")
                .setVoltageLevel2("VL")
                .setConnectableBus2("B4")
                .setBus2("B4")
                .setR(0.05 / zpu)
                .setX(0.13 / zpu)
                .setG1(0.0 * zpu)
                .setG2(0.0 * zpu)
                .setB1(0.031 / 2 * zpu)
                .setB2(0.031 / 2 * zpu)
                .add();
        l.getTerminal1().getBusBreakerView().getBus()
                .setV(1.01333094 * vbase)
                .setAngle(Math.toDegrees(-0.00177645));
        l.getTerminal2().getBusBreakerView().getBus()
                .setV(1.01329069 * vbase)
                .setAngle(Math.toDegrees(-0.02239499));
        l = network.newLine()
                .setId("L3.4-5")
                .setVoltageLevel1("VL")
                .setConnectableBus1("B3.4")
                .setBus1("B3.4")
                .setVoltageLevel2("VL")
                .setConnectableBus2("B5")
                .setBus2("B5")
                .setR(0.001 / zpu)
                .setX(0.023 / zpu)
                .setG1(0.0 * zpu)
                .setG2(0.0 * zpu)
                .setB1(0.022 / 2 * zpu)
                .setB2(0.022 / 2 * zpu)
                .add();
        l.getTerminal1().getBusBreakerView().getBus()
                .setV(1.01333094 * vbase)
                .setAngle(Math.toDegrees(-0.00177645));
        l.getTerminal2().getBusBreakerView().getBus()
                .setV(1.0 * vbase)
                .setAngle(Math.toDegrees(0.00865175));
        l = network.newLine()
                .setId("L4-5")
                .setVoltageLevel1("VL")
                .setConnectableBus1("B4")
                .setBus1("B4")
                .setVoltageLevel2("VL")
                .setConnectableBus2("B5")
                .setBus2("B5")
                .setR(0.05 / zpu)
                .setX(0.045 / zpu)
                .setG1(0.0 * zpu)
                .setG2(0.0 * zpu)
                .setB1(0.051 / 2 * zpu)
                .setB2(0.051 / 2 * zpu)
                .add();
        l = network.newLine()
                .setId("L3.1-3.2")
                .setVoltageLevel1("VL")
                .setConnectableBus1("B3.1")
                .setBus1("B3.1")
                .setVoltageLevel2("VL")
                .setConnectableBus2("B3.2")
                .setBus2("B3.2")
                .setR(0.0 / zpu)
                .setX(0.0 / zpu)
                .setG1(0.0 * zpu)
                .setG2(0.0 * zpu)
                .setB1(0.0 / 2 * zpu)
                .setB2(0.0 / 2 * zpu)
                .add();
        l = network.newLine()
                .setId("L3.1-3.3")
                .setVoltageLevel1("VL")
                .setConnectableBus1("B3.1")
                .setBus1("B3.1")
                .setVoltageLevel2("VL")
                .setConnectableBus2("B3.3")
                .setBus2("B3.3")
                .setR(0.0 / zpu)
                .setX(0.0 / zpu)
                .setG1(0.0 * zpu)
                .setG2(0.0 * zpu)
                .setB1(0.0 / 2 * zpu)
                .setB2(0.0 / 2 * zpu)
                .add();
        l = network.newLine()
                .setId("L3.2-3.3")
                .setVoltageLevel1("VL")
                .setConnectableBus1("B3.2")
                .setBus1("B3.2")
                .setVoltageLevel2("VL")
                .setConnectableBus2("B3.3")
                .setBus2("B3.3")
                .setR(0.0 / zpu)
                .setX(0.0 / zpu)
                .setG1(0.0 * zpu)
                .setG2(0.0 * zpu)
                .setB1(0.0 / 2 * zpu)
                .setB2(0.0 / 2 * zpu)
                .add();
        l = network.newLine()
                .setId("L3.2-3.4")
                .setVoltageLevel1("VL")
                .setConnectableBus1("B3.2")
                .setBus1("B3.2")
                .setVoltageLevel2("VL")
                .setConnectableBus2("B3.4")
                .setBus2("B3.4")
                .setR(0.0 / zpu)
                .setX(0.0 / zpu)
                .setG1(0.0 * zpu)
                .setG2(0.0 * zpu)
                .setB1(0.0 / 2 * zpu)
                .setB2(0.0 / 2 * zpu)
                .add();
        l = network.newLine()
                .setId("L3.2-3.5")
                .setVoltageLevel1("VL")
                .setConnectableBus1("B3.2")
                .setBus1("B3.2")
                .setVoltageLevel2("VL")
                .setConnectableBus2("B3.5")
                .setBus2("B3.5")
                .setR(0.0 / zpu)
                .setX(0.0 / zpu)
                .setG1(0.0 * zpu)
                .setG2(0.0 * zpu)
                .setB1(0.0 / 2 * zpu)
                .setB2(0.0 / 2 * zpu)
                .add();
        l.getTerminal2().getBusBreakerView().getBus()
                .setV(1.01333094 * vbase)
                .setAngle(Math.toDegrees(-0.00177645));
        l = network.newLine()
                .setId("L3.3-3.4")
                .setVoltageLevel1("VL")
                .setConnectableBus1("B3.3")
                .setBus1("B3.3")
                .setVoltageLevel2("VL")
                .setConnectableBus2("B3.4")
                .setBus2("B3.4")
                .setR(0.0 / zpu)
                .setX(0.0 / zpu)
                .setG1(0.0 * zpu)
                .setG2(0.0 * zpu)
                .setB1(0.0 / 2 * zpu)
                .setB2(0.0 / 2 * zpu)
                .add();
        l = network.newLine()
                .setId("L3.4-3.5")
                .setVoltageLevel1("VL")
                .setConnectableBus1("B3.4")
                .setBus1("B3.4")
                .setVoltageLevel2("VL")
                .setConnectableBus2("B3.5")
                .setBus2("B3.5")
                .setR(0.0 / zpu)
                .setX(0.0 / zpu)
                .setG1(0.0 * zpu)
                .setG2(0.0 * zpu)
                .setB1(0.0 / 2 * zpu)
                .setB2(0.0 / 2 * zpu)
                .add();
        Load ld = vl.newLoad()
                .setId("LD2")
                .setConnectableBus("B2")
                .setBus("B2")
                .setP0(15.0)
                .setQ0(10.0)
                .add();
        ld.getTerminal().setP(15.0).setQ(10.0);
        ld = vl.newLoad()
                .setId("LD4")
                .setConnectableBus("B4")
                .setBus("B4")
                .setP0(30.0)
                .setQ0(-50.0)
                .add();
        ld.getTerminal().setP(30.0).setQ(-50.0);
        ld = vl.newLoad()
                .setId("LD3.1")
                .setConnectableBus("B3.1")
                .setBus("B3.1")
                .setP0(35.0)
                .setQ0(30.0)
                .add();
        ld.getTerminal().setP(35.0).setQ(30.0);
        ld = vl.newLoad()
                .setId("LD3.3")
                .setConnectableBus("B3.3")
                .setBus("B3.3")
                .setP0(10.0)
                .setQ0(12.0)
                .add();
        ld.getTerminal().setP(10.0).setQ(12.0);
        ld = vl.newLoad()
                .setId("LD3.4")
                .setConnectableBus("B3.4")
                .setBus("B3.4")
                .setP0(10.0)
                .setQ0(-50.0)
                .add();
        ld.getTerminal().setP(10.0).setQ(-50.0);
        ld = vl.newLoad()
                .setId("LD3.5")
                .setConnectableBus("B3.5")
                .setBus("B3.5")
                .setP0(5.0)
                .setQ0(-50.0)
                .add();
        ld.getTerminal().setP(5.0).setQ(-50.0);
        Generator g = vl.newGenerator()
                .setId("G1")
                .setConnectableBus("B1")
                .setBus("B1")
                .setVoltageRegulatorOn(true)
                .setMinP(-9999.99)
                .setMaxP(9999.99)
                .setTargetV(115.0)
                .setTargetP(0.490536)
                .setTargetQ(-16.774788)
                .add();
        g.getTerminal().setP(-0.490536).setQ(16.774788);
        g = vl.newGenerator()
                .setId("G2")
                .setConnectableBus("B2")
                .setBus("B2")
                .setVoltageRegulatorOn(true)
                .setMinP(-9999.99)
                .setMaxP(9999.99)
                .setTargetV(115.0)
                .setTargetP(15.488468)
                .setTargetQ(0.613118)
                .add();
        g.getTerminal().setP(-15.488468).setQ(-0.613118);
        g = vl.newGenerator()
                .setId("G5")
                .setConnectableBus("B5")
                .setBus("B5")
                .setVoltageRegulatorOn(true)
                .setMinP(-9999.99)
                .setMaxP(9999.99)
                .setTargetV(115.0)
                .setTargetP(60.486267)
                .setTargetQ(-110.743699)
                .add();
        g.getTerminal().setP(-60.486267).setQ(110.743699);
        g = vl.newGenerator()
                .setId("G3.2")
                .setConnectableBus("B3.2")
                .setBus("B3.2")
                .setVoltageRegulatorOn(false)
                .setMinP(-9999.99)
                .setMaxP(9999.99)
                .setTargetP(30.0)
                .setTargetQ(-18.0)
                .add();
        g.getTerminal().setP(-30.0).setQ(18.0);
        vl.newShuntCompensator()
                .setId("SC3.1")
                .setConnectableBus("B3.1")
                .setBus("B3.1")
                .setSectionCount(1)
                .setVoltageRegulatorOn(false)
                .newLinearModel()
                    .setBPerSection(25.0 / Math.pow(vbase, 2))
                    .setMaximumSectionCount(1)
                    .add()
                .add();
        vl.newShuntCompensator()
                .setId("SC3.4")
                .setConnectableBus("B3.4")
                .setBus("B3.4")
                .setSectionCount(1)
                .setVoltageRegulatorOn(false)
                .newLinearModel()
                    .setBPerSection(5.00 / Math.pow(vbase, 2))
                    .setMaximumSectionCount(1)
                    .add()
                .add();
        return network;
    }

    private void disconnectLine(Network network) {
        Line l = network.getLine("L3.1-3.2");
        l.getTerminal1().disconnect();
    }

    private void splitNetwork(Network network) {
        double vbase = 115.0;

        network.getGenerator("G1")
                .setTargetP(1.180568)
                .setTargetQ(18.060835);
        network.getGenerator("G1").getTerminal().setP(-1.180568).setQ(-18.060835);
        network.getGenerator("G2")
                .setTargetP(16.128860)
                .setTargetQ(-4.462291);
        network.getGenerator("G2").getTerminal().setP(-16.128860).setQ(4.462291);
        network.getGenerator("G5")
                .setTargetP(61.123773)
                .setTargetQ(-134.882226);
        network.getGenerator("G5").getTerminal().setP(-61.123773).setQ(134.882226);
        network.getLine("L1-2")
                .getTerminal2().getBusBreakerView().getBus()
                .setAngle(Math.toDegrees(0.02318848));
        network.getLine("L4-5")
                .getTerminal1().getBusBreakerView().getBus()
                .setV(0.99583652 * vbase)
                .setAngle(Math.toDegrees(0.01323237));
        network.getLine("L4-5")
                .getTerminal2().getBusBreakerView().getBus()
                .setAngle(Math.toDegrees(0.06442617));
        network.getLine("L3.1-3.3")
                .getTerminal1().getBusBreakerView().getBus()
                .setV(0.98359818 * vbase)
                .setAngle(Math.toDegrees(-0.01808407));
        network.getLine("L3.1-3.3")
                .getTerminal2().getBusBreakerView().getBus()
                .setV(0.98359818 * vbase)
                .setAngle(Math.toDegrees(-0.01808407));
        network.getLine("L3.2-3.4")
                .getTerminal1().getBusBreakerView().getBus()
                .setV(1.01844096 * vbase)
                .setAngle(Math.toDegrees(0.06264234));
        network.getLine("L3.3-3.4")
                .getTerminal2().getBusBreakerView().getBus()
                .setV(1.01844096 * vbase)
                .setAngle(Math.toDegrees(0.06264234));
        network.getLine("L3.4-3.5")
                .getTerminal2().getBusBreakerView().getBus()
                .setV(1.01844096 * vbase)
                .setAngle(Math.toDegrees(0.06264234));
        network.getLine("L3.1-3.2").getTerminal1().disconnect();
        network.getLine("L3.1-3.2").getTerminal2().disconnect();
        network.getLine("L3.2-3.3").getTerminal1().disconnect();
        network.getLine("L3.2-3.3").getTerminal2().disconnect();
        network.getLine("L3.3-3.4").getTerminal1().disconnect();
        network.getLine("L3.3-3.4").getTerminal2().disconnect();
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

    private FileSystem fileSystem;
}
