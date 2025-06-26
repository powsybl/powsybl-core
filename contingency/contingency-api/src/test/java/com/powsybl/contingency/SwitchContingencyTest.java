/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.contingency;

import com.google.common.testing.EqualsTester;
import com.powsybl.iidm.modification.tripping.SwitchTripping;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Switch;
import com.powsybl.iidm.network.test.FourSubstationsNodeBreakerFactory;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Etienne Lesot {@literal <etienne.lesot@rte-france.com>}
 */
class SwitchContingencyTest {
    @Test
    void test() {
        var switchContingency = new SwitchContingency("switch");
        assertEquals("switch", switchContingency.getId());
        assertEquals(ContingencyElementType.SWITCH, switchContingency.getType());

        assertNotNull(switchContingency.toModification());
        assertInstanceOf(SwitchTripping.class, switchContingency.toModification());

        new EqualsTester()
            .addEqualityGroup(new SwitchContingency("foo"), new SwitchContingency("foo"))
            .addEqualityGroup(new SwitchContingency("bar"), new SwitchContingency("bar"))
            .testEquals();
    }

    @Test
    void testContingencyElement() {
        Network network = FourSubstationsNodeBreakerFactory.create();
        Switch networkSwitch = network.getSwitch("S1VL1_LD1_BREAKER");
        assertNotNull(networkSwitch);
        ContingencyElement element = ContingencyElementFactory.create(networkSwitch);
        assertNotNull(element);
        assertInstanceOf(SwitchContingency.class, element);
        assertEquals("S1VL1_LD1_BREAKER", element.getId());
        assertEquals(ContingencyElementType.SWITCH, element.getType());
    }

    @Test
    void testContingencyElementNotFound() {
        Network network = FourSubstationsNodeBreakerFactory.create();
        ContingencyElement element = new SwitchContingency("id");
        Contingency contingency = new Contingency("contingencyId", "contingencyName", List.of(element));
        assertFalse(contingency.isValid(network));
        ContingencyElement element2 = new SwitchContingency("S1VL1_LD1_BREAKER");
        Contingency contingency2 = new Contingency("contingencyId2", "contingencyName2", List.of(element2));
        assertTrue(contingency2.isValid(network));
    }
}
