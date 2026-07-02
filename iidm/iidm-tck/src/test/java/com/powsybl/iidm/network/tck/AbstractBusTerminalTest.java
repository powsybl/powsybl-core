/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.iidm.network.tck;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.jupiter.api.Test;

import static com.powsybl.iidm.network.test.EurostagTutorialExample1Factory.VLLOAD;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Mathieu Bague {@literal <mathieu.bague@rte-france.com>}
 */
public abstract class AbstractBusTerminalTest {

    public static final String NLOAD_2 = "NLOAD2";
    public static final String NLOAD_3 = "NLOAD3";

    @Test
    public void testSetInvalidConnectableBus() {
        Network network = EurostagTutorialExample1Factory.create();
        Terminal.BusBreakerView busBreakerView = network.getLoad("LOAD").getTerminal().getBusBreakerView();
        assertThrows(PowsyblException.class, () -> busBreakerView.setConnectableBus("UNKNOWN"));
    }

    @Test
    public void testConnectableBusVariantManagement() {
        Network network = EurostagTutorialExample1Factory.create();
        String initialVariantId = network.getVariantManager().getWorkingVariantId();
        VoltageLevel vlLoad = network.getVoltageLevel(VLLOAD);
        vlLoad.getBusBreakerView().newBus().setId(NLOAD_2).add();
        vlLoad.getBusBreakerView().newBus().setId(NLOAD_3).add();

        Load l = network.getLoad("LOAD");
        assertConnection(l, "NLOAD", true);

        // Change the connectable bus
        l.getTerminal().getBusBreakerView().setConnectableBus(NLOAD_2);
        assertConnection(l, NLOAD_2, true);

        // Create a new variant, "VARIANT 2", and use it
        network.getVariantManager().cloneVariant(initialVariantId, "VARIANT 2");
        network.getVariantManager().setWorkingVariant("VARIANT 2");
        assertConnection(l, NLOAD_2, true);

        // Change the connectable bus in "VARIANT 2"
        l.getTerminal().getBusBreakerView().setConnectableBus(NLOAD_3);
        assertConnection(l, NLOAD_3, true);

        // Disconnect the load in "VARIANT 2"
        l.disconnect();
        assertConnection(l, NLOAD_3, false);
        assertNotNull(l.getTerminal().getVoltageLevel());

        // Use initial variant
        network.getVariantManager().setWorkingVariant(initialVariantId);
        assertConnection(l, NLOAD_2, true);
    }

    private void assertConnection(Load load, String expectedConnectableBusId, boolean expectedConnected) {
        assertEquals(expectedConnectableBusId, load.getTerminal().getBusBreakerView().getConnectableBus().getId());
        assertEquals(expectedConnected, load.getTerminal().isConnected());
        if (expectedConnected) {
            assertEquals(expectedConnectableBusId, load.getTerminal().getBusBreakerView().getBus().getId());
        } else {
            assertNull(load.getTerminal().getBusBreakerView().getBus());
        }
    }

    @Test
    void busVoltageShouldNotDependOnNodeInsertionOrder() {
        Network network = createNetwork();
        VoltageLevel vlN = network.getVoltageLevel("VL");
        Load loadNode2 = network.getLoad("L1"); // node 2
        Terminal.BusView busView = loadNode2.getTerminal().getBusView();

        // LF simulation
        busView.getBus().setV(400.0);
        assertEquals(400.0, busView.getBus().getV(), 1e-6);

        // When insert new load in low node
        vlN.getNodeBreakerView().newInternalConnection().setNode1(2).setNode2(0).add();
        vlN.newLoad().setId("newLoadLow").setP0(1).setQ0(1)
                .setNode(0) // node 0
                .add();
        assertEquals(400.0, busView.getBus().getV(), 1e-6);

        // When insert new load in high node
        vlN.getNodeBreakerView().newInternalConnection().setNode1(2).setNode2(4).add();
        vlN.newLoad().setId("newLoadHigh").setP0(1).setQ0(1)
                .setNode(4) // node 4
                .add();
        assertEquals(400.0, busView.getBus().getV(), 1e-6);
    }

    @Test
    void busAngleShouldNotDependOnNodeInsertionOrder() {
        Network network = createNetwork();
        VoltageLevel vlN = network.getVoltageLevel("VL");
        Load loadNode2 = network.getLoad("L1"); // node 2
        Terminal.BusView busView = loadNode2.getTerminal().getBusView();

        // LF simulation
        busView.getBus().setAngle(10.0);
        assertEquals(10.0, busView.getBus().getAngle(), 1e-6);

        // When insert new load in low node
        vlN.getNodeBreakerView().newInternalConnection().setNode1(2).setNode2(0).add();
        vlN.newLoad().setId("newLoadLow").setP0(1).setQ0(1)
                .setNode(0) // node 0
                .add();
        assertEquals(10.0, busView.getBus().getAngle(), 1e-6);

        // When insert new load in high node
        vlN.getNodeBreakerView().newInternalConnection().setNode1(2).setNode2(4).add();
        vlN.newLoad().setId("newLoadHigh").setP0(1).setQ0(1)
                .setNode(4) // node 4
                .add();
        assertEquals(10.0, busView.getBus().getAngle(), 1e-6);
    }

    //           BBS (node 1)
    // ---------------(1)-----------------
    //                 |                |
    //                 |                |
    //                (2) L1           (3) G1
    private Network createNetwork() {
        Network network = Network.create("test", "test");
        Substation s = network.newSubstation().setId("S").add();
        VoltageLevel vl = s.newVoltageLevel()
                .setId("VL")
                .setNominalV(400)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();
        vl.getNodeBreakerView().newBusbarSection().setId("BBS").setNode(1).add();
        vl.newLoad()
                .setId("L1")
                .setNode(2)
                .setP0(10)
                .setQ0(-50)
                .add();
        vl.newGenerator()
                .setId("G1")
                .setNode(3)
                .setMinP(0)
                .setMaxP(100)
                .setTargetP(50)
                .setTargetV(400)
                .setVoltageRegulatorOn(true)
                .add();
        vl.getNodeBreakerView().newBreaker()
                .setId("B1")
                .setNode1(1)
                .setNode2(2)
                .setOpen(false)
                .add();
        vl.getNodeBreakerView().newBreaker()
                .setId("B2")
                .setNode1(1)
                .setNode2(3)
                .setOpen(false)
                .add();
        return network;
    }
}
