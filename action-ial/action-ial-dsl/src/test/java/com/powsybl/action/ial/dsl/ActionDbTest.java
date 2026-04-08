/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.action.ial.dsl;

import com.powsybl.contingency.Contingency;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * @author Mathieu Bague {@literal <mathieu.bague at rte-france.com>}
 */
class ActionDbTest {

    @Test
    void testContingencies() {
        ActionDb actionDb = new ActionDb();
        Contingency contingency = new Contingency("id");
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
    void testDuplicateContingency() {
        ActionDb actionDb = new ActionDb();
        Contingency c1 = new Contingency("c1");
        actionDb.addContingency(c1);

        assertThatExceptionOfType(ActionDslException.class)
                .isThrownBy(() -> actionDb.addContingency(c1))
                .withMessage("Contingency 'c1' is defined several times");
    }

    @Test
    void testRules() {
        Rule rule = Mockito.mock(Rule.class);

        ActionDb actionDb = new ActionDb();
        actionDb.addRule(rule);
        assertEquals(1, actionDb.getRules().size());
    }

    @Test
    void testActions() {
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
