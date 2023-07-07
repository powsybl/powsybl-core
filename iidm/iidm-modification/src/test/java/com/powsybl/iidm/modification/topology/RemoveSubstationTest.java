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
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.network.test.FourSubstationsNodeBreakerFactory;
import com.powsybl.iidm.network.test.HvdcTestNetwork;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Maissa Souissi <maissa.souissi at rte-france.com> eurostag-remove-voltage-level-bb.xml
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
    void testRemoveVoltageLevel() {
        Network network = HvdcTestNetwork.createBase();
        addListener(network);
        new RemoveSubstationBuilder().withSubstationId("S1").build().apply(network);
        assertEquals(Set.of("VL1", "S1", "B1"), beforeRemovalObjects);
        assertEquals(Set.of("VL1", "S1", "B1"), removedObjects);
        assertNull(network.getSubstation("S1"));
        assertNull(network.getVoltageLevel("VL1"));
    }

    @Test
    void testRemoveSubstationWithManyVL() throws IOException {
        Network network = EurostagTutorialExample1Factory.create();
        addListener(network);
        assertEquals("VLGEN", network.getVoltageLevel("VLGEN").getId());
        assertEquals("VLHV1", network.getVoltageLevel("VLHV1").getId());
        new RemoveSubstationBuilder().withSubstationId("P1").build().apply(network);
        System.out.println(network.getVoltageLevel("VLGEN"));
        assertEquals(Set.of("GEN", "NHV1", "P1", "VLHV1", "VLGEN", "NGEN_NHV1", "NGEN", "NHV1_NHV2_2", "NHV1_NHV2_1"), beforeRemovalObjects);
        assertEquals(Set.of("GEN", "NHV1", "P1", "VLHV1", "VLGEN", "NGEN_NHV1", "NGEN", "NHV1_NHV2_2", "NHV1_NHV2_1"), removedObjects);
        assertNull(network.getSubstation("P1"));
        assertNull(network.getVoltageLevel("VLGEN"));
        assertNull(network.getVoltageLevel("VLHV1"));
        assertNull(network.getTwoWindingsTransformer("NGEN_NHV1"));
    }

    @Test
    public void testRemoveSubstation() {
        Network network = FourSubstationsNodeBreakerFactory.create();
        addListener(network);
        assertNotNull(network.getSubstation("S1"));
        assertNotNull(network.getSubstation("S1").getVoltageLevels());
        new RemoveSubstationBuilder().withSubstationId("S1").build().apply(network);
        assertNull(network.getSubstation("S1"));
        assertNull(network.getVoltageLevel("S1VL1"));
        assertNull(network.getTwoWindingsTransformer("TWT"));
        assertNull(network.getVoltageLevel("S1VL1"));
        assertNull(network.getVscConverterStation("LCC1"));
        assertNull(network.getHvdcLine("HVDC2"));

        new RemoveSubstationBuilder().withSubstationId("S2").build().apply(network);
        assertNull(network.getSubstation("S2"));
        assertNull(network.getVoltageLevel("S2VL1"));
        assertNull(network.getLine("LINE_S2S3"));
        assertNull(network.getHvdcLine("HVDC1"));
        assertNull(network.getVscConverterStation("VSC2"));

        RemoveSubstation removeUnknown = new RemoveSubstation("UNKNOWN");
        removeUnknown.apply(network, false, Reporter.NO_OP);
        PowsyblException e = assertThrows(PowsyblException.class, () -> removeUnknown.apply(network, true, Reporter.NO_OP));
        assertEquals("Substation not found: UNKNOWN", e.getMessage());
    }
}
