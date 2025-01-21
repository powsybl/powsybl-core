/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification.topology;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.iidm.modification.AbstractNetworkModification;
import com.powsybl.iidm.modification.NetworkModification;
import com.powsybl.iidm.modification.NetworkModificationImpact;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.test.FourSubstationsNodeBreakerFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import static com.powsybl.iidm.modification.topology.TopologyTestUtils.createBbNetwork;
import static com.powsybl.iidm.modification.topology.TopologyTestUtils.createNbNetwork;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Etienne Homer {@literal <etienne.homer at rte-france.com>}
 */
class RemoveVoltageLevelTest extends AbstractModificationTest {

    private final Set<String> removedObjects = new HashSet<>();
    private final Set<String> beforeRemovalObjects = new HashSet<>();

    @AfterEach
    public void tearDown() {
        removedObjects.clear();
    }

    private void addListener(Network network) {
        network.addListener(new DefaultNetworkListener() {
            @Override
            public void beforeRemoval(Identifiable id) {
                beforeRemovalObjects.add(id.getId());
            }

            @Override
            public void afterRemoval(String id) {
                removedObjects.add(id);
            }
        });
    }

    @Test
    void testFaultyRemoveVoltageLevel() {
        Network network = Network.create("test", "test");
        var s = network.newSubstation().setId("s").add();

        // First voltage level (which will be removed)
        var vl1 = s.newVoltageLevel().setId("vl1").setTopologyKind(TopologyKind.NODE_BREAKER).setNominalV(225).add();
        vl1.getNodeBreakerView().newBusbarSection().setId("bbs1").setNode(0).add();
        vl1.getNodeBreakerView().newBusbarSection().setId("bbs2").setNode(1).add();
        vl1.getNodeBreakerView().newSwitch().setId("Coupler").setNode1(0).setNode2(1).setKind(SwitchKind.BREAKER).add();
        vl1.getNodeBreakerView().newSwitch().setId("d_l1_bbs1").setNode1(0).setNode2(2).setKind(SwitchKind.DISCONNECTOR).add();
        vl1.getNodeBreakerView().newSwitch().setId("d_l2_bbs1").setNode1(0).setNode2(3).setKind(SwitchKind.DISCONNECTOR).add();
        vl1.getNodeBreakerView().newSwitch().setId("d_l1_bbs2").setNode1(1).setNode2(2).setKind(SwitchKind.DISCONNECTOR).add();
        vl1.getNodeBreakerView().newSwitch().setId("d_l2_bbs2").setNode1(1).setNode2(3).setKind(SwitchKind.DISCONNECTOR).add();

        // Second voltage level
        var vl2 = s.newVoltageLevel().setId("vl2").setTopologyKind(TopologyKind.NODE_BREAKER).setNominalV(225).add();
        vl2.getNodeBreakerView().newBusbarSection().setId("bbs").setNode(0).add();
        vl2.getNodeBreakerView().newInternalConnection().setNode1(0).setNode2(1).add();
        vl2.getNodeBreakerView().newInternalConnection().setNode1(0).setNode2(2).add();

        // Parallel lines between voltage levels
        var line1 = network.newLine().setId("line1").setVoltageLevel1(vl1.getId()).setVoltageLevel2(vl2.getId()).setNode1(2).setNode2(1)
                .setR(0.01).setX(20.0).setG1(0.0).setB1(0.0).setG2(0.0).setB2(0.0).add();
        network.newLine(line1).setId("line2").setNode1(3).setNode2(2).add();

        addListener(network);

        new RemoveVoltageLevelBuilder().withVoltageLevelId(vl1.getId()).build().apply(network);

        assertEquals(Set.of("bbs1", "bbs2", "Coupler", "d_l2_bbs2", "vl1", "d_l2_bbs1", "d_l1_bbs2", "line2", "line1", "d_l1_bbs1"), removedObjects);
        assertNull(network.getVoltageLevel("vl1"));
    }

