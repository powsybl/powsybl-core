/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.tools.autocompletion;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Tries to guess option argument type, to guide autocompletion.
 * Works based on option name or argument name.
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at gmail.com>}
 */
public class OptionTypeMapper {

    public static class Key {

        public enum Type {
            OPTION_NAME,
            OPTION_ARG_NAME
        }

        private final String regex;

        private final Type type;

        public Key(String regex, Type type) {
            this.regex = Objects.requireNonNull(regex);
            this.type = Objects.requireNonNull(type);
        }

        public String getRegex() {
            return regex;
        }

        public Type getType() {
            return type;
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof Key key) {
                return regex.equals(key.regex) && type == key.type;
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hash(regex, type);
        }
    }

    private static final class Mapping {

        private final Key key;

        private final OptionType optionType;

        private Mapping(Key key, OptionType optionType) {
            this.key = key;
            this.optionType = optionType;
        }

        private Key getKey() {
            return key;
        }

        private OptionType getOptionType() {
            return optionType;
        }
    }

    private final List<Mapping> mappings = new ArrayList<>();
    private OptionType defaultType;

    public OptionTypeMapper add(Key key, OptionType optionType) {
        mappings.add(new Mapping(key, optionType));
        return this;
    }

    /**
     * Maps a regex on the option name to a specific type
     */
    public OptionTypeMapper addOptionNameMapping(String regex, OptionType optionType) {
        add(new Key(regex, Key.Type.OPTION_NAME), optionType);
        return this;
    }

    /**
     * Maps a regex on the argument name to a specific type
     */
    public OptionTypeMapper addArgNameMapping(String regex, OptionType optionType) {
        add(new Key(regex, Key.Type.OPTION_ARG_NAME), optionType);
        return this;
    }

    /**
     * Sets the type to be used as a fallback
     */
    public OptionTypeMapper setDefaultType(OptionType defaultType) {
        this.defaultType = defaultType;
        return this;
    }

    public void map(BashOption option) {
        if (!option.hasArg()) {
            return;
        }
        for (Mapping mapping : mappings) {
            switch (mapping.getKey().getType()) {
                case OPTION_NAME:
                    if (option.getName().matches(mapping.getKey().getRegex())) {
                        option.setType(mapping.getOptionType());
                    }
                    break;
                case OPTION_ARG_NAME:
                    option.getArgName().ifPresent(name -> {
                        if (name.matches(mapping.getKey().getRegex())) {
                            option.setType(mapping.getOptionType());
                        }
                    });
                    break;
                default:
                    throw new IllegalStateException();
            }
        }
        if (option.getType() == null) {
            option.setType(defaultType);
        }
    }

    public void map(BashCommand command) {
        for (BashOption option : command.getOptions()) {
            if (option.getType() == null) {
                map(option);
            }
        }
    }

    public void map(List<BashCommand> commands) {
        for (BashCommand command : commands) {
            map(command);
        }
    }

}
