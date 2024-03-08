/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.action.ial.simulator.tools;

import com.powsybl.iidm.network.Exporter;
import com.powsybl.iidm.network.ExportersLoaderList;
import com.powsybl.tools.Command;
import com.powsybl.tools.Tool;
import com.powsybl.tools.ToolRunningContext;
import com.powsybl.tools.test.AbstractToolTest;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Mathieu Bague {@literal <mathieu.bague at rte-france.com>}
 */
class ActionSimulatorToolTest extends AbstractToolTest {

    private ToolRunningContext runningContext;

    private CommandLine commandLine;

    private final ActionSimulatorTool tool = new ActionSimulatorTool() {
        @Override
        protected Collection<String> getFormats() {
            return Exporter.getFormats(new ExportersLoaderList());
        }
    };

    @Override
    protected Iterable<Tool> getTools() {
        return Collections.singleton(new ActionSimulatorTool());
    }

    @Override
    public void assertCommand() {
        Command command = tool.getCommand();

        assertCommand(command, "action-simulator", 15, 2);
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
        assertOption(command.getOptions(), "task-count", false, true);
        assertOption(command.getOptions(), "task", false, true);
        assertOption(command.getOptions(), "export-after-each-round", false, false);
    }

    @BeforeEach
    void mockup() {
        runningContext = mock(ToolRunningContext.class);
        when(runningContext.getFileSystem()).thenReturn(fileSystem);

        commandLine = mock(CommandLine.class);
        when(commandLine.getOptionValue("case-file")).thenReturn("/path-case-file");
        when(commandLine.getOptionValue("dsl-file")).thenReturn("/path-dsl-file");
    }

    @Test
    void failedOutputCaseOptions() throws Exception {
        when(commandLine.hasOption("output-case-folder")).thenReturn(true);
        when(commandLine.getOptionValue("output-case-folder")).thenReturn("/outcasefolder");
        when(commandLine.hasOption("output-case-format")).thenReturn(false);
        ParseException e = assertThrows(ParseException.class, () -> tool.run(commandLine, runningContext));
        assertTrue(e.getMessage().contains("Missing required option: output-case-format"));
    }

    @Test
    void missingOutputFileInParallelMode() throws Exception {
        when(commandLine.hasOption("task-count")).thenReturn(true);
        when(commandLine.hasOption("output-file")).thenReturn(false);
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> tool.run(commandLine, runningContext));
        assertTrue(e.getMessage().contains("Missing required option: output-file in parallel mode"));
    }

    @Test
    void notsupportOptionsInParallelMode() throws Exception {
        when(commandLine.hasOption("task-count")).thenReturn(true);
        when(commandLine.hasOption("output-file")).thenReturn(true);
        when(commandLine.hasOption("output-case-folder")).thenReturn(true);
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> tool.run(commandLine, runningContext));
        assertTrue(e.getMessage().contains("Not supported in parallel mode yet."));
    }
}
