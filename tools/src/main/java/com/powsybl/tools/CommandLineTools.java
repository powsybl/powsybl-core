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

import static com.powsybl.tools.ToolConstants.TASK;
import static com.powsybl.tools.ToolConstants.TASK_COUNT;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
public class CommandLineTools {

    private static final String TOOL_NAME = "itools";
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
        HelpFormatter formatter = new HelpFormatter();
        PrintWriter usage = new PrintWriter(err);

        formatter.printUsage(usage, 80, "itools [OPTIONS] COMMAND [ARGS]");

        usage.append(System.lineSeparator())
            .append("Available options are:")
            .append(System.lineSeparator());

        formatter.printOptions(usage, 80, getScriptOptions(), formatter.getLeftPadding(), formatter.getDescPadding());

        usage.append(System.lineSeparator())
            .append("Available commands are:")
            .append(System.lineSeparator())
            .append(System.lineSeparator());

        List<Tool> allTools = Lists.newArrayList(tools).stream()
                .filter(t -> !t.getCommand().isHidden()).collect(Collectors.toList());

        // group commands by theme
        Map<String, Collection<Tool>> toolsByTheme = new TreeMap<>(Multimaps.index(allTools, tool -> tool.getCommand().getTheme()).asMap());
        for (Map.Entry<String, Collection<Tool>> entry : toolsByTheme.entrySet()) {
            String theme = entry.getKey();
            usage.append(theme != null ? theme : "Others").append(":").append(System.lineSeparator());
            entry.getValue().stream()
                .sorted(Comparator.comparing(t -> t.getCommand().getName()))
                .forEach(tool ->
                    usage.append(String.format("    %-40s %s", tool.getCommand().getName(), tool.getCommand().getDescription())).append(System.lineSeparator())
            );
            usage.append(System.lineSeparator());
        }

        usage.flush();
        return COMMAND_NOT_FOUND_STATUS;
    }

    private static Options getScriptOptions() {
        Options options = new Options();
        options.addOption(Option.builder()
            .longOpt("config-name")
            .desc("Override configuration file name")
            .required(false)
            .hasArg()
            .argName("CONFIG_NAME")
            .build());
        options.addOption(Option.builder()
            .longOpt("parallel")
            .desc("Run command in parallel mode")
            .required(false)
            .build());

        return options;
    }

    private static Options hideOptions(Options originalOptions, String... hiddenOptions) {
        Options filteredOptions = new Options();
        Set<String> hiddenOptionsSet = new HashSet<>(Arrays.asList(hiddenOptions));
        originalOptions.getOptions().stream()
                .filter(o -> !hiddenOptionsSet.contains(o.getLongOpt()))
                .forEach(filteredOptions::addOption);
        return filteredOptions;
    }

    private static Options getOptionsWithHelp(Options options) {
        Options optionsWithHelp = hideOptions(options, TASK, TASK_COUNT);
        optionsWithHelp.addOption(Option.builder()
                .longOpt("help")
                .desc("display the help and quit")
                .build());
        return optionsWithHelp;
    }

    public static void printCommandUsage(String name, Options options, String usageFooter, PrintStream err) {
        HelpFormatter formatter = new HelpFormatter();
        PrintWriter writer = new PrintWriter(err);

        formatter.printUsage(writer, 80, TOOL_NAME + " [OPTIONS] " + name, getOptionsWithHelp(options));

        formatter.printWrapped(writer, 80, System.lineSeparator() + "Available options are:" + System.lineSeparator());
        formatter.printOptions(writer, 80, getScriptOptions(), formatter.getLeftPadding(), formatter.getDescPadding());

        formatter.printWrapped(writer, 80, System.lineSeparator() + "Available arguments are:" + System.lineSeparator());
        formatter.printOptions(writer, 80, getOptionsWithHelp(options), formatter.getLeftPadding(), formatter.getDescPadding());

        formatter.printWrapped(writer, 80, System.lineSeparator() + Objects.toString(usageFooter, ""));

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
                try (ComputationManager shortTimeExecutionComputationManager = initContext.createShortTimeExecutionComputationManager(line);
                     ComputationManager longRunningTaskComputationManager = initContext.createLongTimeExecutionComputationManager(line)) {
                    tool.run(line, new ToolRunningContext(initContext.getOutputStream(),
                                                          initContext.getErrorStream(),
                                                          initContext.getFileSystem(),
                                                          shortTimeExecutionComputationManager,
                                                          longRunningTaskComputationManager));
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
