/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.computation.mpi;

import com.powsybl.computation.ComputationManager;
import com.powsybl.tools.CommandLineTools;
import com.powsybl.tools.ToolInitializationContext;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import java.io.PrintStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public final class MpiMaster {

    private MpiMaster() {
    }

    public static void main(String[] args) {

        ToolInitializationContext initContext = new ToolInitializationContext() {

            private ComputationManager computationManager;

            @Override
            public PrintStream getOutputStream() {
                return System.out;
            }

            @Override
            public PrintStream getErrorStream() {
                return System.err;
            }

            @Override
            public Options getAdditionalOptions() {
                return MpiToolUtil.createMpiOptions();
            }

            @Override
            public FileSystem getFileSystem() {
                return FileSystems.getDefault();
            }

            private synchronized ComputationManager createComputationManager(CommandLine commandLine) {
                if (computationManager == null) {
                    computationManager = MpiToolUtil.createMpiComputationManager(commandLine, FileSystems.getDefault());
                }
                return computationManager;
            }

            @Override
            public ComputationManager createShortTimeExecutionComputationManager(CommandLine commandLine) {
                return createComputationManager(commandLine);
            }

            @Override
            public ComputationManager createLongTimeExecutionComputationManager(CommandLine commandLine) {
                return createComputationManager(commandLine);
            }
        };
        int status = new CommandLineTools().run(args, initContext);
        if (status != CommandLineTools.COMMAND_OK_STATUS) {
            System.exit(status);
        }
    }
}
