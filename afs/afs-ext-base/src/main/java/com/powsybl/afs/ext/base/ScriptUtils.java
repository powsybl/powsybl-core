/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.ext.base;

import com.powsybl.iidm.network.Network;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.MissingMethodException;
import groovy.lang.MissingPropertyException;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.MultipleCompilationErrorsException;

import java.io.*;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public final class ScriptUtils {

    private static final String SCRIPT_FILE_NAME = "test";

    private ScriptUtils() {
    }

    static ScriptError runGroovyScript(Network network, Reader reader, Writer out) {
        // put network in the binding so that it is accessible from the script
        Binding binding = new Binding();
        binding.setProperty("network", network);
        binding.setProperty("out", out);

        CompilerConfiguration conf = new CompilerConfiguration();
        GroovyShell shell = new GroovyShell(binding, conf);
        try {
            shell.evaluate(reader, SCRIPT_FILE_NAME);
        } catch (MultipleCompilationErrorsException e) {
            return ScriptError.fromGroovyException(e);
        } catch (MissingPropertyException | MissingMethodException e) {
            return ScriptError.fromGroovyException(e, SCRIPT_FILE_NAME);
        }
        return null;
    }

    static ScriptError runScript(Network network, ScriptType scriptType, String scriptContent, Writer scriptOutputWriter) {
        try (Reader reader = new StringReader(scriptContent)) {
            if (scriptType == ScriptType.GROOVY) {
                return runGroovyScript(network, reader, scriptOutputWriter);
            } else {
                throw new AssertionError("Script type " + scriptType + " not supported");
            }

        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
