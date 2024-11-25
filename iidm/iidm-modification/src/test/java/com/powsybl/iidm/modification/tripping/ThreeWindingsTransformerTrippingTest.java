/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification.tripping;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.modification.AbstractNetworkModification;
import com.powsybl.iidm.network.ThreeWindingsTransformer;
import com.powsybl.iidm.network.test.ThreeWindingsTransformerNetworkFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Yichen TANG {@literal <yichen.tang at rte-france.com>}
 */
class ThreeWindingsTransformerTrippingTest extends AbstractTrippingTest {

    @Test
    void test() {
        var network = ThreeWindingsTransformerNetworkFactory.create();
        ThreeWindingsTransformer twt3 = network.getThreeWindingsTransformer("3WT");
        assertTrue(twt3.getLeg1().getTerminal().isConnected());
        assertTrue(twt3.getLeg2().getTerminal().isConnected());
        assertTrue(twt3.getLeg3().getTerminal().isConnected());
        var tripping = new ThreeWindingsTransformerTripping("3WT");
        tripping.apply(network);
        assertFalse(twt3.getLeg1().getTerminal().isConnected());
        assertFalse(twt3.getLeg2().getTerminal().isConnected());
        assertFalse(twt3.getLeg3().getTerminal().isConnected());

        var notExistsTripping = new ThreeWindingsTransformerTripping("NOT_EXISTS");
        Exception e = assertThrows(PowsyblException.class, () -> notExistsTripping.apply(network));
        assertEquals("ThreeWindingsTransformer 'NOT_EXISTS' not found", e.getMessage());
    }

    @Test
    void testGetName() {
        AbstractNetworkModification networkModification = new ThreeWindingsTransformerTripping("ID");
        assertEquals("ThreeWindingsTransformerTripping", networkModification.getName());
    }
}
