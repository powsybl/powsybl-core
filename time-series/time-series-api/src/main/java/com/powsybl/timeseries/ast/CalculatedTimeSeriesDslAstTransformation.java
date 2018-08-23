/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.timeseries.ast;

import com.powsybl.commons.ast.AbstractAstTransformation;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassCodeExpressionTransformer;
import org.codehaus.groovy.ast.expr.*;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.transform.GroovyASTTransformation;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@GroovyASTTransformation
public class CalculatedTimeSeriesDslAstTransformation extends AbstractAstTransformation {

    private static final boolean DEBUG = false;

    public void visit(ASTNode[] nodes, SourceUnit sourceUnit) {
        visit(nodes, sourceUnit, new CustomClassCodeExpressionTransformer(sourceUnit), DEBUG);
    }

    class CustomClassCodeExpressionTransformer extends ClassCodeExpressionTransformer {
        SourceUnit sourceUnit;

        CustomClassCodeExpressionTransformer(SourceUnit sourceUnit) {
            this.sourceUnit = sourceUnit;
        }

        @Override
        protected SourceUnit getSourceUnit() {
            return sourceUnit;
        }

        @Override
        public Expression transform(Expression exp) {
            if (exp instanceof BinaryExpression) {
                BinaryExpression binExpr = (BinaryExpression) exp;
                String op = binExpr.getOperation().getText();
                switch (op) {
                    case ">":
                    case ">=":
                    case "<":
                    case "<=":
                    case "==":
                    case "!=":
                        return new MethodCallExpression(transform(binExpr.getLeftExpression()),
                                "compareTo2",
                                new ArgumentListExpression(transform(binExpr.getRightExpression()), new ConstantExpression(op)));
                    default:
                        break;
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
