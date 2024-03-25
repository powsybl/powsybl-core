/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.test.NetworkTest1Factory;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringWriter;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
class MergeTest {

    @Test
    void mergeNodeBreakerTestNPE() throws IOException {
        Network n1 = createNetworkWithDanglingLine("1");
        Network n2 = createNetworkWithDanglingLine("2");

        logVoltageLevel("Network 1 first voltage level", n1.getVoltageLevels().iterator().next());
        Network merge = Network.merge(n1, n2);
        // If we try to get connected components directly on the merged network,
        // A Null Pointer Exception happens in AbstractConnectable.notifyUpdate:
        // There is a CalculatedBus that has a terminal that refers to the removed DanglingLine
        // DanglingLine object has VoltageLevel == null,
        // NPE comes from trying to getNetwork() using VoltageLevel to notify a change in connected components
        checkConnectedComponents(merge);
    }

    @Test
    void mergeNodeBreakerTestPass1() {
        Network n1 = createNetworkWithDanglingLine("1");
        Network n2 = createNetworkWithDanglingLine("2");

        // The test passes if we do not log voltage level (exportTopology)
        Network merge = Network.merge(n1, n2);
        checkConnectedComponents(merge);
    }

    @Test
    void mergeNodeBreakerTestPass2() throws IOException {
        Network n1 = createNetworkWithDanglingLine("1");
        Network n2 = createNetworkWithDanglingLine("2");

        logVoltageLevel("Network 1 first voltage level", n1.getVoltageLevels().iterator().next());
        // The test also passes if we "force" the connected component calculation before merge
        checkConnectedComponents(n1);
        Network merge = Network.merge(n1, n2);
        checkConnectedComponents(n1);
    }

    private static void logVoltageLevel(String title, VoltageLevel vl) throws IOException {
        LOG.info(title);
        try (StringWriter w = new StringWriter()) {
            vl.exportTopology(w);
            LOG.info(w.toString());
        }
    }

    private static void checkConnectedComponents(Network n) {
        n.getBusView().getBuses().forEach(b -> assertEquals(0, b.getConnectedComponent().getNum()));
    }

    private static Network createNetworkWithDanglingLine(String nid) {
        Network n = NetworkTest1Factory.create(nid);
        VoltageLevel vl = n.getVoltageLevel(id("voltageLevel1", nid));
        DanglingLine dl = vl.newDanglingLine()
                .setId(id("danglingLineb", nid))
                .setNode(6)
                .setR(1.0)
                .setX(0.1)
                .setG(0.0)
                .setB(0.001)
                .setP0(10)
                .setQ0(1)
                // Same pairing key for dangling lines
                .setPairingKey("X")
                .add();
        vl.getNodeBreakerView().newBreaker()
                .setId(id("voltageLevel1BreakerDLb", nid))
                .setRetained(false)
                .setOpen(false)
                .setNode1(n.getBusbarSection(id("voltageLevel1BusbarSection1", nid)).getTerminal().getNodeBreakerView().getNode())
                .setNode2(dl.getTerminal().getNodeBreakerView().getNode())
                .add();
        return n;
    }

    @Test
    void mergeTwoNetworksWithVoltageAngleLimits() {
        Network network1 = createNodeBreakerWithVoltageAngleLimit("1");
        Network network2 = createNodeBreakerWithVoltageAngleLimit("2");

        List<VoltageAngleLimit> val1 = network1.getVoltageAngleLimitsStream().toList();
        List<VoltageAngleLimit> val2 = network2.getVoltageAngleLimitsStream().toList();
        List<VoltageAngleLimit> valMerge = new ArrayList<>();
        valMerge.addAll(val1);
        valMerge.addAll(val2);

        Network merge = Network.merge(network1, network2);
        assertTrue(voltageAngleLimitsAreEqual(valMerge, merge.getVoltageAngleLimitsStream().toList()));

        network1 = merge.getSubnetwork(network1.getId()).detach();
        assertTrue(voltageAngleLimitsAreEqual(val1, network1.getVoltageAngleLimitsStream().toList()));
        assertEquals(1, merge.getVoltageAngleLimitsStream().count());

        network2 = merge.getSubnetwork(network2.getId()).detach();
        assertTrue(voltageAngleLimitsAreEqual(val2, network2.getVoltageAngleLimitsStream().toList()));
        assertEquals(0, merge.getVoltageAngleLimitsStream().count());
    }

    @Test
    void failMergeWithVoltageAngleLimits() {
        Network network1 = createNodeBreakerWithVoltageAngleLimit("1", "duplicate");
        Network network2 = createNodeBreakerWithVoltageAngleLimit("2", "duplicate");
        PowsyblException e = assertThrows(PowsyblException.class, () -> Network.merge(network1, network2));
        assertEquals("The following voltage angle limit(s) exist(s) in both networks: [duplicate]", e.getMessage());
    }

