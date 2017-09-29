/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.contingency;

import com.powsybl.contingency.tasks.CompoundModificationTask;
import com.powsybl.contingency.tasks.ModificationTask;
import org.junit.Test;

import java.util.Arrays;
import java.util.Iterator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
public class ContingencyImplTest {

    @Test
    public void test() {
        ContingencyElement element1 = new BranchContingency("line");
        ContingencyElement element2 = new GeneratorContingency("generator");
        ContingencyImpl contingency = new ContingencyImpl("contingency", Arrays.asList(element1, element2));

        assertEquals("contingency", contingency.getId());
        assertEquals(2, contingency.getElements().size());

        Iterator<ContingencyElement> iterator = contingency.getElements().iterator();
        assertEquals(element1, iterator.next());
        assertEquals(element2, iterator.next());

        ModificationTask task = contingency.toTask();
        assertTrue(task instanceof CompoundModificationTask);
    }
}
