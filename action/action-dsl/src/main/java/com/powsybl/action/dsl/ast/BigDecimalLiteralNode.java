/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.action.dsl.ast;

import java.math.BigDecimal;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class BigDecimalLiteralNode extends AbstractLiteralNode {

    private final BigDecimal value;

    public BigDecimalLiteralNode(BigDecimal value) {
        this.value = value;
    }

    @Override
    public LiteralType getType() {
        return LiteralType.BIG_DECIMAL;
    }

    @Override
    public Object getValue() {
        return value;
    }
}
