/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.computation.local;

import eu.itesla_project.commons.tools.CommandLineTools;
import eu.itesla_project.commons.tools.ToolInitializationContext;
import eu.itesla_project.computation.ComputationManager;
import eu.itesla_project.computation.local.LocalComputationManager;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import java.io.IOException;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class Main {

    public static void main(String[] args) throws IOException {
        int status = new CommandLineTools().run(args, new ToolInitializationContext() {
            @Override
            public PrintStream getOutputStream() {
                return System.out;
            }

            @Override
            public PrintStream getErrorStream() {
                return System.err;
            }

            @Override
            public FileSystem getFileSystem() {
                return FileSystems.getDefault();
            }

            @Override
            public Options getAdditionalOptions() {
                return new Options();
            }

            @Override
            public ComputationManager createComputationManager(CommandLine commandLine) {
                try {
                    return new LocalComputationManager();
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }
        });
        if (status != CommandLineTools.COMMAND_OK_STATUS) {
            System.exit(status);
        }
    }
}
