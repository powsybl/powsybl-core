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
import com.powsybl.iidm.xml.NetworkXml;
import com.powsybl.security.SecurityAnalysis;
import com.powsybl.security.SecurityAnalysisParameters;
import com.powsybl.security.SecurityAnalysisResult;
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

import static com.powsybl.tools.ToolConstants.TASK_COUNT;
import static java.util.Objects.requireNonNull;

/**
 * Submits execution of an "itools security-analysis" command to the ComputationManager,
 * for example to remotely execute the security-analysis.
 *
 * @author Sylvain Leclerc <sylvain.leclerc at rte-france.com>
 */
public class ExternalSecurityAnalysis implements SecurityAnalysis {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExternalSecurityAnalysis.class);

    protected final ExternalSecurityAnalysisConfig config;
    protected final Network network;
    protected final ComputationManager computationManager;
    protected final List<String> extensions;
    protected final Integer taskCount;

    public ExternalSecurityAnalysis(ExternalSecurityAnalysisConfig config, Network network,
                                    ComputationManager computationManager, List<String> extensions) {
        this(config, network, computationManager, extensions, null);
    }

    private ExternalSecurityAnalysis(ExternalSecurityAnalysisConfig config, Network network,
                                     ComputationManager computationManager, List<String> extensions, Integer taskCount) {
        this.config = requireNonNull(config);
        this.network = requireNonNull(network);
        this.computationManager = requireNonNull(computationManager);
        this.extensions = new ArrayList<>(requireNonNull(extensions));
        this.taskCount = taskCount;
    }

    public ExternalSecurityAnalysis(ExternalSecurityAnalysisConfig config, Network network,
                                    ComputationManager computationManager, List<String> extensions, int taskCount) {
        this(config, network, computationManager, extensions, Integer.valueOf(taskCount));
    }

    @Override
    public void addInterceptor(SecurityAnalysisInterceptor interceptor) {
        throw new UnsupportedOperationException("External security analysis does not support interceptors. Use extension names instead.");
    }

    @Override
    public boolean removeInterceptor(SecurityAnalysisInterceptor interceptor) {
        throw new UnsupportedOperationException("External security analysis does not support interceptors.");
    }

    @Override
    public CompletableFuture<SecurityAnalysisResult> run(String workingStateId, SecurityAnalysisParameters parameters, ContingenciesProvider contingenciesProvider) {
        LOGGER.debug("Starting external security analysis.");

        ExecutionEnvironment itoolsEnv = new ExecutionEnvironment(Collections.emptyMap(), "security_analysis_", config.isDebug());
        return computationManager.execute(itoolsEnv, new SecurityAnalysisExecutionHandler(workingStateId, parameters, contingenciesProvider));
    }

    /**
     * Execution handler for external process.
     * Copies network, contingencies and parameters files to working directory,
     * then launches one itools command.
     * Finally, reads results.
     */
    protected class SecurityAnalysisExecutionHandler extends AbstractExecutionHandler<SecurityAnalysisResult> {

        static final String NETWORK_FILE = "network.xiidm";
        static final String CONTINGENCIES_FILE = "contingencies.groovy";
        static final String PARAMETERS_FILE = "parameters.json";
        static final String OUTPUT_FILE = "result.json";

        private final String workingStateId;
        private final SecurityAnalysisParameters parameters;
        private final ContingenciesProvider contingenciesProvider;

        SecurityAnalysisExecutionHandler(String workingStateId, SecurityAnalysisParameters parameters, ContingenciesProvider contingenciesProvider) {
            this.workingStateId = workingStateId;
            this.parameters = parameters;
            this.contingenciesProvider = contingenciesProvider;
        }

        @Override
        public List<CommandExecution> before(Path workingDir) throws IOException {

            network.getStateManager().setWorkingState(workingStateId);
            copyInputFiles(workingDir);
            return buildCommandExecution();
        }

        /**
         * Reads result file.
         */
        @Override
        public SecurityAnalysisResult after(Path workingDir, ExecutionReport report) {
            LOGGER.debug("End of command execution in {}. ", workingDir);
            return readResults(workingDir);
        }

        private void copyInputFiles(Path workingDir) throws IOException {
            // copy input files to slave workingDir
            Path networkDest = workingDir.resolve(NETWORK_FILE);
            LOGGER.debug("Copying network to file {}", networkDest);
            NetworkXml.write(network, networkDest);

            Path dslFileDest = workingDir.resolve(CONTINGENCIES_FILE);
            LOGGER.debug("Writing contingencies to file {}", dslFileDest);
            Files.write(dslFileDest, contingenciesProvider.asScript().getBytes(StandardCharsets.UTF_8));

            Path parametersFileDest = workingDir.resolve(PARAMETERS_FILE);
            LOGGER.debug("Writing parameters to file {}", parametersFileDest);
            JsonSecurityAnalysisParameters.write(parameters, parametersFileDest);
        }

        /**
         * Handles case-file, contingencies-file, parameters-file, and extensions arguments.
         */
        protected SimpleCommandBuilder baseCommand(String id) {
            SimpleCommandBuilder cmdBuilder = new SimpleCommandBuilder()
                    .id(id)
                    .program(config.getItoolsCommand())
                    .arg("security-analysis")
                    .option("case-file", NETWORK_FILE)
                    .option("contingencies-file", CONTINGENCIES_FILE)
                    .option("parameters-file", PARAMETERS_FILE);
            if (!extensions.isEmpty()) {
                cmdBuilder.option("with-extensions", String.join(",", extensions));
            }
            return cmdBuilder;
        }

        protected List<CommandExecution> buildCommandExecution() {
            SimpleCommandBuilder builder = baseCommand("security-analysis")
                    .option("output-format", "JSON")
                    .option("output-file", OUTPUT_FILE);
            if (taskCount != null) {
                builder.option(TASK_COUNT, Integer.toString(taskCount));
            }
            SimpleCommand cmd = builder.build();
            return Collections.singletonList(new CommandExecution(cmd, 1, 1));
        }

        /**
         * Read results from the working dir: may be overriden by child implementations.
         */
        protected SecurityAnalysisResult readResults(Path workingDir) {
            Path taskResultFile = workingDir.resolve(OUTPUT_FILE);
            return SecurityAnalysisResultDeserializer.read(taskResultFile);
        }
    }
}
