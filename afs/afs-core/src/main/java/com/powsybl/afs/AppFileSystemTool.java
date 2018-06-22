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

import java.nio.file.Path;
import java.util.Optional;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@AutoService(Tool.class)
public class AppFileSystemTool implements Tool {

    public static final String LS = "ls";
    public static final String ARCHIVE = "archive";
    public static final String UNARCHIVE = "unarchive";
    public static final String DIR = "dir";

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
                OptionGroup topLevelOptions = new OptionGroup();
                topLevelOptions.addOption(Option.builder()
                        .longOpt(LS)
                        .desc("list files")
                        .hasArg()
                        .optionalArg(true)
                        .argName("PATH")
                        .build());
                topLevelOptions.addOption(Option.builder()
                        .longOpt(ARCHIVE)
                        .desc("archive file system")
                        .hasArg()
                        .optionalArg(true)
                        .argName("FILE_SYSTEM_NAME")
                        .build());
                topLevelOptions.addOption(Option.builder()
                        .longOpt(UNARCHIVE)
                        .desc("unarchive file system")
                        .hasArg()
                        .optionalArg(true)
                        .argName("FILE_SYSTEM_NAME")
                        .build());
                options.addOptionGroup(topLevelOptions);
                options.addOption(Option.builder()
                        .longOpt(DIR)
                        .desc("directory")
                        .hasArg()
                        .argName("DIR")
                        .build());
                return options;
            }

            @Override
            public String getUsageFooter() {
                return null;
            }
        };
    }

    private void runLs(CommandLine line, ToolRunningContext context) {
        try (AppData appData = createAppData(context)) {
            String path = line.getOptionValue(LS);
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
    }

    private void runUnarchive(CommandLine line, ToolRunningContext context) {
        if (!line.hasOption(DIR)) {
            throw new AfsException("dir option is missing");
        }
        try (AppData appData = createAppData(context)) {
            String fileSystemName = line.getOptionValue(UNARCHIVE);
            AppFileSystem fs = appData.getFileSystem(fileSystemName);
            if (fs == null) {
                throw new  AfsException("File system '" + fileSystemName + "' not found");
            }
            Path dir = context.getFileSystem().getPath(line.getOptionValue(DIR));
            fs.getRootFolder().unarchive(dir);
        }
    }

    private void runArchive(CommandLine line, ToolRunningContext context) {
        if (!line.hasOption(DIR)) {
            throw new AfsException("dir option is missing");
        }
        try (AppData appData = createAppData(context)) {
            String fileSystemName = line.getOptionValue(ARCHIVE);
            AppFileSystem fs = appData.getFileSystem(fileSystemName);
            if (fs == null) {
                throw new  AfsException("File system '" + fileSystemName + "' not found");
            }
            Path dir = context.getFileSystem().getPath(line.getOptionValue(DIR));
            fs.getRootFolder().archive(dir);
        }
    }

    @Override
    public void run(CommandLine line, ToolRunningContext context) {
        if (line.hasOption(LS)) {
            runLs(line, context);
        } else if (line.hasOption(ARCHIVE)) {
            runArchive(line, context);
        } else if (line.hasOption(UNARCHIVE)) {
            runUnarchive(line, context);
        } else {
            Command command = getCommand();
            CommandLineTools.printCommandUsage(command.getName(), command.getOptions(), command.getUsageFooter(), context.getErrorStream());
        }
    }
}
