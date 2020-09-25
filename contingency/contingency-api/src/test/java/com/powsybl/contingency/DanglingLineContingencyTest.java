/*
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.contingency;

import com.google.common.testing.EqualsTester;
import com.powsybl.contingency.tasks.DanglingLineTripping;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Sebastien Murgey {@literal <sebastien.murgey at rte-france.com>}
 */
public class DanglingLineContingencyTest {
    @Test
    public void test() {
        DanglingLineContingency contingency = new DanglingLineContingency("id");
        assertEquals("id", contingency.getId());
        assertEquals(ContingencyElementType.DANGLING_LINE, contingency.getType());

        assertNotNull(contingency.toTask());
        assertTrue(contingency.toTask() instanceof DanglingLineTripping);

        new EqualsTester()
                .addEqualityGroup(new DanglingLineContingency("dl1"), new DanglingLineContingency("dl1"))
                .addEqualityGroup(new DanglingLineContingency("dl2"), new DanglingLineContingency("dl2"))
                .testEquals();
    }
}