    @Test
    void testRemoveVoltageLevel() {
        Network network = FourSubstationsNodeBreakerFactory.create();
        ReportNode reportNode = ReportNode.newRootReportNode().withMessageTemplate("reportTestRemoveVL", "Testing reportNode on remove voltage level").build();
        addListener(network);

        new RemoveVoltageLevelBuilder().withVoltageLevelId("S1VL1").build().apply(network);
        assertEquals(Set.of("TWT", "S1VL1_BBS", "S1VL1_BBS_TWT_DISCONNECTOR", "S1VL1", "S1VL1_LD1_BREAKER", "LD1", "S1VL1_TWT_BREAKER", "S1VL1_BBS_LD1_DISCONNECTOR", "S1VL2_BBS1_TWT_DISCONNECTOR", "S1VL2_BBS2_TWT_DISCONNECTOR", "S1VL2_TWT_BREAKER"), beforeRemovalObjects);
        assertEquals(Set.of("TWT", "S1VL1_BBS", "S1VL1_BBS_TWT_DISCONNECTOR", "S1VL1", "S1VL1_LD1_BREAKER", "LD1", "S1VL1_TWT_BREAKER", "S1VL1_BBS_LD1_DISCONNECTOR", "S1VL2_BBS1_TWT_DISCONNECTOR", "S1VL2_BBS2_TWT_DISCONNECTOR", "S1VL2_TWT_BREAKER"), removedObjects);
        assertNull(network.getVoltageLevel("S1VL1"));
        assertNull(network.getTwoWindingsTransformer("TWT"));

        new RemoveVoltageLevelBuilder().withVoltageLevelId("S1VL2").build().apply(network);
        assertNull(network.getVoltageLevel("S1VL1"));
        assertNull(network.getVscConverterStation("LCC1"));
        assertNull(network.getHvdcLine("HVDC2"));

        new RemoveVoltageLevelBuilder().withVoltageLevelId("S2VL1").build().apply(network);
        assertNull(network.getVoltageLevel("S2VL1"));
        assertNull(network.getLine("LINE_S2S3"));
        assertNull(network.getHvdcLine("HVDC1"));
        assertNull(network.getVscConverterStation("VSC2"));

        RemoveVoltageLevel removeUnknown = new RemoveVoltageLevel("UNKNOWN");
        removeUnknown.apply(network, false, reportNode);
        PowsyblException e = assertThrows(PowsyblException.class, () -> removeUnknown.apply(network, true, reportNode));
        assertEquals("Voltage level not found: UNKNOWN", e.getMessage());
        assertEquals("voltageLevelNotFound", reportNode.getChildren().get(0).getMessageKey());
    }

    @Test
    void testRemoveVLRoundTripNB() throws IOException {
        Network network = createNbNetwork();
        NetworkModification modification = new RemoveVoltageLevelBuilder().withVoltageLevelId("C").build();
        modification.apply(network);
        writeXmlTest(network, "/eurostag-remove-voltage-level-nb.xml");
    }

    @Test
    void testRemoveVLRoundTriBB() throws IOException {
        Network network = createBbNetwork();
        NetworkModification modification = new RemoveVoltageLevelBuilder().withVoltageLevelId("VLGEN").build();
        modification.apply(network);
        writeXmlTest(network, "/eurostag-remove-voltage-level-bb.xml");
    }

    @Test
    void testGetName() {
        AbstractNetworkModification networkModification = new RemoveVoltageLevelBuilder().withVoltageLevelId("VLGEN").build();
        assertEquals("RemoveVoltageLevel", networkModification.getName());
    }

    @Test
    void testHasImpact() {
        Network network = FourSubstationsNodeBreakerFactory.create();

        NetworkModification modification1 = new RemoveVoltageLevelBuilder().withVoltageLevelId("WRONG_ID").build();
        assertEquals(NetworkModificationImpact.CANNOT_BE_APPLIED, modification1.hasImpactOnNetwork(network));

        NetworkModification modification2 = new RemoveVoltageLevelBuilder().withVoltageLevelId("S1VL1").build();
        assertEquals(NetworkModificationImpact.HAS_IMPACT_ON_NETWORK, modification2.hasImpactOnNetwork(network));
    }
}
