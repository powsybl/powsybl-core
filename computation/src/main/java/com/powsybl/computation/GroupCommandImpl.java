/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.computation;

import com.google.common.collect.ImmutableList;

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
