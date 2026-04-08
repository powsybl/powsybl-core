/**
 * Copyright (c) 2025, SuperGrid Institute (https://www.supergrid-institute.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.contingency;

import com.google.common.testing.EqualsTester;
import com.powsybl.contingency.list.ContingencyList;
import com.powsybl.iidm.modification.tripping.DcGroundTripping;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.DcDetailedNetworkFactory;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Denis Bonnand {@literal <denis.bonnand at supergrid-institute.com>}
 */
class DcGroundContingencyTest {

    @Test
    void test() {
        DcGroundContingency contingency = new DcGroundContingency("id");
        assertEquals("id", contingency.getId());
        assertEquals(ContingencyElementType.DC_GROUND, contingency.getType());

        assertNotNull(contingency.toModification());
        assertInstanceOf(DcGroundTripping.class, contingency.toModification());

        new EqualsTester()
                .addEqualityGroup(new DcGroundContingency("g1"), new DcGroundContingency("g1"))
                .testEquals();
    }

    @Test
    void test2() {
        Network network = DcDetailedNetworkFactory.createVscAsymmetricalMonopole();

        ContingencyList contingencyList = ContingencyList.of(
                Contingency.dcGround("dcGroundFr"),
                Contingency.dcGround("dcGroundGb"),
                Contingency.dcGround("UNKNOWN")
        );

        List<Contingency> contingencies = contingencyList.getContingencies(network);
        assertEquals(2, contingencies.size());

        DcGroundContingency dcGroundCtg = (DcGroundContingency) contingencies.getFirst().getElements().getFirst();
        assertEquals("dcGroundFr", dcGroundCtg.getId());

        dcGroundCtg = (DcGroundContingency) contingencies.get(1).getElements().getFirst();
        assertEquals("dcGroundGb", dcGroundCtg.getId());
    }
}
