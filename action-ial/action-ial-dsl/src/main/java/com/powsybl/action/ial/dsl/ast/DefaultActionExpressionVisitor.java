/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.action.ial.dsl.ast;

import com.powsybl.dsl.ast.*;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class DefaultActionExpressionVisitor<R, A> extends DefaultExpressionVisitor<R, A> implements ActionExpressionVisitor<R, A> {

    @Override
    public R visitNetworkComponent(NetworkComponentNode node, A arg) {
        return null;
    }

    @Override
    public R visitNetworkProperty(NetworkPropertyNode node, A arg) {
        node.getParent().accept(this, arg);
        return null;
    }

    @Override
    public R visitNetworkMethod(NetworkMethodNode node, A arg) {
        node.getParent().accept(this, arg);
        return null;
    }

    @Override
    public R visitActionTaken(ActionTakenNode node, A arg) {
        return null;
    }

    @Override
    public R visitContingencyOccurred(ContingencyOccurredNode node, A arg) {
        return null;
    }

    @Override
    public R visitLoadingRank(LoadingRankNode node, A arg) {
        return null;
    }

    @Override
    public R visitMostLoaded(MostLoadedNode mostLoadedNode, A arg) {
        return null;
    }

    @Override
    public R visitIsOverloaded(IsOverloadedNode isOverloadedNode, A arg) {
        return null;
    }

    @Override
    public R visitAllOverloaded(AllOverloadedNode allOverloadedNode, A arg) {
        return null;
    }
}
