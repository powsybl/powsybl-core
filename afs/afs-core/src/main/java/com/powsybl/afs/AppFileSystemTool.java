/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs;

import com.google.auto.service.AutoService;
import com.powsybl.tools.Command;
import com.powsybl.tools.Tool;
import com.powsybl.tools.ToolRunningContext;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@AutoService(Tool.class)
public class AppFileSystemTool implements Tool {

    public static final String LS = "ls";

    protected AppData createAppData() {
        return new AppData();
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

    @Override
    public void run(CommandLine line, ToolRunningContext context) throws Exception {
        try (AppData appData = createAppData()) {
            if (line.hasOption(LS)) {
                String path = line.getOptionValue(LS);
                if (path == null) {
                    for (AppFileSystem afs : appData.getFileSystems()) {
                        context.getOutputStream().println(afs.getName());
                    }
                } else {
                    Node node = appData.getNode(path);
                    if (node == null) {
                        context.getErrorStream().println("'" + path + "' does not exist");
                    } else {
                        if (node.isFolder()) {
                            ((Folder) node).getChildren().forEach(child -> context.getOutputStream().println(child.getName()));
                        } else {
                            context.getErrorStream().println("'" + path + "' is not a folder");
                        }
                    }
                }
            } else {
                throw new AfsException("Undefined sub command");
            }
        }
    }
}
