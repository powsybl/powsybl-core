/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.tools;

import org.apache.commons.cli.CommandLine;

import java.util.Objects;

/**
 * @author Mathieu Bague <mathieu.bague@rte-france.com>
 */
public final class CommandLineUtil {

    private CommandLineUtil() {
    }

    public static <T extends Enum<T>> T getOptionValue(CommandLine line, String option, Class<T> clazz, T defaultValue) {
        Objects.requireNonNull(line);
        Objects.requireNonNull(option);
        Objects.requireNonNull(clazz);

        if (line.hasOption(option)) {
            return Enum.valueOf(clazz, line.getOptionValue(option));
        } else {
            return defaultValue;
        }
    }

}
