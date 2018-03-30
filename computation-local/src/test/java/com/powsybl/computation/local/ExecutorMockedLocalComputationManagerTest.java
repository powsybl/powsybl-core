/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.computation.local;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.computation.ExecutionEnvironment;
import com.powsybl.computation.ExecutionHandler;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.util.reflection.Whitebox;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class ExecutorMockedLocalComputationManagerTest {

    @Mock
    LocalCommandExecutor localCommandExecutor;

    Path localDir;

    @Before
    public void setup() throws IOException {
        FileSystem jimfs = Jimfs.newFileSystem(Configuration.unix());
        localDir = jimfs.getPath("/jimfs");
        Files.createDirectories(localDir);
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testWorkingDirWithDebugMode() throws IOException, InterruptedException {
        try (LocalComputationManager mgr = new LocalComputationManager(localDir)) {
            Whitebox.setInternalState(mgr, "localCommandExecutor", localCommandExecutor);

            assertEquals(1, Files.list(localDir).count());
            assertTrue(Files.list(localDir).allMatch(p -> p.getFileName().toString().startsWith("itools_common")));

            ExecutionEnvironment debugEnv = new ExecutionEnvironment(Collections.EMPTY_MAP, "unitTest_", true);
            CompletableFuture<String> completableFuture = mgr.execute(debugEnv, mock(ExecutionHandler.class));
            completableFuture.join();
            assertEquals(2, Files.list(localDir).count());
            assertTrue(Files.list(localDir).anyMatch(p -> p.getFileName().toString().startsWith("unitTest")));
        }

        assertEquals(1, Files.list(localDir).count());
        assertTrue(Files.list(localDir).allMatch(p -> p.getFileName().toString().startsWith("unitTest")));
    }

    @Test
    public void testWorkingDirWithNonDebugMode() throws IOException, InterruptedException {
        try (LocalComputationManager mgr = new LocalComputationManager(localDir)) {
            Whitebox.setInternalState(mgr, "localCommandExecutor", localCommandExecutor);
            ExecutionEnvironment nonDebugEnv = new ExecutionEnvironment(Collections.EMPTY_MAP, "unitTest_", false);
            CompletableFuture<String> anotherFuture = mgr.execute(nonDebugEnv, mock(ExecutionHandler.class));
            anotherFuture.join();
        }

        assertEquals(0, Files.list(localDir).count());
    }

    @Test
    public void testCancel() throws IOException, InterruptedException {
        try (LocalComputationManager mgr = new LocalComputationManager(localDir)) {
            Whitebox.setInternalState(mgr, "localCommandExecutor", localCommandExecutor);

            doAnswer(new Answer() {
                @Override
                public Integer answer(InvocationOnMock invocation) throws Throwable {
                    return 42;
                }
            }).when(localCommandExecutor).execute(any(), any(), any(), any(), any(), any());

            ExecutionEnvironment environment = new ExecutionEnvironment(Collections.EMPTY_MAP, "unitTest_", true);
            CompletableFuture execute = mgr.execute(environment, mock(ExecutionHandler.class));
            execute.cancel(true);
            verify(localCommandExecutor, times(1)).stop(any());
        }
    }

}
