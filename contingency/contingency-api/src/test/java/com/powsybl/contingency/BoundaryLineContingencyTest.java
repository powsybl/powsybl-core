/*
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.contingency;

import com.google.common.testing.EqualsTester;
import com.powsybl.contingency.contingency.list.ContingencyList;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.BoundaryLineNetworkFactory;
import com.powsybl.iidm.modification.tripping.BoundaryLineTripping;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Sebastien Murgey {@literal <sebastien.murgey at rte-france.com>}
 */
class BoundaryLineContingencyTest {
    @Test
    void test() {
        Contingency contingency = Contingency.boundaryLine("id");
        assertEquals("id", contingency.getId());
        assertEquals(1, contingency.getElements().size());

        BoundaryLineContingency dlContingency = new BoundaryLineContingency("id");
        assertEquals("id", dlContingency.getId());
        assertEquals(ContingencyElementType.BOUNDARY_LINE, dlContingency.getType());

        assertNotNull(dlContingency.toModification());
        assertTrue(dlContingency.toModification() instanceof BoundaryLineTripping);

        new EqualsTester()
                .addEqualityGroup(new BoundaryLineContingency("dl1"), new BoundaryLineContingency("dl1"))
                .addEqualityGroup(new BoundaryLineContingency("dl2"), new BoundaryLineContingency("dl2"))
                .testEquals();
    }

    @Test
    void test2() {
        Network network = BoundaryLineNetworkFactory.create();
        ContingencyList contingencyList = ContingencyList.of(Contingency.boundaryLine("DL"), Contingency.boundaryLine("unknown"));
        List<Contingency> contingencies = contingencyList.getContingencies(network);
        assertEquals(1, contingencies.size());

        BoundaryLineContingency dlCtg = (BoundaryLineContingency) contingencies.get(0).getElements().get(0);
        assertEquals("DL", dlCtg.getId());
    }
}
