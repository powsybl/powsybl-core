/*
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.contingency;

import com.google.common.testing.EqualsTester;
import com.powsybl.contingency.tasks.DanglingLineTripping;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.DanglingLineNetworkFactory;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Sebastien Murgey {@literal <sebastien.murgey at rte-france.com>}
 */
public class DanglingLineContingencyTest {
    @Test
    public void test() {
        Contingency contingency = Contingency.danglingLine("id");
        assertEquals("id", contingency.getId());
        assertEquals(1, contingency.getElements().size());

        DanglingLineContingency dlContingency = new DanglingLineContingency("id");
        assertEquals("id", dlContingency.getId());
        assertEquals(ContingencyElementType.DANGLING_LINE, dlContingency.getType());

        assertNotNull(dlContingency.toTask());
        assertTrue(dlContingency.toTask() instanceof DanglingLineTripping);

        new EqualsTester()
                .addEqualityGroup(new DanglingLineContingency("dl1"), new DanglingLineContingency("dl1"))
                .addEqualityGroup(new DanglingLineContingency("dl2"), new DanglingLineContingency("dl2"))
                .testEquals();
    }

    @Test
    public void test2() {
        Network network = DanglingLineNetworkFactory.create();
        ContingencyList contingencyList = ContingencyList.of(Contingency.danglingLine("DL"), Contingency.danglingLine("unknown"));
        List<Contingency> contingencies = contingencyList.getContingencies(network);
        assertEquals(1, contingencies.size());

        DanglingLineContingency dlCtg = (DanglingLineContingency) contingencies.get(0).getElements().get(0);
        assertEquals("DL", dlCtg.getId());
    }
}
