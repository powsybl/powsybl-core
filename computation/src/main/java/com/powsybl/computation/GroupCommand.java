/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.computation;

import java.util.List;

/**
 * Commands to be executed sequentially on a single computation node.
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface GroupCommand extends Command {

    interface SubCommand {

        String getProgram();

        List<String> getArgs(int executionNumber);

        int getTimeout();

        // only used for display
        String toString(int executionNumber);

    }

    List<SubCommand> getSubCommands();

}
