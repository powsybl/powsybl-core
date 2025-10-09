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

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Mathieu Bague {@literal <mathieu.bague at rte-france.com>}
 */
class DefaultNetworkPredicateTest {

    private final NetworkPredicate predicate = new DefaultNetworkPredicate();

    @Test
    void testDefault() {
        Network network = EurostagTutorialExample1Factory.create();
        assertTrue(network.getSubstationStream().allMatch(predicate::test));
        assertTrue(network.getVoltageLevelStream().allMatch(predicate::test));
    }
}
