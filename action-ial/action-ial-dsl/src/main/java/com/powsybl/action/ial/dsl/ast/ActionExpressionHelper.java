/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.action.ial.dsl.ast;

import com.powsybl.dsl.ast.ExpressionNode;

import java.util.List;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public final class ActionExpressionHelper {

    private ActionExpressionHelper() {
    }

    public static NetworkComponentNode newNetworkComponent(String id, NetworkComponentNode.ComponentType componentType) {
        return new NetworkComponentNode(id, componentType);
    }

    public static NetworkPropertyNode newNetworkProperty(NetworkNode parent, String propertyName) {
        return new NetworkPropertyNode(parent, propertyName);
    }

    public static NetworkMethodNode newNetworkMethod(NetworkNode parent, String methodName, Object[] args) {
        return new NetworkMethodNode(parent, methodName, args);
    }

    public static ActionTakenNode newActionTaken(String actionId) {
        return new ActionTakenNode(actionId);
    }

    public static ContingencyOccurredNode newContingencyOccured() {
        return new ContingencyOccurredNode();
    }

    public static LoadingRankNode newLoadingRank(ExpressionNode branchIdToRank, List<ExpressionNode> branchIds) {
        return new LoadingRankNode(branchIdToRank, branchIds);
    }

    public static MostLoadedNode newMostLoaded(List<String> branchIds) {
        return new MostLoadedNode(branchIds);
    }

    public static IsOverloadedNode newIsOverloadedNode(List<String> branchIds, double limitReduction) {
        return new IsOverloadedNode(branchIds, limitReduction);
    }

    public static AllOverloadedNode newAllOverloadedNode(List<String> branchIds, double limitReduction) {
        return new AllOverloadedNode(branchIds, limitReduction);
    }

    public static ContingencyOccurredNode newContingencyOccured(String contingencyId) {
        return new ContingencyOccurredNode(contingencyId);
    }
}
