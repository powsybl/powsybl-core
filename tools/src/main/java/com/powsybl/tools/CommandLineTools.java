/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.tools;

import com.google.common.collect.Lists;
import com.google.common.collect.Multimaps;
import com.powsybl.computation.ComputationManager;
import org.apache.commons.cli.*;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.*;
import java.util.stream.Collectors;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
public class CommandLineTools {

    public static final int COMMAND_OK_STATUS = 0;
    public static final int COMMAND_NOT_FOUND_STATUS = 1;
    public static final int INVALID_COMMAND_STATUS = 2;
    public static final int EXECUTION_ERROR_STATUS = 3;

    private final Iterable<Tool> tools;

    public CommandLineTools() {
        this(ServiceLoader.load(Tool.class));
    }

    public CommandLineTools(Iterable<Tool> tools) {
        this.tools = Objects.requireNonNull(tools);
    }

    private int printUsage(PrintStream err) {
        StringBuilder usage = new StringBuilder();
        usage.append("Available commands are:")
                .append(System.lineSeparator())
                .append(System.lineSeparator());

        List<Tool> allTools = Lists.newArrayList(tools).stream()
                .filter(t -> !t.getCommand().isHidden()).collect(Collectors.toList());

        // group commands by theme
        Map<String, Collection<Tool>> toolsByTheme = new TreeMap<>(Multimaps.index(allTools, tool -> tool.getCommand().getTheme()).asMap());
        for (Map.Entry<String, Collection<Tool>> entry : toolsByTheme.entrySet()) {
            String theme = entry.getKey();
            List<Tool> tools = new ArrayList<>(entry.getValue());
            Collections.sort(tools, Comparator.comparing(t -> t.getCommand().getName()));
            usage.append(theme != null ? theme : "Others").append(":").append(System.lineSeparator());
            for (Tool tool : tools) {
                usage.append(String.format("   %-40s %s", tool.getCommand().getName(), tool.getCommand().getDescription())).append(System.lineSeparator());
            }
            usage.append(System.lineSeparator());
        }

        err.print(usage);
        return COMMAND_NOT_FOUND_STATUS;
    }

    private static Options getOptionsWithHelp(Options options) {
        Options optionsWithHelp = new Options();
        options.getOptions().forEach(optionsWithHelp::addOption);
        optionsWithHelp.addOption(Option.builder()
                .longOpt("help")
                .desc("display the help and quit")
                .build());
        return optionsWithHelp;
    }

    private void printCommandUsage(String name, Options options, String usageFooter, PrintStream err) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.setSyntaxPrefix("command usage: ");
        PrintWriter writer = new PrintWriter(err);
        formatter.printHelp(writer,
                            80,
                            name,
                            "", // header
                            getOptionsWithHelp(options),
                            formatter.getLeftPadding(),
                            formatter.getDescPadding(),
                            System.lineSeparator() + Objects.toString(usageFooter, ""),
                            true);
        writer.flush();
    }

    private Tool findTool(String commandName) {
        for (Tool tool : tools) {
            if (tool.getCommand().getName().equals(commandName)) {
                return tool;
            }
        }
        return null;
    }

    public int run(String[] args, ToolInitializationContext initContext) {
        Objects.requireNonNull(args);
        Objects.requireNonNull(initContext);

        if (args.length < 1) {
            return printUsage(initContext.getErrorStream());
        }

        Tool tool = findTool(args[0]);
        if (tool == null) {
            return printUsage(initContext.getErrorStream());
        }

        Options optionsExt = new Options();
        initContext.getAdditionalOptions().getOptions().forEach(optionsExt::addOption);
        tool.getCommand().getOptions().getOptions().forEach(optionsExt::addOption);

        try {
            CommandLineParser parser = new DefaultParser();
            if (Arrays.asList(args).contains("--help")) {
                printCommandUsage(tool.getCommand().getName(), optionsExt, tool.getCommand().getUsageFooter(), initContext.getErrorStream());
            } else {
                CommandLine line = parser.parse(optionsExt, Arrays.copyOfRange(args, 1, args.length));
                try (ComputationManager computationManager = initContext.createComputationManager(line)) {
                    tool.run(line, new ToolRunningContext(initContext.getOutputStream(),
                                                          initContext.getErrorStream(),
                                                          initContext.getFileSystem(),
                                                          computationManager));
                }
            }
            return COMMAND_OK_STATUS;
        } catch (ParseException e) {
            initContext.getErrorStream().println("error: " + e.getMessage());
            printCommandUsage(tool.getCommand().getName(), optionsExt, tool.getCommand().getUsageFooter(), initContext.getErrorStream());
            return INVALID_COMMAND_STATUS;
        } catch (Exception e) {
            e.printStackTrace(initContext.getErrorStream());
            return EXECUTION_ERROR_STATUS;
        }
    }
}
