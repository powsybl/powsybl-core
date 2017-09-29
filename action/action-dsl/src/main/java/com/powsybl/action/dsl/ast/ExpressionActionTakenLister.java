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
public class ExpressionActionTakenLister extends DefaultExpressionVisitor<Void, List<String>> {

    public static List<String> list(ExpressionNode root) {
        List<String> actionIds = new ArrayList<>();
        root.accept(new ExpressionActionTakenLister(), actionIds);
        return actionIds;
    }

    @Override
    public Void visitActionTaken(ActionTakenNode node, List<String> actionIds) {
        actionIds.add(node.getActionId());
        return null;
    }
}
