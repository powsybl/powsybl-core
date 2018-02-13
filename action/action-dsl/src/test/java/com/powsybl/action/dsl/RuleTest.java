/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.action.dsl;

import org.junit.Test;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
public class RuleTest {

    @Test
    public void test() {
        Condition condition = Mockito.mock(Condition.class);

        Rule rule = new Rule("id", condition, 1, Collections.emptyList());
        assertEquals("id", rule.getId());
        assertSame(condition, rule.getCondition());
        assertEquals(1, rule.getLife());
        assertEquals(0, rule.getActions().size());

        assertNull(rule.getDescription());
        rule.setDescription("description");
        assertEquals("description", rule.getDescription());

        rule = new Rule("id", condition, 1, "a1", "a2");
        assertEquals(2, rule.getActions().size());
    }

    @Test
    public void testInvalid() {
        Condition condition = Mockito.mock(Condition.class);

        testInvalid(null, null, -1, null);
        testInvalid("id", null, -1, null);
        testInvalid("id", condition, -1, null);
        testInvalid("id", condition, 0, null);
    }

    private void testInvalid(String id, Condition condition, int life, List<String> actions) {
        try {
            new Rule(id, condition, life, actions);
            fail();
        } catch (RuntimeException ignored) {
        }
    }

}
