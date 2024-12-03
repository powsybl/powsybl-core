/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.cgmes.conversion.test;

import com.powsybl.cgmes.conversion.Conversion;
import com.powsybl.commons.test.AbstractSerDeTest;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Switch;
import com.powsybl.iidm.network.SwitchKind;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */

class SwitchConversionTest extends AbstractSerDeTest {

    @Test
    void jumperImportTest() {
        Network network = Network.read("jumperTest.xml", getClass().getResourceAsStream("/jumperTest.xml"));

        String cgmesSwitchType = Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "switchType";

        Switch switch1 = network.getSwitch("Jumper1");
        assertEquals(SwitchKind.DISCONNECTOR, switch1.getKind());
        assertEquals("Jumper", switch1.getProperty(cgmesSwitchType));
        assertEquals("opened jumper", switch1.getNameOrId());
        assertTrue(switch1.isOpen());
        assertFalse(switch1.isRetained());

        Switch switch2 = network.getSwitch("Jumper2");
        assertEquals(SwitchKind.DISCONNECTOR, switch2.getKind());
        assertEquals("Jumper", switch2.getProperty(cgmesSwitchType));
        assertEquals("closed jumper", switch2.getNameOrId());
        assertFalse(switch2.isOpen());
        assertTrue(switch2.isRetained());
    }
}
