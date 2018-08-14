/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.action.simulator.loadflow;

import com.powsybl.action.dsl.ActionDb;
import com.powsybl.commons.PowsyblException;
import com.powsybl.computation.*;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.xml.NetworkXml;
import com.powsybl.security.SecurityAnalysisResult;
import com.powsybl.security.SecurityAnalysisResultMerger;
import com.powsybl.security.json.SecurityAnalysisResultDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.powsybl.action.simulator.tools.ActionSimulatorToolConstants.*;

/**
 * @author Yichen Tang <yichen.tang at rte-france.com>
 */
public class ParallelLoadFlowActionSimulator extends LoadFlowActionSimulator {

    private static final Logger LOGGER = LoggerFactory.getLogger(ParallelLoadFlowActionSimulator.class);

    private static final String ITOOLS_PRG = "itools";

    private final int taskCount;
    private final Path dslFile;
    private final List<Consumer<SecurityAnalysisResult>> resultHandlers;

    private static final String SUB_TASK_ID = "sas"; // sub_action_simulator

    public ParallelLoadFlowActionSimulator(Network network, Path dslFile, ComputationManager cm, int taskCount) {
        this(network, dslFile, cm, taskCount, LoadFlowActionSimulatorConfig.load(), false, Collections.emptyList());
    }

    public ParallelLoadFlowActionSimulator(Network network, Path dslFile, ComputationManager cm, int taskCount, LoadFlowActionSimulatorConfig config,
                                           boolean applyIfSolved, Consumer<SecurityAnalysisResult>... resultHandlers) {
        this(network, dslFile, cm, taskCount, config, applyIfSolved, Arrays.asList(resultHandlers));
    }

    public ParallelLoadFlowActionSimulator(Network network, Path dslFile, ComputationManager cm, int taskCount, LoadFlowActionSimulatorConfig config,
                                           boolean applyIfSolved, List<Consumer<SecurityAnalysisResult>> resultHandlers) {
        super(network, cm, config, applyIfSolved);
        this.dslFile = Objects.requireNonNull(dslFile);
        this.taskCount = taskCount;
        this.resultHandlers = resultHandlers;
    }

    @Override
    public String getName() {
        return "parallel loadflow action-simulator";
    }

    @Override
    public void start(ActionDb actionDb, List<String> contingencyIds) {

        LOGGER.debug("Starting parallel action simulator.");

        ComputationManager manager = getComputationManager();
        ExecutionEnvironment itoolsEnvironment = new ExecutionEnvironment(Collections.emptyMap(), "subTask_", getConfig().isDebug());

        int actualTaskCount = Math.min(taskCount, contingencyIds.size());
        CompletableFuture<SecurityAnalysisResult> future = manager.execute(itoolsEnvironment, new SubTaskHandler(actualTaskCount));
        try {
            SecurityAnalysisResult result = future.get();
            resultHandlers.forEach(h -> h.accept(result));
        } catch (Exception e) {
            throw new PowsyblException(e);
        }
    }

    @Override
    public void start(ActionDb actionDb, String... contingencyIds) {
        start(actionDb, Arrays.asList(contingencyIds));
    }

    /**
     * Execution handler for sub-tasks.
     * copies network and groovy file to working directory,
     * then launches one itools command for each subtask.
     * Finally, reads and merge results.
     */
    private class SubTaskHandler extends AbstractExecutionHandler<SecurityAnalysisResult> {

        private final int actualTaskCount;

        SubTaskHandler(int actualTaskCount) {
            this.actualTaskCount = actualTaskCount;
        }

        @Override
        public List<CommandExecution> before(Path workingDir) throws IOException {
            SimpleCommand subItoolsCmd = buildCommand(workingDir)
                    .program(ITOOLS_PRG)
                    .id(SUB_TASK_ID)
                    .build();
            LOGGER.debug("Submitting command: {}", subItoolsCmd.toString(1));
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

        private SimpleCommandBuilder buildCommand(Path workingDir) throws IOException {

            OptionCommandBuilder builder = new OptionCommandBuilder();

            // copy input files to slave workingDir
            Path networkDest = workingDir.resolve("network.xiidm");
            LOGGER.debug("Copying network to file {}", networkDest);
            NetworkXml.write(getNetwork(), networkDest);

            Path dslFileDest = workingDir.resolve("action-script.groovy");
            LOGGER.debug("Copying action file from {} to {}", dslFile, dslFileDest);
            Files.copy(dslFile, dslFileDest);

            builder
                .arg("action-simulator")
                .addOption(CASE_FILE, networkDest.toString())
                .addOption(DSL_FILE, dslFileDest.toString())
                .addOption(TASK, i -> new Partition(i + 1, actualTaskCount).toString())
                .addOption(OUTPUT_FILE, i -> getOutputFileName(i))
                .addOption(OUTPUT_FORMAT, "JSON")
                .addFlag(APPLY_IF_SOLVED_VIOLATIONS, isApplyIfSolvedViolations());

            return builder;
        }
    }

    /**
     * Simple command builder with additional methods to easily add options as "--opt=value"
     */
    protected static class OptionCommandBuilder extends SimpleCommandBuilder {

        /**
         * Adds a literal option.
         */
        public OptionCommandBuilder addFlag(String flagName, boolean flagValue) {
            if (flagValue) {
                arg("--" + flagName);
            }
            return this;
        }

        /**
         * Adds a literal option.
         */
        public OptionCommandBuilder addOption(String opt, String value) {
            arg("--" + opt + "=" + value);
            return this;
        }

        /**
         * Adds an option dependent on the execution count
         */
        public OptionCommandBuilder addOption(String opt, Function<Integer, String> fn) {
            arg(i -> "--" + opt + "=" + fn.apply(i));
            return this;
        }

        @Override
        public OptionCommandBuilder arg(String arg) {
            super.arg(arg);
            return this;
        }

        @Override
        public SimpleCommandBuilder arg(Function<Integer, String> arg) {
            super.arg(arg);
            return this;
        }
    }

    private static String getOutputFileName(int taskNumber) {
        return "task_" + taskNumber + "_result.json";
    }
}
