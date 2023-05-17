/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification.topology;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.reporter.Reporter;
import com.powsybl.commons.test.AbstractConverterTest;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Substation;
import com.powsybl.iidm.network.TopologyKind;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.test.HvdcTestNetwork;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

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
    void testRemoveHvdcLineLccWithShuntCompensator() {
        Network network = HvdcTestNetwork.createLcc();
        addVoltageLevelWithShuntCompensator(network);
        new RemoveHvdcLineBuilder().withHvdcLineId("L").withShuntCompensatorIds(List.of("C1_Filter1", "C1_Filter2", "C5_Filter5")).build().apply(network);
        assertNull(network.getHvdcLine("L"));
        assertNull(network.getLccConverterStation("C1"));
        assertNull(network.getShuntCompensator("C1_Filter1"));
        assertNull(network.getShuntCompensator("C1_Filter2"));
        assertNotNull(network.getShuntCompensator("C5_Filter5"));
    }

    @Test
    void testRemoveHvdcLineVsc() {
        Network network = HvdcTestNetwork.createVsc();
        new RemoveHvdcLineBuilder().withHvdcLineId("L").build().apply(network);
        assertNull(network.getHvdcLine("L"));
        assertNull(network.getVscConverterStation("C1"));
        assertNull(network.getVscConverterStation("C2"));
    }

    @Test
    void testRemoveHvdcLineVscWithIgnoredShunt() {
        Network network = HvdcTestNetwork.createVsc();
        addVoltageLevelWithShuntCompensator(network);
        new RemoveHvdcLineBuilder().withHvdcLineId("L").withShuntCompensatorIds("C5_Filter5").build().apply(network);
        assertNull(network.getHvdcLine("L"));
        assertNull(network.getVscConverterStation("C1"));
        assertNull(network.getVscConverterStation("C2"));
        assertNotNull(network.getShuntCompensator("C5_Filter5"));
    }

    @Test
    void testRemoveHvdcLineUnknownShunt() {
        Network network = HvdcTestNetwork.createLcc();
        RemoveHvdcLine removeHvdcLine = new RemoveHvdcLineBuilder().withHvdcLineId("L").withShuntCompensatorIds("UnknownShunt").build();
        PowsyblException e = assertThrows(PowsyblException.class, () -> removeHvdcLine.apply(network, true, Reporter.NO_OP));
        assertEquals("Shunt UnknownShunt not found", e.getMessage());
    }

    @Test
    void testRemoveHvdcLineUnknownLine() {
        Network network = Network.create("empty", "test");
        RemoveHvdcLine removeHvdcLine = new RemoveHvdcLineBuilder().withHvdcLineId("L").build();
        PowsyblException e = assertThrows(PowsyblException.class, () -> removeHvdcLine.apply(network, true, Reporter.NO_OP));
        assertEquals("Hvdc Line L not found", e.getMessage());
    }

    private static void addVoltageLevelWithShuntCompensator(Network network) {
        Substation s1 = network.getSubstation("S1");
        VoltageLevel vl3 = s1.newVoltageLevel()
                .setId("VL3")
                .setNominalV(400)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        vl3.getBusBreakerView().newBus()
                .setId("B5")
                .add();
        vl3.newShuntCompensator()
                .setId("C5_Filter5")
                .setName("Filter 5")
                .setConnectableBus("B5")
                .setBus("B5")
                .setSectionCount(1)
                .newLinearModel()
                .setBPerSection(1e-5)
                .setMaximumSectionCount(1)
                .add()
                .add();
    }
}
