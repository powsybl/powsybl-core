/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.computation.local;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;

/**
 * @author Yichen TANG {@literal <yichen.tang at rte-france.com>}
 */
class LocalCommandExecutorTest {

    private static class FakeUnixLocalCommandExecutor extends UnixLocalCommandExecutor {
        @Override
        public int execute(String program, long timeoutSecondes, List<String> args, Path outFile, Path errFile, Path workingDir, Map<String, String> env) throws IOException, InterruptedException {
            assertEquals(-1L, timeoutSecondes);
            return 0;
        }
    }

    private static class FakeWinLocalCommandExecutor extends WindowsLocalCommandExecutor {
        @Override
        public int execute(String program, long timeoutSecondes, List<String> args, Path outFile, Path errFile, Path workingDir, Map<String, String> env) throws IOException, InterruptedException {
            assertEquals(-1L, timeoutSecondes);
            return 0;
        }
    }

    @Test
    void testWithoutTimeoutCall() {
        FakeUnixLocalCommandExecutor unixFake = new FakeUnixLocalCommandExecutor();
        FakeUnixLocalCommandExecutor winFake = new FakeUnixLocalCommandExecutor();
        try {
            unixFake.execute("program", Collections.emptyList(), mock(Path.class), mock(Path.class), mock(Path.class), Collections.emptyMap());
            winFake.execute("program", Collections.emptyList(), mock(Path.class), mock(Path.class), mock(Path.class), Collections.emptyMap());
        } catch (Exception e) {
            fail();
        }
    }
}
