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
import java.util.Set;

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

    @Test
    void mergeNodeBreakerWithAreasTestPass1() {
        Network n1 = createNodeBreaker("1", "nodeBreakerWithAreas");
        Network n2 = createNodeBreaker("2", "nodeBreakerWithAreas");

        AreaType n1Bz = createAreaType(n1, "bz");
        AreaType n2Bz = createAreaType(n2, "bz");

        Area n1Bza = createArea(n1, "bza", n1Bz);
        Area n2Bz2 = createArea(n2, "bza", n2Bz);

        VoltageLevel n1S1VL1 = n1.getVoltageLevel(id("S1VL1", "1"));
        VoltageLevel n2S1VL1 = n2.getVoltageLevel(id("S1VL1", "2"));

        n1Bza.addVoltageLevel(n1S1VL1);
        n2Bz2.addVoltageLevel(n2S1VL1);

        // Merge
        Network merged = Network.merge(n1, n2);

        assertEquals(List.of(n1Bz), merged.getAreaTypeStream().toList());
        assertEquals(List.of(n1Bza), merged.getAreaStream().toList());
        assertEquals(Set.of(n1S1VL1, n2S1VL1), n1Bza.getVoltageLevels());

        // Detach
        Network subnetwork1 = merged.getSubnetwork(n1.getId());
        Network n1Detached = subnetwork1.detach();

        // Detached Network
        AreaType detachedBz = n1Detached.getAreaType("bz");
        Area detachedBza = n1Detached.getArea("bza");

        assertEquals(List.of(detachedBz), n1Detached.getAreaTypeStream().toList());
        assertEquals(List.of(detachedBza), n1Detached.getAreaStream().toList());
        assertEquals(Set.of(n1S1VL1), detachedBza.getVoltageLevels());

        // Previous Network
        assertEquals(List.of(n1Bz), merged.getAreaTypeStream().toList());
        assertEquals(List.of(n1Bza), merged.getAreaStream().toList());
        assertEquals(Set.of(n2S1VL1), n1Bza.getVoltageLevels());
    }

    @Test
    void mergeNodeBreakerWithAreasTestPass2() {
        Network n1 = createNodeBreaker("1", "nodeBreakerWithAreas");
        Network n2 = createNodeBreaker("2", "nodeBreakerWithAreas");

        AreaType n1Bz = createAreaType(n1, "bz");
        AreaType n2Bz = createAreaType(n2, "bz");

        Area n1Bza = createArea(n1, "bza", n1Bz);
        Area n2Bzb = createArea(n2, "bzb", n2Bz);

        VoltageLevel n1S1VL1 = n1.getVoltageLevel(id("S1VL1", "1"));
        VoltageLevel n2S1VL1 = n2.getVoltageLevel(id("S1VL1", "2"));

        n1Bza.addVoltageLevel(n1S1VL1);
        n2Bzb.addVoltageLevel(n2S1VL1);

        // Merge
        Network merged = Network.merge(n1, n2);

        assertEquals(List.of(n1Bz), merged.getAreaTypeStream().toList());
        Area mergedBzb = merged.getArea("bzb"); // not the same object as n2Bzb but a duplicate
        assertEquals(n1Bz, mergedBzb.getAreaType());
        assertEquals(List.of(n1Bza, mergedBzb), merged.getAreaStream().toList());
        assertEquals(Set.of(n1S1VL1), n1Bza.getVoltageLevels());
        assertEquals(Set.of(n2S1VL1), mergedBzb.getVoltageLevels());

        // Detach
        Network subnetwork1 = merged.getSubnetwork(n1.getId());
        Network n1Detached = subnetwork1.detach();

        // Detached Network
        AreaType detachedBz = n1Detached.getAreaType("bz");
        Area detachedBza = n1Detached.getArea("bza");

        assertEquals(List.of(detachedBz), n1Detached.getAreaTypeStream().toList());
        assertEquals(List.of(detachedBza), n1Detached.getAreaStream().toList());
        assertEquals(Set.of(n1S1VL1), detachedBza.getVoltageLevels());

        // Previous Network
        assertEquals(List.of(n1Bz), merged.getAreaTypeStream().toList());
        assertEquals(List.of(mergedBzb), merged.getAreaStream().toList());
        assertEquals(Set.of(n2S1VL1), mergedBzb.getVoltageLevels());
    }

    @Test
    void mergeNodeBreakerWithAreasTestPass3() {
        Network n1 = createNodeBreaker("1", "nodeBreakerWithAreas");
        Network n2 = createNodeBreaker("2", "nodeBreakerWithAreas");

        AreaType n1Bz = createAreaType(n1, "bz");
        AreaType n2Tso = createAreaType(n2, "tso");

        Area n1Bza = createArea(n1, "bza", n1Bz);
        Area n2Tsoa = createArea(n2, "tsoa", n2Tso);

        VoltageLevel n1S1VL1 = n1.getVoltageLevel(id("S1VL1", "1"));
        VoltageLevel n2S1VL1 = n2.getVoltageLevel(id("S1VL1", "2"));

        n1Bza.addVoltageLevel(n1S1VL1);
        n2Tsoa.addVoltageLevel(n2S1VL1);

        // Merge
        Network merged = Network.merge(n1, n2);

        assertEquals(List.of(n1Bz, n2Tso), merged.getAreaTypeStream().toList());
        assertEquals(List.of(n1Bza, n2Tsoa), merged.getAreaStream().toList());
        assertEquals(Set.of(n1S1VL1), n1Bza.getVoltageLevels());
        assertEquals(Set.of(n2S1VL1), n2Tsoa.getVoltageLevels());

        // Detach
        Network subnetwork1 = merged.getSubnetwork(n1.getId());
        Network n1Detached = subnetwork1.detach();

        // Detached Network
        AreaType detachedBz = n1Detached.getAreaType("bz");
        Area detachedBza = n1Detached.getArea("bza");

        assertEquals(List.of(detachedBz), n1Detached.getAreaTypeStream().toList());
        assertEquals(List.of(detachedBza), n1Detached.getAreaStream().toList());
        assertEquals(Set.of(n1S1VL1), detachedBza.getVoltageLevels());

        // Previous Network
        assertEquals(List.of(n2Tso), merged.getAreaTypeStream().toList());
        assertEquals(List.of(n2Tsoa), merged.getAreaStream().toList());
        assertEquals(Set.of(n2S1VL1), n2Tsoa.getVoltageLevels());
    }

    @Test
    void mergeNodeBreakerWithAreasTestPass4() {
        Network n1 = createNodeBreaker("1", "nodeBreakerWithAreas");
        Network n2 = createNodeBreaker("2", "nodeBreakerWithAreas");

        AreaType n1Bz = createAreaType(n1, "bz");
        Area n1Bza = createArea(n1, "bza", n1Bz);

        VoltageLevel n1S1VL1 = n1.getVoltageLevel(id("S1VL1", "1"));

        n1Bza.addVoltageLevel(n1S1VL1);

        // Merge
        Network merged = Network.merge(n1, n2);

        assertEquals(List.of(n1Bz), merged.getAreaTypeStream().toList());
        assertEquals(List.of(n1Bza), merged.getAreaStream().toList());
        assertEquals(Set.of(n1S1VL1), n1Bza.getVoltageLevels());

        // Detach
        Network subnetwork1 = merged.getSubnetwork(n1.getId());
        Network n1Detached = subnetwork1.detach();

        // Detached Network
        AreaType detachedBz = n1Detached.getAreaType("bz");
        Area detachedBza = n1Detached.getArea("bza");
        assertEquals(List.of(detachedBz), n1Detached.getAreaTypeStream().toList());
        assertEquals(List.of(detachedBza), n1Detached.getAreaStream().toList());

        // Previous Network
        assertEquals(List.of(), merged.getAreaTypeStream().toList());
        assertEquals(List.of(), merged.getAreaStream().toList());
    }

    @Test
    void mergeNodeBreakerWithAreasTestPass5() {
        Network n1 = createNodeBreaker("1", "nodeBreakerWithAreas");
        Network n2 = createNodeBreaker("2", "nodeBreakerWithAreas");

        AreaType n1Bz = n1.newAreaType()
                .setId("bz").setName("BZ")
                .setFictitious(false)
                .add();
        Area n1Bza = n1.newArea()
                .setId("bza").setName("BZA")
                .setAreaType(n1Bz)
                .setFictitious(false)
                .setAcNetInterchangeTarget(null)
                .setAcNetInterchangeTolerance(1.0)
                .add();

        VoltageLevel n1S1VL1 = n1.getVoltageLevel(id("S1VL1", "1"));
        n1S1VL1.addArea(n1Bza);

        // Merge
        Network merged = Network.merge(n1, n2);
        Area mergedBza = merged.getArea("bza");

        assertEquals("bza", mergedBza.getId());
        assertEquals("BZA", mergedBza.getNameOrId());
        assertEquals(n1Bz, mergedBza.getAreaType());
        assertFalse(mergedBza.isFictitious());
        assertTrue(mergedBza.getAcNetInterchangeTarget().isEmpty());
        assertEquals(1.0, mergedBza.getAcNetInterchangeTolerance().get());

        // Detach
        Network subnetwork1 = merged.getSubnetwork(n1.getId());
        Network n1Detached = subnetwork1.detach();

        // Detached Network
        AreaType detachedBz = n1Detached.getAreaType("bz");
        assertEquals("bz", detachedBz.getId());
        assertEquals("BZ", detachedBz.getNameOrId());
        assertFalse(detachedBz.isFictitious());

        Area detachedBza = n1Detached.getArea("bza");
        assertEquals("bza", detachedBza.getId());
        assertEquals("BZA", detachedBza.getNameOrId());
        assertEquals(detachedBz, detachedBza.getAreaType());
        assertFalse(detachedBza.isFictitious());
        assertTrue(detachedBza.getAcNetInterchangeTarget().isEmpty());
        assertEquals(1.0, detachedBza.getAcNetInterchangeTolerance().get());
    }

    @Test
    void mergeNodeBreakerWithAreasTestFail1() {
        Network n1 = createNodeBreaker("1", "nodeBreakerWithAreas");
        Network n2 = createNodeBreaker("2", "nodeBreakerWithAreas");

        createAreaType(n1, "bz");
        createAreaType(n2, "bz").setName("BZ_otherName");

        //Merge
        PowsyblException e = assertThrows(PowsyblException.class, () -> Network.merge(n1, n2));
        assertEquals("Cannot merge object(s) of type 'AreaTypeImpl' with same id: 'bz' but with different name: 'bz_Name' and 'BZ_otherName'", e.getMessage());
    }

    @Test
    void mergeNodeBreakerWithAreasTestFail2() {
        Network n1 = createNodeBreaker("1", "nodeBreakerWithAreas");
        Network n2 = createNodeBreaker("2", "nodeBreakerWithAreas");

        createAreaType(n1, "bz");
        createAreaType(n2, "bz");

        createArea(n1, "bza", n1.getAreaType("bz"));
        n2.newArea()
                .setId("bza").setName("bza_Name")
                .setAreaType(n2.getAreaType("bz"))
                .setAcNetInterchangeTarget(20.0)
                .add();

        //Merge
        PowsyblException e = assertThrows(PowsyblException.class, () -> Network.merge(n1, n2));
        assertEquals("Cannot merge object(s) of type 'AreaImpl' with same id: 'bza' but with different acNetInterchangeTarget: 'null' and '20.0'", e.getMessage());
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
        Network network = createNodeBreaker(networkFactory, nid, "nodeBreakerWithVoltageAngleLimit");

        network.newVoltageAngleLimit()
            .setId(valId)
            .from(network.getLine(id("Line-2-2", nid)).getTerminal1())
            .to(network.getDanglingLine(id("Dl-3", nid)).getTerminal())
            .setHighLimit(0.25)
            .add();

        return network;
    }

    private static Network createNodeBreaker(String nid, String networkId) {
        return createNodeBreaker(NetworkFactory.findDefault(), nid, networkId);
    }

    private static Network createNodeBreaker(NetworkFactory networkFactory, String nid, String networkId) {
        Network network = networkFactory.createNetwork(id(networkId, nid), "test");
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

    private static AreaType createAreaType(Network network, String id) {
        return network.newAreaType()
                .setId(id)
                .setName(id + "_Name")
                .add();
    }

    private static Area createArea(Network network, String id, AreaType areaType) {
        return network.newArea()
                .setId(id)
                .setName(id + "_Name")
                .setAreaType(areaType)
                .add();
    }

    private static String id(String localId, String networkId) {
        return NetworkTest1Factory.id(localId, networkId);
    }

    private static final Logger LOG = LoggerFactory.getLogger(MergeTest.class);
}
