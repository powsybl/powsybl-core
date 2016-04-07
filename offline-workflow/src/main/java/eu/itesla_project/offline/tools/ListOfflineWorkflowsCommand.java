/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.offline.tools;

import eu.itesla_project.commons.tools.Command;
import static eu.itesla_project.offline.tools.Themes.OFFLINE_APPLICATION_REMOTE_CONTROL;
import org.apache.commons.cli.Options;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ListOfflineWorkflowsCommand implements Command {

    public static final ListOfflineWorkflowsCommand INSTANCE = new ListOfflineWorkflowsCommand();

    @Override
    public String getName() {
        return "list-offline-workflows";
    }

    @Override
    public String getTheme() {
        return OFFLINE_APPLICATION_REMOTE_CONTROL;
    }
    
    @Override
    public String getDescription() {
        return "list offline workflows and their status (running or not)";
    }

    @Override
    @SuppressWarnings("static-access")
    public Options getOptions() {
        return new Options();
    }

    @Override
    public String getUsageFooter() {
        return null;
    }

}
