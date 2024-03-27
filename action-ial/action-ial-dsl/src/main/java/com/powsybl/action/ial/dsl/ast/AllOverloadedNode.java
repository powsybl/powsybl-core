/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.action.ial.dsl.ast;

import java.util.List;

/**
 * @author Olivier Bretteville {@literal <olivier.bretteville at rte-france.com>}
 */
public class AllOverloadedNode extends AbstractBranchActionExpressionNode {

    public AllOverloadedNode(List<String> branchIds, double limitReduction) {
        super(branchIds, limitReduction);
    }

    @Override
    public <R, A> R accept(ActionExpressionVisitor<R, A> visitor, A arg) {
        return visitor.visitAllOverloaded(this, arg);
    }
}
