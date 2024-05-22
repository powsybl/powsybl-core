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
import com.powsybl.commons.report.ReportNodeRootBuilderImpl;
import com.powsybl.iidm.modification.NetworkModification;
import com.powsybl.iidm.network.DefaultNetworkListener;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.Network;
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
    void testRemoveVoltageLevel() {
        Network network = FourSubstationsNodeBreakerFactory.create();
        ReportNode reportNode = new ReportNodeRootBuilderImpl().withMessageTemplate("reportTestRemoveVL", "Testing reportNode on remove voltage level").build();
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
}
