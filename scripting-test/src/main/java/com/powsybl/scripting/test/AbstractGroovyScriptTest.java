/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.scripting.test;

import com.powsybl.computation.ComputationManager;
import com.powsybl.scripting.groovy.GroovyScriptExtension;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.mockito.Mockito;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public abstract class AbstractGroovyScriptTest {

    protected abstract String getCode();

    protected abstract String getExpectedOutput();

    protected abstract List<GroovyScriptExtension> getExtensions();

    public void doTest() {
        ComputationManager computationManager = Mockito.mock(ComputationManager.class);
        Binding binding = new Binding();
        StringWriter out = null;
        try {
            try (StringWriter writer = new StringWriter()) {
                binding.setVariable("out", writer);

                CompilerConfiguration conf = new CompilerConfiguration();
                getExtensions().forEach(it -> it.load(binding, computationManager));
                GroovyShell shell = new GroovyShell(binding, conf);
                shell.evaluate(getCode());
                out = (StringWriter) binding.getProperty("out");

                assertEquals(getExpectedOutput(), out.toString());
            } finally {
                if (out != null) {
                    out.close();
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
