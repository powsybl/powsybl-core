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
public abstract class AbstractDcGroundTest {

    private Network network;
    private DcNode dcNode;

    @BeforeEach
    void setup() {
        network = Network.create("test", "test");
        dcNode = network.newDcNode().setId("dcNode").setNominalV(1.).add();
    }

    @Test
    public void testAddAndRemove() {
        DcGround dcGround1 = network.newDcGround()
                .setId("dcGround1")
                .setDcNodeId(dcNode.getId())
                .setConnected(true)
                .setR(0.1)
                .add();
        assertSame(IdentifiableType.DC_GROUND, dcGround1.getType());
        assertEquals("dcGround1", dcGround1.getId());
        assertTrue(dcGround1.getDcTerminal().isConnected());
        assertSame(dcGround1, dcGround1.getDcTerminal().getDcConnectable());
        assertSame(dcNode, dcGround1.getDcTerminal().getDcNode());
        assertEquals(1, network.getDcGroundCount());
        assertEquals(0.1, dcGround1.getR());
        DcGround dcGround2 = network.newDcGround()
                .setId("dcGround2")
                .setDcNodeId(dcNode.getId())
                .setConnected(false)
                .setR(0.2)
                .add();
        assertEquals("dcGround2", dcGround2.getId());
        assertEquals(0.2, dcGround2.getR());
        assertFalse(dcGround2.getDcTerminal().isConnected());
        assertSame(dcNode, dcGround2.getDcTerminal().getDcNode());
        List<DcGround> dcGroundList = List.of(dcGround1, dcGround2);
        assertEquals(2, ((Collection<?>) network.getDcGrounds()).size());
        network.getDcGrounds().forEach(dcGround -> assertTrue(dcGroundList.contains(dcGround)));
        network.getDcGroundStream().forEach(dcGround -> assertTrue(dcGroundList.contains(dcGround)));
        assertEquals(2, network.getDcGroundCount());
        DcGroundAdder dcGroundDuplicateAdder = network.newDcGround()
                .setId("dcGround1")
                .setDcNodeId(dcNode.getId())
                .setConnected(false)
                .setR(0.2);
        PowsyblException exception = assertThrows(PowsyblException.class, dcGroundDuplicateAdder::add);
        assertTrue(Pattern.compile("The network test already contains an object '(\\w+)' with the id 'dcGround1'").matcher(exception.getMessage()).find());
        dcGround1.remove();
        assertNull(network.getDcGround("dcGround1"));
        assertEquals(1, network.getDcGroundCount());
    }
}
