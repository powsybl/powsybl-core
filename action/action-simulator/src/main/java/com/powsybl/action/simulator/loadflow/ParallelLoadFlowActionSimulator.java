/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.action.simulator.loadflow;

import com.powsybl.computation.*;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.xml.NetworkXml;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static com.powsybl.action.simulator.tools.ActionSimulatorToolConstants.*;
import static com.powsybl.tools.ToolConstants.TASK;

/**
 * Runs a load flow action simulator through calls to itools action-simulator command,
 * submitted to the computation manager.
 *
 * @author Yichen Tang <yichen.tang at rte-france.com>
 */
public class ParallelLoadFlowActionSimulator {

    private static final Logger LOGGER = LoggerFactory.getLogger(ParallelLoadFlowActionSimulator.class);

    private static final String ITOOLS_PRG = "itools";

    private final ComputationManager computationManager;
    private final Network network;
    private final LoadFlowActionSimulatorConfig config;
    private final boolean applyIfSolved;
    private final int taskCount;
    private final LoadFlowActionSimulatorResultBuilder resultBuilder;

    private static final String SUB_TASK_ID = "sas"; // sub_action_simulator

    public ParallelLoadFlowActionSimulator(Network network, ComputationManager cm, int taskCount) {
        this(network, cm, taskCount, LoadFlowActionSimulatorConfig.load(), false, new SecurityAnalysisResultBuilder());
    }

    public ParallelLoadFlowActionSimulator(Network network, ComputationManager cm, int taskCount, LoadFlowActionSimulatorConfig config) {
        this(network, cm, taskCount, config, false, new SecurityAnalysisResultBuilder());
    }

    public ParallelLoadFlowActionSimulator(Network network, ComputationManager cm, int taskCount, LoadFlowActionSimulatorConfig config,
                                           boolean applyIfSolved, LoadFlowActionSimulatorResultBuilder resultBuilder) {
        this.network = Objects.requireNonNull(network);
        this.computationManager = Objects.requireNonNull(cm);
        this.config = Objects.requireNonNull(config);
        this.applyIfSolved = applyIfSolved;
        this.taskCount = taskCount;
        this.resultBuilder = resultBuilder;
    }

    /**
     * Runs a load flow action simulator through calls to itools action-simulator command,
     * submitted to the computation manager.
     *
     * @param script the content of the groovy DSL script representing contingencies and actions.
     *
     */
    public void run(String script, List<String> contingencyIds) {
        LOGGER.debug("Starting parallel action simulator.");

        ExecutionEnvironment itoolsEnvironment = new ExecutionEnvironment(Collections.emptyMap(), "subTask_", config.isDebug());

        int actualTaskCount = Math.min(taskCount, contingencyIds.size());
        computationManager.execute(itoolsEnvironment, new SubTaskHandler(actualTaskCount, script, contingencyIds))
                .join();
    }

    /**
     * Execution handler for sub-tasks.
     * copies network and groovy file to working directory,
     * then launches one itools command for each subtask.
     * Finally, reads and merge results.
     */
    private final class SubTaskHandler extends AbstractExecutionHandler<Void> {


        private final int actualTaskCount;
        private final String script;
        private final List<String> contingencyIds;

        private SubTaskHandler(int actualTaskCount, String script, List<String> contingencyIds) {
            this.actualTaskCount = actualTaskCount;
            this.script = script;
            this.contingencyIds = contingencyIds;
        }

        @Override
        public List<CommandExecution> before(Path workingDir) throws IOException {
            SimpleCommand subItoolsCmd = buildCommand(workingDir)
                    .program(ITOOLS_PRG)
                    .id(SUB_TASK_ID)
                    .build();
            return Collections.singletonList(new CommandExecution(subItoolsCmd, actualTaskCount, 1));
        }

        /**
         * Reads result files and merge them.
         */
        @Override
        public Void after(Path workingDir, ExecutionReport report) {
            LOGGER.debug("End of command execution in {}. ", workingDir);

            ResultMerger merger = resultBuilder.createMerger();
            for (int taskNum = 0; taskNum < actualTaskCount; taskNum++) {
                Path taskResultFile = workingDir.resolve(getOutputFileName(taskNum));

                try (InputStream is = Files.newInputStream(taskResultFile)) {
                    merger.readResult(is);
                } catch (IOException exc) {
                    throw new UncheckedIOException(exc);
                }
            }
            merger.mergeResults();
            return null;
        }

        private String getOutputFileName(int taskNumber) {
            return "task_" + taskNumber + "_result.json";
        }

        private SimpleCommandBuilder buildCommand(Path workingDir) throws IOException {

            SimpleCommandBuilder builder = new SimpleCommandBuilder();

            // copy input files to slave workingDir
            Path networkDest = workingDir.resolve("network.xiidm");
            LOGGER.debug("Copying network to file {}", networkDest);
            NetworkXml.write(network, networkDest);

            Path dslFileDest = workingDir.resolve("strategy.groovy");
            LOGGER.debug("Copying strategy file to {}", dslFileDest);
            Files.write(dslFileDest, script.getBytes(StandardCharsets.UTF_8));

            builder
                .arg("action-simulator")
                .option(CASE_FILE, networkDest.toString())
                .option(DSL_FILE, dslFileDest.toString())
                .option(TASK, i -> new Partition(i + 1, actualTaskCount).toString())
                .option(OUTPUT_FILE, this::getOutputFileName)
                .option(OUTPUT_FORMAT, "JSON")
                .option(OUTPUT_BUILDER, resultBuilder.getName())
                .flag(APPLY_IF_SOLVED_VIOLATIONS, applyIfSolved);

            if (!contingencyIds.isEmpty()) {
                builder.option(CONTINGENCIES, String.join(",", contingencyIds));
            }

            return builder;
        }
    }
}
