/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.computation.script

import eu.itesla_project.computation.ComputationManager
import org.codehaus.groovy.control.CompilerConfiguration

import java.nio.charset.StandardCharsets
import java.nio.file.Path

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class GroovyScript {

    static void run(Path file, ComputationManager computationManager) {
        run(file, computationManager, null)
    }

    static void run(Path file, ComputationManager computationManager, Writer out) {
        file.withReader(StandardCharsets.UTF_8.name(), { reader ->
            run(reader, computationManager, out)
        })
    }

    static void run(Reader reader, ComputationManager computationManager, Writer out) {
        run(reader, computationManager, new ServiceLoaderGroovyExtensionLoader(), out)
    }

    static void run(Reader codeReader, ComputationManager computationManager, GroovyExtensionLoader extensionLoader, Writer out) {
        assert codeReader
        assert computationManager
        assert extensionLoader

        CompilerConfiguration conf = new CompilerConfiguration()
        Binding binding = new Binding()

        if (out != null) {
            binding.setProperty("out", out)
        }

        // load extensions
        extensionLoader.load(binding, computationManager)

        GroovyShell shell = new GroovyShell(binding, conf)
        shell.evaluate(codeReader)
    }
}
