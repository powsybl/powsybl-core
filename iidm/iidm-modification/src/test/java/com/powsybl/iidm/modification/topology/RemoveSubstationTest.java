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
import com.powsybl.iidm.network.DefaultNetworkListener;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Maissa Souissi <maissa.souissi at rte-france.com>
 */
class RemoveSubstationTest extends AbstractConverterTest {
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
    void testRemoveSubstationAndItsEquipments() {
        Network network = EurostagTutorialExample1Factory.create();
        addListener(network);
        new RemoveSubstationBuilder().withSubstationId("P2").build().apply(network);
        Set<String> processedObjects = Set.of("NHV1_NHV2_1", "NHV1_NHV2_2", "NHV2_NLOAD", "NHV2", "VLHV2", "LOAD", "NLOAD", "VLLOAD", "P2");
        assertEquals(processedObjects, removedObjects);
        assertEquals(processedObjects, beforeRemovalObjects);
        assertNull(network.getSubstation("P2"));
        assertNull(network.getVoltageLevel("NHV2"));
        assertNull(network.getBusBreakerView().getBus("nload"));
        assertNull(network.getTwoWindingsTransformer("NHV2_NLOAD"));

        new RemoveSubstationBuilder().withSubstationId("P1").build().apply(network);
        assertNull(network.getSubstation("P1"));
        assertNull(network.getVoltageLevel("VLGEN"));
        assertNull(network.getVoltageLevel("VLHV1"));

        RemoveSubstation removeUnknown = new RemoveSubstation("UNKNOWN");
        removeUnknown.apply(network, false, Reporter.NO_OP);
        PowsyblException e = assertThrows(PowsyblException.class, () -> removeUnknown.apply(network, true, Reporter.NO_OP));
        assertEquals("Substation not found: UNKNOWN", e.getMessage());
    }
}
