/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.commons.tools;

import java.io.IOException;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class Main {

    public static void main(String[] args) throws IOException {
        int status = new CommandLineTools().run(args, new ToolRunningContext());
        if (status != CommandLineTools.COMMAND_OK_STATUS) {
            System.exit(status);
        }
    }
}
