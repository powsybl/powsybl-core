/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.computation;

import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class GroupCommandImpl extends AbstractCommand implements GroupCommand {

    private final List<SubCommand> subCommands;

    static class SubCommandImpl implements SubCommand {

        private final String program;

        private final Function<Integer, List<String>> args;
        private List<String> argsPattern;

        private final int timeout;

        SubCommandImpl(String program, Function<Integer, List<String>> args, int timeout) {
            this.program = program;
            this.args = args;
            this.timeout = timeout;
        }

        @Override
        public String getProgram() {
            return program;
        }

        @Override
        public List<String> getArgs(int executionNumber) {
            return args.apply(executionNumber);
        }

        @Override
        public List<String> getArgs() {
            if (argsPattern == null) {
                rebuildPattern();
            }
            return argsPattern;
        }

        private void rebuildPattern() {
            argsPattern = new ArrayList<>();
            List<String> list1 = args.apply(1);
            List<String> list2 = args.apply(2);
            for (int i = 0; i < list1.size(); i++) {
                String str1 = list1.get(i);
                String str2 = list2.get(i);
                int idx = -1;
                for (int j = 0; j < str1.length(); j++) {
                    if (str1.charAt(j) == '1' && str2.charAt(j) == '2') {
                        idx = j;
                        String before = str1.substring(0, idx);
                        String after = str1.substring(idx + 1, str1.length());
                        String pattern = before + Command.EXECUTION_NUMBER_PATTERN + after;
                        argsPattern.add(pattern);
                    }
                }
                if (idx == -1) {
                    argsPattern.add(str1);
                }
            }
        }

        @Override
        public int getTimeout() {
            return timeout;
        }

        @Override
        public String toString(int executionNumber) {
            return ImmutableList.<String>builder()
                .add(program)
                .addAll(getArgs(executionNumber))
                .build().toString();
        }

    }

    GroupCommandImpl(String id, List<SubCommand> subCommands,
                     List<InputFile> inputFiles, List<OutputFile> outputFiles) {
        super(id, inputFiles, outputFiles);
        this.subCommands = subCommands;
    }

    @Override
    public List<SubCommand> getSubCommands() {
        return subCommands;
    }

    @Override
    public CommandType getType() {
        return CommandType.GROUP;
    }

    @Override
    public String toString(final int executionNumber) {
        return subCommands.stream()
                .map(subCommand -> subCommand.toString(executionNumber))
                .collect(Collectors.toList())
                .toString();
    }

}