    @Test
    void failDetachWithVoltageAngleLimits() {
        Network network1 = createNodeBreakerWithVoltageAngleLimit("1");
        Network network2 = createNodeBreakerWithVoltageAngleLimit("2");
        Network merge = Network.merge(network1, network2);
        merge.newVoltageAngleLimit()
                .setId("valMerge")
                .from(merge.getLine(id("Line-2-2", "1")).getTerminal1())
                .to(merge.getLine(id("Line-2-2", "2")).getTerminal1())
                .setHighLimit(0.25)
                .add();
        Network subnetwork1 = merge.getSubnetwork(network1.getId());
        PowsyblException e = assertThrows(PowsyblException.class, subnetwork1::detach);
        assertEquals("VoltageAngleLimits prevent the subnetwork to be detached: valMerge", e.getMessage());
    }

    @Test
    void mergeTwoNetworksWithVoltageAngleLimitsFail() {
        Network network1 = createNodeBreakerWithVoltageAngleLimit("1");
        Network network2 = createNodeBreakerWithVoltageAngleLimit("2");

        network1.newVoltageAngleLimit()
                .setId("LimitCollision")
                .from(network1.getLine(id("Line-2-2", "1")).getTerminal1())
                .to(network1.getDanglingLine(id("Dl-3", "1")).getTerminal())
                .setHighLimit(0.25)
                .add();
        network2.newVoltageAngleLimit()
                .setId("LimitCollision")
                .from(network2.getLine(id("Line-2-2", "2")).getTerminal1())
                .to(network2.getDanglingLine(id("Dl-3", "2")).getTerminal())
                .setHighLimit(0.25)
                .add();

        PowsyblException e = assertThrows(PowsyblException.class, () -> Network.merge(network1, network2));
        assertEquals("The following voltage angle limit(s) exist(s) in both networks: [LimitCollision]", e.getMessage());
    }

    @Test
    void mergeNetworksWithDifferentCaseDates() {
        ZonedDateTime zonedDateTime1 = ZonedDateTime.of(1999, 12, 1, 10, 30, 0, 0, ZoneId.of("UTC"));
        ZonedDateTime zonedDateTime2 = ZonedDateTime.of(2021, 10, 31, 11, 30, 0, 0, ZoneId.of("UTC"));
        Network network1 = createNodeBreakerWithVoltageAngleLimit("1");
        network1.setCaseDate(zonedDateTime1);
        Network network2 = createNodeBreakerWithVoltageAngleLimit("2");
        network2.setCaseDate(zonedDateTime2);
        Network network3 = createNodeBreakerWithVoltageAngleLimit("3");
        network3.setCaseDate(zonedDateTime1);
        Network networkMerged = Network.merge(network1, network2, network3);
        assertNotEquals(zonedDateTime1, networkMerged.getCaseDate());
        assertNotEquals(zonedDateTime2, networkMerged.getCaseDate());
    }

    @Test
    void mergeNetworksWithSameCaseDates() {
        ZonedDateTime zonedDateTime1 = ZonedDateTime.of(1999, 12, 1, 10, 30, 0, 0, ZoneId.of("UTC"));
        Network network1 = createNodeBreakerWithVoltageAngleLimit("1");
        network1.setCaseDate(zonedDateTime1);
        Network network2 = createNodeBreakerWithVoltageAngleLimit("2");
        network2.setCaseDate(zonedDateTime1);
        Network network3 = createNodeBreakerWithVoltageAngleLimit("3");
        network3.setCaseDate(zonedDateTime1);
        Network networkMerged = Network.merge(network1, network2, network3);
        assertEquals(zonedDateTime1, networkMerged.getCaseDate());
    }

