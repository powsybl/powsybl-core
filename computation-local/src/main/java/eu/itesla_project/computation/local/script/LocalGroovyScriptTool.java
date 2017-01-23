/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.computation.local.script;

import com.google.auto.service.AutoService;
import eu.itesla_project.commons.tools.Command;
import eu.itesla_project.commons.tools.Tool;
import eu.itesla_project.commons.tools.ToolRunningContext;
import eu.itesla_project.computation.ComputationManager;
import eu.itesla_project.computation.local.LocalComputationManager;
import eu.itesla_project.computation.script.GroovyScripts;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Path;
import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@AutoService(Tool.class)
public class LocalGroovyScriptTool implements Tool {

    private static final Command COMMAND = new Command() {
        @Override
        public String getName() {
            return "local-groovy-script";
        }

        @Override
        public String getTheme() {
            return "Computation";
        }

        @Override
        public String getDescription() {
            return "run groovy script in local computation mode";
        }

        @Override
        public Options getOptions() {
            Options options = new Options();
            options.addOption(Option.builder()
                    .longOpt("script")
                    .desc("the groovy script")
                    .hasArg()
                    .required()
                    .argName("FILE")
                    .build());
            return options;
        }

        @Override
        public String getUsageFooter() {
            return null;
        }
    };

    private final ComputationManager computationManager;

    public LocalGroovyScriptTool() {
        this(LocalComputationManager.getDefault());
    }

    public LocalGroovyScriptTool(ComputationManager computationManager) {
        this.computationManager = Objects.requireNonNull(computationManager);
    }

    @Override
    public Command getCommand() {
        return COMMAND;
    }

    @Override
    public void run(CommandLine line, ToolRunningContext context) throws Exception {
        Path file = context.getFileSystem().getPath(line.getOptionValue("script"));
        Writer writer = new OutputStreamWriter(context.getOutputStream());
        GroovyScripts.run(file, computationManager, writer);
        writer.flush();
    }
}
