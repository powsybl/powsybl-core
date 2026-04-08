/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.action.ial.dsl.ast;

import com.powsybl.action.ial.dsl.ConditionDslLoader;
import com.powsybl.dsl.ast.ExpressionNode;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * @author Yichen TANG {@literal <yichen.tang at rte-france.com>}
 */
class ActionExpressionPrinterTest {

    @Test
    void test() {
        String script = "loadingRank('NHV1_NHV2_1', ['NHV1_NHV2_1', 'NHV1_NHV2_2'])";
        ExpressionNode node = (ExpressionNode) new ConditionDslLoader(script).load(EurostagTutorialExample1Factory.create());
        assertEquals(script, ActionExpressionPrinter.toString(node));

        String script2 = "line('NHV1_NHV2_1').currentLimits1.getTemporaryLimitValue(1200)";
        ExpressionNode node2 = (ExpressionNode) new ConditionDslLoader("line('NHV1_NHV2_1').currentLimits1.getTemporaryLimitValue(1200)").load(EurostagTutorialExample1Factory.create());
        assertEquals(script2, ActionExpressionPrinter.toString(node2));

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ActionExpressionPrinter.print(node, bos);
        assertEquals(script, bos.toString());

        bos = new ByteArrayOutputStream();
        ActionExpressionPrinter.print(node, bos, StandardCharsets.ISO_8859_1);
        try {
            assertEquals(script, bos.toString(StandardCharsets.ISO_8859_1.name()));
        } catch (UnsupportedEncodingException e) {
            fail();
        }
    }
}
