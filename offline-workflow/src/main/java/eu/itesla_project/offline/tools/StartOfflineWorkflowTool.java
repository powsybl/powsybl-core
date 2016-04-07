/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.offline.tools;

import eu.itesla_project.commons.tools.Tool;
import eu.itesla_project.commons.tools.Command;
import com.google.auto.service.AutoService;
import eu.itesla_project.offline.OfflineWorkflowStartParameters;
import eu.itesla_project.offline.OfflineApplication;
import eu.itesla_project.offline.RemoteOfflineApplicationImpl;
import org.apache.commons.cli.CommandLine;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@AutoService(Tool.class)
public class StartOfflineWorkflowTool implements Tool {

    private OfflineWorkflowStartParameters defaultParameters;

    @Override
    public Command getCommand() {
        return StartOfflineWorkflowCommand.INSTANCE;
    }

    private OfflineWorkflowStartParameters getDefaultParameters() {
        if (defaultParameters == null) {
            defaultParameters = OfflineWorkflowStartParameters.load();
        }
        return defaultParameters;
    }

    @Override
    public void run(CommandLine line) throws Exception {
        String workflowId = line.getOptionValue("workflow");
        int duration = line.hasOption("duration")
                ? Integer.parseInt(line.getOptionValue("duration"))
                : getDefaultParameters().getDuration();
        int stateQueueSize = line.hasOption("state-queue-size")
                ? Integer.parseInt(line.getOptionValue("state-queue-size"))
                : getDefaultParameters().getStateQueueSize();
        int sampleQueueSize = line.hasOption("sample-queue-size")
                ? Integer.parseInt(line.getOptionValue("sample-queue-size"))
                : getDefaultParameters().getSampleQueueSize();
        int samplingThreads = line.hasOption("sampling-threads")
                ? Integer.parseInt(line.getOptionValue("sampling-threads"))
                : getDefaultParameters().getSamplingThreads();
        int samplesPerThread = line.hasOption("samples-per-thread")
                ? Integer.parseInt(line.getOptionValue("samples-per-thread"))
                : getDefaultParameters().getSamplesPerThread();
        int maxProcessedSamples = line.hasOption("max-processed-samples")
                ? Integer.parseInt(line.getOptionValue("max-processed-samples"))
                : getDefaultParameters().getMaxProcessedSamples();
        OfflineWorkflowStartParameters parameters = new OfflineWorkflowStartParameters(sampleQueueSize,
                                                                                       samplingThreads,
                                                                                       samplesPerThread,
                                                                                       stateQueueSize,
                                                                                       duration,
                                                                                       maxProcessedSamples);
        try (OfflineApplication app = new RemoteOfflineApplicationImpl()) {
            app.startWorkflow(workflowId, parameters);
        }
    }

}
