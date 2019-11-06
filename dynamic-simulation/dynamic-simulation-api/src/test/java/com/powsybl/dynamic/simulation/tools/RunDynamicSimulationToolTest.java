/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.dynamic.simulation.tools;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.apache.commons.cli.CommandLine;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.ExpectedException;

import com.powsybl.tools.AbstractToolTest;
import com.powsybl.tools.Command;
import com.powsybl.tools.Tool;
import com.powsybl.tools.ToolRunningContext;

/**
 * @author Marcos de Miguel <demiguelm at aia.es>
 */
public class RunDynamicSimulationToolTest extends AbstractToolTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private ToolRunningContext runningContext;

    private CommandLine commandLine;

    private final RunDynamicSimulationTool tool = new RunDynamicSimulationTool();

    @Override
    protected Iterable<Tool> getTools() {
        return Collections.singleton(new RunDynamicSimulationTool());
    }

    @Override
    public void assertCommand() {
        Command command = tool.getCommand();

        assertCommand(command, "dynamic-simulation", 7, 1);
        assertOption(command.getOptions(), "case-file", true, true);
        assertOption(command.getOptions(), "output-file", false, true);
        assertOption(command.getOptions(), "skip-postproc", false, false);
    }

    @Before
    public void mockup() {
        runningContext = mock(ToolRunningContext.class);
        when(runningContext.getFileSystem()).thenReturn(fileSystem);

        commandLine = mock(CommandLine.class);
        when(commandLine.getOptionValue("case-file")).thenReturn("/path-case-file");
    }
}
