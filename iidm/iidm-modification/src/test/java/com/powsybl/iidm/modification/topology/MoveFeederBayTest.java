/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification.topology;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Ghazwa Rehili {@literal <ghazwa.rehili at rte-france.com>}
 */
class MoveFeederBayTest {
    @Test
    void testMoveInjectionNodeBreaker() {
        Network network = Network.read("testNetworkNodeBreaker.xiidm", getClass().getResourceAsStream("/testNetworkNodeBreaker.xiidm"));
        assertEquals("vl1", network.getLoad("load1").getTerminal().getVoltageLevel().getId());
        new MoveFeederBayBuilder().withConnectableId("load1").withBusOrBusbarSectionId("bbs5").withVoltageLevelId("vl2")
                .withTerminal(network.getLoad("load1").getTerminal()).build().apply(network);
        assertEquals("vl2", network.getLoad("load1").getTerminal().getVoltageLevel().getId());
        new MoveFeederBayBuilder().withConnectableId("load1").withBusOrBusbarSectionId("bbs6").withVoltageLevelId("vl2")
                .withTerminal(network.getLoad("load1").getTerminal()).build().apply(network);
        assertEquals("vl2", network.getLoad("load1").getTerminal().getVoltageLevel().getId());
    }

    @Test
    void testMoveBranchNodeBreaker() {
        Network network = Network.read("testNetworkNodeBreaker.xiidm", getClass().getResourceAsStream("/testNetworkNodeBreaker.xiidm"));
        assertEquals("vl1", network.getLine("line1").getTerminal1().getVoltageLevel().getId());
        new MoveFeederBayBuilder().withConnectableId("line1").withBusOrBusbarSectionId("bbs5").withVoltageLevelId("vl2")
                .withTerminal(network.getLine("line1").getTerminal1()).build().apply(network);
        assertEquals("vl2", network.getLine("line1").getTerminal1().getVoltageLevel().getId());
    }

    @Test
    void testMoveInjectionBusBarBreaker() {
        Network network = EurostagTutorialExample1Factory.create();
        new MoveFeederBayBuilder().withConnectableId("GEN").withBusOrBusbarSectionId("NHV1").withVoltageLevelId("VLLOAD")
                .withTerminal(network.getGenerator("GEN").getTerminal()).build().apply(network);

    }

    @Test
    void testMoveBbsBreaker() {
        Network network = Network.read("testNetworkNodeBreaker.xiidm", getClass().getResourceAsStream("/testNetworkNodeBreaker.xiidm"));
        ReportNode reportNode = ReportNode.newRootReportNode().withMessageTemplate("reportTestMoveBbs", "Testing reportNode when trying to remove a busbar section").build();
        MoveFeederBay moveFeederBay = new MoveFeederBayBuilder().withConnectableId("bbs6").withBusOrBusbarSectionId("bbs5").withVoltageLevelId("vl1")
                .withTerminal(network.getLine("line1").getTerminal1()).build();
        PowsyblException e = assertThrows(PowsyblException.class, () -> moveFeederBay.apply(network, true, reportNode));
        assertEquals("BusbarSection connectables are not allowed as MoveFeederBay input: bbs6", e.getMessage());
    }
}
