/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.action.ial.simulator.loadflow;

import com.powsybl.action.ial.simulator.EurostagTutorialExample1WithTemporaryLimitFactory;
import com.powsybl.iidm.network.Network;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
class CopyStateStrategyTest {
    @Test
    void test() {
        Network network = EurostagTutorialExample1WithTemporaryLimitFactory.create();
        CopyStateStrategy strategy = new CopyStateStrategy(network);
        String initialVariant = network.getVariantManager().getWorkingVariantId();

        strategy.createState("Test");
        assertNotEquals(initialVariant, network.getVariantManager().getWorkingVariantId());
        assertTrue(network.getVariantManager().getWorkingVariantId().startsWith("Test"));
        assertEquals(2, network.getVariantManager().getVariantIds().size());

        strategy.removeState();
        assertEquals(initialVariant, network.getVariantManager().getWorkingVariantId());
        assertEquals(1, network.getVariantManager().getVariantIds().size());
    }
}
