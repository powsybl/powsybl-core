/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.action.dsl;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.network.modification.NetworkModification;
import com.powsybl.network.modification.tripping.GeneratorTripping;
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
        NetworkModification mock = Mockito.mock(NetworkModification.class);
        List<NetworkModification> modifications = new ArrayList<>();

        Action action = new Action("id");
        assertEquals("id", action.getId());
        assertEquals(0, action.getTasks().size());

        action = new Action("id", modifications);
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

    private void testInvalid(String id, List<NetworkModification> modifications) {
        try {
            new Action(id, modifications);
            fail();
        } catch (NullPointerException ignored) {
        }
    }

    @Test
    public void testRun() {
        Network network = EurostagTutorialExample1Factory.create();
        assertTrue(network.getGenerator("GEN").getTerminal().isConnected());

        NetworkModification task = new GeneratorTripping("GEN");
        Action action = new Action("action", Collections.singletonList(task));
        action.run(network);
        assertFalse(network.getGenerator("GEN").getTerminal().isConnected());
    }
}
