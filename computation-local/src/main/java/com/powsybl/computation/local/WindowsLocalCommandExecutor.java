/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.computation.local;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 * @author Nicolas Lhuillier {@literal <nicolas.lhuillier at rte-france.com>}
 */
public class WindowsLocalCommandExecutor extends AbstractLocalCommandExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(WindowsLocalCommandExecutor.class);

    @Override
    public int execute(String program, List<String> args, Path outFile, Path errFile, Path workingDir, Map<String, String> env) throws IOException, InterruptedException {
        return execute(program, -1, args, outFile, errFile, workingDir, env);
    }

    @Override
    public int execute(String program, long timeoutSecondes, List<String> args, Path outFile, Path errFile, Path workingDir, Map<String, String> env) throws IOException, InterruptedException {
        // set TMP and TEMP to working dir to avoid issues
        Map<String, String> env2 = ImmutableMap.<String, String>builder()
                .putAll(env)
                .put("TEMP", workingDir.toAbsolutePath().toString())
                .put("TMP", workingDir.toAbsolutePath().toString())
                .build();

        List<String> cmdLs = ImmutableList.<String>builder()
                .add("cmd")
                .add("/c")
                .add(program)
                .addAll(args)
                .build();
        return execute(cmdLs, workingDir, outFile, errFile, timeoutSecondes, env2);
    }

    @Override
    void nonZeroLog(List<String> cmdLs, int exitCode) {
        LOGGER.debug(NON_ZERO_LOG_PATTERN, cmdLs, exitCode);
    }
}
