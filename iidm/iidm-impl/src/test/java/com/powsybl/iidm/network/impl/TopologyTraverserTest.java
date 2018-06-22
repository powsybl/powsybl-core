/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.*;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class TopologyTraverserTest {

    private Network createNodeBreakerNetwork() {
        Network network = NetworkFactory.create("test", "test");
        Substation s1 = network.newSubstation()
                .setId("S1")
                .setCountry(Country.FR)
                .add();
        VoltageLevel vl1 = s1.newVoltageLevel()
                .setId("VL1")
                .setNominalV(400.0)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();
        vl1.getNodeBreakerView().setNodeCount(4);
        vl1.getNodeBreakerView().newBusbarSection()
                .setId("BBS1")
                .setNode(0)
                .add();
        vl1.newGenerator()
                .setId("G")
                .setNode(1)
                .setMaxP(100.0)
                .setMinP(50.0)
                .setTargetP(100.0)
                .setTargetV(400.0)
                .setVoltageRegulatorOn(true)
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
        vl2.getNodeBreakerView().setNodeCount(5);
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

        List<String> traversed = new ArrayList<>();
        network.getGenerator("G").getTerminal().traverse(new VoltageLevel.TopologyTraverser() {
            @Override
            public boolean traverse(Terminal terminal, boolean connected) {
                traversed.add(terminal.getConnectable().getId());
                return true;
            }

            @Override
            public boolean traverse(Switch aSwitch) {
                return true;
            }
        });
        Assert.assertEquals(traversed, Arrays.asList("G", "BBS1", "L1", "L1", "BBS2", "LD"));
    }

    @Test
    public void test2() {
        Network network = createNodeBreakerNetwork();

        List<String> traversed = new ArrayList<>();
        network.getVoltageLevel("VL1").getNodeBreakerView().getBusbarSection("BBS1")
            .getTerminal().traverse(new VoltageLevel.TopologyTraverser() {
                @Override
                public boolean traverse(Terminal terminal, boolean connected) {
                    traversed.add(terminal.getConnectable().getId());
                    return connected;
                }

                @Override
                public boolean traverse(Switch aSwitch) {
                    return !aSwitch.isOpen() && aSwitch.getKind() != SwitchKind.BREAKER;
                }
            });
        Assert.assertEquals(traversed, Arrays.asList("BBS1", "G"));
    }

    @Test
    public void test3() {
        Network network = createMixedNodeBreakerBusBreakerNetwork();

        List<String> traversed = new ArrayList<>();
        network.getGenerator("G").getTerminal().traverse(new VoltageLevel.TopologyTraverser() {
            @Override
            public boolean traverse(Terminal terminal, boolean connected) {
                traversed.add(terminal.getConnectable().getId());
                return true;
            }

            @Override
            public boolean traverse(Switch aSwitch) {
                return true;
            }
        });
        Assert.assertEquals(traversed, Arrays.asList("G", "BBS1", "L1", "L1", "BBS2", "LD", "L2", "L2", "LD2"));
    }
}
