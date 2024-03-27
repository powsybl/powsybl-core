/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.tck;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.math.graph.TraverseResult;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public abstract class AbstractTopologyTraverserTest {

    private Network createNodeBreakerNetwork() {
        Network network = Network.create("test", "test");
        Substation s1 = network.newSubstation()
                .setId("S1")
                .setCountry(Country.FR)
                .add();
        VoltageLevel vl1 = s1.newVoltageLevel()
                .setId("VL1")
                .setNominalV(400.0)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();
        vl1.getNodeBreakerView().newBusbarSection()
                .setId("BBS1")
                .setNode(0)
                .add();
        vl1.newGenerator()
                .setId("G")
                .setNode(4)
                .setMaxP(100.0)
                .setMinP(50.0)
                .setTargetP(100.0)
                .setTargetV(400.0)
                .setVoltageRegulatorOn(true)
                .add();
        vl1.getNodeBreakerView().newInternalConnection()
                .setNode1(1)
                .setNode2(4)
                .add();
        vl1.getNodeBreakerView().newDisconnector()
                .setId("BR1")
                .setNode1(0)
                .setNode2(1)
                .setOpen(false)
                .add();
        vl1.getNodeBreakerView().newDisconnector()
                .setId("D1")
                .setNode1(0)
                .setNode2(2)
                .setOpen(false)
                .add();
        vl1.getNodeBreakerView().newBreaker()
                .setId("BR2")
                .setNode1(2)
                .setNode2(3)
                .setOpen(false)
                .add();

        Substation s2 = network.newSubstation()
                .setId("S2")
                .setCountry(Country.FR)
                .add();
        VoltageLevel vl2 = s2.newVoltageLevel()
                .setId("VL2")
                .setNominalV(400.0)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();
        vl2.getNodeBreakerView().newBusbarSection()
                .setId("BBS2")
                .setNode(0)
                .add();
        vl2.newLoad()
                .setId("LD")
                .setNode(1)
                .setP0(1)
                .setQ0(1)
                .add();
        vl2.getNodeBreakerView().newDisconnector()
                .setId("BR3")
                .setNode1(0)
                .setNode2(1)
                .setOpen(false)
                .add();
        vl2.getNodeBreakerView().newDisconnector()
                .setId("D2")
                .setNode1(0)
                .setNode2(2)
                .setOpen(false)
                .add();
        vl2.getNodeBreakerView().newBreaker()
                .setId("BR4")
                .setNode1(2)
                .setNode2(3)
                .setOpen(false)
                .add();
        network.newLine()
                .setId("L1")
                .setVoltageLevel1("VL1")
                .setNode1(3)
                .setVoltageLevel2("VL2")
                .setNode2(3)
                .setR(1.0)
                .setX(1.0)
                .setG1(0.0)
                .setB1(0.0)
                .setG2(0.0)
                .setB2(0.0)
                .add();
        return network;
    }

    protected Network createMixedNodeBreakerBusBreakerNetwork() {
        Network network = createNodeBreakerNetwork();
        Substation s3 = network.newSubstation()
                .setId("S3")
                .setCountry(Country.FR)
                .add();
        VoltageLevel vl3 = s3.newVoltageLevel()
                .setId("VL3")
                .setNominalV(400.0)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        vl3.getBusBreakerView().newBus()
                .setId("B1")
                .add();
        vl3.newLoad()
                .setId("LD2")
                .setConnectableBus("B1")
                .setBus("B1")
                .setP0(1.0)
                .setQ0(1.0)
                .add();
        network.getVoltageLevel("VL2").getNodeBreakerView().newBreaker()
                .setId("BR5")
                .setNode1(0)
                .setNode2(4)
                .setOpen(false)
                .add();
        network.newLine()
                .setId("L2")
                .setVoltageLevel1("VL2")
                .setNode1(4)
                .setVoltageLevel2("VL3")
                .setConnectableBus2("B1")
                .setBus2("B1")
                .setR(1.0)
                .setX(1.0)
                .setG1(0.0)
                .setB1(0.0)
                .setG2(0.0)
                .setB2(0.0)
                .add();
        return network;
    }

    @Test
    public void test1() {
        Network network = createNodeBreakerNetwork();
        Terminal start = network.getGenerator("G").getTerminal();
        Set<Pair<String, Integer>> visited = getVisitedSet(start, s -> TraverseResult.CONTINUE);
        assertEquals(Set.of(Pair.of("G", 0), Pair.of("BBS1", 0), Pair.of("L1", 0),
                        Pair.of("L1", 1), Pair.of("BBS2", 0), Pair.of("LD", 0)),
                visited);
    }

    @Test
    public void test2() {
        Network network = createNodeBreakerNetwork();
        Terminal start = network.getVoltageLevel("VL1").getNodeBreakerView().getBusbarSection("BBS1").getTerminal();
        Set<Pair<String, Integer>> visited = getVisitedSet(start, aSwitch ->
                !aSwitch.isOpen() && aSwitch.getKind() != SwitchKind.BREAKER ? TraverseResult.CONTINUE : TraverseResult.TERMINATE_PATH);
        assertEquals(Set.of(Pair.of("BBS1", 0), Pair.of("G", 0)), visited);
    }

    @Test
    public void test3() {
        Network network = createMixedNodeBreakerBusBreakerNetwork();
        Terminal start = network.getGenerator("G").getTerminal();
        Set<Pair<String, Integer>> visited1 = getVisitedSet(start, s -> TraverseResult.CONTINUE);
        assertEquals(Set.of(Pair.of("G", 0), Pair.of("BBS1", 0), Pair.of("L1", 0),
                        Pair.of("L1", 1), Pair.of("BBS2", 0), Pair.of("LD", 0), Pair.of("L2", 0),
                        Pair.of("L2", 1), Pair.of("LD2", 0)),
                visited1);

        Set<Pair<String, Integer>> visited2 = getVisitedSet(start, s -> TraverseResult.CONTINUE,
            t -> t.getConnectable().getId().equals("L2") ? TraverseResult.TERMINATE_PATH : TraverseResult.CONTINUE);
        assertEquals(Set.of(Pair.of("G", 0), Pair.of("BBS1", 0), Pair.of("L1", 0), Pair.of("L1", 1),
                    Pair.of("BBS2", 0), Pair.of("LD", 0), Pair.of("L2", 0)),
                visited2);

        Set<Pair<String, Integer>> visited3 = getVisitedSet(network.getLoad("LD2").getTerminal(), s -> TraverseResult.CONTINUE,
            t -> t.getConnectable().getId().equals("L2") ? TraverseResult.TERMINATE_PATH : TraverseResult.CONTINUE);
        assertEquals(Set.of(Pair.of("LD2", 0), Pair.of("L2", 1)), visited3);

    }

    @Test
    public void test4() {
        Network network = EurostagTutorialExample1Factory.create();
        Terminal start = network.getGenerator("GEN").getTerminal();
        Set<Pair<String, Integer>> visited = getVisitedSet(start, s -> TraverseResult.CONTINUE);
        assertEquals(Set.of(Pair.of("GEN", 0), Pair.of("NGEN_NHV1", 0), Pair.of("NGEN_NHV1", 1),
                        Pair.of("NHV1_NHV2_1", 0), Pair.of("NHV1_NHV2_2", 0), Pair.of("NHV1_NHV2_1", 1),
                        Pair.of("NHV1_NHV2_2", 1), Pair.of("NHV2_NLOAD", 0), Pair.of("NHV2_NLOAD", 1), Pair.of("LOAD", 0)),
                visited);
    }

    @Test
    public void test5() {
        Network network = EurostagTutorialExample1Factory.create();

        // Duplicate 2wt to go from VLGEN to VLHV1 even if traverser stops at one of them
        TwoWindingsTransformer transformer = network.getTwoWindingsTransformer("NGEN_NHV1");
        TwoWindingsTransformer duplicatedTransformer = network.getSubstation("P1")
                .newTwoWindingsTransformer()
                .setId("duplicate")
                .setVoltageLevel1("VLGEN").setBus1("NGEN")
                .setVoltageLevel2("VLHV1").setBus2("NHV1")
                .setRatedU1(transformer.getRatedU1())
                .setRatedU2(transformer.getRatedU2())
                .setR(transformer.getR())
                .setX(transformer.getX())
                .setG(transformer.getG())
                .setB(transformer.getB())
                .add();

        Terminal start = network.getGenerator("GEN").getTerminal();
        Set<Pair<String, Integer>> visited = getVisitedSet(start, s -> TraverseResult.CONTINUE,
            t -> t.getConnectable() == duplicatedTransformer && t.getVoltageLevel().getId().equals("VLGEN") ? TraverseResult.TERMINATE_PATH : TraverseResult.CONTINUE);
        assertEquals(Set.of(Pair.of("GEN", 0), Pair.of("NGEN_NHV1", 0), Pair.of("duplicate", 0), Pair.of("NGEN_NHV1", 1),
                        Pair.of("NHV1_NHV2_1", 0), Pair.of("NHV1_NHV2_2", 0), Pair.of("duplicate", 1), Pair.of("NHV1_NHV2_1", 1),
                        Pair.of("NHV1_NHV2_2", 1), Pair.of("NHV2_NLOAD", 0), Pair.of("NHV2_NLOAD", 1), Pair.of("LOAD", 0)),
                visited);
    }

    protected List<Pair<String, Integer>> getVisitedList(Terminal start, Function<Switch, TraverseResult> switchTest) {
        return getVisitedList(start, switchTest, t -> TraverseResult.CONTINUE);
    }

    protected List<Pair<String, Integer>> getVisitedList(Terminal start, Function<Switch, TraverseResult> switchTest, Function<Terminal, TraverseResult> terminalTest) {
        return getVisitedStream(start, switchTest, terminalTest).collect(Collectors.toList());
    }

    protected Set<Pair<String, Integer>> getVisitedSet(Terminal start, Function<Switch, TraverseResult> switchTest) {
        return getVisitedSet(start, switchTest, t -> TraverseResult.CONTINUE);
    }

    protected Set<Pair<String, Integer>> getVisitedSet(Terminal start, Function<Switch, TraverseResult> switchTest, Function<Terminal, TraverseResult> terminalTest) {
        return getVisitedStream(start, switchTest, terminalTest).collect(Collectors.toSet());
    }

    protected Stream<Pair<String, Integer>> getVisitedStream(Terminal start, Function<Switch, TraverseResult> switchTest, Function<Terminal, TraverseResult> terminalTest) {
        Set<Terminal> visited = new LinkedHashSet<>();
        start.traverse(new Terminal.TopologyTraverser() {
            @Override
            public TraverseResult traverse(Terminal terminal, boolean connected) {
                if (!visited.add(terminal)) {
                    fail("Traversing an already visited terminal");
                }
                return terminalTest.apply(terminal);
            }

            @Override
            public TraverseResult traverse(Switch aSwitch) {
                return switchTest.apply(aSwitch);
            }
        });
        return visited.stream().map(t -> Pair.of(t.getConnectable().getId(), indexOfTerminal(t)));
    }

    static Integer indexOfTerminal(Terminal t) {
        int index = t.getConnectable().getTerminals().indexOf(t);
        return index == -1 ? null : index;
    }

}
