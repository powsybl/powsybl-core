/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.tools.test;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.commons.config.InMemoryPlatformConfig;
import com.powsybl.computation.ComputationManager;
import com.powsybl.tools.Command;
import com.powsybl.tools.CommandLineTools;
import com.powsybl.tools.Tool;
import com.powsybl.tools.ToolInitializationContext;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.opentest4j.AssertionFailedError;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.util.Objects;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public abstract class AbstractToolTest {

    protected FileSystem fileSystem;

    protected InMemoryPlatformConfig platformConfig;

    private CommandLineTools tools;

    @BeforeEach
    public void setUp() throws Exception {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
        platformConfig = new InMemoryPlatformConfig(fileSystem);
        tools = new CommandLineTools(getTools());
    }

    protected void createFile(String filename, String content) throws IOException {
        Objects.requireNonNull(filename);
        Objects.requireNonNull(content);

        try (BufferedWriter writer = Files.newBufferedWriter(fileSystem.getPath(filename))) {
            writer.write(content);
        }
    }

    @AfterEach
    public void tearDown() throws IOException {
        fileSystem.close();
    }

    protected abstract Iterable<Tool> getTools();

    private void assertMatches(String expected, String actual) {
        //The empty string is matched exactly, other strings as regexs
        if (!actual.equals(expected) && ("".equals(expected) || !Pattern.compile(expected).matcher(actual).find())) {
            throw new AssertionFailedError("", expected, actual);
        }
    }

    protected void assertCommand(String[] args, int expectedStatus, String expectedOut, String expectedErr) throws IOException {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ByteArrayOutputStream berr = new ByteArrayOutputStream();
        int status;
        try (PrintStream out = new PrintStream(bout);
             PrintStream err = new PrintStream(berr)) {
            ComputationManager computationManager = Mockito.mock(ComputationManager.class);
            status = tools.run(args, new ToolInitializationContext() {
                @Override
                public PrintStream getOutputStream() {
                    return out;
                }

                @Override
                public PrintStream getErrorStream() {
                    return err;
                }

                @Override
                public Options getAdditionalOptions() {
                    return new Options();
                }

                @Override
                public FileSystem getFileSystem() {
                    return fileSystem;
                }

                @Override
                public ComputationManager createShortTimeExecutionComputationManager(CommandLine commandLine) {
                    return computationManager;
                }

                @Override
                public ComputationManager createLongTimeExecutionComputationManager(CommandLine commandLine) {
                    return computationManager;
                }
            });
        }
        if (expectedErr != null) {
            assertMatches(expectedErr, berr.toString(StandardCharsets.UTF_8));
        }
        assertEquals(expectedStatus, status);
        if (expectedOut != null) {
            assertMatches(expectedOut, bout.toString(StandardCharsets.UTF_8));
        }
    }

    @Test
    public abstract void assertCommand();

    protected void assertCommand(Command command, String commandName, int optionCount, int requiredOptionCount) {
        assertEquals(commandName, command.getName());
        assertEquals(optionCount, command.getOptions().getOptions().size());
        assertEquals(requiredOptionCount, command.getOptions().getRequiredOptions().size());
    }

    protected void assertOption(Options options, String optionName, boolean isRequired, boolean hasArgument) {
        Option option = options.getOption(optionName);
        assertNotNull(option);
        assertEquals(isRequired, option.isRequired());
        assertEquals(hasArgument, option.hasArg());
    }
}
