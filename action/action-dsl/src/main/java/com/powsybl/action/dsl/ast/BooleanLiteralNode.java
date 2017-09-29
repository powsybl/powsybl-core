/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.action.dsl.ast;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class BooleanLiteralNode extends AbstractLiteralNode {

    public static final BooleanLiteralNode TRUE = new BooleanLiteralNode(Boolean.TRUE);
    public static final BooleanLiteralNode FALSE = new BooleanLiteralNode(Boolean.FALSE);

    private final boolean value;

    public BooleanLiteralNode(boolean value) {
        this.value = value;
    }

    @Override
    public LiteralType getType() {
        return LiteralType.BOOLEAN;
    }

    @Override
    public Object getValue() {
        return value;
    }
}
