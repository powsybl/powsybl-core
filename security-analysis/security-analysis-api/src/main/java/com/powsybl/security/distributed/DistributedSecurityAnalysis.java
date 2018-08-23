/**
 * Copyright (c) 2018, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.distributed;

import com.powsybl.computation.*;
import com.powsybl.contingency.ContingenciesProvider;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.xml.NetworkXml;
import com.powsybl.security.SecurityAnalysis;
import com.powsybl.security.SecurityAnalysisParameters;
import com.powsybl.security.SecurityAnalysisResult;
import com.powsybl.security.SecurityAnalysisResultMerger;
import com.powsybl.security.interceptors.SecurityAnalysisInterceptor;
import com.powsybl.security.json.JsonSecurityAnalysisParameters;
import com.powsybl.security.json.SecurityAnalysisResultDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 *
 * Security analysis implementation which distributes the work through X
 * executions of the "itools security-analysis" command.
 *
 * @author Sylvain Leclerc <sylvain.leclerc@rte-france.com>
 */
public class DistributedSecurityAnalysis implements SecurityAnalysis {

    private static final Logger LOGGER = LoggerFactory.getLogger(DistributedSecurityAnalysis.class);

    private final Network network;
    private final ComputationManager computationManager;

    public DistributedSecurityAnalysis(Network network, ComputationManager computationManager, List<String> extensions, int taskCount) {
        this.network = network;
        this.computationManager = computationManager;
        this.extensions = extensions;
        this.taskCount = taskCount;
    }

    private final List<String> extensions;
    private final int taskCount;


    @Override
    public void addInterceptor(SecurityAnalysisInterceptor interceptor) {
        throw new UnsupportedOperationException("Distributed security analysis does not support interceptors. Use extension names instead.");
    }

    @Override
    public boolean removeInterceptor(SecurityAnalysisInterceptor interceptor) {
        throw new UnsupportedOperationException("Distributed security analysis does not support interceptors.");
    }

    @Override
    public CompletableFuture<SecurityAnalysisResult> run(String workingStateId, SecurityAnalysisParameters parameters, ContingenciesProvider contingenciesProvider) {
        LOGGER.debug("Starting distributed security analysis.");

        ExecutionEnvironment itoolsEnv = new ExecutionEnvironment(Collections.emptyMap(), "itesla_sa_task_", true);

        int actualTaskCount = Math.min(taskCount, contingenciesProvider.getContingencies(network).size());
        return computationManager.execute(itoolsEnv, new SubTaskHandler(workingStateId, parameters, contingenciesProvider, actualTaskCount));
    }

    /**
     * Execution handler for sub-tasks.
     * copies network, contingencies and parameters files to working directory,
     * then launches one itools command for each subtask.
     * Finally, reads and merge results.
     */
    private class SubTaskHandler extends AbstractExecutionHandler<SecurityAnalysisResult> {

        private final String workingStateId;
        private final SecurityAnalysisParameters parameters;
        private final ContingenciesProvider contingenciesProvider;
        private final int actualTaskCount;

        public SubTaskHandler(String workingStateId, SecurityAnalysisParameters parameters, ContingenciesProvider contingenciesProvider, int actualTaskCount) {
            this.workingStateId = workingStateId;
            this.parameters = parameters;
            this.contingenciesProvider = contingenciesProvider;
            this.actualTaskCount = actualTaskCount;
        }


        @Override
        public List<CommandExecution> before(Path workingDir) throws IOException {

            network.getStateManager().setWorkingState(workingStateId);

            // copy input files to slave workingDir
            Path networkDest = workingDir.resolve("network.xiidm");
            LOGGER.debug("Copying network to file {}", networkDest);
            NetworkXml.write(network, networkDest);

            Path dslFileDest = workingDir.resolve("contingencies.groovy");
            LOGGER.debug("Writing contingencies to file {}", dslFileDest);
            Files.write(dslFileDest, contingenciesProvider.asScript().getBytes(StandardCharsets.UTF_8));

            Path parametersFileDest = workingDir.resolve("parameters.json");
            LOGGER.debug("Writing parameters to file {}", parametersFileDest);
            JsonSecurityAnalysisParameters.write(parameters, parametersFileDest);

            SimpleCommand subItoolsCmd = new SimpleCommandBuilder()
                    .id("security-analysis-task")
                    .program("itools")
                    .arg("security-analysis")
                    .option("case-file", networkDest.toString())
                    .option("contingencies-file", dslFileDest.toString())
                    .option("parameters-file", parametersFileDest.toString())
                    .option("task", i -> new Partition(i + 1, actualTaskCount).toString())
                    .option("output-file", this::getOutputFileName)
                    .option("output-format", "JSON")
                    .option("with-extensions", extensions.stream().collect(Collectors.joining(",")))
                    .build();

            return Collections.singletonList(new CommandExecution(subItoolsCmd, actualTaskCount, 1));
        }

        /**
         * Reads result files and merge them.
         */
        @Override
        public SecurityAnalysisResult after(Path workingDir, ExecutionReport report) {
            LOGGER.debug("End of command execution in {}. ", workingDir);
            List<SecurityAnalysisResult> results = new ArrayList<>();
            for (int taskNum = 0; taskNum < actualTaskCount; taskNum++) {
                Path taskResultFile = workingDir.resolve(getOutputFileName(taskNum));
                results.add(SecurityAnalysisResultDeserializer.read(taskResultFile));
            }
            return SecurityAnalysisResultMerger.merge(results);
        }

        private String getOutputFileName(int taskNumber) {
            return "task_" + taskNumber + "_result.json";
        }
    }
}
