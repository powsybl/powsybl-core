/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.offline.tools;

import com.google.auto.service.AutoService;
import eu.itesla_project.commons.tools.Command;
import eu.itesla_project.commons.tools.Tool;
import eu.itesla_project.offline.OfflineApplication;
import eu.itesla_project.offline.RemoteOfflineApplicationImpl;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@AutoService(Tool.class)
public class StopOfflineApplicationTool implements Tool {

    @Override
    public Command getCommand() {
        return new Command() {
            @Override
            public String getName() {
                return "stop-offline-application";
            }

            @Override
            public String getTheme() {
                return Themes.OFFLINE_APPLICATION_REMOTE_CONTROL;
            }

            @Override
            public String getDescription() {
                return "stop offline application";
            }

            @Override
            public boolean isHidden() {
                return true;
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
        };
    }

    @Override
    public void run(CommandLine line) throws Exception {
        try (OfflineApplication app = new RemoteOfflineApplicationImpl()) {
            app.stopApplication();
        }
    }

}
