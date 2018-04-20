/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.action.dsl;

import com.powsybl.contingency.Contingency;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

/**
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
public class ActionDbTest {

    @Test
    public void testContingencies() {
        Contingency contingency = Mockito.mock(Contingency.class);
        Mockito.when(contingency.getId()).thenReturn("id");

        ActionDb actionDb = new ActionDb();
        actionDb.addContingency(contingency);
        assertEquals(1, actionDb.getContingencies().size());
        assertSame(contingency, actionDb.getContingency("id"));

        try {
            actionDb.getContingency("id2");
            fail();
        } catch (RuntimeException ignored) {
        }
    }

    @Test
    public void testRules() {
        Rule rule = Mockito.mock(Rule.class);

        ActionDb actionDb = new ActionDb();
        actionDb.addRule(rule);
        assertEquals(1, actionDb.getRules().size());
    }

    @Test
    public void testActions() {
        Action action = Mockito.mock(Action.class);
        Mockito.when(action.getId()).thenReturn("id");

        ActionDb actionDb = new ActionDb();
        actionDb.addAction(action);
        assertSame(action, actionDb.getAction("id"));

        try {
            actionDb.getAction("id2");
            fail();
        } catch (RuntimeException ignored) {
        }
    }
}
