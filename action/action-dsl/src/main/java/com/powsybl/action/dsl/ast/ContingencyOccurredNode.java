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
public class ContingencyOccurredNode implements ExpressionNode {

    private final String contingencyId;

    public ContingencyOccurredNode() {
        this(null);
    }

    public ContingencyOccurredNode(String contingencyId) {
        this.contingencyId = contingencyId;
    }

    public String getContingencyId() {
        return contingencyId;
    }

    @Override
    public <R, A> R accept(ExpressionVisitor<R, A> visitor, A arg) {
        return visitor.visitContingencyOccurred(this, arg);
    }
}
