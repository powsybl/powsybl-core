/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.distributed;

import com.powsybl.computation.*;
import com.powsybl.contingency.ContingenciesProvider;
import com.powsybl.iidm.network.Network;
import com.powsybl.security.SecurityAnalysisParameters;
import com.powsybl.security.SecurityAnalysisResult;
import com.powsybl.security.SecurityAnalysisResultMerger;
import com.powsybl.security.json.SecurityAnalysisResultDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.powsybl.tools.ToolConstants.TASK;

/**
 * Security analysis implementation which distributes the work through X
 * executions of the "itools security-analysis" command.
 *
 * @author Sylvain Leclerc <sylvain.leclerc at rte-france.com>
 */
public class DistributedSecurityAnalysis extends ExternalSecurityAnalysis {

    private static final Logger LOGGER = LoggerFactory.getLogger(DistributedSecurityAnalysis.class);

    public DistributedSecurityAnalysis(ExternalSecurityAnalysisConfig config, Network network,
                                       ComputationManager computationManager, List<String> extensions, int taskCount) {
        super(config, network, computationManager, extensions, taskCount);
    }

    @Override
    public CompletableFuture<SecurityAnalysisResult> run(String workingStateId, SecurityAnalysisParameters parameters, ContingenciesProvider contingenciesProvider) {
        LOGGER.debug("Starting distributed security analysis.");

        ExecutionEnvironment itoolsEnv = new ExecutionEnvironment(Collections.emptyMap(), "security_analysis_task_", config.isDebug());

        int actualTaskCount = Math.min(taskCount, contingenciesProvider.getContingencies(network).size());
        return computationManager.execute(itoolsEnv, new SubTaskHandler(workingStateId, parameters, contingenciesProvider, actualTaskCount));
    }

    /**
     * Execution handler for sub-tasks.
     * Extends the base handler to launch one itools command for each subtask.
     * Finally, reads and merge results.
     */
    private class SubTaskHandler extends SecurityAnalysisExecutionHandler {

        private final int actualTaskCount;

        SubTaskHandler(String workingStateId, SecurityAnalysisParameters parameters, ContingenciesProvider contingenciesProvider, int actualTaskCount) {
            super(workingStateId, parameters, contingenciesProvider);
            this.actualTaskCount = actualTaskCount;
        }

        private String getOutputFileName(int taskNumber) {
            return "task_" + taskNumber + "_result.json";
        }

        /**
         * Merges the subtasks result files.
         */
        @Override
        protected SecurityAnalysisResult readResults(Path workingDir) {
            List<SecurityAnalysisResult> results = new ArrayList<>(actualTaskCount);
            for (int taskNum = 0; taskNum < actualTaskCount; taskNum++) {
                Path taskResultFile = workingDir.resolve(getOutputFileName(taskNum));
                results.add(SecurityAnalysisResultDeserializer.read(taskResultFile));
            }
            return SecurityAnalysisResultMerger.merge(results);
        }

        /**
         *  Command execution which requests one command execution for each subset of contingencies.
         *  The input files are the same for all commands, but the "task" parameters and the
         *  output files are different.
         */
        @Override
        protected List<CommandExecution> buildCommandExecution() {
            SimpleCommand cmd = baseCommand("security-analysis-task")
                    .option(TASK, i -> new Partition(i + 1, actualTaskCount).toString())
                    .option("output-file", this::getOutputFileName)
                    .option("output-format", "JSON")
                    .build();
            return Collections.singletonList(new CommandExecution(cmd, actualTaskCount, 1));
        }
    }
}
