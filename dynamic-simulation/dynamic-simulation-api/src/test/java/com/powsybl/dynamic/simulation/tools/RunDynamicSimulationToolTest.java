/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.dynamic.simulation.tools;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.powsybl.commons.PowsyblException;
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
        assertEquals("Computation", command.getTheme());
        assertEquals("Run dynamic simulation", command.getDescription());
        assertNull(command.getUsageFooter());
        assertOption(command.getOptions(), "case-file", true, true);
        assertOption(command.getOptions(), "output-file", false, true);
        assertOption(command.getOptions(), "skip-postproc", false, false);
    }

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();

        runningContext = mock(ToolRunningContext.class);
        when(runningContext.getFileSystem()).thenReturn(fileSystem);
        when(runningContext.getOutputStream()).thenReturn(System.out);

        commandLine = mock(CommandLine.class);
        when(commandLine.getOptionValue("case-file")).thenReturn("/path-case-file.xiidm");
        when(commandLine.hasOption("output-file")).thenReturn(true);
        when(commandLine.getOptionValue("output-file")).thenReturn("/outfile");
        when(commandLine.hasOption("skip-postproc")).thenReturn(true);
        when(commandLine.getOptionProperties("I")).thenReturn(new Properties());
        createFile("/path-case-file.xiidm", "");
        createFile("/outfile", "");
    }

    @Test
    public void failedOutputFormatOptions() throws Exception {
        thrown.expect(PowsyblException.class);
        thrown.expectMessage("Unsupported file format or invalid file.");
        tool.run(commandLine, runningContext);
    }
}
