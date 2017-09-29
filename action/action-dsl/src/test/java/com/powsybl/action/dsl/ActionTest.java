/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.action.dsl;

import com.powsybl.contingency.tasks.GeneratorTripping;
import com.powsybl.contingency.tasks.ModificationTask;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
public class ActionTest {

    @Test
    public void test() {
        ModificationTask mock = Mockito.mock(ModificationTask.class);
        List<ModificationTask> tasks = new ArrayList<>();

        Action action = new Action("id");
        assertEquals("id", action.getId());
        assertEquals(0, action.getTasks().size());

        action = new Action("id", tasks);
        assertEquals(0, action.getTasks().size());
        action.getTasks().add(mock);
        assertEquals(1, action.getTasks().size());

        assertNull(action.getDescription());
        action.setDescription("description");
        assertEquals("description", action.getDescription());
    }

    @Test
    public void testInvalid() {
        testInvalid(null, Collections.emptyList());
        testInvalid("id", null);
    }

    private void testInvalid(String id, List<ModificationTask> tasks) {
        try {
            new Action(id, tasks);
            fail();
        } catch (NullPointerException ignored) {
        }
    }

    @Test
    public void testRun() {
        Network network = EurostagTutorialExample1Factory.create();
        assertTrue(network.getGenerator("GEN").getTerminal().isConnected());

        ModificationTask task = new GeneratorTripping("GEN");
        Action action = new Action("action", Collections.singletonList(task));
        action.run(network, null);
        assertFalse(network.getGenerator("GEN").getTerminal().isConnected());
    }
}
