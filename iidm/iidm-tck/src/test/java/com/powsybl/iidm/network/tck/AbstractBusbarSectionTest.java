/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.tck;

import com.powsybl.iidm.network.BusbarSection;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.TopologyKind;
import com.powsybl.iidm.network.VoltageLevel;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Coline Piloquet {@literal <coline.piloquet at rte-france.com>}
 */
public abstract class AbstractBusbarSectionTest {

    @Test
    public void testEquivalent() {
        Network network = createNetwork();

        BusbarSection bbs = network.getBusbarSection("BBS");
        assertFalse(bbs.isEquivalent()); // default value
        bbs.setEquivalent(true);
        assertTrue(bbs.isEquivalent());
    }

    private Network createNetwork() {
        Network network = Network.create("test", "test");
        network.newSubstation()
                .setId("S")
                .add();
        VoltageLevel vl = network.newVoltageLevel()
                .setId("VL")
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .setNominalV(380)
                .add();
        vl.getNodeBreakerView()
                .newBusbarSection()
                .setId("BBS")
                .setNode(0)
                .add();
        return network;
    }
}
