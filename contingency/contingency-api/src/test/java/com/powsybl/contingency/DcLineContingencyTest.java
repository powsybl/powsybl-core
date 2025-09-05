/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.contingency;

import com.google.common.testing.EqualsTester;
import com.powsybl.contingency.contingency.list.ContingencyList;
import com.powsybl.iidm.modification.tripping.DcLineTripping;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.DcDetailedNetworkFactory;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Mathieu Bague {@literal <mathieu.bague at rte-france.com>}
 */
class DcLineContingencyTest {

    @Test
    void test() {
        DcLineContingency contingency = new DcLineContingency("id");
        assertEquals("id", contingency.getId());
        assertNull(contingency.getVoltageLevelId());
        assertEquals(ContingencyElementType.DC_LINE, contingency.getType());

        assertNotNull(contingency.toModification());
        assertInstanceOf(DcLineTripping.class, contingency.toModification());

        contingency = new DcLineContingency("id", "dcNodeId");
        assertEquals("dcNodeId", contingency.getVoltageLevelId());

        new EqualsTester()
                .addEqualityGroup(new DcLineContingency("c1", "dn1"), new DcLineContingency("c1", "dn1"))
                .addEqualityGroup(new DcLineContingency("c2"), new DcLineContingency("c2"))
                .testEquals();
    }

    @Test
    void test2() {
        Network network = DcDetailedNetworkFactory.createVscSymmetricalMonopole();

        ContingencyList contingencyList = ContingencyList.of(
                Contingency.dcLine("dcLinePos"),
                Contingency.dcLine("UNKNOWN"),
                Contingency.dcLine("dcLineNeg", "UNKNOWN"),
                Contingency.dcLine("dcLineNeg", "dcNodeFrNeg")
        );

        List<Contingency> contingencies = contingencyList.getContingencies(network);
        assertEquals(2, contingencies.size());

        DcLineContingency dcLineCtg = (DcLineContingency) contingencies.get(0).getElements().get(0);
        assertEquals("dcLinePos", dcLineCtg.getId());
        assertNull(dcLineCtg.getVoltageLevelId());

        dcLineCtg = (DcLineContingency) contingencies.get(1).getElements().get(0);
        assertEquals("dcLineNeg", dcLineCtg.getId());
        assertEquals("dcNodeFrNeg", dcLineCtg.getVoltageLevelId());
    }
}
