/**
 * Copyright (c) 2025, SuperGrid Institute (https://www.supergrid-institute.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.contingency;

import com.google.common.testing.EqualsTester;
import com.powsybl.contingency.contingency.list.ContingencyList;
import com.powsybl.iidm.modification.tripping.DcNodeTripping;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.DcDetailedNetworkFactory;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Denis Bonnand {@literal <denis.bonnand at supergrid-institute.com>}
 */
class DcNodeContingencyTest {

    @Test
    void test() {
        Contingency contingency = Contingency.dcNode("idDcNode");
        assertEquals("idDcNode", contingency.getId());
        assertEquals(1, contingency.getElements().size());
        assertEquals(ContingencyElementType.DC_NODE, contingency.getElements().getFirst().getType());

        DcNodeContingency dcNodeContingency = new DcNodeContingency("idDcNode");
        assertEquals("idDcNode", dcNodeContingency.getId());
        assertEquals(ContingencyElementType.DC_NODE, dcNodeContingency.getType());

        assertNotNull(dcNodeContingency.toModification());
        assertInstanceOf(DcNodeTripping.class, dcNodeContingency.toModification());

        new EqualsTester()
                .addEqualityGroup(new DcNodeContingency("dcNode1"), new DcNodeContingency("dcNode1"))
                .addEqualityGroup(new DcNodeContingency("dcNode2"), new DcNodeContingency("dcNode2"))
                .testEquals();
    }

    @Test
    void test2() {
        Network network = DcDetailedNetworkFactory.createVscAsymmetricalMonopole();
        ContingencyBuilder builder = new ContingencyBuilder("dcNodeGbNeg");
        builder.addIdentifiable(network.getDcNode("dcNodeGbNeg"));
        ContingencyList contingencyList = ContingencyList.of(Contingency.dcNode("dcNodeGbPos"), builder.build());
        List<Contingency> contingencies = contingencyList.getContingencies(network);
        assertEquals(2, contingencies.size());

        DcNodeContingency dcNodeCtg = (DcNodeContingency) contingencies.getFirst().getElements().getFirst();
        assertEquals("dcNodeGbPos", dcNodeCtg.getId());
        DcNodeContingency dcNodeCtg2 = (DcNodeContingency) contingencies.get(1).getElements().getFirst();
        assertEquals("dcNodeGbNeg", dcNodeCtg2.getId());
    }
}
