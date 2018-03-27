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

    private static ScriptResult<Object> runGroovyScript(Network network, Reader reader) {
        String output = "";
        ScriptError error = null;
        Object value = null;
        try (StringWriter outputWriter = new StringWriter()) {
            // put network in the binding so that it is accessible from the script
            Binding binding = new Binding();
            binding.setProperty("network", network);
            binding.setProperty("out", outputWriter);

            CompilerConfiguration config = new CompilerConfiguration();
            GroovyShell shell = new GroovyShell(binding, config);
            value = shell.evaluate(reader, SCRIPT_FILE_NAME);
            outputWriter.flush();
            output = outputWriter.toString();
        } catch (MultipleCompilationErrorsException e) {
            error = ScriptError.fromGroovyException(e);
        } catch (MissingPropertyException | MissingMethodException e) {
            error = ScriptError.fromGroovyException(e, SCRIPT_FILE_NAME);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return new ScriptResult<>(value, output, error);
    }

    static ScriptResult<Object> runScript(Network network, ScriptType scriptType, String scriptContent) {
        try (Reader reader = new StringReader(scriptContent)) {
            if (scriptType == ScriptType.GROOVY) {
                return runGroovyScript(network, reader);
            } else {
                throw new AssertionError("Script type " + scriptType + " not supported");
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
