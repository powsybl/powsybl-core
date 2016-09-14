/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.commons.tools;

import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import com.google.common.collect.TreeMultimap;
import org.apache.commons.cli.*;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Mathieu Bague <mathieu.bague@rte-france.com>
 */
public class Main {

    private static final String TOOL_NAME = "itools";

    private Main() {
    }

    private static void printUsage() {
        StringBuilder usage = new StringBuilder();
        usage.append("usage: " + TOOL_NAME + " COMMAND [ARGS]\n\nAvailable commands are:\n\n");

        List<Tool> allTools = Lists.newArrayList(ServiceLoader.load(Tool.class)).stream()
                .filter(t -> !t.getCommand().isHidden()).collect(Collectors.toList());

        // group commands by theme
        Map<String, Collection<Tool>> toolsByTheme = new TreeMap<>(Multimaps.index(allTools, tool -> tool.getCommand().getTheme()).asMap());
        for (Map.Entry<String, Collection<Tool>> entry : toolsByTheme.entrySet()) {
            String theme = entry.getKey();
            List<Tool> tools = new ArrayList<>(entry.getValue());
            Collections.sort(tools, (t1, t2) -> t1.getCommand().getName().compareTo(t2.getCommand().getName()));
            usage.append(theme != null ? theme : "Others").append(":\n");
            for (Tool tool : tools) {
                usage.append(String.format("   %-40s %s", tool.getCommand().getName(), tool.getCommand().getDescription())).append("\n");
            }
            usage.append("\n");
        }

        System.err.print(usage);
        System.exit(1);
    }

    private static Options getOptionsWithHelp(Options options) {
        Options optionsWithHelp = new Options();
        options.getOptions().forEach(optionsWithHelp::addOption);
        optionsWithHelp.addOption(Option.builder().longOpt("help")
                .desc("display the help and quit")
                .build());
        return optionsWithHelp;
    }

    private static void printCommandUsage(Command command) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(80, TOOL_NAME + " " + command.getName(), "",
                            getOptionsWithHelp(command.getOptions()), "\n" + Objects.toString(command.getUsageFooter(), ""), true);
    }

    private static Tool findTool(String commandName) {
        for (Tool tool : ServiceLoader.load(Tool.class)) {
            if (tool.getCommand().getName().equals(commandName)) {
                return tool;
            }
        }
        return null;
    }

    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            printUsage();
        }

        Tool tool = findTool(args[0]);
        if (tool == null) {
            printUsage();
        }

        try {
            CommandLineParser parser = new DefaultParser();
            CommandLine line = parser.parse(getOptionsWithHelp(tool.getCommand().getOptions()), Arrays.copyOfRange(args, 1, args.length));
            if (line.hasOption("help")) {
                printCommandUsage(tool.getCommand());
            } else {
                tool.run(line);
            }
        } catch (ParseException e) {
            System.err.println("error: " + e.getMessage());
            printCommandUsage(tool.getCommand());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
