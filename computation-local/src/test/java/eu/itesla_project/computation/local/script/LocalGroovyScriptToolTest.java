/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.computation.local.script;

import eu.itesla_project.commons.tools.AbstractToolTest;
import eu.itesla_project.commons.tools.CommandLineTools;
import eu.itesla_project.commons.tools.Tool;
import eu.itesla_project.computation.ComputationManager;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.BufferedWriter;
import java.nio.file.Files;
import java.util.Collections;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class LocalGroovyScriptToolTest extends AbstractToolTest {

    private final ComputationManager computationManager = Mockito.mock(ComputationManager.class);

    @Override
    protected Iterable<Tool> getTools() {
        return Collections.singleton(new LocalGroovyScriptTool(computationManager));
    }

    @Override
    public void assertCommand() {
        Tool tool = new LocalGroovyScriptTool();
        assertCommand(tool.getCommand(), "local-groovy-script", 1, 1);
        assertOption(tool.getCommand().getOptions(), "script", true, true);
    }

    @Test
    public void run() throws Exception {
        String helloFile = "/hello.groovy";
        try (BufferedWriter writer = Files.newBufferedWriter(fileSystem.getPath(helloFile))) {
            writer.write("print 'hello'");
        }
        assertCommand(new String[] {"local-groovy-script", "--script", helloFile}, CommandLineTools.COMMAND_OK_STATUS, "hello", "");
    }

    @Test
    public void runWithParameters() throws Exception {
        String helloFile = "/hello.groovy";
        try (BufferedWriter writer = Files.newBufferedWriter(fileSystem.getPath(helloFile))) {
            writer.write("print 'hello ' + args[0]");
        }
        assertCommand(new String[] {"local-groovy-script", "--script", helloFile, "John Doe"}, CommandLineTools.COMMAND_OK_STATUS, "hello John Doe", "");
    }
}