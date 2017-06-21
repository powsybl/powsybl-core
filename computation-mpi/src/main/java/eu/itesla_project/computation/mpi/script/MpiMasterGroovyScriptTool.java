/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.computation.mpi.script;

import com.google.auto.service.AutoService;
import eu.itesla_project.commons.tools.Command;
import eu.itesla_project.commons.tools.Tool;
import eu.itesla_project.commons.tools.ToolRunningContext;
import eu.itesla_project.computation.ComputationManager;
import eu.itesla_project.computation.mpi.tools.MpiToolUtil;
import eu.itesla_project.computation.script.GroovyScripts;
import groovy.lang.Binding;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import java.nio.file.Path;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@AutoService(Tool.class)
public class MpiMasterGroovyScriptTool implements Tool {

    private static final Command COMMAND = new Command() {
        @Override
        public String getName() {
            return "mpi-master-groovy-script";
        }

        @Override
        public String getTheme() {
            return "Computation";
        }

        @Override
        public String getDescription() {
            return "run groovy script in mpi computation mode";
        }

        @Override
        public Options getOptions() {
            Options options = MpiToolUtil.createMpiOptions();
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
        public boolean isHidden() {
            return true;
        }

        @Override
        public String getUsageFooter() {
            return null;
        }
    };
    @Override
    public Command getCommand() {
        return COMMAND;
    }

    @Override
    public void run(CommandLine line, ToolRunningContext context) throws Exception {
        try (ComputationManager computationManager = MpiToolUtil.createMpiComputationManager(line, context.getFileSystem())) {
            Path file = context.getFileSystem().getPath(line.getOptionValue("script"));

            Binding binding = new Binding();
            binding.setProperty("args", line.getArgs());

            GroovyScripts.run(file, computationManager, binding, null);
        }
    }
}
