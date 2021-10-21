/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.tck;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.network.test.FictitiousSwitchFactory;
import com.powsybl.math.graph.TraverseResult;
import org.junit.Test;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
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

    private Network createMixedNodeBreakerBusBreakerNetwork() {
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
        List<String> visited = recordVisited(start, s -> TraverseResult.CONTINUE);
        assertEquals(Arrays.asList("G", "BBS1", "L1", "L1", "BBS2", "LD"), visited);
    }

    @Test
    public void test2() {
        Network network = createNodeBreakerNetwork();
        Terminal start = network.getVoltageLevel("VL1").getNodeBreakerView().getBusbarSection("BBS1").getTerminal();
        List<String> visited = recordVisited(start, aSwitch ->
                !aSwitch.isOpen() && aSwitch.getKind() != SwitchKind.BREAKER ? TraverseResult.CONTINUE : TraverseResult.TERMINATE_PATH);
        assertEquals(Arrays.asList("BBS1", "G"), visited);
    }

    @Test
    public void test3() {
        Network network = createMixedNodeBreakerBusBreakerNetwork();
        Terminal start = network.getGenerator("G").getTerminal();
        List<String> visited = recordVisited(start, s -> TraverseResult.CONTINUE);
        assertEquals(Arrays.asList("G", "BBS1", "L1", "L1", "BBS2", "LD", "L2", "L2", "LD2"), visited);
    }

    @Test
    public void test4() {
        Network network = EurostagTutorialExample1Factory.create();
        Terminal start = network.getGenerator("GEN").getTerminal();
        List<String> visited = recordVisited(start, s -> TraverseResult.CONTINUE);
        assertEquals(Arrays.asList("GEN", "NGEN_NHV1", "NGEN_NHV1", "NHV1_NHV2_1", "NHV1_NHV2_2", "NHV1_NHV2_1", "NHV1_NHV2_2", "NHV2_NLOAD", "NHV2_NLOAD", "LOAD"), visited);
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
        List<String> visited = recordVisited(start, s -> TraverseResult.CONTINUE,
            t -> t.getConnectable() == duplicatedTransformer && t.getVoltageLevel().getId().equals("VLGEN") ? TraverseResult.TERMINATE_PATH : TraverseResult.CONTINUE);
        assertEquals(Arrays.asList("GEN", "NGEN_NHV1", "duplicate", "NGEN_NHV1", "NHV1_NHV2_1", "NHV1_NHV2_2", "duplicate", "NHV1_NHV2_1", "NHV1_NHV2_2", "NHV2_NLOAD", "NHV2_NLOAD", "LOAD"), visited);
    }

    @Test
    public void testTerminateTraverser() {
        Network network = createMixedNodeBreakerBusBreakerNetwork();
        Terminal startGNbv = network.getGenerator("G").getTerminal();
        List<String> visited0 = recordVisited(startGNbv, s -> s != null && s.getId().equals("BR2") ? TraverseResult.TERMINATE_TRAVERSER : TraverseResult.CONTINUE);
        assertEquals(List.of("G", "BBS1"), visited0);

        List<String> visited1 = recordVisited(startGNbv, s -> TraverseResult.CONTINUE, t -> TraverseResult.TERMINATE_TRAVERSER);
        assertEquals(List.of("G"), visited1);

        List<String> visited2 = recordVisited(startGNbv, s -> TraverseResult.CONTINUE,
            t -> t.getConnectable() instanceof BusbarSection ? TraverseResult.TERMINATE_TRAVERSER : TraverseResult.CONTINUE);
        assertEquals(List.of("G", "BBS1"), visited2);

        List<String> visited3 = recordVisited(startGNbv, s -> TraverseResult.CONTINUE,
            t -> t.getConnectable().getId().equals("L2") ? TraverseResult.TERMINATE_PATH : TraverseResult.CONTINUE);
        assertEquals(List.of("G", "BBS1", "L1", "L1", "BBS2", "LD", "L2"), visited3);

        Terminal startLBbv = network.getLoad("LD2").getTerminal();
        List<String> visited4 = recordVisited(startLBbv, s -> TraverseResult.CONTINUE, t -> TraverseResult.TERMINATE_TRAVERSER);
        assertEquals(List.of("LD2"), visited4);

        List<String> visited5 = recordVisited(startLBbv, s -> TraverseResult.CONTINUE,
            t -> t.getConnectable().getId().equals("L2") ? TraverseResult.TERMINATE_TRAVERSER : TraverseResult.CONTINUE);
        assertEquals(List.of("LD2", "L2"), visited5);

        List<String> visited6 = recordVisited(startLBbv, s -> TraverseResult.CONTINUE,
            t -> t.getConnectable().getId().equals("L2") ? TraverseResult.TERMINATE_PATH : TraverseResult.CONTINUE);
        assertEquals(List.of("LD2", "L2"), visited6);

        Network network2 = FictitiousSwitchFactory.create();
        List<String> visited8 = recordVisited(network2.getGenerator("CB").getTerminal(), s -> TraverseResult.CONTINUE);
        assertEquals(List.of("CB", "O", "P", "CF", "CH", "CC", "CD", "CE", "CJ", "CI", "CG", "CJ", "D", "CI"), visited8);
    }

    private List<String> recordVisited(Terminal start, Function<Switch, TraverseResult> switchTest) {
        return recordVisited(start, switchTest, t -> TraverseResult.CONTINUE);
    }

    private List<String> recordVisited(Terminal start, Function<Switch, TraverseResult> switchTest, Function<Terminal, TraverseResult> terminalTest) {
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
        return visited.stream().map(t -> t.getConnectable().getId()).collect(Collectors.toList());
    }

}
