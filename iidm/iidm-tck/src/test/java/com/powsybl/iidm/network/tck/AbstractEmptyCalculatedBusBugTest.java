/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.tck;

import com.powsybl.iidm.network.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * ArrayIndexOutOfBoundsException fix test
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public abstract class AbstractEmptyCalculatedBusBugTest {

    private Network createNetwork(boolean retained) {
        Network network = Network.create("test", "test");
        Substation s = network.newSubstation()
                .setId("S")
                .setCountry(Country.FR)
                .add();
        VoltageLevel vl = s.newVoltageLevel()
                .setId("VL")
                .setNominalV(400.0)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();
        vl.getNodeBreakerView().newBreaker()
                .setId("SW1")
                .setNode1(0)
                .setNode2(1)
                .setOpen(false)
                .setRetained(retained)
                .add();

        return network;
    }

    @Test
    public void test() {
        Network network = createNetwork(false);
        assertEquals(1, network.getVoltageLevel("VL").getBusBreakerView().getBusStream().count());

        network = createNetwork(true);
        assertEquals(2, network.getVoltageLevel("VL").getBusBreakerView().getBusStream().count());
    }

    @Test
    public void testNullPointer() {
        Network network = createNetwork(true);

        VoltageLevel vl = network.getVoltageLevel("VL");
        vl.getNodeBreakerView()
                .newInternalConnection()
                .setNode1(1)
                .setNode2(2)
                .add();

        Load l1 = vl.newLoad()
                .setId("L1")
                .setNode(0)
                .setP0(100.0)
                .setQ0(50.0)
                .add();

        assertNotNull(l1.getTerminal().getBusBreakerView().getBus());
    }
}
