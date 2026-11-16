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
public interface ActionExpressionVisitor<R, A> extends ExpressionVisitor<R, A> {

    R visitNetworkComponent(NetworkComponentNode node, A arg);

    R visitNetworkProperty(NetworkPropertyNode node, A arg);

    R visitNetworkMethod(NetworkMethodNode node, A arg);

    R visitActionTaken(ActionTakenNode node, A arg);

    R visitContingencyOccurred(ContingencyOccurredNode node, A arg);

    R visitLoadingRank(LoadingRankNode node, A arg);

    R visitMostLoaded(MostLoadedNode mostLoadedNode, A arg);

    R visitIsOverloaded(IsOverloadedNode isOverloadedNode, A arg);

    R visitAllOverloaded(AllOverloadedNode allOverloadedNode, A arg);
}
