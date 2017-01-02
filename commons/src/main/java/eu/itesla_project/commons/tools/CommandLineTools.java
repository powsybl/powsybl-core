/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.commons.tools;

import com.google.common.collect.Lists;
import com.google.common.collect.Multimaps;
import org.apache.commons.cli.*;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.*;
import java.util.stream.Collectors;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Mathieu Bague <mathieu.bague@rte-france.com>
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
        StringBuilder usage = new StringBuilder();
        usage.append("usage: ")
                .append(TOOL_NAME)
                .append(" COMMAND [ARGS]")
                .append(System.lineSeparator())
                .append(System.lineSeparator())
                .append("Available commands are:")
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

    private void printCommandUsage(Command command, PrintStream err) {
        HelpFormatter formatter = new HelpFormatter();
        PrintWriter writer = new PrintWriter(err);
        formatter.printHelp(writer,
                            80,
                            TOOL_NAME + " " + command.getName(),
                            "", // header
                            getOptionsWithHelp(command.getOptions()),
                            formatter.getLeftPadding(),
                            formatter.getDescPadding(),
                            System.lineSeparator() + Objects.toString(command.getUsageFooter(), ""),
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

    public int run(String[] args, ToolRunningContext context) {
        Objects.requireNonNull(args);
        Objects.requireNonNull(context);

        if (args.length < 1) {
            return printUsage(context.getErrorStream());
        }

        Tool tool = findTool(args[0]);
        if (tool == null) {
            return printUsage(context.getErrorStream());
        }

        try {
            CommandLineParser parser = new DefaultParser();
            if (Arrays.asList(args).contains("--help")) {
                printCommandUsage(tool.getCommand(), context.getErrorStream());
            } else {
                CommandLine line = parser.parse(getOptionsWithHelp(tool.getCommand().getOptions()), Arrays.copyOfRange(args, 1, args.length));
                tool.run(line, context);
            }
            return COMMAND_OK_STATUS;
        } catch (ParseException e) {
            context.getErrorStream().println("error: " + e.getMessage());
            printCommandUsage(tool.getCommand(), context.getErrorStream());
            return INVALID_COMMAND_STATUS;
        } catch (Exception e) {
            e.printStackTrace(context.getErrorStream());
            return EXECUTION_ERROR_STATUS;
        }
    }
}
