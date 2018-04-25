/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.action.simulator.tools;

import com.powsybl.tools.AbstractToolTest;
import com.powsybl.tools.Command;
import com.powsybl.tools.Tool;

import java.util.Collections;

/**
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
public class ActionSimulatorToolTest extends AbstractToolTest {

    @Override
    protected Iterable<Tool> getTools() {
        return Collections.singleton(new ActionSimulatorTool());
    }

    @Override
    public void assertCommand() {
        ActionSimulatorTool tool = new ActionSimulatorTool();
        Command command = tool.getCommand();

        assertCommand(command, "action-simulator", 10, 2);
        assertOption(command.getOptions(), "case-file", true, true);
        assertOption(command.getOptions(), "dsl-file", true, true);
        assertOption(command.getOptions(), "contingencies", false, true);
        assertOption(command.getOptions(), "apply-if-solved-violations", false, false);
        assertOption(command.getOptions(), "verbose", false, false);
        assertOption(command.getOptions(), "output-file", false, true);
        assertOption(command.getOptions(), "output-format", false, true);
        assertOption(command.getOptions(), "output-case-folder", false, true);
        assertOption(command.getOptions(), "output-case-format", false, true);
        assertOption(command.getOptions(), "output-compression-format", false, true);
    }
}
