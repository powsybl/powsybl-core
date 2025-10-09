/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.reducer;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Mathieu Bague {@literal <mathieu.bague at rte-france.com>}
 */
class NominalVoltageNetworkPredicateTest {

    private static final String INVALID_MINIMAL_NOMINAL_VOLTAGE_MESSAGE = "Minimal nominal voltage must be greater or equal to zero";
    private static final String INVALID_MAXIMAL_NOMINAL_VOLTAGE_MESSAGE = "Maximal nominal voltage must be greater or equal to zero";

    @Test
    void testVHV() {
        NetworkPredicate predicate = new NominalVoltageNetworkPredicate(200.0, 400.0);

        Network network = EurostagTutorialExample1Factory.create();
        assertEquals(2, network.getSubstationStream().filter(predicate::test).count());
        assertEquals(2, network.getVoltageLevelStream().filter(predicate::test).count());
    }

    @Test
    void testFailure() {
        IllegalArgumentException e;

        e = assertThrows(IllegalArgumentException.class, () -> new NominalVoltageNetworkPredicate(Double.NaN, Double.NaN));
        assertTrue(e.getMessage().contains("Minimal nominal voltage is undefined"));

        e = assertThrows(IllegalArgumentException.class, () -> new NominalVoltageNetworkPredicate(-400.0, Double.NaN));
        assertTrue(e.getMessage().contains(INVALID_MINIMAL_NOMINAL_VOLTAGE_MESSAGE));

        e = assertThrows(IllegalArgumentException.class, () -> new NominalVoltageNetworkPredicate(400.0, Double.NaN));
        assertTrue(e.getMessage().contains("Maximal nominal voltage is undefined"));

        e = assertThrows(IllegalArgumentException.class, () -> new NominalVoltageNetworkPredicate(400.0, -400.0));
        assertTrue(e.getMessage().contains(INVALID_MAXIMAL_NOMINAL_VOLTAGE_MESSAGE));

        e = assertThrows(IllegalArgumentException.class, () -> new NominalVoltageNetworkPredicate(400.0, 0.0));
        assertTrue(e.getMessage().contains("Nominal voltage range is empty"));
    }
}
