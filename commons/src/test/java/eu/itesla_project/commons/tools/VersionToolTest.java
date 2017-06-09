/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.commons.tools;

import eu.itesla_project.commons.Version;
import org.junit.Test;

import java.util.Collections;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class VersionToolTest extends AbstractToolTest {

    @Override
    protected Iterable<Tool> getTools() {
        return Collections.singleton(new VersionTool());
    }

    @Override
    public void assertCommand() {
        VersionTool tool = new VersionTool();
        assertCommand(tool.getCommand(), "version", 0, 0);
    }

    @Test
    public void run() throws Exception {
        assertCommand(new String[] {}, CommandLineTools.COMMAND_NOT_FOUND_STATUS, "",
                "usage: itools COMMAND [ARGS]" + System.lineSeparator() +
                System.lineSeparator() +
                "Available commands are:" + System.lineSeparator() +
                System.lineSeparator());
        assertCommand(new String[] {"version"}, CommandLineTools.COMMAND_OK_STATUS, Version.VERSION + System.lineSeparator(), "");
    }

}