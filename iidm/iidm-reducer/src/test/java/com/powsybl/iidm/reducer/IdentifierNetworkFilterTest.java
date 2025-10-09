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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Mathieu Bague {@literal <mathieu.bague at rte-france.com>}
 */
class IdentifierNetworkFilterTest {

    @Test
    void test() {
        Network network = EurostagTutorialExample1Factory.create();

        NetworkPredicate predicate = IdentifierNetworkPredicate.of("P2");
        assertFalse(predicate.test(network.getSubstation("P1")));
        assertFalse(predicate.test(network.getVoltageLevel("VLGEN")));
        assertFalse(predicate.test(network.getVoltageLevel("VLHV1")));

        assertTrue(predicate.test(network.getSubstation("P2")));
        assertTrue(predicate.test(network.getVoltageLevel("VLLOAD")));
        assertTrue(predicate.test(network.getVoltageLevel("VLHV2")));
    }
}
