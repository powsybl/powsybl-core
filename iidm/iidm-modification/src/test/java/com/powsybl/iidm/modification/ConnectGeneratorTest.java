/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification;

import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.FourSubstationsNodeBreakerFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
class ConnectGeneratorTest {
    private Network network;
    private Generator g2;
    private Generator g3;

    @BeforeEach
    void setUp() {
        network = FourSubstationsNodeBreakerFactory.create();
        Generator g1 = network.getGenerator("GH1");
        g2 = network.getGenerator("GH2");
        g3 = network.getGenerator("GH3");
        g1.setTargetV(11.);
        g2.setTargetV(22.);
        g3.setTargetV(33.);
        g1.getTerminal().disconnect();
        g2.getTerminal().disconnect();
        network.getVoltageLevel("S1VL2").getBusView().getBus("S1VL2_0").setV(99.);
    }

    @Test
    void testConnectVoltageRegulatorOff() {
        g2.setVoltageRegulatorOn(false);
        new ConnectGenerator(g2.getId()).apply(network);
        assertTrue(g2.getTerminal().isConnected());
        assertEquals(22., g2.getTargetV(), 0.01);
    }

    @Test
    void testWithAlreadyConnectedGenerators() {
        g2.setVoltageRegulatorOn(true);
        new ConnectGenerator(g2.getId()).apply(network);
        assertTrue(g2.getTerminal().isConnected());
        assertEquals(33., g2.getTargetV(), 0.01);
    }

    @Test
    void testWithoutAlreadyConnectedGenerators() {
        g3.getTerminal().disconnect();
        g2.setVoltageRegulatorOn(true);
        new ConnectGenerator(g2.getId()).apply(network);
        assertTrue(g2.getTerminal().isConnected());
        assertEquals(22.0, g2.getTargetV(), 0.01);
    }

    @Test
    void testConnectGeneratorWithWrongTargetV() {
        g2.setVoltageRegulatorOn(true);
        g2.setRegulatingTerminal(g3.getTerminal());
        new ConnectGenerator(g2.getId()).apply(network);
        assertTrue(g2.getTerminal().isConnected());
        assertEquals(33, g2.getTargetV(), 0.01);
    }

    @Test
    void testConnectGeneratorWithWrongTargetV2() {
        // heterogeneous controls not taken into account yet.
        double shuntTargetV = 123;
        network.getShuntCompensatorStream().forEach(sc -> {
            sc.setTargetV(shuntTargetV);
            sc.setTargetDeadband(1);
            sc.setVoltageRegulatorOn(true);
        });
        g2.setVoltageRegulatorOn(true);
        g2.setRegulatingTerminal(g3.getTerminal());
        new ConnectGenerator(g2.getId()).apply(network);
        assertTrue(g2.getTerminal().isConnected());
        assertEquals(33.0, g2.getTargetV(), 0.01);
    }

    @Test
    void testConnectGeneratorWithNoNetworkInformation() {
        g3.setVoltageRegulatorOn(false);
        g2.setVoltageRegulatorOn(false);
        g2.setRegulatingTerminal(g3.getTerminal());
        g2.setTargetV(Double.NaN);
        GeneratorModification.Modifs modifs = new GeneratorModification.Modifs();
        modifs.setVoltageRegulatorOn(true); // no targetV provided!
        modifs.setConnected(true);
        new GeneratorModification(g2.getId(), modifs).apply(network);
        assertTrue(g2.getTerminal().isConnected());
        assertEquals(g2.getRegulatingTerminal().getBusView().getBus().getV(), g2.getTargetV(), 0.01);
    }

    @Test
    void testConnectGeneratorWithTransformers() {
        Network network = Network.read("/testCase_with_transformers.xiidm", getClass().getResourceAsStream("/testCase_with_transformers.xiidm"));

        // These generators are directly connected to main grid: it should remain that way
        List<String> generatorsInitiallyConnected = List.of("ESDCGU1 _generator", "ESDCGN1 _generator", "ESCTGU1 _generator", "ESCTGN1 _generator");
        checkInitialStatusConnected(network, generatorsInitiallyConnected);

        // These generators are directly connected to grid but its terminal is disconnected: they should be reconnected
        List<String> generatorsToConnectTerminal = List.of("ESDDGU1 _generator", "ESDDGN1 _generator");
        checkInitalStatusTerminalDisconnected(network, generatorsToConnectTerminal);

        // These generators are linked to grid through a line that is disconnected: it should remain that way
        List<String> generatorsRemainsDisconnectedToTheMainComponent = List.of("ESCDGU1 _generator", "ESCDGN1 _generator");

        // These generators are connected to grid through 2 transformers that are disconnected: they should be reconnected
        List<String> generatorsToConnectToTheMainComponent = List.of("ESD2GU1 _generator", "ESDTGU1 _generator", "ESD2GN1 _generator", "ESDTGN1 _generator");
        checkInitialStatusDisconnected(network, Stream.concat(generatorsToConnectToTheMainComponent.stream(), generatorsRemainsDisconnectedToTheMainComponent.stream()).collect(Collectors.toList()));

        for (List<String> strings : Arrays.asList(generatorsInitiallyConnected, generatorsToConnectTerminal, generatorsToConnectToTheMainComponent, generatorsRemainsDisconnectedToTheMainComponent)) {
            for (String id : strings) {
                new ConnectGenerator(id).apply(network);
            }
        }
        checkTerminalConnectedButNotInMain(network, generatorsRemainsDisconnectedToTheMainComponent);
        checkInitialStatusConnected(network, generatorsInitiallyConnected);
        checkInitialStatusConnected(network, Stream.concat(generatorsToConnectToTheMainComponent.stream(), generatorsToConnectTerminal.stream()).collect(Collectors.toList()));

    }

    private void checkTerminalConnectedButNotInMain(Network network, List<String> ids) {
        ids.forEach(id -> {
            Generator gen = network.getGenerator(id);
            assertTrue(gen.getTerminal().isConnected());
            assertFalse(gen.getTerminal().getBusBreakerView().getConnectableBus().isInMainSynchronousComponent());
        });
    }

    private void checkInitialStatusConnected(Network network, List<String> ids) {
        ids.forEach(id -> {
            Generator gen = network.getGenerator(id);
            assertTrue(gen.getTerminal().isConnected());
            assertTrue(gen.getTerminal().getBusBreakerView().getConnectableBus().isInMainSynchronousComponent());
        });
    }

    private void checkInitialStatusDisconnected(Network network, List<String> ids) {
        ids.forEach(id -> {
            Generator gen = network.getGenerator(id);
            assertFalse(gen.getTerminal().isConnected());
            assertFalse(gen.getTerminal().getBusBreakerView().getConnectableBus().isInMainSynchronousComponent());
        });
    }

    private void checkInitalStatusTerminalDisconnected(Network network, List<String> ids) {
        ids.forEach(id -> {
            Generator gen = network.getGenerator(id);
            assertFalse(gen.getTerminal().isConnected());
            assertTrue(gen.getTerminal().getBusBreakerView().getConnectableBus().isInMainSynchronousComponent());
        });
    }

}
