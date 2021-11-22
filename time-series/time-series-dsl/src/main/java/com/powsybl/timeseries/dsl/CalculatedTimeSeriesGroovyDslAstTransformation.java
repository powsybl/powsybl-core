/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.timeseries.dsl;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassCodeExpressionTransformer;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.expr.*;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.transform.ASTTransformation;
import org.codehaus.groovy.transform.GroovyASTTransformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@GroovyASTTransformation
public class CalculatedTimeSeriesGroovyDslAstTransformation implements ASTTransformation {

    protected static final Logger LOGGER = LoggerFactory.getLogger(CalculatedTimeSeriesGroovyDslAstTransformation.class);

    protected void visit(SourceUnit sourceUnit, ClassCodeExpressionTransformer transformer) {
        LOGGER.trace("Apply AST transformation");
        ModuleNode ast = sourceUnit.getAST();
        BlockStatement blockStatement = ast.getStatementBlock();

        List<MethodNode> methods = ast.getMethods();
        for (MethodNode methodNode : methods) {
            methodNode.getCode().visit(transformer);
        }

        blockStatement.visit(transformer);
    }

    public void visit(ASTNode[] nodes, SourceUnit sourceUnit) {
        visit(sourceUnit, new CustomClassCodeExpressionTransformer(sourceUnit));
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
