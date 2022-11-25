/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.timeseries.dsl;

import com.powsybl.dsl.AbstractPowsyblDslAstTransformation;
import org.codehaus.groovy.ast.ClassCodeExpressionTransformer;
import org.codehaus.groovy.ast.expr.*;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.transform.GroovyASTTransformation;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@GroovyASTTransformation
public class CalculatedTimeSeriesGroovyDslAstTransformation extends AbstractPowsyblDslAstTransformation {

    public CalculatedTimeSeriesGroovyDslAstTransformation() {
        super(CustomClassCodeExpressionTransformer::new);
    }

    static class CustomClassCodeExpressionTransformer extends ClassCodeExpressionTransformer {

        private final SourceUnit sourceUnit;

        CustomClassCodeExpressionTransformer(SourceUnit sourceUnit) {
            this.sourceUnit = sourceUnit;
        }

        @Override
        protected SourceUnit getSourceUnit() {
            return sourceUnit;
        }

        private Expression transform(BinaryExpression binExpr) {
            String op = binExpr.getOperation().getText();
            switch (op) {
                case ">":
                case ">=":
                case "<":
                case "<=":
                case "==":
                case "!=":
                    return new MethodCallExpression(transform(binExpr.getLeftExpression()),
                            "compareToNodeCalc",
                            new ArgumentListExpression(transform(binExpr.getRightExpression()), new ConstantExpression(op)));
                default:
                    break;
            }
            return null;
        }

        @Override
        public Expression transform(Expression exp) {
            if (exp instanceof BinaryExpression) {
                Expression binExpr = transform((BinaryExpression) exp);
                if (binExpr != null) {
                    return binExpr;
                }
            }

            // propagate visit inside transformed expression
            Expression newExpr = super.transform(exp);
            if (newExpr != null) {
                newExpr.visit(this);
            }

            return newExpr;
        }
    }
}
