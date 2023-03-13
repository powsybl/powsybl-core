/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification.topology;

import com.powsybl.commons.test.AbstractConverterTest;
import com.powsybl.iidm.modification.NetworkModification;
import com.powsybl.iidm.network.DefaultNetworkListener;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.HvdcTestNetwork;
import com.powsybl.iidm.xml.NetworkXml;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.powsybl.iidm.modification.topology.TopologyTestUtils.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Anis Touri <anis-1.touri@rte-france.com>
 */
class RemoveHvdcLineTest extends AbstractConverterTest {

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
    void testRemoveHvdcLineLcc() {
        Network network = HvdcTestNetwork.createLcc();
        addListener(network);
        new RemoveHvdcLineBuilder().withHvdcLineId("L", null).build().apply(network);
        assertEquals(Set.of("L", "C1", "C2"), beforeRemovalObjects);
        assertEquals(Set.of("L", "C1", "C2"), removedObjects);
        assertNull(network.getHvdcLine("L"));
        assertNull(network.getLccConverterStation("C1"));
        assertNotNull(network.getShuntCompensator("C1_Filter1"));
    }

    @Test
    void testRemoveHvdcLineLccWithMcs() {
        Network network = HvdcTestNetwork.createLcc();
        addListener(network);
        new RemoveHvdcLineBuilder().withHvdcLineId("L", List.of("C1_Filter1")).build().apply(network);
        assertEquals(Set.of("L", "C1", "C2", "C1_Filter1"), beforeRemovalObjects);
        assertEquals(Set.of("L", "C1", "C2", "C1_Filter1"), removedObjects);
        assertNull(network.getHvdcLine("L"));
        assertNull(network.getLccConverterStation("C1"));
        assertNull(network.getShuntCompensator("C1_Filter1"));
    }

    @Test
    void testRemoveHvdcLineVsc() {
        Network network = HvdcTestNetwork.createVsc();
        addListener(network);
        new RemoveHvdcLineBuilder().withHvdcLineId("L", null).build().apply(network);
        assertEquals(Set.of("L", "C1", "C2"), beforeRemovalObjects);
        assertEquals(Set.of("L", "C1", "C2"), removedObjects);
        assertNull(network.getHvdcLine("L"));
        assertNull(network.getVscConverterStation("C1"));
        assertNull(network.getVscConverterStation("C2"));
    }

    @Test
    void testRemoveHvdcWithLccConverterStation() throws IOException {
        Network network = HvdcTestNetwork.createLcc();
        NetworkModification modification = new RemoveHvdcLineBuilder().withHvdcLineId("L", null).build();
        modification.apply(network);
        roundTripXmlTest(network, NetworkXml::writeAndValidate, NetworkXml::validateAndRead,
                "/eurostag-remove-HVDC-Line-LCC.xml");
    }

    @Test
    void testRemoveHvdcWithLccConverterStationAndMcs() throws IOException {
        Network network = HvdcTestNetwork.createLcc();
        NetworkModification modification = new RemoveHvdcLineBuilder().withHvdcLineId("L", List.of("C1_Filter1", "C1_Filter2")).build();
        modification.apply(network);
        roundTripXmlTest(network, NetworkXml::writeAndValidate, NetworkXml::validateAndRead,
                "/eurostag-remove-HVDC-Line-LCC-MCS.xml");
    }

    @Test
    void testRemoveHvdcWithVscConverterStation() throws IOException {
        Network network = HvdcTestNetwork.createVsc();
        NetworkModification modification = new RemoveHvdcLineBuilder().withHvdcLineId("L", null).build();
        modification.apply(network);
        roundTripXmlTest(network, NetworkXml::writeAndValidate, NetworkXml::validateAndRead,
                "/eurostag-remove-HVDC-Line-VSC.xml");
    }
}
