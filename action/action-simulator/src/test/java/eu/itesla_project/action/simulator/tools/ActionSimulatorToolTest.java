/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.action.simulator.tools;

import eu.itesla_project.commons.tools.AbstractToolTest;
import eu.itesla_project.commons.tools.Command;
import eu.itesla_project.commons.tools.Tool;

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

        assertCommand(command, "action-simulator", 5, 2);
        assertOption(command.getOptions(), "case-file", true, true);
        assertOption(command.getOptions(), "dsl-file", true, true);
        assertOption(command.getOptions(), "contingencies", false, true);
        assertOption(command.getOptions(), "verbose", false, false);
        assertOption(command.getOptions(), "output-csv", false, true);
    }
}
