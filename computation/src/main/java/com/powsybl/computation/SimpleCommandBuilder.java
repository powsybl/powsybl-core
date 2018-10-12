/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.computation;

import com.powsybl.commons.PowsyblException;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class SimpleCommandBuilder extends AbstractCommandBuilder<SimpleCommandBuilder> {

    private String program;

    private Function<Integer, List<String>> args = executionNumber -> Collections.emptyList();

    private int timeout = -1;

    public SimpleCommandBuilder program(String program) {
        this.program = program;
        return this;
    }

    public SimpleCommandBuilder args(Function<Integer, List<String>> args) {
        this.args = Objects.requireNonNull(args);
        return this;
    }

    public SimpleCommandBuilder args(List<String> args) {
        Objects.requireNonNull(args);
        this.args = executionNumber -> args.stream()
                                           .map(arg -> arg.replace(CommandConstants.EXECUTION_NUMBER_PATTERN, executionNumber.toString()))
                                           .collect(Collectors.toList());
        return this;
    }

    public SimpleCommandBuilder args(String... args) {
        Objects.requireNonNull(args);
        args(Arrays.asList(args));
        return this;
    }

    public SimpleCommandBuilder arg(String arg) {
        Objects.requireNonNull(args);
        arg(i -> arg);
        return this;
    }

    public SimpleCommandBuilder arg(Function<Integer, String> arg) {
        Objects.requireNonNull(arg);
        Function<Integer, List<String> > previous = args;
        args = i -> {
            List<String> r = new ArrayList<>(previous.apply(i));
            r.add(arg.apply(i));
            return r;
        };
        return this;
    }

    /**
     * Adds a flag if {@param flagValue} is true.
     */
    public SimpleCommandBuilder flag(String flagName, boolean flagValue) {
        Objects.requireNonNull(flagName);

        if (flagValue) {
            arg("--" + flagName);
        }
        return this;
    }

    /**
     * Adds an option "--opt=value".
     */
    public SimpleCommandBuilder option(String opt, String value) {
        Objects.requireNonNull(opt);
        Objects.requireNonNull(value);

        arg("--" + opt + "=" + value);
        return this;
    }

    /**
     * Adds an option dependent on the execution count.
     */
    public SimpleCommandBuilder option(String opt, Function<Integer, String> fn) {
        Objects.requireNonNull(opt);
        Objects.requireNonNull(fn);
        arg(i -> "--" + opt + "=" + fn.apply(i));
        return this;
    }

    public SimpleCommandBuilder timeout(int timeout) {
        this.timeout = timeout;
        if (timeout < -1 || timeout == 0) {
            throw new PowsyblException("invalid timeout");
        }
        return this;
    }

    public SimpleCommand build() {
        if (id == null) {
            throw new PowsyblException("id is not set");
        }
        if (program == null) {
            throw new PowsyblException("program is not set");
        }
        return new SimpleCommandImpl(id, program, args, timeout, inputFiles, outputFiles);
    }

}
