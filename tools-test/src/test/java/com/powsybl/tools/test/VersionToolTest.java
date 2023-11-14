/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.tools.test;

import com.powsybl.tools.CommandLineTools;
import com.powsybl.tools.Tool;
import com.powsybl.tools.Version;
import com.powsybl.tools.VersionTool;
import org.junit.jupiter.api.Test;

import java.util.Collections;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class VersionToolTest extends AbstractToolTest {

    @Override
    protected Iterable<Tool> getTools() {
        return Collections.singleton(new VersionTool(platformConfig));
    }

    @Override
    public void assertCommand() {
        VersionTool tool = new VersionTool(platformConfig);
        assertCommand(tool.getCommand(), "version", 0, 0);
    }

    @Test
    void run() throws Exception {
        assertCommandErrorMatch(new String[] {}, CommandLineTools.COMMAND_NOT_FOUND_STATUS,
                "Available commands are:" + System.lineSeparator() +
                System.lineSeparator());
        assertCommandSuccessful(new String[] {"version"}, Version.getTableString(platformConfig) + System.lineSeparator());
    }

}
