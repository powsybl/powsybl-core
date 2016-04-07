/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.computation;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.util.List;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class GroupCommandImpl extends AbstractCommand implements GroupCommand {

    private final List<SubCommand> subCommands;

    static class SubCommandImpl implements SubCommand {

        private final String program;

        private final List<String> args;

        private final int timeout;
        
        SubCommandImpl(String program, List<String> args, int timeout) {
            this.program = program;
            this.args = args;
            this.timeout = timeout;
        }

        @Override
        public String getProgram() {
            return program;
        }

        @Override
        public List<String> getArgs(final String executionNumber) {
            return Lists.transform(args, new Function<String, String>() {
                @Override
                public String apply(String args) {
                    return args.replace(EXECUTION_NUMBER_PATTERN, executionNumber);
                }
            });
        }

        @Override
        public int getTimeout() {
            return timeout;
        }

        @Override
        public String toString(String executionNumber) {
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
    public String toString(final String executionNumber) {
        return Lists.transform(subCommands, new Function<SubCommand, String>() {
            @Override
            public String apply(SubCommand subCommand) {
                return subCommand.toString(executionNumber);
            }
        }).toString();
    }

}
