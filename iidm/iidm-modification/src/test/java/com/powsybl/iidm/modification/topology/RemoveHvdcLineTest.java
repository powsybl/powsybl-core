/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification.topology;

import com.powsybl.commons.test.AbstractConverterTest;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.HvdcTestNetwork;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * @author Anis Touri <anis-1.touri@rte-france.com>
 */
class RemoveHvdcLineTest extends AbstractConverterTest {

    @Test
    void testRemoveHvdcLineLcc() {
        Network network = HvdcTestNetwork.createLcc();
        new RemoveHvdcLineBuilder().withHvdcLineId("L").build().apply(network);
        assertNull(network.getHvdcLine("L"));
        assertNull(network.getLccConverterStation("C1"));
        assertNotNull(network.getShuntCompensator("C1_Filter1"));
    }

    @Test
    void testRemoveHvdcLineLccWithMcs() {
        Network network = HvdcTestNetwork.createLcc();
        new RemoveHvdcLineBuilder().withHvdcLineId("L").withShuntCompensatorIds(List.of("C1_Filter1", "C1_Filter2")).build().apply(network);
        assertNull(network.getHvdcLine("L"));
        assertNull(network.getLccConverterStation("C1"));
        assertNull(network.getShuntCompensator("C1_Filter1"));
        assertNull(network.getShuntCompensator("C1_Filter2"));
    }

    @Test
    void testRemoveHvdcLineVsc() {
        Network network = HvdcTestNetwork.createVsc();
        new RemoveHvdcLineBuilder().withHvdcLineId("L").build().apply(network);
        assertNull(network.getHvdcLine("L"));
        assertNull(network.getVscConverterStation("C1"));
        assertNull(network.getVscConverterStation("C2"));
    }
}
