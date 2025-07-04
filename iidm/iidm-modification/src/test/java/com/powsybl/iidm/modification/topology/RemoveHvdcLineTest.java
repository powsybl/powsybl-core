/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification.topology;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.report.PowsyblCoreReportResourceBundle;
import com.powsybl.commons.test.PowsyblTestReportResourceBundle;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.commons.test.AbstractSerDeTest;
import com.powsybl.iidm.modification.AbstractNetworkModification;
import com.powsybl.iidm.modification.NetworkModification;
import com.powsybl.iidm.modification.NetworkModificationImpact;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Substation;
import com.powsybl.iidm.network.TopologyKind;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.test.HvdcTestNetwork;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Anis Touri {@literal <anis-1.touri@rte-france.com>}
 */
class RemoveHvdcLineTest extends AbstractSerDeTest {

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
        assertNotNull(network.getShuntCompensator("C5_Filter5")); // not removed because it is not in the same VLs as the Lcc
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
        ReportNode reportNode = ReportNode.newRootReportNode()
                .withResourceBundles(PowsyblTestReportResourceBundle.TEST_BASE_NAME, PowsyblCoreReportResourceBundle.BASE_NAME)
                .withMessageTemplate("reportTestRemoveHvdcLineWithUnknownShunt")
                .build();
        RemoveHvdcLine removeHvdcLine = new RemoveHvdcLineBuilder().withHvdcLineId("L").withShuntCompensatorIds("UnknownShunt").build();
        PowsyblException e = assertThrows(PowsyblException.class, () -> removeHvdcLine.apply(network, true, reportNode));
        assertEquals("Shunt UnknownShunt not found", e.getMessage());
        assertEquals("core.iidm.modification.notFoundShunt", reportNode.getChildren().get(0).getMessageKey());
        assertDoesNotThrow(() -> removeHvdcLine.apply(network, false, reportNode));
    }

    @Test
    void testRemoveHvdcLineUnknownLine() {
        Network network = Network.create("empty", "test");
        ReportNode reportNode = ReportNode.newRootReportNode()
                .withResourceBundles(PowsyblTestReportResourceBundle.TEST_BASE_NAME, PowsyblCoreReportResourceBundle.BASE_NAME)
                .withMessageTemplate("reportTestRemoveHvdcLineUnknownLine")
                .build();
        RemoveHvdcLine removeHvdcLine = new RemoveHvdcLineBuilder().withHvdcLineId("L").build();
        PowsyblException e = assertThrows(PowsyblException.class, () -> removeHvdcLine.apply(network, true, reportNode));
        assertEquals("Hvdc Line L not found", e.getMessage());
        assertEquals("core.iidm.modification.HvdcNotFound", reportNode.getChildren().get(0).getMessageKey());
        assertDoesNotThrow(() -> removeHvdcLine.apply(network, false, reportNode));
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

    @Test
    void testGetName() {
        AbstractNetworkModification networkModification = new RemoveHvdcLineBuilder().withHvdcLineId("L").build();
        assertEquals("RemoveHvdcLine", networkModification.getName());
    }

    @Test
    void testHasImpact() {
        Network network = HvdcTestNetwork.createLcc();

        NetworkModification modification1 = new RemoveHvdcLineBuilder().withHvdcLineId("WRONG_ID").build();
        assertEquals(NetworkModificationImpact.CANNOT_BE_APPLIED, modification1.hasImpactOnNetwork(network));

        NetworkModification modification2 = new RemoveHvdcLineBuilder().withHvdcLineId("L").build();
        assertEquals(NetworkModificationImpact.HAS_IMPACT_ON_NETWORK, modification2.hasImpactOnNetwork(network));
    }
}
