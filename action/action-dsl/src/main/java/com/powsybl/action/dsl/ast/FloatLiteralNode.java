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
public class FloatLiteralNode extends AbstractLiteralNode {

    private final float value;

    public FloatLiteralNode(float value) {
        this.value = value;
    }

    @Override
    public LiteralType getType() {
        return LiteralType.FLOAT;
    }

    @Override
    public Object getValue() {
        return value;
    }
}
