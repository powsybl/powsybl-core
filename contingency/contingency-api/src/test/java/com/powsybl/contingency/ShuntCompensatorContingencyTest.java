/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.contingency;

import com.google.common.testing.EqualsTester;
import com.powsybl.contingency.tasks.ShuntCompensatorTripping;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Teofil Calin BANC <teofil-calin.banc at rte-france.com>
 */
public class ShuntCompensatorContingencyTest {

    @Test
    public void test() {
        ShuntCompensatorContingency contingency = new ShuntCompensatorContingency("id");
        assertEquals("id", contingency.getId());
        assertEquals(ContingencyElementType.SHUNT_COMPENSATOR, contingency.getType());

        assertNotNull(contingency.toTask());
        assertTrue(contingency.toTask() instanceof ShuntCompensatorTripping);

        new EqualsTester()
                .addEqualityGroup(new ShuntCompensatorContingency("sc1"), new ShuntCompensatorContingency("sc1"))
                .addEqualityGroup(new ShuntCompensatorContingency("sc2"), new ShuntCompensatorContingency("sc2"))
                .testEquals();
    }
}
