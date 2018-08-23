/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.ast;

import groovy.inspect.swingui.AstNodeToScriptVisitor;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassCodeExpressionTransformer;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.transform.ASTTransformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.util.List;

/**
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
public abstract class AbstractAstTransformation implements ASTTransformation {

    protected static Logger LOGGER = LoggerFactory.getLogger(AbstractAstTransformation.class);

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

    protected void visit(ASTNode[] nodes, SourceUnit sourceUnit, ClassCodeExpressionTransformer transformer, boolean debug) {
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
}
