/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.action.simulator.parallel;

import com.powsybl.action.dsl.ActionDb;
import com.powsybl.action.simulator.loadflow.LoadFlowActionSimulator;
import com.powsybl.action.simulator.loadflow.LoadFlowActionSimulatorConfig;
import com.powsybl.action.simulator.loadflow.LoadFlowActionSimulatorObserver;
import com.powsybl.action.simulator.tools.ActionSimulatorToolConstants;
import com.powsybl.commons.PowsyblException;
import com.powsybl.computation.*;
import com.powsybl.iidm.network.Network;
import com.powsybl.security.SecurityAnalysisResult;
import com.powsybl.security.SecurityAnalysisResultMerger;
import com.powsybl.security.converter.SecurityAnalysisResultExporters;
import com.powsybl.security.json.SecurityAnalysisResultDeserializer;
import com.powsybl.tools.ToolRunningContext;
import org.apache.commons.cli.CommandLine;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class ParallelLoadFlowActionSimulator extends LoadFlowActionSimulator implements ActionSimulatorToolConstants {

    private static final String ITOOLS_PRG = "itools";

    private final int para;
    private final CommandLine commandLine;

    private static final String SUB_TASK_ID = "sas"; // sub_action_simulator

    public ParallelLoadFlowActionSimulator(Network network, ToolRunningContext context, int para, CommandLine commandLine) {
        this(network, context, para, commandLine, LoadFlowActionSimulatorConfig.load(), false, Collections.emptyList());
    }

    public ParallelLoadFlowActionSimulator(Network network, ToolRunningContext context, int para, CommandLine commandLine,
                                           LoadFlowActionSimulatorConfig config, boolean applyIfSolved, LoadFlowActionSimulatorObserver... observers) {
        this(network, context, para, commandLine, config, applyIfSolved, Arrays.asList(observers));
    }

    public ParallelLoadFlowActionSimulator(Network network, ToolRunningContext context, int para, CommandLine commandLine,
                                           LoadFlowActionSimulatorConfig config, boolean applyIfSolved, List<LoadFlowActionSimulatorObserver> observers) {
        super(network, context.getShortTimeExecutionComputationManager(), config, applyIfSolved, observers);
        this.para = para;
        this.commandLine = Objects.requireNonNull(commandLine);
    }

    @Override
    public String getName() {
        return "parallel loadflow actionsimulator";
    }

    @Override
    public void start(ActionDb actionDb, List<String> contingencyIds) {
        ComputationManager manager = super.getComputationManager();
        ExecutionEnvironment itoolsEnvironment = new ExecutionEnvironment(Collections.emptyMap(), "subTask_", true);
        int realPara = contingencyIds.size() > this.para ? this.para : contingencyIds.size();
        List<CompletableFuture<SecurityAnalysisResult>> results = new ArrayList<>();
        for (int i = 1; i <= realPara; i++) {
            CompletableFuture<SecurityAnalysisResult> future = manager.execute(itoolsEnvironment,
                    new SubTaskHandler(i, realPara));
            results.add(future);
        }
        CompletableFuture.allOf(results.stream().toArray(CompletableFuture[]::new)).join();
        // merge results and re-print to output-file
        SecurityAnalysisResult[] securityAnalysisResults = new SecurityAnalysisResult[results.size()];
        try {
            for (int i = 0; i < results.size(); i++) {
                securityAnalysisResults[i] = results.get(i).get();
            }
        } catch (Exception e) {
            throw new PowsyblException(e);
        }

        SecurityAnalysisResult securityAnalysisResult = SecurityAnalysisResultMerger.merge(securityAnalysisResults);
        SecurityAnalysisResultExporters.export(securityAnalysisResult, Paths.get(commandLine.getOptionValue(OUTPUT_FILE)), commandLine.getOptionValue(OUTPUT_FORMAT));
    }

    @Override
    public void start(ActionDb actionDb, String... contingencyIds) {
        start(actionDb, Arrays.asList(contingencyIds));
    }

    private class SubTaskHandler extends AbstractExecutionHandler<SecurityAnalysisResult> {

        private final Integer index;
        private final Integer total;

        SubTaskHandler(Integer x, Integer y) {
            index = Objects.requireNonNull(x);
            total = Objects.requireNonNull(y);
        }

        @Override
        public List<CommandExecution> before(Path workingDir) throws IOException {
            List<String> args = rebuildSubProgramArgs(workingDir);
            SimpleCommand subItoolsCmd = new SimpleCommandBuilder()
                    .program(ITOOLS_PRG)
                    .id(SUB_TASK_ID)
                    .args(args)
                    .build();
            return Collections.singletonList(new CommandExecution(subItoolsCmd, 1, 1));
        }

        @Override
        public SecurityAnalysisResult after(Path workingDir, ExecutionReport report) throws IOException {
            Path fileName = Paths.get(commandLine.getOptionValue(OUTPUT_FILE)).getFileName();
            Path outputFile = workingDir.resolve(fileName);
            return SecurityAnalysisResultDeserializer.read(outputFile);
        }

        private void addRequiredArgs(List<String> list, String optName) {
            if (commandLine.hasOption(optName)) {
                String optValue = toOption(optName, commandLine.getOptionValue(optName));
                list.add(optValue);
            } else {
                throw new IllegalArgumentException("Missing option[" + optName + "]");
            }
        }

        private void addArgs(List<String> list, String optName) {
            if (commandLine.hasOption(optName)) {
                String optValue = toOption(optName, commandLine.getOptionValue(optName));
                list.add(optValue);
            }
        }

        private List<String> rebuildSubProgramArgs(Path workingDir) throws IOException {
            List<String> args = new ArrayList<>(); // subtask command args
            args.add("action-simulator");
            // copy input files to slave workingDir
            Path caseFile = copyFromOptionValueToWorkingDir(CASE_FILE, workingDir);
            String caseFileOpt = toOption(CASE_FILE, caseFile.toAbsolutePath().toString());
            args.add(caseFileOpt);
            Path dslFile = copyFromOptionValueToWorkingDir(DSL_FILE, workingDir);
            String dslFileOpt = toOption(DSL_FILE, dslFile.toAbsolutePath().toString());
            args.add(dslFileOpt);


            String filtrationOpt = toOption(SUB_CONTINGENCIES, index + "/" + total);
            args.add(filtrationOpt);

            if (commandLine.hasOption(OUTPUT_FILE)) {
                Path fileName = Paths.get(commandLine.getOptionValue(OUTPUT_FILE)).getFileName();
                String tmpFolderOpt = toOption(OUTPUT_FILE, workingDir.resolve(fileName).toAbsolutePath().toString());
                args.add(tmpFolderOpt);
            }

            addRequiredArgs(args, OUTPUT_FORMAT);

            addArgs(args, APPLY_IF_SOLVED_VIOLATIONS);

            return args;
        }

        private Path copyFromOptionValueToWorkingDir(String opt, Path workingDir) throws IOException {
            Path source = Paths.get(commandLine.getOptionValue(opt));
            String name = source.getFileName().toString();
            Path dest = workingDir.resolve(name);
            return Files.copy(source, dest);
        }

        private String toOption(String opt, String value) {
            StringBuilder sb = new StringBuilder().append("--").append(opt).append("=").append(value);
            return sb.toString();
        }
    }
}
