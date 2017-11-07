/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.action.dsl;

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
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@GroovyASTTransformation
public class ActionDslAstTransformation implements ASTTransformation {

    private static final Logger LOGGER = LoggerFactory.getLogger(ActionDslAstTransformation.class);

    private static final boolean DEBUG = false;

    private static void printAST(BlockStatement blockStatement) {
        Writer writer = new OutputStreamWriter(System.out);
        blockStatement.visit(new AstNodeToScriptVisitor(writer));
        try {
            writer.flush();
        } catch (IOException e) {
            LOGGER.error(e.toString(), e);
        }
    }

    public void visit(ASTNode[] nodes, SourceUnit sourceUnit) {
        LOGGER.trace("Apply AST transformation");
        ModuleNode ast = sourceUnit.getAST();
        BlockStatement blockStatement = ast.getStatementBlock();

        if (DEBUG) {
            printAST(blockStatement);
        }

        List<MethodNode> methods = ast.getMethods();
        for (MethodNode methodNode : methods) {
            methodNode.getCode().visit(new CustomClassCodeExpressionTransformer(sourceUnit));
        }

        blockStatement.visit(new CustomClassCodeExpressionTransformer(sourceUnit));

        if (DEBUG) {
            printAST(blockStatement);
        }
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
                    case "&&":
                        return new MethodCallExpression(transform(binExpr.getLeftExpression()),
                                "and2",
                                new ArgumentListExpression(transform(binExpr.getRightExpression())));
                    case "||":
                        return new MethodCallExpression(transform(binExpr.getLeftExpression()),
                                "or2",
                                new ArgumentListExpression(transform(binExpr.getRightExpression())));
                    default:
                        break;
                }
            } else if (exp instanceof NotExpression) {
                return new MethodCallExpression(transform(((NotExpression) exp).getExpression()),
                        "not",
                        new ArgumentListExpression());
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
