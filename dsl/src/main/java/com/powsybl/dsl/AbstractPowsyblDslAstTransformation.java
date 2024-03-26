/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.dsl;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassCodeExpressionTransformer;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.transform.ASTTransformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public abstract class AbstractPowsyblDslAstTransformation implements ASTTransformation {

    protected static final Logger LOGGER = LoggerFactory.getLogger(AbstractPowsyblDslAstTransformation.class);

    protected final Function<SourceUnit, ClassCodeExpressionTransformer> transformer;

    protected AbstractPowsyblDslAstTransformation(Function<SourceUnit, ClassCodeExpressionTransformer> transformer) {
        this.transformer = Objects.requireNonNull(transformer);
    }

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
        visit(sourceUnit, transformer.apply(sourceUnit));
    }
}
