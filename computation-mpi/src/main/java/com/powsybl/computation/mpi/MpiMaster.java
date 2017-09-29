/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.computation.mpi;

import eu.itesla_project.commons.tools.CommandLineTools;
import eu.itesla_project.commons.tools.ToolInitializationContext;
import eu.itesla_project.computation.ComputationManager;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import java.io.IOException;
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

    public static void main(String[] args) throws IOException {
        ToolInitializationContext initContext = new ToolInitializationContext() {
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

            @Override
            public ComputationManager createComputationManager(CommandLine commandLine) {
                return MpiToolUtil.createMpiComputationManager(commandLine, FileSystems.getDefault());
            }
        };
        int status = new CommandLineTools().run(args, initContext);
        if (status != CommandLineTools.COMMAND_OK_STATUS) {
            System.exit(status);
        }
    }
}
