/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.tools.test;

import com.powsybl.tools.PluginsInfoTool;
import com.powsybl.tools.Tool;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * @author Mathieu Bague {@literal <mathieu.bague at rte-france.com>}
 */
class PluginsInfoToolTest extends AbstractToolTest {

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
    void run() {
        assertCommandSuccessfulMatch(new String[] {"plugins-info"}, "dummy");
    }
}
