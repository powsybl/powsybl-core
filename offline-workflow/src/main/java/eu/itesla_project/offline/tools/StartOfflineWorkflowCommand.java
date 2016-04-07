/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.offline.tools;

import eu.itesla_project.commons.tools.Command;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class StartOfflineWorkflowCommand implements Command {

    public static final StartOfflineWorkflowCommand INSTANCE = new StartOfflineWorkflowCommand();

    @Override
    public String getName() {
        return "start-offline-workflow";
    }

    @Override
    public String getTheme() {
        return Themes.OFFLINE_APPLICATION_REMOTE_CONTROL;
    }

    @Override
    public String getDescription() {
        return "start an offline workflow";
    }

    @Override
    @SuppressWarnings("static-access")
    public Options getOptions() {
        Options options = new Options();
        options.addOption(Option.builder().longOpt("workflow")
                        .desc("the workflow id")
                        .hasArg()
                        .required()
                        .argName("ID")
                        .build());
        options.addOption(Option.builder().longOpt("duration")
                        .desc("run duration in minutes")
                        .hasArg()
                        .argName("MINUTES")
                        .build());
        options.addOption(Option.builder().longOpt("state-queue-size")
                        .desc("state queue size")
                        .hasArg()
                        .argName("SIZE")
                        .build());
        options.addOption(Option.builder().longOpt("sample-queue-size")
                        .desc("sample queue size")
                        .hasArg()
                        .argName("SIZE")
                        .build());
        options.addOption(Option.builder().longOpt("sampling-threads")
                        .desc("sampling threads")
                        .hasArg()
                        .argName("THREADS")
                        .build());
        options.addOption(Option.builder().longOpt("samples-per-thread")
                        .desc("samples per thread")
                        .hasArg()
                        .argName("SAMPLES")
                        .build());
        options.addOption(Option.builder().longOpt("max-processed-samples")
                        .desc("maximum number of samples to process")
                        .hasArg()
                        .argName("MAX_SAMPLES")
                        .build());
        return options;
    }

    @Override
    public String getUsageFooter() {
        return null;
    }

}
