/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.action.dsl;

import com.powsybl.action.dsl.ast.BooleanLiteralNode;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

/**
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
public class ExpressionConditionTest {

    @Test
    public void test() {
        ExpressionCondition condition = new ExpressionCondition(BooleanLiteralNode.TRUE);

        assertEquals(ConditionType.EXPRESSION, condition.getType());
        assertSame(BooleanLiteralNode.TRUE, condition.getNode());
    }

    @Test(expected = NullPointerException.class)
    public void testNull() {
        new ExpressionCondition(null);
    }
}
