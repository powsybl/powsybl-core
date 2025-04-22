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
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Damien Jeandemange {@literal <damien.jeandemange at artelys.com>}
 */
public abstract class AbstractDcNodeTest {

    @Test
    public void testAddAndRemove() {
        Network network = Network.create("test", "test");
        String dcNode1Id = "dcNode1";
        String dcNode2Id = "dcNode2";
        DcNode dcNode1 = network.newDcNode().setId(dcNode1Id).setNominalV(500.).add();
        assertSame(IdentifiableType.DC_NODE, dcNode1.getType());
        assertEquals(dcNode1.getId(), dcNode1Id);
        assertEquals(500., dcNode1.getNominalV());
        assertSame(network.getDcNode(dcNode1Id), dcNode1);
        assertEquals(1, network.getDcNodeCount());
        DcNode dcNode2 = network.newDcNode().setId(dcNode2Id).setNominalV(510.).add();
        assertEquals(dcNode2.getId(), dcNode2Id);
        assertEquals(510., dcNode2.getNominalV());
        assertSame(network.getDcNode(dcNode2Id), dcNode2);
        List<DcNode> dcNodeList = List.of(dcNode1, dcNode2);
        assertEquals(2, ((Collection<?>) network.getDcNodes()).size());
        network.getDcNodes().forEach(dcNode -> assertTrue(dcNodeList.contains(dcNode)));
        network.getDcNodeStream().forEach(dcNode -> assertTrue(dcNodeList.contains(dcNode)));
        assertEquals(2, network.getDcNodeCount());
        DcNodeAdder dcNodeDuplicateAdder = network.newDcNode().setId(dcNode1Id);
        PowsyblException exception = assertThrows(PowsyblException.class, dcNodeDuplicateAdder::add);
        assertTrue(Pattern.compile("The network test already contains an object '(\\w+)' with the id 'dcNode1'").matcher(exception.getMessage()).find());
        assertEquals(2, network.getDcNodeCount());
        dcNode1.remove();
        assertNull(network.getDcNode(dcNode1Id));
        assertEquals(1, network.getDcNodeCount());
    }
}
