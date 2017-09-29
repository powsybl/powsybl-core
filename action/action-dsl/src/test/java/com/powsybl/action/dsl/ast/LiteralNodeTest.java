/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.action.dsl.ast;

import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

/**
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
public class LiteralNodeTest {

    @Test
    public void testFloat() {
        FloatLiteralNode node = new FloatLiteralNode(1.0f);
        assertSame(LiteralType.FLOAT, node.getType());
        assertEquals(Float.class, node.getValue().getClass());
        assertEquals(1.0f, node.getValue());
    }

    @Test
    public void testDouble() {
        DoubleLiteralNode node = new DoubleLiteralNode(1.0);
        assertSame(LiteralType.DOUBLE, node.getType());
        assertEquals(Double.class, node.getValue().getClass());
        assertEquals(1.0, node.getValue());
    }

    @Test
    public void testInteger() {
        IntegerLiteralNode node = new IntegerLiteralNode(1);
        assertSame(LiteralType.INTEGER, node.getType());
        assertEquals(Integer.class, node.getValue().getClass());
        assertEquals(1, node.getValue());
    }

    @Test
    public void testBoolean() {
        BooleanLiteralNode node = new BooleanLiteralNode(true);
        assertSame(LiteralType.BOOLEAN, node.getType());
        assertEquals(Boolean.class, node.getValue().getClass());
        assertEquals(true, node.getValue());
    }

    @Test
    public void testBigDecimal() {
        BigDecimalLiteralNode node = new BigDecimalLiteralNode(new BigDecimal(1.0));
        assertSame(LiteralType.BIG_DECIMAL, node.getType());
        assertEquals(BigDecimal.class, node.getValue().getClass());
        assertEquals(BigDecimal.ONE, node.getValue());
    }

    @Test
    public void testString() {
        StringLiteralNode node = new StringLiteralNode("abc");
        assertSame(LiteralType.STRING, node.getType());
        assertEquals(String.class, node.getValue().getClass());
        assertEquals("abc", node.getValue());
    }
}
