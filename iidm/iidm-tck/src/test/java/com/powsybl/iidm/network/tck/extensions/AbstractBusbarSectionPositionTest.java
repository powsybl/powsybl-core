/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.tck.extensions;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.BusbarSectionPosition;
import com.powsybl.iidm.network.extensions.BusbarSectionPositionAdder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Samir Romdhani {@literal <samir.romdhani at rte-france.com>}
 */
public abstract class AbstractBusbarSectionPositionTest {

    @Test
    void testExtension() {
        BusbarSectionPosition busbarSectionPosition = busbarSection().newExtension(BusbarSectionPositionAdder.class)
                .withBusbarIndex(1)
                .withSectionIndex(2)
                .add();
        assertEquals(1, busbarSectionPosition.getBusbarIndex());
        assertEquals(2, busbarSectionPosition.getSectionIndex());

        // Wrong Busbar index
        IllegalArgumentException e0 = assertThrows(IllegalArgumentException.class, () -> busbarSectionPosition.setBusbarIndex(-1));
        assertEquals("Busbar index (-1) has to be greater or equals to zero for busbar section BBS", e0.getMessage());

        // Right busbar index
        busbarSectionPosition.setBusbarIndex(10);
        busbarSectionPosition.setSectionIndex(5);
        assertEquals(10, busbarSectionPosition.getBusbarIndex());
        assertEquals(5, busbarSectionPosition.getSectionIndex());
    }

    protected BusbarSection busbarSection() {
        Network network = Network.create("test", "test");
        Substation s = network.newSubstation()
                .setId("S")
                .setCountry(Country.FR)
                .add();
        VoltageLevel vl = s.newVoltageLevel()
                .setId("VL")
                .setNominalV(400.0)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();
        return vl.getNodeBreakerView().newBusbarSection()
                .setId("BBS")
                .setNode(0)
                .add();
    }
}
