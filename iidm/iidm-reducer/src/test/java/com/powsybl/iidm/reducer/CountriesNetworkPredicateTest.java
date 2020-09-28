/*
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.reducer;

import com.powsybl.iidm.network.Country;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.FourCountriesNetworkFactory;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Sebastien Murgey {@literal <sebastien.murgey at rte-france.com>}
 */
public class CountriesNetworkPredicateTest {

    @Test
    public void test() {
        Network network = FourCountriesNetworkFactory.create();
        NetworkPredicate predicate = CountriesNetworkPredicate.of(Country.FR, Country.DE);

        assertFalse(predicate.test(network.getSubstation("Substation BE")));
        assertFalse(predicate.test(network.getSubstation("Substation NL")));
        assertFalse(predicate.test(network.getVoltageLevel("Voltage level BE")));
        assertFalse(predicate.test(network.getVoltageLevel("Voltage level NL")));

        assertTrue(predicate.test(network.getSubstation("Substation FR")));
        assertTrue(predicate.test(network.getSubstation("Substation DE")));
        assertTrue(predicate.test(network.getVoltageLevel("Voltage level FR")));
        assertTrue(predicate.test(network.getVoltageLevel("Voltage level DE")));
    }

}