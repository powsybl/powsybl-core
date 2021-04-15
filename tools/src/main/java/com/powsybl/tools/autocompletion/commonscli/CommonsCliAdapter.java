/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.tools.autocompletion.commonscli;

import com.powsybl.tools.autocompletion.BashCommand;
import com.powsybl.tools.autocompletion.BashOption;
import org.apache.commons.cli.Options;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Adapter for apache commons CLI options
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at gmail.com>
 */
public final class CommonsCliAdapter {

    private CommonsCliAdapter() {
    }

    public static List<BashOption> adaptOptions(Options commonsCliOptions) {
        List<BashOption> options = new ArrayList<>();
        for (org.apache.commons.cli.Option commonsCliOption : commonsCliOptions.getOptions()) {
            String optionName = commonsCliOption.getLongOpt();
            if (optionName == null) {
                optionName = commonsCliOption.getOpt();
            }
            String argName = commonsCliOption.getArgName();
            BashOption option = new BashOption(optionName, argName, null);
            options.add(option);
        }
        return options;
    }

    public static List<BashCommand> adaptCommands(Map<String, Options> commonsCliCommands) {
        List<BashCommand> commands = new ArrayList<>();
        for (Map.Entry<String, Options> entry : commonsCliCommands.entrySet()) {
            String commandName = entry.getKey();
            org.apache.commons.cli.Options commonsCliOptions = entry.getValue();
            List<BashOption> options = adaptOptions(commonsCliOptions);
            commands.add(new BashCommand(commandName, options));
        }
        return commands;
    }
}
