/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.tck;

import com.powsybl.iidm.network.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

/**
 * @author Mathieu BAGUE {@literal <mathieu.bague at rte-france.com>}
 */
public abstract class AbstractInvalidateCacheNbVlTest {

    @Test
    public void test() {
        Network network = Network.create("test", "test");
        Substation substation = network.newSubstation().setId("S1")
                .setCountry(Country.FR)
                .setTso("TSO")
                .add();
        VoltageLevel vl1 = substation.newVoltageLevel().setId("VL1")
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .setNominalV(90.0)
                .setLowVoltageLimit(88.0)
                .setHighVoltageLimit(96.0)
                .add();
        BusbarSection bbs1 = vl1.getNodeBreakerView().newBusbarSection().setId("BBS1")
                .setName("BBS1_NAME")
                .setNode(0)
                .add();

        // In that case, the bus cache is initialized, but there is no bus for node 0
        assertNull(bbs1.getTerminal().getBusView().getBus());

        BusbarSection bbs2 = vl1.getNodeBreakerView().newBusbarSection().setId("BBS2")
                .setName("BBS2_NAME")
                .setNode(1)
                .add();
        // Without the fix, this call throws an exception (ArrayIndexOutOfBoundsException), but Null is expected
        assertNull(bbs2.getTerminal().getBusView().getBus());
    }
}
