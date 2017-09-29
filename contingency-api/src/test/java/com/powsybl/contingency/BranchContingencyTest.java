/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.contingency;

import eu.itesla_project.contingency.tasks.BranchTripping;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

/**
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
public class BranchContingencyTest {

    @Test
    public void test() {
        BranchContingency contingency = new BranchContingency("id");
        assertEquals("id", contingency.getId());
        assertNull(contingency.getVoltageLevelId());
        assertEquals(ContingencyElementType.BRANCH, contingency.getType());

        assertNotNull(contingency.toTask());
        assertTrue(contingency.toTask() instanceof BranchTripping);

        contingency = new BranchContingency("id", "voltageLevelId");
        assertEquals("voltageLevelId", contingency.getVoltageLevelId());
    }
}
