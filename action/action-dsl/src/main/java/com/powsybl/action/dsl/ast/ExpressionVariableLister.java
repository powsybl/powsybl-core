/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.action.dsl.ast;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ExpressionVariableLister extends DefaultExpressionVisitor<Void, List<NetworkNode>> {

    public static List<NetworkNode> list(ExpressionNode root) {
        List<NetworkNode> variables = new ArrayList<>();
        root.accept(new ExpressionVariableLister(), variables);
        return variables;
    }

    @Override
    public Void visitNetworkProperty(NetworkPropertyNode node, List<NetworkNode> variables) {
        variables.add(node);
        return null;
    }

    @Override
    public Void visitNetworkMethod(NetworkMethodNode node, List<NetworkNode> variables) {
        variables.add(node);
        return null;
    }
}
