/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.computation.local;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

/**
 * @author Yichen TANG {@literal <yichen.tang at rte-france.com>}
 */
class LocalCommandExecutorTest {

    private static final class FakeUnixLocalCommandExecutor extends UnixLocalCommandExecutor {
        @Override
        public int execute(String program, long timeoutSecondes, List<String> args, Path outFile, Path errFile, Path workingDir, Map<String, String> env) throws IOException, InterruptedException {
            assertEquals(-1L, timeoutSecondes);
            return 0;
        }
    }

    private static final class FakeWinLocalCommandExecutor extends WindowsLocalCommandExecutor {
        @Override
        public int execute(String program, long timeoutSecondes, List<String> args, Path outFile, Path errFile, Path workingDir, Map<String, String> env) throws IOException, InterruptedException {
            assertEquals(-1L, timeoutSecondes);
            return 0;
        }
    }

    @Test
    void testWithoutTimeoutCall() {
        FakeUnixLocalCommandExecutor unixFake = new FakeUnixLocalCommandExecutor();
        FakeWinLocalCommandExecutor winFake = new FakeWinLocalCommandExecutor();
        try {
            unixFake.execute("program", Collections.emptyList(), mock(Path.class), mock(Path.class), mock(Path.class), Collections.emptyMap());
            winFake.execute("program", Collections.emptyList(), mock(Path.class), mock(Path.class), mock(Path.class), Collections.emptyMap());
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    @EnabledOnOs(OS.WINDOWS)
    void testWindowsLocalCommandExecutor(@TempDir Path tempDir) throws IOException, InterruptedException {
        WindowsLocalCommandExecutor windowsLocalCommandExecutor = new WindowsLocalCommandExecutor();
        Path outPath = tempDir.resolve("out");
        Path errPath = tempDir.resolve("err");
        int exitCode;

        // Built-in command
        exitCode = windowsLocalCommandExecutor.execute("echo", 5, List.of("Hello World"), outPath, errPath, tempDir, Collections.emptyMap());
        assertTrue(Files.readString(outPath).contains("Hello World"));
        assertTrue(Files.readString(errPath).isEmpty());
        assertEquals(0, exitCode);

        // Executable
        Files.delete(outPath);
        exitCode = windowsLocalCommandExecutor.execute("whoami.exe", 5, List.of(), outPath, errPath, tempDir, Collections.emptyMap());
        assertTrue(Files.readString(outPath).length() > 3);
        assertTrue(Files.readString(errPath).isEmpty());
        assertEquals(0, exitCode);

        // Batch file, and environment variable
        Files.delete(outPath);
        Files.writeString(tempDir.resolve("my_batch.bat"), "@echo %~1%MY_VAR% !");
        exitCode = windowsLocalCommandExecutor.execute("my_batch.bat", 5, List.of("Hello"), outPath, errPath, tempDir, Map.of("MY_VAR", " World"));
        assertTrue(Files.readString(outPath).contains("Hello World !"));
        assertTrue(Files.readString(errPath).isEmpty());
        assertEquals(0, exitCode);

        // Spaces and some special characters
        Files.delete(outPath);
        Files.writeString(tempDir.resolve("my @ batch (copy).bat"), "@echo Hello, World");
        exitCode = windowsLocalCommandExecutor.execute("my @ batch (copy).bat", 5, List.of(), outPath, errPath, tempDir, Collections.emptyMap());
        System.out.println("Files.readString(outPath) = " + Files.readString(outPath));
        System.out.println("Files.readString(errPath) = " + Files.readString(errPath));
        System.out.println("exitCode = " + exitCode);
        assertTrue(Files.readString(outPath).contains("Hello, World"));
        assertTrue(Files.readString(errPath).isEmpty());
        assertEquals(0, exitCode);

        // Check exit code
        Files.delete(outPath);
        Files.writeString(tempDir.resolve("my_batch_which_fails.bat"), "@exit /B 37");
        exitCode = windowsLocalCommandExecutor.execute("my_batch_which_fails.bat", 5, List.of(), outPath, errPath, tempDir, Map.of("MY_VAR", " World"));
        assertTrue(Files.readString(outPath).isEmpty());
        assertTrue(Files.readString(errPath).isEmpty());
        assertEquals(37, exitCode);

        // Program not found
        Files.delete(outPath);
        Path nonExistentProgram = tempDir.resolve("non_existent_program.exe");
        System.out.println("nonExistentProgram = " + nonExistentProgram);
        exitCode = windowsLocalCommandExecutor.execute(nonExistentProgram.toString(), 5, List.of(), outPath, errPath, tempDir, Map.of("MY_VAR", " World"));
        assertTrue(Files.readString(outPath).isEmpty());
        assertTrue(Files.readString(errPath).length() > 3);
        assertEquals(1, exitCode);
    }

}
