/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.computation;

import eu.itesla_project.computation.GroupCommand.SubCommand;
import eu.itesla_project.computation.GroupCommandImpl.SubCommandImpl;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class GroupCommandBuilder extends AbstractCommandBuilder<GroupCommandBuilder> {

    private final List<SubCommand> subCommands = new ArrayList<>();

    public class SubCommandBuilder {

        private String program;

        private List<String> args = Collections.emptyList();

        private int timeout = -1;

        public SubCommandBuilder program(String program) {
            this.program = program;
            return this;
        }

        public SubCommandBuilder args(String... args) {
            this.args = Arrays.asList(args);
            return this;
        }

        public SubCommandBuilder timeout(int timeout) {
            if (timeout < -1 || timeout == 0) {
                throw new RuntimeException("invalid timeout");
            }
            this.timeout = timeout;
            return this;
        }

        public GroupCommandBuilder add() {
            if (program == null) {
                throw new RuntimeException("program is not set");
            }
            subCommands.add(new SubCommandImpl(program, args, timeout));
            return GroupCommandBuilder.this;
        }

    }

    public GroupCommandBuilder() {
    }

    public SubCommandBuilder subCommand() {
        return new SubCommandBuilder();
    }

    public GroupCommand build() {
        if (id == null) {
            throw new RuntimeException("id is not set");
        }
        return new GroupCommandImpl(id, subCommands, inputFiles, outputFiles);
    }

}
