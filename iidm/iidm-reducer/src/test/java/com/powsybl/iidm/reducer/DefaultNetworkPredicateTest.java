/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.reducer;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
public class DefaultNetworkPredicateTest {

    private final NetworkPredicate predicate = new DefaultNetworkPredicate();

    @Test
    public void testDefault() {
        Network network = EurostagTutorialExample1Factory.create();
        assertTrue(network.getSubstationStream().allMatch(predicate::test));
        assertTrue(network.getVoltageLevelStream().allMatch(predicate::test));
    }
}
