/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.dsl;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.customizers.ASTTransformationCustomizer;
import org.codehaus.groovy.control.customizers.ImportCustomizer;
import org.codehaus.groovy.transform.ASTTransformation;
import org.codehaus.groovy.transform.GroovyASTTransformation;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class AstUtilTest {

    @GroovyASTTransformation
    private static class FakeTransformer implements ASTTransformation {
        @Override
        public void visit(ASTNode[] astNodes, SourceUnit sourceUnit) {
            ModuleNode ast = sourceUnit.getAST();
            BlockStatement blockStatement = ast.getStatementBlock();
            assertEquals("this.print('hello')" + System.lineSeparator(), AstUtil.toString(blockStatement));
        }
    }

    @Test
    public void testPrint() {
        ASTTransformationCustomizer astCustomizer = new ASTTransformationCustomizer(new FakeTransformer());
        ImportCustomizer imports = new ImportCustomizer();
        CompilerConfiguration config = new CompilerConfiguration();
        config.addCompilationCustomizers(astCustomizer, imports);
        Binding binding = new Binding();
        assertNull(new GroovyShell(binding, config).evaluate("print('hello')"));
    }
}
