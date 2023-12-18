/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification.topology;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.reporter.ReporterModel;
import com.powsybl.iidm.network.DefaultNetworkListener;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Maissa Souissi {@literal <maissa.souissi at rte-france.com>}
 */
class RemoveSubstationTest extends AbstractModificationTest {
    private final List<String> removedObjects = new ArrayList<>();

    @AfterEach
    public void tearDown() {
        removedObjects.clear();
    }

    private void addListener(Network network) {
        network.addListener(new DefaultNetworkListener() {
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

        assertEquals(List.of("NHV1_NHV2_1", "NHV1_NHV2_2", "NHV2_NLOAD", "NHV2", "VLHV2", "LOAD", "NLOAD", "VLLOAD", "P2"), removedObjects);
        assertNull(network.getSubstation("P2"));
        assertNull(network.getVoltageLevel("NHV2"));
        assertNull(network.getBusBreakerView().getBus("nload"));
        assertNull(network.getTwoWindingsTransformer("NHV2_NLOAD"));
    }

    @Test
    void testRemoveSubstationWithManyVL() {
        Network network = EurostagTutorialExample1Factory.create();
        addListener(network);
        new RemoveSubstationBuilder().withSubstationId("P1").build().apply(network);

        assertEquals(List.of("NGEN_NHV1", "GEN", "NGEN", "VLGEN", "NHV1_NHV2_1", "NHV1_NHV2_2", "NHV1", "VLHV1", "P1"), removedObjects);
        assertNull(network.getSubstation("P1"));
        assertNull(network.getVoltageLevel("VLGEN"));
        assertNull(network.getVoltageLevel("VLHV1"));
    }

    @Test
    void testRemoveUnknownSubstation() throws IOException {
        RemoveSubstation removeUnknown = new RemoveSubstationBuilder()
                .withSubstationId("UNKNOWN")
                .build();
        ReporterModel reporter = new ReporterModel("reportTestRemoveUnknownSubstation", "Testing reporter on removing unknown substation");
        Network network = EurostagTutorialExample1Factory.create();
        removeUnknown.apply(network, false, reporter);
        PowsyblException e = assertThrows(PowsyblException.class, () -> removeUnknown.apply(network, true, reporter));
        assertEquals("Substation not found: UNKNOWN", e.getMessage());
        testReporter(reporter, "/reporter/remove-unknown-substation-report.txt");
    }
}
