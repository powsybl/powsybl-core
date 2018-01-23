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

class BuilderSpec {

    private final def builder

    BuilderSpec(Object builder) {
        assert builder
        this.builder = builder
    }

    def methodMissing(String name, args) {
        ["with", "set"].forEach {
            def setterName = it + name.capitalize();
            def setter = builder.metaClass.getMetaMethod(setterName, args)
            if (setter) {
                setter.invoke(builder, args)
            }
        }
    }
}

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class GroovyScripts {

    private static String findBuilderPseudoClass(String name, args) {
        if (name.endsWith("Builder") && name.size() > 7 && args.length == 0) {
            name.substring(0, name.length() - 7)
        }
    }

    private static String findGroovyBuilderPseudoClass(String name, args) {
        if (name.startsWith("build") && name.size() > 5 && args.length == 1 && args[0] instanceof Closure) {
            name.substring(5).uncapitalize()
        }
    }

    private static findGetChildPseudoClass(String name, args) {
        if (name.startsWith("get") && name.size() > 3 && args.length > 0 && !args.find { !(it instanceof String) }) {
            name.substring(3).uncapitalize()
        }
    }

    private static createBuilder(projectFilePseudoClass, delegate) {
        ProjectFileExtension extension = delegate.getFileSystem().getData()
                .getProjectFileExtensionByPseudoClass(projectFilePseudoClass)
        if (!extension) {
            throw new AfsException("No extension found for project file pseudo class '"
                    + projectFilePseudoClass + "'");
        }
        delegate.fileBuilder(extension.getProjectFileBuilderClass())
    }

    static {
        ProjectFolder.metaClass.methodMissing = { String name, args ->
            String projectFilePseudoClass;
            if ((projectFilePseudoClass = findBuilderPseudoClass(name, args))) {
                createBuilder(projectFilePseudoClass, delegate)
            } else if ((projectFilePseudoClass = findGroovyBuilderPseudoClass(name, args))) {
                def closure = args[0]

                def builder = createBuilder(projectFilePseudoClass, delegate)

                def cloned = closure.clone()
                BuilderSpec spec = new BuilderSpec(builder)
                cloned.delegate = spec
                cloned()

                builder.build();
            } else if ((projectFilePseudoClass = findGetChildPseudoClass(name, args))) {
                ProjectFileExtension extension = delegate.getProject().getFileSystem().getData()
                        .getProjectFileExtensionByPseudoClass(projectFilePseudoClass)
                if (extension) {
                    delegate.invokeMethod("getChild", args).orElse(null)
                }
            }
        }

        Folder.metaClass.methodMissing = { String name, args ->
            String filePseudoClass = findGetChildPseudoClass(name, args)
            if (filePseudoClass) {
                FileExtension extension = delegate.getFileSystem().getData().getFileExtensionByPseudoClass(filePseudoClass)
                if (extension) {
                    delegate.invokeMethod("getChild", args).orElse(null)
                }
            }
        }
    }

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

        binding.computationManager = data.getComputationManager()

        if (out != null) {
            binding.out = out
        }

        // load extensions
        extensions.forEach { it.load(binding, data.getComputationManager()) }

        GroovyShell shell = new GroovyShell(binding, conf)
        shell.evaluate(codeReader)
    }
}
