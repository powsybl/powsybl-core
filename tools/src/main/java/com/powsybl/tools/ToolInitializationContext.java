/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.tools;

import com.powsybl.computation.ComputationManager;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import java.io.PrintStream;
import java.nio.file.FileSystem;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface ToolInitializationContext {

    PrintStream getOutputStream();

    PrintStream getErrorStream();

    FileSystem getFileSystem();

    Options getAdditionalOptions();

    ComputationManager createShortTimeExecutionComputationManager(CommandLine commandLine);

    ComputationManager createLongTimeExecutionComputationManager(CommandLine commandLine);
}
