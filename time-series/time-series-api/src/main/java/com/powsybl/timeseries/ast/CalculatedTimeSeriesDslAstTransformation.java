/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.timeseries.ast;

import groovy.inspect.swingui.AstNodeToScriptVisitor;
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

import java.io.IOException;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.util.List;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@GroovyASTTransformation
public class CalculatedTimeSeriesDslAstTransformation implements ASTTransformation {

    protected static final Logger LOGGER = LoggerFactory.getLogger(CalculatedTimeSeriesDslAstTransformation.class);

    private static final boolean DEBUG = false;

    private static void printAST(BlockStatement blockStatement) {
        try (StringWriter writer = new StringWriter()) {
            blockStatement.visit(new AstNodeToScriptVisitor(writer));
            writer.flush();
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(writer.toString());
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    protected void visit(SourceUnit sourceUnit, ClassCodeExpressionTransformer transformer, boolean debug) {
        LOGGER.trace("Apply AST transformation");
        ModuleNode ast = sourceUnit.getAST();
        BlockStatement blockStatement = ast.getStatementBlock();

        if (debug) {
            printAST(blockStatement);
        }

        List<MethodNode> methods = ast.getMethods();
        for (MethodNode methodNode : methods) {
            methodNode.getCode().visit(transformer);
        }

        blockStatement.visit(transformer);

        if (debug) {
            printAST(blockStatement);
        }
    }

    public void visit(ASTNode[] nodes, SourceUnit sourceUnit) {
        visit(sourceUnit, new CustomClassCodeExpressionTransformer(sourceUnit), DEBUG);
    }

    class CustomClassCodeExpressionTransformer extends ClassCodeExpressionTransformer {

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
