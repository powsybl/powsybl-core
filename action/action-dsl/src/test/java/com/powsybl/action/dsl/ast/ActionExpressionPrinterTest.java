/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.action.dsl.ast;

import com.powsybl.action.dsl.ConditionDslLoader;
import com.powsybl.dsl.ast.ExpressionNode;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Yichen TANG <yichen.tang at rte-france.com>
 */
public class ActionExpressionPrinterTest {

    @Test
    public void test() {
        String script = "loadingRank('NHV1_NHV2_1', ['NHV1_NHV2_1', 'NHV1_NHV2_2'])";
        ExpressionNode node = (ExpressionNode) new ConditionDslLoader(script).load(EurostagTutorialExample1Factory.create());
        assertEquals(script, ActionExpressionPrinter.toString(node));

        String script2 = "line('NHV1_NHV2_1').currentLimits1.getTemporaryLimitValue(1200)";
        ExpressionNode node2 = (ExpressionNode) new ConditionDslLoader("line('NHV1_NHV2_1').currentLimits1.getTemporaryLimitValue(1200)").load(EurostagTutorialExample1Factory.create());
        assertEquals(script2, ActionExpressionPrinter.toString(node2));
    }
}
