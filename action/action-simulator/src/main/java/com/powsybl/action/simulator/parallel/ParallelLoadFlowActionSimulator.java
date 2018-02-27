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
import com.powsybl.commons.io.FileUtil;
import com.powsybl.computation.*;
import com.powsybl.iidm.network.Network;
import com.powsybl.loadflow.LoadFlowResult;
import com.powsybl.tools.ToolRunningContext;
import org.apache.commons.cli.CommandLine;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Semaphore;

import static java.nio.charset.StandardCharsets.UTF_8;

public class ParallelLoadFlowActionSimulator extends LoadFlowActionSimulator implements ActionSimulatorToolConstants {

    private static final String ITOOLS_PRG = "itools";

    private final int initPara;
    private final CommandLine commandLine;
    private final ToolRunningContext context;

    private final Semaphore outputLock = new Semaphore(1);
    private static final String SUB_TASK_ID = "test";

    public ParallelLoadFlowActionSimulator(Network network, ToolRunningContext context, Integer initPara, CommandLine commandLine) {
        this(network, context, initPara, commandLine, LoadFlowActionSimulatorConfig.load(), Collections.emptyList());
    }

    public ParallelLoadFlowActionSimulator(Network network, ToolRunningContext context, Integer initPara, CommandLine commandLine,
                                           LoadFlowActionSimulatorConfig config, LoadFlowActionSimulatorObserver... observers) {
        this(network, context, initPara, commandLine, config, Arrays.asList(observers));
    }

    public ParallelLoadFlowActionSimulator(Network network, ToolRunningContext context, Integer initPara, CommandLine commandLine,
                                           LoadFlowActionSimulatorConfig config, List<LoadFlowActionSimulatorObserver> observers) {
        super(network, context.getComputationManager(), config, observers);
        this.initPara = Objects.requireNonNull(initPara);
        this.commandLine = Objects.requireNonNull(commandLine);
        this.context = Objects.requireNonNull(context);
    }

    @Override
    public String getName() {
        return "parallel loadflow actionsimulator";
    }

    @Override
    public void start(ActionDb actionDb, List<String> contingencyIds) {
        ComputationManager manager = super.getComputationManager();
        // TODO get debug from???
        boolean debug = false;
        ExecutionEnvironment itoolsEnvironment = new ExecutionEnvironment(Collections.emptyMap(), "subTask_", debug);
        int correctedPara = contingencyIds.size() > this.initPara ? this.initPara : contingencyIds.size();
        List<CompletableFuture<LoadFlowResult>> results = new ArrayList<>();
        for (int i = 1; i <= correctedPara; i++) {
            CompletableFuture<LoadFlowResult> future = manager.execute(itoolsEnvironment,
                    new SubTaskHandler(i, correctedPara));
            results.add(future);
        }
        CompletableFuture.allOf(results.stream().toArray(CompletableFuture[]::new)).join();
    }

    @Override
    public void start(ActionDb actionDb, String... contingencyIds) {
        start(actionDb, Arrays.asList(contingencyIds));
    }

    private class SubTaskHandler extends AbstractExecutionHandler<LoadFlowResult> {

        private final Integer index;
        private final Integer total;

        private final String subDirPostfix;

        SubTaskHandler(Integer x, Integer y) {
            index = Objects.requireNonNull(x);
            total = Objects.requireNonNull(y);
            subDirPostfix = index + "_" + total;
        }

        @Override
        public List<CommandExecution> before(Path workingDir) throws IOException {
            List<String> args = rebuildSubProgramArgs(workingDir);
            SimpleCommand command = new SimpleCommandBuilder()
                    .program(ITOOLS_PRG)
                    .id(SUB_TASK_ID)
                    .args(args)
                    .build();
            return Collections.singletonList(new CommandExecution(command, 1, 1));
        }

        @Override
        public LoadFlowResult after(Path workingDir, ExecutionReport report) throws IOException {
            // re-print sub process output
            outputLock.acquireUninterruptibly();
            try (PrintStream out = context.getOutputStream();
                PrintStream err = context.getErrorStream()) {
                Files.lines(workingDir.resolve(SUB_TASK_ID + "_0.out"), UTF_8).forEach(out::println);
                Files.lines(workingDir.resolve(SUB_TASK_ID + "_0.err"), UTF_8).forEach(err::println);
            } finally {
                outputLock.release();
            }

            LoadFlowResult result = super.after(workingDir, report);
            // copy from slave output folders to user's output
            if (commandLine.hasOption(OUTPUT_CASE_FOLDER)) {
                Path dest = Paths.get(commandLine.getOptionValue(OUTPUT_CASE_FOLDER), subDirPostfix);
                Path source = workingDir.resolve(subDirPostfix);
                FileUtil.copyDir(source, dest);
            }
            // copy output file to X_Y/ folder under user's output
            if (commandLine.hasOption(OUTPUT_CSV)) {
                Path path = Paths.get(commandLine.getOptionValue(OUTPUT_CSV));
                Path dest = path.getParent().resolve(subDirPostfix).resolve(path.getFileName());
                Path parent = dest.getParent();
                Files.createDirectories(parent);
                Path source = workingDir.resolve(path.getFileName());
                Files.copy(source, dest);
            }
            return result;
        }

        private void addArgsIfExistsInOriginal(List<String> list, String optName) {
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

            // TODO iterator on new Tool.getCommand
            // type0: no value
            if (commandLine.hasOption(VERBOSE)) {
                args.add("--" + VERBOSE);
            }
            // type1: args value can be same copied
            addArgsIfExistsInOriginal(args, OUTPUT_CASE_FORMAT);
            addArgsIfExistsInOriginal(args, OUTPUT_COMPRESSION_FORMAT);

            // type2: is a folder
            // temp save in workingDir/X_Y and copy to original output folder in after()
            if (commandLine.hasOption(OUTPUT_CASE_FOLDER)) {
                String tmpFolderOpt = toOption(OUTPUT_CASE_FOLDER, workingDir.resolve(subDirPostfix).toAbsolutePath().toString());
                args.add(tmpFolderOpt);
            }

            // type3: is a file
            // temp save in workingDir and copy to original output file in after()
            if (commandLine.hasOption(OUTPUT_CSV)) {
                String optValue = commandLine.getOptionValue(OUTPUT_CSV);
                Path fileName = Paths.get(optValue).getFileName();
                String tmpFileOpt = toOption(OUTPUT_CSV, workingDir.resolve(fileName).toAbsolutePath().toString());
                args.add(tmpFileOpt);
            }

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
