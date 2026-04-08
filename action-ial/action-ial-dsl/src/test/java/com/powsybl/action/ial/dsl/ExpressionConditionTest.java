/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.action.ial.dsl;

import com.powsybl.dsl.ast.BooleanLiteralNode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Mathieu Bague {@literal <mathieu.bague at rte-france.com>}
 */
class ExpressionConditionTest {

    @Test
    void test() {
        ExpressionCondition condition = new ExpressionCondition(BooleanLiteralNode.TRUE);

        assertEquals(ConditionType.EXPRESSION, condition.getType());
        assertSame(BooleanLiteralNode.TRUE, condition.getNode());
    }

    @Test
    void testNull() {
        assertThrows(NullPointerException.class, () -> new ExpressionCondition(null));
    }
}
