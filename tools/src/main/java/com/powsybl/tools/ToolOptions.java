/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.tools;

import org.apache.commons.cli.CommandLine;

import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Wrapper around {@link CommandLine} to provide a more user friendly syntax,
 * in particular based on {@link Optional}s.
 *
 * @author Sylvain Leclerc {@literal <sylvain.leclerc at rte-france.com>}
 */
public class ToolOptions {

    private final CommandLine line;
    private final FileSystem fileSystem;

    public ToolOptions(CommandLine line, FileSystem fileSystem) {
        this.line = Objects.requireNonNull(line);
        this.fileSystem = Objects.requireNonNull(fileSystem);
    }

    public ToolOptions(CommandLine line, ToolRunningContext context) {
        this(line, context.getFileSystem());
    }

    /**
     * If exists, return the option value as a string.
     * @param option The option name
     * @return       The option value as a string if it exists, otherwise empty.
     */
    public Optional<String> getValue(String option) {
        Objects.requireNonNull(option);
        return line.hasOption(option) ? Optional.of(line.getOptionValue(option)) : Optional.empty();
    }

    /**
     * If exists, return the option value parsed with the provided parser.
     * @param option The option name
     * @param parser A function to transform the value from string to target type.
     * @return       The option value parsed with the provided parser if it exists, otherwise empty.
     */
    public <T> Optional<T> getValue(String option, Function<String, T> parser) {
        return getValue(option).map(parser);
    }

    /**
     * If exists, return the option value as an int.
     * @param option The option name
     * @return       The option value as an int if it exists, otherwise empty.
     */
    public Optional<Integer> getInt(String option) {
        return getValue(option, Integer::parseInt);
    }

    /**
     * If exists, return the option value as a float.
     * @param option The option name
     * @return       The option value as a float if it exists, otherwise empty.
     */
    public Optional<Float> getFloat(String option) {
        return getValue(option, Float::parseFloat);
    }

    /**
     * If exists, return the option value as a double.
     * @param option The option name
     * @return       The option value as a double if it exists, otherwise empty.
     */
    public Optional<Double> getDouble(String option) {
        return getValue(option, Double::parseDouble);
    }

    /**
     * If exists, return the option value as a list of strings, assuming they were provided
     * as a comma separated list.
     * @param option The option name
     * @return       The option value as a list of strings if it exists, otherwise empty.
     */
    public Optional<List<String>> getValues(String option) {
        return getValue(option)
                .map(ToolOptions::splitCommaSeparatedList);
    }

    /**
     * If exists, return the option value as an enum.
     * @param option The option name
     * @param clazz  The enum class
     * @return       The option value as as an enum. if it exists, otherwise empty.
     */
    public <E extends Enum<E>> Optional<E> getEnum(String option, Class<E> clazz) {
        return getValue(option, s -> E.valueOf(clazz, s));
    }

    private static List<String> splitCommaSeparatedList(String csl) {
        return Arrays.stream(csl.split(","))
                .filter(ext -> !ext.isEmpty())
                .collect(Collectors.toList());
    }

    /**
     * Return true if the option is defined.
     * @param option The option name
     * @return       {@code true} if the option is defined.
     */
    public boolean hasOption(String option) {
        return line.hasOption(option);
    }

    /**
     * If exists, return the option value as a file system {@link Path}.
     * @param option The option name
     * @return       The option value as a double if it exists, otherwise empty.
     */
    public Optional<Path> getPath(String option) {
        return getValue(option, fileSystem::getPath);
    }
}