    private static boolean voltageAngleLimitsAreEqual(List<VoltageAngleLimit> expected, List<VoltageAngleLimit> actual) {
        if (expected.size() != actual.size()) {
            return false;
        }
        for (VoltageAngleLimit voltageAngleLimit : actual) {
            if (!isContained(expected, voltageAngleLimit)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isContained(List<VoltageAngleLimit> expected, VoltageAngleLimit actual) {
        return expected.stream().filter(val -> val.getTerminalFrom().getConnectable().getId().equals(actual.getTerminalFrom().getConnectable().getId())
            && Terminal.getConnectableSide(val.getTerminalFrom()).equals(Terminal.getConnectableSide(actual.getTerminalFrom()))
            && val.getTerminalTo().getConnectable().getId().equals(actual.getTerminalTo().getConnectable().getId())
            && Terminal.getConnectableSide(val.getTerminalTo()).equals(Terminal.getConnectableSide(actual.getTerminalTo()))).count() == 1;
    }

    private static Network createNodeBreakerWithVoltageAngleLimit(String nid, String valId) {
        return createNodeBreakerWithVoltageAngleLimit(NetworkFactory.findDefault(), nid, valId);
    }

    private static Network createNodeBreakerWithVoltageAngleLimit(String nid) {
        return createNodeBreakerWithVoltageAngleLimit(nid, id("VoltageAngleLimit_Line-2-2_Dl-3", nid));
    }

    private static Network createNodeBreakerWithVoltageAngleLimit(NetworkFactory networkFactory, String nid, String valId) {

        Network network = networkFactory.createNetwork(id("nodeBreakerWithVoltageAngleLimit", nid), "test");
        double vn = 225.0;

        // First substation
        Substation s1 = network.newSubstation()
            .setId(id("S1", nid))
            .add();
        VoltageLevel s1vl1 = s1.newVoltageLevel()
            .setId(id("S1VL1", nid))
            .setNominalV(vn)
            .setLowVoltageLimit(vn * 0.9)
            .setHighVoltageLimit(vn * 1.1)
            .setTopologyKind(TopologyKind.NODE_BREAKER)
            .add();

        createBusbarSection(s1vl1, id("S1VL1_BBS0A", nid), id("S1VL1_BBS0A", nid), 0);
        createInternalConnection(s1vl1, 0, 1);
        createInternalConnection(s1vl1, 0, 2);
        createGenerator(s1vl1, id("S1VL1-Generator", nid), vn, 80.0, 10.0, 1);

        // Second substation
        Substation s2 = network.newSubstation()
            .setId(id("S2", nid))
            .add();
        VoltageLevel s2vl1 = s2.newVoltageLevel()
            .setId(id("S2VL1", nid))
            .setNominalV(vn)
            .setLowVoltageLimit(vn * 0.9)
            .setHighVoltageLimit(vn * 1.1)
            .setTopologyKind(TopologyKind.NODE_BREAKER)
            .add();

        createBusbarSection(s2vl1, id("S2VL1_BBS0", nid), id("S2VL1_BBS0", nid), 0);
        createInternalConnection(s2vl1, 0, 1);
        createInternalConnection(s2vl1, 0, 2);
        createInternalConnection(s2vl1, 0, 3);
        createLoad(s2vl1, id("S2VL1-Load", nid), 45.0, 9.0, 1);

        createDanglingLine(network, id("S2VL1", nid), id("Dl-3", nid), 70.0, 10.0, "pairingKey", 3);

        // Line between both substations
        createLine(network, id("S1VL1", nid), id("S2VL1", nid), id("Line-2-2", nid), 2, 2);

        network.newVoltageAngleLimit()
            .setId(valId)
            .from(network.getLine(id("Line-2-2", nid)).getTerminal1())
            .to(network.getDanglingLine(id("Dl-3", nid)).getTerminal())
            .setHighLimit(0.25)
            .add();

        return network;
    }

    private static void createBusbarSection(VoltageLevel vl, String id, String name, int node) {
        vl.getNodeBreakerView().newBusbarSection()
            .setId(id)
            .setName(name)
            .setNode(node)
            .add();
    }

    private static void createInternalConnection(VoltageLevel vl, int node1, int node2) {
        vl.getNodeBreakerView().newInternalConnection()
            .setNode1(node1)
            .setNode2(node2)
            .add();
    }

    private static void createLoad(VoltageLevel vl, String id, double p, double q, int node) {
        Load load = vl.newLoad()
            .setId(id)
            .setLoadType(LoadType.UNDEFINED)
            .setP0(p)
            .setQ0(q)
            .setNode(node)
            .add();
        load.getTerminal().setP(p).setQ(q);
    }

    private static void createGenerator(VoltageLevel vl, String id, double targetV, double p, double q, int node) {
        Generator generator = vl.newGenerator()
            .setId(id)
            .setEnergySource(EnergySource.HYDRO)
            .setMinP(-500.0)
            .setMaxP(500.0)
            .setVoltageRegulatorOn(true)
            .setTargetP(p)
            .setTargetV(targetV)
            .setTargetQ(q)
            .setNode(node)
            .add();
        generator.newMinMaxReactiveLimits()
            .setMinQ(-500.0)
            .setMaxQ(500.0)
            .add();
        generator.getTerminal().setP(-p).setQ(-q);
    }

    private static void createLine(Network network, String vl1id, String vl2id, String id, int node1, int node2) {
        network.newLine()
            .setId(id)
            .setR(0.01)
            .setX(2.0)
            .setG1(0.0)
            .setB1(0.0005)
            .setG2(0.0)
            .setB2(0.0005)
            .setNode1(node1)
            .setVoltageLevel1(vl1id)
            .setNode2(node2)
            .setVoltageLevel2(vl2id)
            .add();
    }

    private static void createDanglingLine(Network network, String vlId, String id, double p0, double q0, String pairingKey, int node) {
        network.getVoltageLevel(vlId).newDanglingLine()
            .setId(id)
            .setR(0.01)
            .setX(2.0)
            .setG(0.0)
            .setB(0.0)
            .setP0(p0)
            .setQ0(q0)
            .setPairingKey(pairingKey)
            .setNode(node)
            .setEnsureIdUnicity(false)
            .add();
    }

    private static String id(String localId, String networkId) {
        return NetworkTest1Factory.id(localId, networkId);
    }

    private static final Logger LOG = LoggerFactory.getLogger(MergeTest.class);
}
