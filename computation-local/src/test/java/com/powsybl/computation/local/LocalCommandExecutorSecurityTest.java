/*
 * Copyright (c) 2026, RTE (https://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.computation.local;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
class LocalCommandExecutorSecurityTest {

    Path tempDir;
    Path outFile;
    Path errFile;
    Path pwndFileArgumentDoubleQuotes;
    Path pwndFileArgumentEscapeBypass;
    Path pwndFileArgumentTrailingBackslash;
    Path pwndFileEnvVariable;
    Path pwndFileEnvVarName;
    Path pwndFileNewline;
    Path pwndFilePercentExpansion;
    Path pwndFileUnixNewline;

    @BeforeEach
    void setUp() throws IOException {
        tempDir = Files.createTempDirectory("powsybl-repro");
        outFile = tempDir.resolve("out.log");
        errFile = tempDir.resolve("err.log");
        pwndFileArgumentDoubleQuotes = tempDir.resolve("pwned.txt");
        pwndFileArgumentEscapeBypass = tempDir.resolve("pwned2.txt");
        pwndFileArgumentTrailingBackslash = tempDir.resolve("pwned3.txt");
        pwndFileEnvVariable = tempDir.resolve("pwned4.txt");
        pwndFileEnvVarName = tempDir.resolve("pwned5.txt");
        pwndFileNewline = tempDir.resolve("pwned6.txt");
        pwndFilePercentExpansion = tempDir.resolve("pwned7.txt");
        pwndFileUnixNewline = tempDir.resolve("pwned8.txt");
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.deleteIfExists(outFile);
        Files.deleteIfExists(errFile);
        Files.deleteIfExists(pwndFileArgumentDoubleQuotes);
        Files.deleteIfExists(pwndFileArgumentEscapeBypass);
        Files.deleteIfExists(pwndFileArgumentTrailingBackslash);
        Files.deleteIfExists(pwndFileEnvVariable);
        Files.deleteIfExists(pwndFileEnvVarName);
        Files.deleteIfExists(pwndFileNewline);
        Files.deleteIfExists(pwndFilePercentExpansion);
        Files.deleteIfExists(pwndFileUnixNewline);
        Files.deleteIfExists(tempDir);
    }

    @Test
    @EnabledOnOs(OS.LINUX)
    void testCommandInjectionInParametersOnUnix() {
        // Attacker-controlled contingency ID with shell substitution
        // 1. Command chaining with & after breaking out of double quotes
        String payloadDoubleQuotes = String.format("--contingencies=$(touch %s)", pwndFileArgumentDoubleQuotes);
        // 2. Command chaining attempt using escaped single-quote bypass
        String payloadSingleQuotes = String.format("--dsl-file='; touch %s; echo '", pwndFileArgumentEscapeBypass);
        // 3. Newline injection to break out of the current command
        String payloadNewline = String.format("--test=value%ntouch %s", pwndFileUnixNewline);
        Map<String, String> env = new HashMap<>();
        // 4. Environment variable injection with command chaining
        env.put("VARIABLE", String.format("$(touch %s)", pwndFileEnvVariable));
        env.put("TEST_PATH", "new_value");

        // The executor will not launch an itools command, but simulate its usage instead:
        // the command "echo" is used with options that are similar
        // to the ones that could have been used for an itools command.
        List<String> cmdArgs = new ArrayList<>();
        cmdArgs.add("action-simulator");
        cmdArgs.add("--case-file=network.xiidm");
        cmdArgs.add(payloadDoubleQuotes);
        cmdArgs.add(payloadSingleQuotes);
        cmdArgs.add(payloadNewline);

        UnixLocalCommandExecutor executor = new UnixLocalCommandExecutor();
        assertDoesNotThrow(() -> executor.execute("echo", cmdArgs, outFile, errFile, tempDir, env));

        assertFalse(Files.exists(pwndFileArgumentDoubleQuotes), String.format("[!] Command injection confirmed: %s created", pwndFileArgumentDoubleQuotes));
        assertFalse(Files.exists(pwndFileArgumentEscapeBypass), String.format("[!] Command injection confirmed: %s created", pwndFileArgumentEscapeBypass));
        assertFalse(Files.exists(pwndFileEnvVariable), String.format("[!] Command injection confirmed: %s created", pwndFileEnvVariable));
        assertFalse(Files.exists(pwndFileUnixNewline), String.format("[!] Command injection confirmed: %s created", pwndFileUnixNewline));
    }

    @Test
    @EnabledOnOs(OS.WINDOWS)
    void testCommandInjectionInParametersOnWindows() {
        // Attacker-controlled argument with shell substitution
        // 1. Command chaining with & after breaking out of double quotes
        String payloadDoubleQuotes = String.format("--test1=file.txt\" & type nul > \"%s\" & echo \"", pwndFileArgumentDoubleQuotes);
        // 2. Command chaining attempt using escaped double-quote bypass
        String payloadEscapeBypass = String.format("--dsl-file=file.txt\"\" & type nul > \"%s\" & echo \"\"", pwndFileArgumentEscapeBypass);
        // 3. Command injection via trailing backslash escaping the closing quote
        String payloadTrailingBackslash = String.format("--test2=C:\\path\\\" & type nul > \"%s\" & echo \\", pwndFileArgumentTrailingBackslash);
        // 4. Newline injection to break out of the current command
        String payloadNewline = String.format("--test3=value%ntype nul > \"%s\"", pwndFileNewline);
        // 5. Percent expansion injection: if %TEMP% expands to a crafted value, it could inject commands
        String payloadPercentExpansion = String.format("--test4=%%TEMP%%\" & type nul > \"%s\" & echo \"", pwndFilePercentExpansion);

        Map<String, String> env = new HashMap<>();
        // 6. Environment variable value injection with command chaining
        env.put("VARIABLE", String.format("value\" & type nul > \"%s\" & echo\"", pwndFileEnvVariable));

        // The executor will not launch an itools command but will simulate its usage instead:
        // the command "echo" is used with options that are similar to the ones that could have been used for an itools command.
        List<String> cmdArgs = new ArrayList<>();
        cmdArgs.add("testing");
        cmdArgs.add(payloadDoubleQuotes);
        cmdArgs.add(payloadEscapeBypass);
        cmdArgs.add(payloadTrailingBackslash);
        cmdArgs.add(payloadNewline);
        cmdArgs.add(payloadPercentExpansion);

        WindowsLocalCommandExecutor executor = new WindowsLocalCommandExecutor();
        assertDoesNotThrow(() -> executor.execute("echo", cmdArgs, outFile, errFile, tempDir, env));

        assertFalse(Files.exists(pwndFileArgumentDoubleQuotes), String.format("[!] Command injection confirmed: %s created", pwndFileArgumentDoubleQuotes));
        assertFalse(Files.exists(pwndFileArgumentEscapeBypass), String.format("[!] Command injection confirmed: %s created", pwndFileArgumentEscapeBypass));
        assertFalse(Files.exists(pwndFileArgumentTrailingBackslash), String.format("[!] Command injection confirmed: %s created", pwndFileArgumentTrailingBackslash));
        assertFalse(Files.exists(pwndFileNewline), String.format("[!] Command injection confirmed: %s created", pwndFileNewline));
        assertFalse(Files.exists(pwndFilePercentExpansion), String.format("[!] Command injection confirmed: %s created", pwndFilePercentExpansion));
        assertFalse(Files.exists(pwndFileEnvVariable), String.format("[!] Command injection confirmed: %s created", pwndFileEnvVariable));
    }

    @Test
    @EnabledOnOs(OS.WINDOWS)
    void testCommandInjectionViaEnvVarNameOnWindows() {
        // 7. Injection via environment variable name: names cannot be safely escaped,
        //    so the executor must reject invalid names outright.
        Map<String, String> maliciousEnv = new HashMap<>();
        maliciousEnv.put(
            String.format("FOO\" & type nul > \"%s\" & set \"BAR", pwndFileEnvVarName),
            "value"
        );
        List<String> cmdArgs = List.of("test");

        WindowsLocalCommandExecutor executor = new WindowsLocalCommandExecutor();
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> executor.execute("echo", cmdArgs, outFile, errFile, tempDir, maliciousEnv)
        );
        assertEquals("Invalid environment variable name", exception.getMessage());
        assertFalse(Files.exists(pwndFileEnvVarName), String.format("[!] Command injection confirmed: %s created", pwndFileEnvVarName));
    }

    @Test
    @EnabledOnOs(OS.WINDOWS)
    void testCommandInjectionViaProgramNameOnWindows() {
        // 8. Injection via program name containing a double quote.
        //    The executor must reject program names with quotes rather than pass them raw.
        String maliciousProgram = String.format("echo\" & type nul > \"%s\" & echo \"", pwndFileEnvVarName);
        Map<String, String> env = Map.of();
        List<String> cmdArgs = List.of();

        WindowsLocalCommandExecutor executor = new WindowsLocalCommandExecutor();
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> executor.execute(maliciousProgram, cmdArgs, outFile, errFile, tempDir, env)
        );
        assertEquals("Program name must not contain double quotes", exception.getMessage());
        assertFalse(Files.exists(pwndFileEnvVarName), String.format("[!] Command injection confirmed: %s created", pwndFileEnvVarName));

        // 9. Injection via program name containing spaces.
        //    The executor must reject program names with quotes rather than pass them raw.
        String maliciousProgram2 = String.format("echo & type nul > %s & echo ", pwndFileEnvVarName);
        IllegalArgumentException exception2 = assertThrows(
            IllegalArgumentException.class,
            () -> executor.execute(maliciousProgram2, cmdArgs, outFile, errFile, tempDir, env)
        );
        assertEquals("Program name must not contain spaces", exception2.getMessage());
        assertFalse(Files.exists(pwndFileEnvVarName), String.format("[!] Command injection confirmed: %s created", pwndFileEnvVarName));
    }
}
