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
import com.powsybl.commons.test.ComparisonUtils;
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

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public abstract class AbstractToolTest {

    protected FileSystem fileSystem;

    protected InMemoryPlatformConfig platformConfig;

    private CommandLineTools tools;

    private static final String ASSERT_MATCH_TEXT_BLOCK = """
                         Actual output does not contains expected output
                         Expected:
                         %s
                         Actual:
                         %s
                         """;

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

    private void assertMatches(String expected, ByteArrayOutputStream actualStream, BiConsumer<String, String> comparisonFunction) {
        if (expected != null) {
            String actual = actualStream.toString(StandardCharsets.UTF_8);
            if (expected.isEmpty()) {
                assertTrue(actual.isEmpty(), () -> "Expected output is empty but actual output = " + actual);
            } else {
                comparisonFunction.accept(expected, actual);
            }
        }
    }

    public static void containsTxt(String expected, String actual) {
        assertTrue(actual.contains(expected), () -> ASSERT_MATCH_TEXT_BLOCK.formatted(expected, actual));
    }

    public void matchTextOrRegex(String expected, String actual) {
        assertTrue(actual.equals(expected) || Pattern.compile(expected).matcher(actual).find());
    }

    protected void assertCommandSuccessful(String[] args) {
        assertCommand(args, CommandLineTools.COMMAND_OK_STATUS, null, "", ComparisonUtils::assertTxtEquals);
    }

    protected void assertCommandSuccessful(String[] args, String expectedOut) {
        assertCommand(args, CommandLineTools.COMMAND_OK_STATUS, expectedOut, "", ComparisonUtils::assertTxtEquals);
    }

    protected void assertCommandSuccessfulMatch(String[] args, String expectedOut) {
        assertCommand(args, CommandLineTools.COMMAND_OK_STATUS, expectedOut, "", AbstractToolTest::containsTxt);
    }

    protected void assertCommandError(String[] args, int expectedStatus, String expectedErr) {
        assertCommand(args, expectedStatus, null, expectedErr, ComparisonUtils::assertTxtEquals);
    }

    protected void assertCommandErrorMatch(String[] args, int expectedStatus, String expectedErr) {
        assertCommand(args, expectedStatus, null, expectedErr, AbstractToolTest::containsTxt);
    }

    protected void assertCommandErrorMatch(String[] args, String expectedErr) {
        assertCommand(args, CommandLineTools.EXECUTION_ERROR_STATUS, null, expectedErr, AbstractToolTest::containsTxt);
    }

    /**
     * @deprecated use {@link AbstractToolTest#assertCommandMatchTextOrRegex} instead
     */
    @Deprecated(since = "6.4.0")
    protected void assertCommand(String[] args, int expectedStatus, String expectedOut, String expectedErr) {
        assertCommandMatchTextOrRegex(args, expectedStatus, expectedOut, expectedErr);
    }

    protected void assertCommandMatchTextOrRegex(String[] args, int expectedStatus, String expectedOut, String expectedErr) {
        assertCommand(args, expectedStatus, expectedOut, expectedErr, this::matchTextOrRegex);
    }

    private void assertCommand(String[] args, int expectedStatus, String expectedOut, String expectedErr, BiConsumer<String, String> comparisonFunction) {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ByteArrayOutputStream berr = new ByteArrayOutputStream();
        int status;
        try (PrintStream out = new PrintStream(bout);
             PrintStream err = new PrintStream(berr);
             ComputationManager computationManager = Mockito.mock(ComputationManager.class)) {
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
        assertEquals(expectedStatus, status);
        if (expectedOut != null) {
            assertMatches(expectedOut, bout, comparisonFunction);
        }
        if (expectedErr != null) {
            assertMatches(expectedErr, berr, comparisonFunction);
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
