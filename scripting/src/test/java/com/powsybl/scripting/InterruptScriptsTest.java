/*
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.scripting;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.commons.config.InMemoryPlatformConfig;
import com.powsybl.computation.AbstractTaskInterruptionTest;
import com.powsybl.computation.ComputationManager;
import com.powsybl.computation.local.LocalComputationManager;
import com.powsybl.tools.CommandLineTools;
import com.powsybl.tools.Tool;
import com.powsybl.tools.ToolInitializationContext;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.util.Collections;
import java.util.Objects;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
@Order(2)
class InterruptScriptsTest extends AbstractTaskInterruptionTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(InterruptScriptsTest.class);

    protected FileSystem fileSystem;
    private RunScriptTool tool;
    protected InMemoryPlatformConfig platformConfig;
    private CommandLineTools tools;

    @BeforeEach
    public void setUp() {
        tool = new RunScriptTool();
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
        platformConfig = new InMemoryPlatformConfig(fileSystem);
        tools = new CommandLineTools(getTools());
    }

    protected Iterable<Tool> getTools() {
        return Collections.singleton(tool);
    }

    protected void createFile(String filename, String content) throws IOException {
        Objects.requireNonNull(filename);
        Objects.requireNonNull(content);

        try (BufferedWriter writer = Files.newBufferedWriter(fileSystem.getPath(filename))) {
            writer.write(content);
        }
    }

    protected int runCommand(String[] args) throws InterruptedException {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ByteArrayOutputStream berr = new ByteArrayOutputStream();
        int status;
        try (PrintStream out = new PrintStream(bout);
             PrintStream err = new PrintStream(berr)) {
            ComputationManager computationManager = LocalComputationManager.getDefault();
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
        LOGGER.info("berr: {}", berr.toString().lines().findFirst().orElse(""));
        if (berr.toString().startsWith("java.lang.InterruptedException")) {
            // When the task is interrupted during the initialization: "java.lang.InterruptedException: Execution Interrupted"
            // When the Groovy execution is interrupted: "java.lang.InterruptedException: Execution interrupted. The current thread has been interrupted."
            throw new InterruptedException();
        }
        return status;
    }

    @ParameterizedTest
    @Timeout(5)
    @ValueSource(booleans = {false, true})
    void testCancelTask(boolean isDelayed) throws Exception {
        // Script content
        String scriptFile = "/hello.groovy";
        String scriptContent = """
            for (int i = 0; i < 200; i++) {
                print(i)
                sleep(500)
            }
            """ + "print ' hello ' + args[0]";
        createFile(scriptFile, scriptContent);

        // Cancel the task
        testCancelLongTask(isDelayed, () -> {
            try {
                return runCommand(new String[] {"run-script", "--file", scriptFile, "John Doe"});
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
}
