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
import com.powsybl.commons.io.FileUtil;
import com.powsybl.commons.io.WorkingDirectory;
import com.powsybl.computation.*;
import com.powsybl.iidm.network.Network;
import com.powsybl.loadflow.LoadFlowResult;
import com.powsybl.tools.ToolRunningContext;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Semaphore;
import java.util.function.BiFunction;
import java.util.stream.Stream;

import static java.nio.charset.StandardCharsets.UTF_8;

public class ParallelLoadFlowActionSimulator extends LoadFlowActionSimulator implements ActionSimulatorToolConstants {

    private static final Logger LOGGER = LoggerFactory.getLogger(ParallelLoadFlowActionSimulator.class);

    private static final String ITOOLS_PRG = "itools";
    private static final String SUB_TASK_ID = "sub_ActionSimulator";

    private final int initPara;
    private final CommandLine commandLine;
    private final ToolRunningContext context;

    private final Semaphore outputLock = new Semaphore(1);

    private static final BiFunction<String, String, String> TO_OPTION = (k, v) -> {
        return "--" + k + "=" + v;
    };

    private static final BiFunction<Integer, Integer, String> TO_SUBDIR = (x, y) -> {
        return x + "_" + y;
    };


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
        boolean debug = true;
        ExecutionEnvironment itoolsEnvironment = new ExecutionEnvironment(Collections.emptyMap(), "subTask_", debug);
        int correctedPara = contingencyIds.size() > this.initPara ? this.initPara : contingencyIds.size();
        try (WorkingDirectory workingDirectory = new WorkingDirectory(super.getComputationManager().getLocalDir(), "paraTask_", debug)) {
            LOGGER.debug("workingDir in parallel start() : {} ", workingDirectory.toPath());
            CompletableFuture<LoadFlowResult>[] futures = new CompletableFuture[correctedPara];
            for (int i = 1; i <= correctedPara; i++) {
                Files.createDirectories(workingDirectory.toPath().resolve(TO_SUBDIR.apply(i, correctedPara)));
                int idx = i - 1;
                futures[idx] = manager.execute(itoolsEnvironment, new SubTaskHandler(i, correctedPara, workingDirectory.toPath()));
            }
            CompletableFuture.allOf(futures).join();

            // merge result to client's path
            observers.stream().map(o -> o.getChunkFiles()).filter(l -> !l.isEmpty()).forEach(l -> {
                l.forEach(chunkFile -> {
                    try {
                        switch (chunkFile.getMergeType()) {
                            case APPEND:
                                String chunkName = chunkFile.getPath().getFileName().toString();
                                String opt = chunkFile.getCreatorOpt();
                                String optionValue = commandLine.getOptionValue(opt);
                                for (int i = 1; i <= correctedPara; i++) {
                                    Path source = workingDirectory.toPath().resolve(TO_SUBDIR.apply(i, correctedPara)).resolve(chunkName);
                                    Path dest = Paths.get(optionValue);
                                    if (i == 1) {
                                        Files.createFile(dest);
                                    }
                                    InputStream inputStream = Files.newInputStream(source);
                                    Files.write(dest, IOUtils.toByteArray(inputStream), StandardOpenOption.APPEND);
                                }
                                break;
                            case DIRCOPY:
                                for (int i = 1; i <= correctedPara; i++) {
                                    Path source = workingDirectory.toPath().resolve(TO_SUBDIR.apply(i, correctedPara)).resolve(chunkFile.getPath().getFileName());
                                    Path dest = Paths.get(commandLine.getOptionValue(chunkFile.getCreatorOpt()));
                                    FileUtil.copyDir(source, dest);
                                }
                                break;
                            default:
                                new PowsyblException("MergeType : " + chunkFile.getMergeType() + " is not valid");
                        }
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                });
            });

        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void start(ActionDb actionDb, String... contingencyIds) {
        start(actionDb, Arrays.asList(contingencyIds));
    }

    private class SubTaskHandler extends AbstractExecutionHandler<LoadFlowResult> {

        private final Integer chunk;
        private final Integer totalSize;

        private final String subDirPostfix;

        private final Path commonDirForParallel;

        SubTaskHandler(Integer x, Integer y, Path commonDirForParallel) {
            chunk = Objects.requireNonNull(x);
            totalSize = Objects.requireNonNull(y);
            this.commonDirForParallel = Objects.requireNonNull(commonDirForParallel);
            subDirPostfix = TO_SUBDIR.apply(x, y);
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
                 PrintStream err = context.getErrorStream();
                 Stream<String> outLines = Files.lines(workingDir.resolve(SUB_TASK_ID + "_0.out"), UTF_8);
                 Stream<String> errLines = Files.lines(workingDir.resolve(SUB_TASK_ID + "_0.err"), UTF_8)
            ) {
                outLines.forEach(out::println);
                errLines.forEach(err::println);
            } finally {
                outputLock.release();
            }

            return super.after(workingDir, report);
        }

        private void addArgsIfExistsInOriginal(List<String> list, String optName) {
            if (commandLine.hasOption(optName)) {
                String optValue = TO_OPTION.apply(optName, commandLine.getOptionValue(optName));
                list.add(optValue);
            }
        }

        private List<String> rebuildSubProgramArgs(Path workingDir) throws IOException {
            List<String> args = new ArrayList<>(); // subtask command args
            args.add("action-simulator");
            // copy input files to slave workingDir
            Path caseFile = copyFromOptionValueToWorkingDir(CASE_FILE, workingDir);
            String caseFileOpt = TO_OPTION.apply(CASE_FILE, caseFile.toString());
            args.add(caseFileOpt);
            Path dslFile = copyFromOptionValueToWorkingDir(DSL_FILE, workingDir);
            String dslFileOpt = TO_OPTION.apply(DSL_FILE, dslFile.toString());
            args.add(dslFileOpt);

            String filtrationOpt = TO_OPTION.apply(SUB_CONTINGENCIES, chunk + "/" + totalSize);
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
            // temp save in common_workingDir/X_Y/CASE_FOLDER and copy to original output folder in after()
            if (commandLine.hasOption(OUTPUT_CASE_FOLDER)) {
                String folderName = Paths.get(commandLine.getOptionValue(OUTPUT_CASE_FOLDER)).getFileName().toString();
                String tmpFolderOpt = TO_OPTION.apply(OUTPUT_CASE_FOLDER, commonDirForParallel.resolve(subDirPostfix).resolve(folderName).toString());
                args.add(tmpFolderOpt);
            }

            // type3: is a file
            // temp save in common_workingDir/X_Y and copy to original output file in after()
            if (commandLine.hasOption(OUTPUT_CSV)) {
                String optValue = commandLine.getOptionValue(OUTPUT_CSV);
                Path fileName = Paths.get(optValue).getFileName();
                String tmpFileOpt = TO_OPTION.apply(OUTPUT_CSV, commonDirForParallel.resolve(subDirPostfix).resolve(fileName).toString());
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

    }
}
