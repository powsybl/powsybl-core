/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.iidm.network.tck;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Substation;
import com.powsybl.iidm.network.TopologyKind;
import com.powsybl.iidm.network.VoltageLevel;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Mathieu Bague <mathieu.bague@rte-france.com>
 */
public class AbstractBusViewTest {

    /**
     * In this test case, the VoltageLevel VL1 is under construction: there should be a bus in VL1 with a small amount of reactive power.
     *
     * [NETWORK] --- [VL2] --- LINE --- [VL1]
     *                 |
     *              [LOAD]
     */
    @Test
    public void noBusbarSectionTest() {
        Network network = Network.create("test", "test");
        Substation substation = network.newSubstation()
                .setId("S")
                .add();

        substation.newVoltageLevel()
                .setId("VL1")
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .setNominalV(400)
                .add();

        VoltageLevel vl2 = substation.newVoltageLevel()
                .setId("VL2")
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .setNominalV(400)
                .add();
        vl2.getBusBreakerView().newBus()
                .setId("VL2_BUS")
                .add();
        vl2.newLoad()
                .setId("LOAD")
                .setP0(0)
                .setQ0(0)
                .setBus("VL2_BUS")
                .add();

        network.newLine()
                .setId("LINE")
                .setR(0)
                .setX(0)
                .setG1(0)
                .setB1(0)
                .setG2(0)
                .setB2(0)
                .setVoltageLevel1("VL1")
                .setNode1(0)
                .setVoltageLevel2("VL2")
                .setBus2("VL2_BUS")
                .add();

        Assert.assertEquals(1, network.getVoltageLevel("VL1").getBusView().getBusStream().count());
        Assert.assertEquals(1, network.getVoltageLevel("VL2").getBusView().getBusStream().count());
    }

    /**
     * In this test, we consider two IGMs connected by an XNode. As the danglingLine is not considered as a branch,
     * there is no bus in the VL2
     *
     * (bus1) --- [SW1] --- (bus2) --- [SW2] --- (bus3) --- [LOAD]
     *                        |
     *                      [DL]
     */
    @Test
    public void noBranchTest() {
        String bus1 = "BUS1";
        String bus2 = "BUS2";
        String bus3 = "BUS3";

        Network network = Network.create("test", "test");
        Substation substation = network.newSubstation()
                .setId("S")
                .add();

        VoltageLevel vl = substation.newVoltageLevel()
                .setId("VL")
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .setNominalV(400)
                .add();

        vl.getBusBreakerView().newBus()
                .setId(bus1)
                .add();
        vl.getBusBreakerView().newBus()
                .setId(bus2)
                .add();
        vl.getBusBreakerView().newBus()
                .setId(bus3)
                .add();
        vl.getBusBreakerView().newSwitch()
                .setId("SW1")
                .setBus1(bus1)
                .setBus2(bus2)
                .add();
        vl.newDanglingLine()
                .setId("DL")
                .setBus(bus2)
                .setR(0)
                .setX(0)
                .setB(0)
                .setG(0)
                .setP0(0)
                .setQ0(0)
                .setUcteXnodeCode("XNODE")
                .add();
        vl.getBusBreakerView().newSwitch()
                .setId("SW2")
                .setBus1(bus1)
                .setBus2(bus3)
                .add();
        vl.newLoad()
                .setId("LOAD")
                .setBus(bus3)
                .setConnectableBus(bus3)
                .setP0(0)
                .setQ0(0)
                .add();

        Assert.assertEquals(3, network.getVoltageLevel("VL").getBusBreakerView().getBusStream().count());
        Assert.assertEquals(1, network.getVoltageLevel("VL").getBusView().getBusStream().count());
    }
}
