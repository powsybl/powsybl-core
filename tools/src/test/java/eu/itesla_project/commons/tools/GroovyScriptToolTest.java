/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.commons.tools;

import org.junit.Test;

import java.util.Collections;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class GroovyScriptToolTest extends AbstractToolTest {

    @Override
    protected Iterable<Tool> getTools() {
        return Collections.singleton(new GroovyScriptTool());
    }

    @Override
    public void assertCommand() {
        Tool tool = new GroovyScriptTool();
        assertCommand(tool.getCommand(), "groovy-script", 1, 1);
        assertOption(tool.getCommand().getOptions(), "script", true, true);
    }

    @Test
    public void run() throws Exception {
        String helloFile = "/hello.groovy";
        createFile(helloFile, "print 'hello'");

        assertCommand(new String[] {"groovy-script", "--script", helloFile}, CommandLineTools.COMMAND_OK_STATUS, "hello", "");
    }

    @Test
    public void runWithParameters() throws Exception {
        String helloFile = "/hello.groovy";
        createFile(helloFile, "print 'hello ' + args[0]");

        assertCommand(new String[] {"groovy-script", "--script", helloFile, "John Doe"}, CommandLineTools.COMMAND_OK_STATUS, "hello John Doe", "");
    }
}