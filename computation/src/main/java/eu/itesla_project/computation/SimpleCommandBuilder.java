/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.computation;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class SimpleCommandBuilder extends AbstractCommandBuilder<SimpleCommandBuilder> {

    private String program;

    private List<String> args = Collections.emptyList();

    private int timeout = -1;

    public SimpleCommandBuilder() {
    }

    public SimpleCommandBuilder program(String program) {
        this.program = program;
        return this;
    }

    public SimpleCommandBuilder args(List<String> args) {
        this.args = args;
        return this;
    }

    public SimpleCommandBuilder args(String... args) {
        this.args = Arrays.asList(args);
        return this;
    }

    public SimpleCommandBuilder timeout(int timeout) {
        this.timeout = timeout;
        if (timeout < -1 || timeout == 0) {
            throw new RuntimeException("invalid timeout");
        }
        return this;
    }

    public SimpleCommand build() {
        if (id == null) {
            throw new RuntimeException("id is not set");
        }
        if (program == null) {
            throw new RuntimeException("program is not set");
        }
        return new SimpleCommandImpl(id, program, args, timeout, inputFiles, outputFiles);
    }

}
