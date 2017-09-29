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

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class SimpleCommandImpl extends AbstractCommand implements SimpleCommand {

    private final String program;

    private final Function<Integer, List<String>> args;

    private final int timeout;

    SimpleCommandImpl(String id, String program, Function<Integer, List<String>> args, int timeout,
                      List<InputFile> inputFiles, List<OutputFile> outputFiles) {
        super(id, inputFiles, outputFiles);
        this.program = program;
        this.args = args;
        this.timeout = timeout;
    }

    @Override
    public CommandType getType() {
        return CommandType.SIMPLE;
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
