/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.scripting.groovy

import com.powsybl.computation.ComputationManager
import com.powsybl.computation.DefaultComputationManagerConfig
import groovy.transform.ThreadInterrupt
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ASTTransformationCustomizer

import java.nio.charset.StandardCharsets
import java.nio.file.Path

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
        run(codeReader, binding, extensions, out, new HashMap<>())
    }

    static void run(Reader codeReader, Binding binding, Iterable<GroovyScriptExtension> extensions, PrintStream out, Map<Class<?>, Object> contextObjects) {
        assert codeReader
        assert extensions != null

        CompilerConfiguration conf = new CompilerConfiguration()

        // Add a check on thread interruption in every loop (for, while) in the script
        conf.addCompilationCustomizers(new ASTTransformationCustomizer(ThreadInterrupt.class))

        // Computation manager
        DefaultComputationManagerConfig config = DefaultComputationManagerConfig.load()
        binding.computationManager = config.createShortTimeExecutionComputationManager()
        contextObjects.put(ComputationManager.class, binding.computationManager)

        if (out != null) {
            binding.out = out
        }

        try {
            // load extensions
            extensions.forEach { it.load(binding, contextObjects) }

            GroovyShell shell = new GroovyShell(binding, conf)

            // Check for thread interruption right before beginning the evaluation
            if (Thread.currentThread().isInterrupted()) throw new InterruptedException("Execution Interrupted")

            shell.evaluate(codeReader)
        } finally {
            extensions.forEach { it.unload() }
        }
    }
}
