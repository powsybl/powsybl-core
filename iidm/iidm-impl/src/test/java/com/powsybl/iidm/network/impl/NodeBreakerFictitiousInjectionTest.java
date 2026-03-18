/*
 * Copyright (c) 2026, RTE (https://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.NetworkTest1Factory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Sylvestre Prabakaran {@literal <sylvestre.prabakaran at rte-france.com>}
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
class NodeBreakerFictitiousInjectionTest {

    @Test
    void testSetFictitiousP0AndFictitiousQ0() {
        // Testing setFictitious with multiple variants (initialState has fictitious values and duplicateState has no fictitious values)
        Network network = NetworkTest1Factory.create();
        String initialVariantId = network.getVariantManager().getWorkingVariantId();
        String duplicateVariantId = "duplicateState";
        network.getVariantManager().cloneVariant(initialVariantId, duplicateVariantId);
        Bus bus = network.getVoltageLevel("voltageLevel1").getBusBreakerView().getBus("voltageLevel1_0");

        // Testing setFictitious in initialState, with fictitious injections
        network.getVariantManager().setWorkingVariant(initialVariantId);
        bus.setFictitiousP0(10);
        bus.setFictitiousQ0(20);
        assertEquals(10, bus.getFictitiousP0());
        assertEquals(20, bus.getFictitiousQ0());

        // Testing on a new variant
        String otherDuplicateVariantId = "otherDuplicateState";
        network.getVariantManager().cloneVariant(initialVariantId, otherDuplicateVariantId);
        network.getVariantManager().setWorkingVariant(otherDuplicateVariantId);
        bus.setFictitiousP0(5);
        bus.setFictitiousQ0(10);
        assertEquals(5, bus.getFictitiousP0());
        assertEquals(10, bus.getFictitiousQ0());

        // Testing setFictitious in duplicateState, which has no fictitiousInjections (but fictitiousInjectionsByNode is not empty)
        network.getVariantManager().setWorkingVariant(duplicateVariantId);
        assertEquals(0, bus.getFictitiousP0());
        assertEquals(0, bus.getFictitiousQ0());

        // Testing setFictitious back in initialState, after removing fictitiousInjections
        network.getVariantManager().setWorkingVariant(initialVariantId);
        assertEquals(10, bus.getFictitiousP0());
        assertEquals(20, bus.getFictitiousQ0());
        bus.setFictitiousP0(0.0);
        bus.setFictitiousQ0(0.0);
        assertEquals(0, bus.getFictitiousP0());
        assertEquals(0, bus.getFictitiousQ0());
    }
}
