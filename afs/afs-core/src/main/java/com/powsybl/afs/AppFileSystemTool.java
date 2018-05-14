/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs;

import com.google.auto.service.AutoService;
import com.powsybl.tools.Command;
import com.powsybl.tools.CommandLineTools;
import com.powsybl.tools.Tool;
import com.powsybl.tools.ToolRunningContext;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;

import java.util.Optional;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@AutoService(Tool.class)
public class AppFileSystemTool implements Tool {

    public static final String LS = "ls";

    protected AppData createAppData(ToolRunningContext context) {
        return new AppData(context.getShortTimeExecutionComputationManager(),
                           context.getLongTimeExecutionComputationManager());
    }

    @Override
    public Command getCommand() {
        return new Command() {
            @Override
            public String getName() {
                return "afs";
            }

            @Override
            public String getTheme() {
                return "Application file system";
            }

            @Override
            public String getDescription() {
                return "application file system command line tool";
            }

            @Override
            public Options getOptions() {
                Options options = new Options();
                OptionGroup optionGroup = new OptionGroup();
                optionGroup.addOption(Option.builder()
                        .longOpt(LS)
                        .desc("list files")
                        .hasArg()
                        .optionalArg(true)
                        .argName("PATH")
                        .build());
                options.addOptionGroup(optionGroup);
                return options;
            }

            @Override
            public String getUsageFooter() {
                return null;
            }
        };
    }

    private void runLs(ToolRunningContext context, AppData appData, String path) {
        if (path == null) {
            for (AppFileSystem afs : appData.getFileSystems()) {
                context.getOutputStream().println(afs.getName());
            }
        } else {
            Optional<Node> node = appData.getNode(path);
            if (node.isPresent()) {
                if (node.get().isFolder()) {
                    ((Folder) node.get()).getChildren().forEach(child -> context.getOutputStream().println(child.getName()));
                } else {
                    context.getErrorStream().println("'" + path + "' is not a folder");
                }
            } else {
                context.getErrorStream().println("'" + path + "' does not exist");
            }
        }
    }

    @Override
    public void run(CommandLine line, ToolRunningContext context) {
        try (AppData appData = createAppData(context)) {
            if (line.hasOption(LS)) {
                String path = line.getOptionValue(LS);
                runLs(context, appData, path);
            } else {
                Command command = getCommand();
                CommandLineTools.printCommandUsage(command.getName(), command.getOptions(), command.getUsageFooter(), context.getErrorStream());
            }
        }
    }
}
