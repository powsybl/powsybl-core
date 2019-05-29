/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs;

import com.google.auto.service.AutoService;
import com.powsybl.afs.storage.NodeInfo;
import com.powsybl.tools.Command;
import com.powsybl.tools.CommandLineTools;
import com.powsybl.tools.Tool;
import com.powsybl.tools.ToolRunningContext;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@AutoService(Tool.class)
public class AppFileSystemTool implements Tool {

    private static final String LS = "ls";
    private static final String ARCHIVE = "archive";
    private static final String UNARCHIVE = "unarchive";
    private static final String DIR = "dir";
    private static final String LS_INCONSISTENT_NODES = "ls-inconsistent-nodes";
    private static final String FIX_INCONSISTENT_NODES = "fix-inconsistent-nodes";
    private static final String RM_INCONSISTENT_NODES = "rm-inconsistent-nodes";
    private static final String FILE_SYSTEM_NAME = "FILE_SYSTEM_NAME";
    private static final String FILE_SYSTEM = "File system'";
    private static final String NOT_FOUND = "not found'";

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
                        .argName(FILE_SYSTEM_NAME)
                        .build());
                topLevelOptions.addOption(Option.builder()
                        .longOpt(UNARCHIVE)
                        .desc("unarchive file system")
                        .hasArg()
                        .optionalArg(true)
                        .argName(FILE_SYSTEM_NAME)
                        .build());
                topLevelOptions.addOption(Option.builder()
                        .longOpt(LS_INCONSISTENT_NODES)
                        .desc("list the inconsistent nodes")
                        .hasArg()
                        .optionalArg(true)
                        .argName(FILE_SYSTEM_NAME)
                        .build());
                topLevelOptions.addOption(Option.builder()
                        .longOpt(FIX_INCONSISTENT_NODES)
                        .desc("make inconsistent nodes consistent")
                        .optionalArg(true)
                        .argName(FILE_SYSTEM_NAME + "> <NODE_ID")
                        .numberOfArgs(2)
                        .valueSeparator(',')
                        .build());
                topLevelOptions.addOption(Option.builder()
                        .longOpt(RM_INCONSISTENT_NODES)
                        .desc("remove inconsistent nodes")
                        .hasArg()
                        .optionalArg(true)
                        .argName(FILE_SYSTEM_NAME + "> <NODE_ID")
                        .numberOfArgs(2)
                        .valueSeparator(',')
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

    private void lsInconsistentNodes(ToolRunningContext context, AppFileSystem afs, String name) {
        List<NodeInfo> nodeInfos = afs.getStorage().getInconsistentNodes();
        if (!nodeInfos.isEmpty()) {
            context.getOutputStream().println(name + ":");
            nodeInfos.forEach(nodeInfo -> context.getOutputStream().println(nodeInfo.getId()));
        }
    }

    private void runLsInconsistentNodes(CommandLine line, ToolRunningContext context) {
        try (AppData appData = createAppData(context)) {
            String fileSystemName = line.getOptionValue(LS_INCONSISTENT_NODES);
            if (fileSystemName == null) {
                for (AppFileSystem afs : appData.getFileSystems()) {
                    lsInconsistentNodes(context, afs, afs.getName());
                }
            } else {
                AppFileSystem fs = appData.getFileSystem(fileSystemName);
                if (fs == null) {
                    throw new AfsException(FILE_SYSTEM + fileSystemName + NOT_FOUND);
                }
                lsInconsistentNodes(context, fs, fileSystemName);
            }
        }
    }

    private void fixInconsistentNodes(ToolRunningContext context, AppFileSystem fs, String nodeId) {
        List<NodeInfo> nodeInfos = fs.getStorage().getInconsistentNodes();
        if (!nodeInfos.isEmpty()) {
            context.getOutputStream().println(fs.getName() + ":");
            if (nodeId == null) {
                nodeInfos.forEach(nodeInfo -> {
                    fs.getStorage().setConsistent(nodeInfo.getId());
                    context.getOutputStream().println(nodeInfo.getId() + " fixed");
                });
            } else {
                nodeInfos.stream()
                        .filter(nodeInfo -> nodeId.equals(nodeInfo.getId()))
                        .forEach(nodeInfo -> {
                            fs.getStorage().setConsistent(nodeInfo.getId());
                            context.getOutputStream().println(nodeInfo.getId() + " fixed");
                        });
            }
        }
    }

    static class InconsistentNodeParam {
        String fileSystemName;
        String nodeId;
    }

    private static InconsistentNodeParam getInconsistentNodeParam(CommandLine line, String optionName) {
        String[] values = line.getOptionValues(optionName);
        if (values == null) {
            throw new IllegalArgumentException("Invalid values for option: " + optionName);
        }
        InconsistentNodeParam param = new InconsistentNodeParam();
        if (values.length == 2) {
            param.fileSystemName = values[0];
            param.nodeId = values[1];
        } else if (values.length == 1) {
            param.fileSystemName = values[0];
        }
        return param;
    }

    private void runFixInconsistentNodes(CommandLine line, ToolRunningContext context) {
        try (AppData appData = createAppData(context)) {
            InconsistentNodeParam param = getInconsistentNodeParam(line, FIX_INCONSISTENT_NODES);
            if (param.fileSystemName == null) {
                for (AppFileSystem afs : appData.getFileSystems()) {
                    fixInconsistentNodes(context, afs, param.nodeId);
                }
            } else {
                AppFileSystem afs = appData.getFileSystem(param.fileSystemName);
                if (afs == null) {
                    throw new AfsException(FILE_SYSTEM + param.fileSystemName + NOT_FOUND);
                }
                fixInconsistentNodes(context, afs, param.nodeId);
            }
        }
    }

    private void removeInconsistentNodes(ToolRunningContext context, AppFileSystem fs, String nodeId) {
        List<NodeInfo> nodeInfos = fs.getStorage().getInconsistentNodes();
        if (!nodeInfos.isEmpty()) {
            context.getOutputStream().println(fs.getName() + ":");
            if (nodeId == null) {
                nodeInfos.forEach(nodeInfo -> {
                    fs.getStorage().deleteNode(nodeInfo.getId());
                    context.getOutputStream().println(nodeInfo.getId() + " cleaned");
                });
            } else {
                nodeInfos.stream()
                        .filter(nodeInfo -> nodeId.equals(nodeInfo.getId()))
                        .forEach(nodeInfo -> {
                            fs.getStorage().deleteNode(nodeInfo.getId());
                            context.getOutputStream().println(nodeInfo.getId() + " cleaned");
                        });
            }
        }
    }

    private void runRemoveInconsistentNodes(CommandLine line, ToolRunningContext context) {
        try (AppData appData = createAppData(context)) {
            InconsistentNodeParam param = getInconsistentNodeParam(line, RM_INCONSISTENT_NODES);
            if (param.fileSystemName == null) {
                for (AppFileSystem afs : appData.getFileSystems()) {
                    removeInconsistentNodes(context, afs, param.nodeId);
                }
            } else {
                AppFileSystem afs = appData.getFileSystem(param.fileSystemName);
                if (afs == null) {
                    throw new AfsException(FILE_SYSTEM + param.fileSystemName + NOT_FOUND);
                }
                removeInconsistentNodes(context, afs, param.nodeId);
            }
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
        } else if (line.hasOption(LS_INCONSISTENT_NODES)) {
            runLsInconsistentNodes(line, context);
        } else if (line.hasOption(FIX_INCONSISTENT_NODES)) {
            runFixInconsistentNodes(line, context);
        } else if (line.hasOption(RM_INCONSISTENT_NODES)) {
            runRemoveInconsistentNodes(line, context);
        } else {
            Command command = getCommand();
            CommandLineTools.printCommandUsage(command.getName(), command.getOptions(), command.getUsageFooter(), context.getErrorStream());
        }
    }
}
