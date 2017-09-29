/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.tools;

import org.apache.commons.cli.Options;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface Command {

    /**
     * Get the name of the command.
     *
     * @return the name of the command
     */
    String getName();

    /**
     * Get the command theme used to group in the help usage commands with the
     * same theme.
     *
     * @return the command theme
     */
    String getTheme();

    /**
     * Get a description of the command.
     *
     * @return a description of the command
     */
    String getDescription();

    /**
     * Get the command options.
     *
     * @return the command options
     */
    Options getOptions();

    /**
     * Get a foot text that will be displayed in the command usage.
     *
     * @return a usage foot text
     */
    String getUsageFooter();

    /**
     * Check if the command must be visible in the help.
     *
     * @return true if the command has to be hidden, false otherwise
     */
    default boolean isHidden() {
        return false;
    }

}
