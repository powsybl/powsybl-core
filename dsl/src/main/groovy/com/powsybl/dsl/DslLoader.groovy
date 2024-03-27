/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.dsl

import groovy.transform.ThreadInterrupt
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ASTTransformationCustomizer
import org.codehaus.groovy.control.customizers.ImportCustomizer

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class DslLoader {

    protected final GroovyCodeSource dslSrc

    DslLoader(GroovyCodeSource dslSrc) {
        this.dslSrc = Objects.requireNonNull(dslSrc)
    }

    DslLoader(File dslFile) {
        this(new GroovyCodeSource(dslFile))
    }

    DslLoader(String script) {
        this(new GroovyCodeSource(script, "script", GroovyShell.DEFAULT_CODE_BASE))
    }

    static GroovyShell createShell(Binding binding) {
        return createShell(binding, new ImportCustomizer())
    }

    static GroovyShell createShell(Binding binding, ImportCustomizer imports) {
        def astCustomizer = new ASTTransformationCustomizer(new PowsyblDslAstTransformation())
        def config = new CompilerConfiguration()
        config.addCompilationCustomizers(astCustomizer, imports)

        // Add a check on thread interruption in every loop (for, while) in the script
        config.addCompilationCustomizers(new ASTTransformationCustomizer(ThreadInterrupt.class))

        ExpressionDslLoader.prepareClosures(binding)
        new GroovyShell(binding, config)
    }
}
