/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.contingency;

import com.google.common.testing.EqualsTester;
import com.powsybl.contingency.tasks.StaticVarCompensatorTripping;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Teofil Calin BANC <teofil-calin.banc at rte-france.com>
 */
public class StaticVarCompensatorContingencyTest {

    @Test
    public void test() {
        StaticVarCompensatorContingency contingency = new StaticVarCompensatorContingency("id");
        assertEquals("id", contingency.getId());
        assertEquals(ContingencyElementType.STATIC_VAR_COMPENSATOR, contingency.getType());

        assertNotNull(contingency.toTask());
        assertTrue(contingency.toTask() instanceof StaticVarCompensatorTripping);

        new EqualsTester()
                .addEqualityGroup(new StaticVarCompensatorContingency("svc1"), new StaticVarCompensatorContingency("svc1"))
                .addEqualityGroup(new StaticVarCompensatorContingency("svc2"), new StaticVarCompensatorContingency("svc2"))
                .testEquals();
    }
}
