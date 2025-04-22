/**
 * Copyright (c) 2025, Coreso SA (https://www.coreso.eu/) and TSCNET Services GmbH (https://www.tscnet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.tck;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Damien Jeandemange {@literal <damien.jeandemange at artelys.com>}
 */
public abstract class AbstractDcSwitchTest {

    private Network network;
    private DcNode dcNode1;
    private DcNode dcNode2;

    @BeforeEach
    void setup() {
        network = Network.create("test", "test");
        dcNode1 = network.newDcNode().setId("dcNode1").setNominalV(500.).add();
        dcNode2 = network.newDcNode().setId("dcNode2").setNominalV(500.).add();
    }

    @Test
    public void testAddAndRemove() {
        DcSwitch dcSwitch1 = network.newDcSwitch()
                .setId("dcSwitch1")
                .setDcNode1Id(dcNode1.getId())
                .setDcNode2Id(dcNode2.getId())
                .setOpen(false)
                .setRetained(false)
                .add();
        assertSame(IdentifiableType.DC_SWITCH, dcSwitch1.getType());
        assertEquals("dcSwitch1", dcSwitch1.getId());
        assertFalse(dcSwitch1.isOpen());
        assertFalse(dcSwitch1.isRetained());
        assertSame(dcNode1, dcSwitch1.getDcNode1());
        assertSame(dcNode2, dcSwitch1.getDcNode2());
        assertEquals(1, network.getDcSwitchCount());
        DcSwitch dcSwitch2 = network.newDcSwitch()
                .setId("dcSwitch2")
                .setDcNode1Id(dcNode1.getId())
                .setDcNode2Id(dcNode2.getId())
                .setOpen(true)
                .setRetained(true)
                .add();
        assertEquals("dcSwitch2", dcSwitch2.getId());
        assertTrue(dcSwitch2.isOpen());
        assertTrue(dcSwitch2.isRetained());
        assertSame(dcNode1, dcSwitch2.getDcNode1());
        assertSame(dcNode2, dcSwitch2.getDcNode2());
        List<DcSwitch> dcSwitchList = List.of(dcSwitch1, dcSwitch2);
        assertEquals(2, ((Collection<?>) network.getDcSwitches()).size());
        network.getDcSwitches().forEach(dcSwitch -> assertTrue(dcSwitchList.contains(dcSwitch)));
        network.getDcSwitchStream().forEach(dcSwitch -> assertTrue(dcSwitchList.contains(dcSwitch)));
        assertEquals(2, network.getDcSwitchCount());

        DcSwitchAdder dcSwitchDuplicateAdder = network.newDcSwitch()
                .setId("dcSwitch1")
                .setDcNode1Id(dcNode1.getId())
                .setDcNode2Id(dcNode2.getId())
                .setOpen(false)
                .setRetained(false);
        PowsyblException exception = assertThrows(PowsyblException.class, dcSwitchDuplicateAdder::add);
        assertTrue(Pattern.compile("The network test already contains an object '(\\w+)' with the id 'dcSwitch1'").matcher(exception.getMessage()).find());
        dcSwitch1.remove();
        assertNull(network.getDcSwitch("dcSwitch1"));
        assertEquals(1, network.getDcSwitchCount());
    }
}
