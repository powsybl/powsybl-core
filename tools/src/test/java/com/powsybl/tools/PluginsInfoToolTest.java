/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.tools;

import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
public class PluginsInfoToolTest extends AbstractToolTest {

    @Override
    protected Iterable<Tool> getTools() {
        return Collections.singleton(new PluginsInfoTool());
    }

    @Override
    public void assertCommand() {
        PluginsInfoTool tool = new PluginsInfoTool();
        assertEquals("plugins-info", tool.getCommand().getName());
        assertEquals("Misc", tool.getCommand().getTheme());
        assertEquals("List the available plugins", tool.getCommand().getDescription());
        assertFalse(tool.getCommand().isHidden());

        assertCommand(tool.getCommand(), "plugins-info", 0, 0);
    }

    @Test
    public void run() throws Exception {
        assertCommand(new String[] {"plugins-info"}, CommandLineTools.COMMAND_OK_STATUS, "dummy", "");
    }
}
