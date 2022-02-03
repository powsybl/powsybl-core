/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.contingency;

import com.google.common.testing.EqualsTester;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.SvcTestCaseFactory;
import com.powsybl.network.modification.tripping.StaticVarCompensatorTripping;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Teofil Calin BANC <teofil-calin.banc at rte-france.com>
 */
public class StaticVarCompensatorContingencyTest {

    @Test
    public void test() {
        Contingency contingency = Contingency.staticVarCompensator("id");
        assertEquals("id", contingency.getId());
        assertEquals(1, contingency.getElements().size());

        StaticVarCompensatorContingency svcContingency = new StaticVarCompensatorContingency("id");
        assertEquals("id", svcContingency.getId());
        assertEquals(ContingencyElementType.STATIC_VAR_COMPENSATOR, svcContingency.getType());

        assertNotNull(svcContingency.toTask());
        assertTrue(svcContingency.toTask() instanceof StaticVarCompensatorTripping);

        new EqualsTester()
                .addEqualityGroup(new StaticVarCompensatorContingency("svc1"), new StaticVarCompensatorContingency("svc1"))
                .addEqualityGroup(new StaticVarCompensatorContingency("svc2"), new StaticVarCompensatorContingency("svc2"))
                .testEquals();
    }

    @Test
    public void test2() {
        Network network = SvcTestCaseFactory.create();
        ContingencyList contingencyList = ContingencyList.of(Contingency.staticVarCompensator("SVC2"), Contingency.staticVarCompensator("bbs2"));
        List<Contingency> contingencies = contingencyList.getContingencies(network);
        assertEquals(1, contingencies.size());

        StaticVarCompensatorContingency svcCtg = (StaticVarCompensatorContingency) contingencies.get(0).getElements().get(0);
        assertEquals("SVC2", svcCtg.getId());
    }
}
