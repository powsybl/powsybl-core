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

import java.io.File;
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

        StringBuilder internalCmd = new StringBuilder();
        internalCmd.append("setlocal & ");
        for (Map.Entry<String, String> entry : env2.entrySet()) {
            String name = entry.getKey();
            String value = entry.getValue();
            // Validate the variable name: only alphanumerics and underscores are allowed.
            // This prevents injection via malicious variable names, which cannot be safely escaped.
            if (!name.matches("[a-zA-Z_]\\w*")) {
                throw new IllegalArgumentException("Invalid environment variable name");
            }
            // Use the 'set "NAME=VALUE"' form: value is inside the outer quotes, so
            // double-quote doubling ("") is the correct escaping strategy here
            internalCmd.append("set \"").append(name).append("=").append(escapeCmdEnvValue(value));
            if (name.endsWith("PATH")) {
                internalCmd.append(File.pathSeparator).append("%").append(name).append("%");
            }
            internalCmd.append("\" & ");
        }
        // Quote the program name to handle spaces in paths and validate it contains no quotes.
        if (program.contains("\"")) {
            throw new IllegalArgumentException("Program name must not contain double quotes");
        }
        if (program.contains(" ")) {
            throw new IllegalArgumentException("Program name must not contain spaces");
        }
        internalCmd.append(program);
        for (String arg : args) {
            internalCmd.append(" \"").append(escapeCmdArg(arg)).append("\"");
        }
        internalCmd.append(" & endlocal");

        List<String> cmdLs = ImmutableList.<String>builder()
                .add("cmd")
                .add("/c")
                .add(internalCmd.toString())
                .build();
        return execute(cmdLs, workingDir, outFile, errFile, timeoutSecondes);
    }

    @Override
    void nonZeroLog(List<String> cmdLs, int exitCode) {
        LOGGER.debug(NON_ZERO_LOG_PATTERN, cmdLs, exitCode);
    }

    private static String escapeCmdEnvValue(String value) {
        // This value is embedded inside set "NAME=VALUE", i.e. already inside double quotes.
        // Inside a quoted set command:
        //   - " must be doubled ("") — caret-escaping does NOT work inside quotes
        //   - % must be doubled (%%) to prevent %VAR% expansion
        //   - ! must be escaped (^!) to prevent delayed expansion (fires even inside quotes)
        //   - ^ does NOT need escaping inside quotes (it is literal)
        //   - &, |, <, > do NOT need escaping inside quotes (they are literal)
        //   - newlines are stripped to prevent command structure breakout
        return value
            .replace("%", "%%")
            .replace("!", "^!")
            .replace("\"", "\"\"")
            .replace("\n", "")
            .replace("\r", "");
    }

    private static String escapeCmdArg(String arg) {
        // Double any trailing backslashes before the closing quote
        int trailingBackslashes = 0;
        for (int i = arg.length() - 1; i >= 0 && arg.charAt(i) == '\\'; i--) {
            trailingBackslashes++;
        }
        String suffix = "\\".repeat(trailingBackslashes); // repeat them once more
        return arg
            .replace("^", "^^")   // escape ^ first (outside-quote context for & | etc.)
            .replace("%", "^%")   // prevent variable expansion like %PATH%
            .replace("!", "^!")   // prevent delayed expansion like !VAR!
            .replace("\"", "\"\"") // double quotes inside the quoted string
            .replace("\n", "") // prevents newline-based injection
            .replace("\r", "") // prevents newline-based injection
            + suffix;
    }
}
