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
import com.powsybl.iidm.network.DefaultNetworkListener;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.FourSubstationsNodeBreakerFactory;
import org.junit.After;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * @author Etienne Homer <etienne.homer at rte-france.com>
 */
public class RemoveVoltageLevelTest {

    private final Set<String> removedObjects = new HashSet<>();
    private final Set<String> beforeRemovalObjects = new HashSet<>();

    @After
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
    public void testRemoveVoltageLevel() {
        Network network = FourSubstationsNodeBreakerFactory.create();
        addListener(network);

        new RemoveVoltageLevelBuilder().withVoltageLevelId("S1VL1").build().apply(network);
        assertEquals(Set.of("TWT", "S1VL1_BBS", "S1VL1_BBS_TWT_DISCONNECTOR", "S1VL1", "S1VL1_LD1_BREAKER", "LD1", "S1VL1_TWT_BREAKER", "S1VL1_BBS_LD1_DISCONNECTOR"), beforeRemovalObjects);
        assertEquals(Set.of("TWT", "S1VL1_BBS", "S1VL1_BBS_TWT_DISCONNECTOR", "S1VL1", "S1VL1_LD1_BREAKER", "LD1", "S1VL1_TWT_BREAKER", "S1VL1_BBS_LD1_DISCONNECTOR"), removedObjects);
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
        removeUnknown.apply(network, false, Reporter.NO_OP);
        PowsyblException e = assertThrows(PowsyblException.class, () -> removeUnknown.apply(network, true, Reporter.NO_OP));
        assertEquals("Voltage level not found: UNKNOWN", e.getMessage());
    }
}
