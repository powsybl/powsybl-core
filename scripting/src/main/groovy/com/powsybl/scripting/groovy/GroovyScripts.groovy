/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.scripting.groovy

import com.powsybl.afs.*
import org.codehaus.groovy.control.CompilerConfiguration

import java.nio.charset.StandardCharsets
import java.nio.file.Path

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class GroovyScripts {

    static void run(Path file, AppData data) {
        run(file, data, null)
    }

    static void run(Path file, AppData data, Writer out) {
        run(file, data, new Binding(), out)
    }

    static void run(Reader codeReader, AppData data, Writer out) {
        run(codeReader, data, new Binding(), out)
    }

    static void run(Path file, AppData data, Binding binding, Writer out) {
        file.withReader(StandardCharsets.UTF_8.name(), { reader ->
            run(reader, data, binding, out)
        })
    }

    static void run(Reader codeReader, AppData data, Binding binding, Writer out) {
        run(codeReader, data, binding, ServiceLoader.load(GroovyScriptExtension.class), out)
    }

    static void run(Reader codeReader, AppData data, Iterable<GroovyScriptExtension> extensions, Writer out) {
        run(codeReader, data, new Binding(), extensions, out)
    }

    static void run(Reader codeReader, AppData data, Binding binding, Iterable<GroovyScriptExtension> extensions, Writer out) {
        assert codeReader
        assert data
        assert extensions != null

        CompilerConfiguration conf = new CompilerConfiguration()

        binding.afs = new AfsGroovyFacade(data)

        binding.computationManager = data.getShortTimeExecutionComputationManager()

        if (out != null) {
            binding.out = out
        }

        // load extensions
        extensions.forEach { it.load(binding, data.getShortTimeExecutionComputationManager()) }

        GroovyShell shell = new GroovyShell(binding, conf)
        shell.evaluate(codeReader)
    }
}
