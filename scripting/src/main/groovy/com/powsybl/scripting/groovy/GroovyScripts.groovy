/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.scripting.groovy

import org.codehaus.groovy.control.CompilerConfiguration

import java.nio.charset.StandardCharsets
import java.nio.file.Path

import com.powsybl.computation.DefaultComputationManagerConfig

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class GroovyScripts {

    static void run(Path file) {
        run(file, null)
    }

    static void run(Path file, PrintStream out) {
        run(file, new Binding(), out)
    }

    static void run(Reader codeReader, PrintStream out) {
        run(codeReader, new Binding(), out)
    }

    static void run(Path file, Binding binding, PrintStream out) {
        file.withReader(StandardCharsets.UTF_8.name(), { reader ->
            run(reader, binding, out)
        })
    }

    static void run(Reader codeReader, Binding binding, PrintStream out) {
        run(codeReader, binding, ServiceLoader.load(GroovyScriptExtension.class, GroovyScripts.class.getClassLoader()), out)
    }

    static void run(Reader codeReader, Iterable<GroovyScriptExtension> extensions, PrintStream out) {
        run(codeReader, new Binding(), extensions, out)
    }

    static void run(Reader codeReader, Binding binding, Iterable<GroovyScriptExtension> extensions, PrintStream out) {
        assert codeReader
        assert extensions != null

        CompilerConfiguration conf = new CompilerConfiguration()

        // Computation manager
        DefaultComputationManagerConfig config = DefaultComputationManagerConfig.load();
        binding.computationManager = config.createShortTimeExecutionComputationManager();

        if (out != null) {
            binding.out = out
        }

        try {
            // load extensions
            extensions.forEach { it.load(binding, binding.computationManager) }

            GroovyShell shell = new GroovyShell(binding, conf)
            shell.evaluate(codeReader)
        } finally {
            extensions.forEach { it.unload() }
        }
    }
}
