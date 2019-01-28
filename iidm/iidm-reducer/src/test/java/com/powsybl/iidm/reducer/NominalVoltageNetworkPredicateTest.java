/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.reducer;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertEquals;

/**
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
public class NominalVoltageNetworkPredicateTest {

    private static final String INVALID_MINIMAL_NOMINAL_VOLTAGE_MESSAGE = "Minimal nominal voltage must be greater or equal to zero";

    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    @Test
    public void testVHV() {
        NetworkPredicate predicate = new NominalVoltageNetworkPredicate(200.0, 400.0);

        Network network = EurostagTutorialExample1Factory.create();
        assertEquals(2, network.getSubstationStream().filter(predicate::test).count());
        assertEquals(2, network.getVoltageLevelStream().filter(predicate::test).count());
    }

    @Test
    public void testFailure() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Minimal nominal voltage is undefined");
        new NominalVoltageNetworkPredicate(Double.NaN, Double.NaN);

        thrown.expectMessage(INVALID_MINIMAL_NOMINAL_VOLTAGE_MESSAGE);
        new NominalVoltageNetworkPredicate(-400.0, Double.NaN);

        thrown.expectMessage("Maximal nominal voltage is undefined");
        new NominalVoltageNetworkPredicate(400.0, Double.NaN);

        thrown.expectMessage(INVALID_MINIMAL_NOMINAL_VOLTAGE_MESSAGE);
        new NominalVoltageNetworkPredicate(400.0, -400.0);

        thrown.expectMessage(INVALID_MINIMAL_NOMINAL_VOLTAGE_MESSAGE);
        new NominalVoltageNetworkPredicate(400.0, -400.0);

        thrown.expectMessage("Nominal voltage range is empty");
        new NominalVoltageNetworkPredicate(400.0, 0.0);
    }
}
