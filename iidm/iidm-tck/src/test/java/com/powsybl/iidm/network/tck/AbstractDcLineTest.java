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
public abstract class AbstractDcLineTest {

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
        DcLine dcLine1 = network.newDcLine()
                .setId("dcLine1")
                .setDcNode1Id(dcNode1.getId())
                .setConnected1(true)
                .setDcNode2Id(dcNode2.getId())
                .setConnected2(true)
                .setR(1.1)
                .add();
        assertSame(IdentifiableType.DC_LINE, dcLine1.getType());
        assertEquals("dcLine1", dcLine1.getId());
        assertTrue(dcLine1.getDcTerminal1().isConnected());
        assertTrue(dcLine1.getDcTerminal2().isConnected());
        assertSame(dcNode1, dcLine1.getDcTerminal1().getDcNode());
        assertSame(dcNode2, dcLine1.getDcTerminal2().getDcNode());
        assertSame(dcLine1, dcLine1.getDcTerminal1().getDcConnectable());
        assertSame(dcLine1, dcLine1.getDcTerminal2().getDcConnectable());
        assertEquals(1, network.getDcLineCount());
        assertEquals(1.1, dcLine1.getR());
        DcLine dcLine2 = network.newDcLine()
                .setId("dcLine2")
                .setDcNode1Id(dcNode1.getId())
                .setConnected1(false)
                .setDcNode2Id(dcNode2.getId())
                .setConnected2(false)
                .setR(1.2)
                .add();
        assertEquals("dcLine2", dcLine2.getId());
        assertFalse(dcLine2.getDcTerminal1().isConnected());
        assertFalse(dcLine2.getDcTerminal2().isConnected());
        assertSame(dcNode1, dcLine2.getDcTerminal1().getDcNode());
        assertSame(dcNode2, dcLine2.getDcTerminal2().getDcNode());
        List<DcLine> dcLineList = List.of(dcLine1, dcLine2);
        assertEquals(2, ((Collection<?>) network.getDcLines()).size());
        network.getDcLines().forEach(dcLine -> assertTrue(dcLineList.contains(dcLine)));
        network.getDcLineStream().forEach(dcLine -> assertTrue(dcLineList.contains(dcLine)));
        assertEquals(2, network.getDcLineCount());
        assertEquals(1.2, dcLine2.getR());

        DcLineAdder dcLineDuplicateAdder = network.newDcLine()
                .setId("dcLine1")
                .setDcNode1Id(dcNode1.getId())
                .setConnected1(false)
                .setDcNode2Id(dcNode2.getId())
                .setConnected2(false)
                .setR(1.2);
        PowsyblException exception = assertThrows(PowsyblException.class, dcLineDuplicateAdder::add);
        assertTrue(Pattern.compile("The network test already contains an object '(\\w+)' with the id 'dcLine1'").matcher(exception.getMessage()).find());
        dcLine1.remove();
        assertNull(network.getDcLine("dcLine1"));
        assertEquals(1, network.getDcLineCount());
    }
}
