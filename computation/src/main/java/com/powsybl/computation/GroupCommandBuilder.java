/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.computation;

import com.powsybl.commons.PowsyblException;
import com.powsybl.computation.GroupCommand.SubCommand;
import com.powsybl.computation.GroupCommandImpl.SubCommandImpl;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class GroupCommandBuilder extends AbstractCommandBuilder<GroupCommandBuilder> {

    private final List<SubCommand> subCommands = new ArrayList<>();

    public class SubCommandBuilder {

        private String program;

        private Function<Integer, List<String>> args = executionNumber -> Collections.emptyList();

        private int timeout = -1;

        public SubCommandBuilder program(String program) {
            this.program = program;
            return this;
        }

        public SubCommandBuilder args(Function<Integer, List<String>> args) {
            this.args = Objects.requireNonNull(args);
            return this;
        }

        public SubCommandBuilder args(List<String> args) {
            Objects.requireNonNull(args);
            this.args = executionNumber -> args.stream()
                                               .map(arg -> arg.replace(CommandConstants.EXECUTION_NUMBER_PATTERN, executionNumber.toString()))
                                               .collect(Collectors.toList());
            return this;
        }

        public SubCommandBuilder args(String... args) {
            Objects.requireNonNull(args);
            args(Arrays.asList(args));
            return this;
        }

        public SubCommandBuilder timeout(int timeout) {
            if (timeout < -1 || timeout == 0) {
                throw new PowsyblException("invalid timeout");
            }
            this.timeout = timeout;
            return this;
        }

        public GroupCommandBuilder add() {
            if (program == null) {
                throw new PowsyblException("program is not set");
            }
            subCommands.add(new SubCommandImpl(program, args, timeout));
            return GroupCommandBuilder.this;
        }

    }

    public SubCommandBuilder subCommand() {
        return new SubCommandBuilder();
    }

    public GroupCommand build() {
        if (id == null) {
            throw new PowsyblException("id is not set");
        }
        return new GroupCommandImpl(id, subCommands, inputFiles, outputFiles);
    }

}
