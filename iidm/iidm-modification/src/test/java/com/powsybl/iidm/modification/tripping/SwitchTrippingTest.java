/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification.tripping;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.computation.local.LocalComputationManager;
import com.powsybl.iidm.modification.AbstractNetworkModification;
import com.powsybl.iidm.modification.topology.DefaultNamingStrategy;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.FourSubstationsNodeBreakerFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class SwitchTrippingTest extends AbstractTrippingTest {

    @Test
    void switchTrippingTest() {
        Network network = FourSubstationsNodeBreakerFactory.create();

        assertFalse(network.getSwitch("S1VL1_LD1_BREAKER").isOpen());

        new SwitchTripping("S1VL1_LD1_BREAKER").apply(network);

        assertTrue(network.getSwitch("S1VL1_LD1_BREAKER").isOpen());
    }

    @Test
    void unknownSwitchTrippingTest() {
        Network network = FourSubstationsNodeBreakerFactory.create();

        SwitchTripping tripping = new SwitchTripping("switch");
        assertThrows(PowsyblException.class, () -> tripping.apply(network, new DefaultNamingStrategy(), true, LocalComputationManager.getDefault(), ReportNode.NO_OP));
        assertDoesNotThrow(() -> tripping.apply(network));
    }

    @Test
    void testGetName() {
        AbstractNetworkModification networkModification = new SwitchTripping("ID");
        assertEquals("SwitchTripping", networkModification.getName());
    }
}
