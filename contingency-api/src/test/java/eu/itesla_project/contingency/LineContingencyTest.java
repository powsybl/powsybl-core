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

/**
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
public class LineContingencyTest {

    @Test
    public void test() {
        LineContingency contingency = new LineContingency("id");
        assertEquals("id", contingency.getId());
        assertNull(contingency.getSubstationId());
        assertEquals(ContingencyElementType.LINE, contingency.getType());

        assertNotNull(contingency.toTask());
        assertTrue(contingency.toTask() instanceof BranchTripping);

        contingency = new LineContingency("id", "substationId");
        assertEquals("substationId", contingency.getSubstationId());
    }
}
