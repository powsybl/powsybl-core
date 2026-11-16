/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.tools.autocompletion;

import org.apache.commons.cli.Options;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Simplified option model for completion script generation.
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at gmail.com>}
 */
public class BashOption {

    private final String name;

    private final String argName;

    private OptionType type;

    public BashOption(String name) {
        this(name, null, null);
    }

    public BashOption(String name, String argName) {
        this(name, argName, null);
    }

    public BashOption(String name, String argName, OptionType type) {
        this.name = Objects.requireNonNull(name);
        this.argName = argName;
        this.type = type;
    }

    public static List<BashOption> convert(Options commonsCliOptions) {
        List<BashOption> options = new ArrayList<>();
        for (org.apache.commons.cli.Option commonsCliOption : commonsCliOptions.getOptions()) {
            String optionName = Optional.ofNullable(commonsCliOption.getLongOpt())
                .map(longOpt -> "--" + longOpt)
                .orElse("-" + commonsCliOption.getOpt());
            String argName = commonsCliOption.getArgName();
            BashOption option = new BashOption(optionName, argName, null);
            options.add(option);
        }
        return options;
    }

    /**
     * Full option name, including the "-" or "--" prefix
     */
    public String getName() {
        return name;
    }

    public boolean hasArg() {
        return argName != null;
    }

    public Optional<String> getArgName() {
        return Optional.ofNullable(argName);
    }

    public OptionType getType() {
        return type;
    }

    public void setType(OptionType type) {
        this.type = type;
    }

    public boolean isFile() {
        return type != null && type.getKind() == OptionType.Kind.FILE;
    }

    public boolean isDir() {
        return type != null && type.getKind() == OptionType.Kind.DIRECTORY;
    }

    public boolean isHostname() {
        return type != null && type.getKind() == OptionType.Kind.HOSTNAME;
    }

    public boolean isEnum() {
        return type != null && type.getKind() == OptionType.Kind.ENUMERATION;
    }

    public Object[] getPossibleValues() {
        if (type instanceof OptionType.Enumeration optionType) {
            return optionType.getClazz().getEnumConstants();
        }
        return null;
    }
}
