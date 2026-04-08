/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.tools.autocompletion;

import org.apache.commons.cli.Options;

import java.util.*;

/**
 * Simplified command model for completion script generation.
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at gmail.com>}
 */
public class BashCommand {

    private final String name;

    private final List<BashOption> options;

    public BashCommand(String name, BashOption... options) {
        this(name, Arrays.asList(options));
    }

    public BashCommand(String name, List<BashOption> options) {
        this.name = Objects.requireNonNull(name);
        this.options = Objects.requireNonNull(options);
    }

    public static List<BashCommand> convert(Map<String, Options> commonsCliCommands) {
        List<BashCommand> commands = new ArrayList<>();
        for (Map.Entry<String, Options> entry : commonsCliCommands.entrySet()) {
            String commandName = entry.getKey();
            org.apache.commons.cli.Options commonsCliOptions = entry.getValue();
            List<BashOption> options = BashOption.convert(commonsCliOptions);
            commands.add(new BashCommand(commandName, options));
        }
        return commands;
    }

    public BashCommand(String name) {
        this(name, new ArrayList<>());
    }

    public String getName() {
        return name;
    }

    public void addOption(BashOption option) {
        options.add(option);
    }

    public List<BashOption> getOptions() {
        return options;
    }

}
