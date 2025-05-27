/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * Copyright (c) 2016-2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.computation.local;

import com.google.common.base.Stopwatch;
import com.google.common.io.ByteStreams;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.commons.io.FileUtil;
import com.powsybl.commons.io.WorkingDirectory;
import com.powsybl.computation.*;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.IntStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

/**
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class LocalComputationManager implements ComputationManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(LocalComputationManager.class);

    private final LocalComputationConfig config;

    private final WorkingDirectory commonDir;

    private final LocalComputationResourcesStatus status;

    private final Semaphore permits;

    private final Executor threadPool;

    private final LocalCommandExecutor localCommandExecutor;

    private static final Lock LOCK = new ReentrantLock();

    private static LocalComputationManager defaultInstance;

    public static ComputationManager getDefault() {
        LOCK.lock();
        try {
            if (defaultInstance == null) {
                try {
                    defaultInstance = new LocalComputationManager();
                    Runtime.getRuntime().addShutdownHook(new Thread(() -> defaultInstance.close()));
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }
            return defaultInstance;
        } finally {
            LOCK.unlock();
        }
    }

    private static LocalCommandExecutor getLocalCommandExecutor() {
        if (SystemUtils.IS_OS_WINDOWS) {
            return new WindowsLocalCommandExecutor();
        } else if (SystemUtils.IS_OS_UNIX) {
            return new UnixLocalCommandExecutor();
        } else {
            throw new UnsupportedOperationException("OS not supported for local execution");
        }
    }

    public LocalComputationManager() throws IOException {
        this(LocalComputationConfig.load());
    }

    public LocalComputationManager(Executor executor) throws IOException {
        this(LocalComputationConfig.load(), executor);
    }

    public LocalComputationManager(PlatformConfig platformConfig) throws IOException {
        this(LocalComputationConfig.load(platformConfig));
    }

    public LocalComputationManager(Path localDir) throws IOException {
        this(new LocalComputationConfig(localDir));
    }

    public LocalComputationManager(LocalComputationConfig config) throws IOException {
        this(config, ForkJoinPool.commonPool());
    }

    public LocalComputationManager(LocalComputationConfig config, Executor executor) throws IOException {
        this(config, getLocalCommandExecutor(), executor);
    }

    public LocalComputationManager(LocalComputationConfig config, LocalCommandExecutor localCommandExecutor, Executor executor) throws IOException {
        this.config = Objects.requireNonNull(config);
        this.localCommandExecutor = Objects.requireNonNull(localCommandExecutor);
        this.threadPool = Objects.requireNonNull(executor);
        status = new LocalComputationResourcesStatus(config.getAvailableCore());
        permits = new Semaphore(config.getAvailableCore());
        //make sure the localdir exists
        Files.createDirectories(config.getLocalDir());
        commonDir = new WorkingDirectory(config.getLocalDir(), "itools_common_", false);
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(config.toString());
        }
    }

    @Override
    public String getVersion() {
        return "none (local mode)";
    }

    @Override
    public Path getLocalDir() {
        return config.getLocalDir();
    }

    @Override
    public OutputStream newCommonFile(String fileName) throws IOException {
        return Files.newOutputStream(commonDir.toPath().resolve(fileName));
    }

    private interface ExecutionMonitor {

        void onProgress(CommandExecution execution, int executionIndex);

    }

    private ExecutionReport execute(Path workingDir, Path debugDir, List<CommandExecution> commandExecutionList, Map<String, String> variables, ComputationParameters computationParameters, ExecutionMonitor monitor)
            throws InterruptedException {
        // TODO concurrent
        List<ExecutionError> errors = new ArrayList<>();
        ExecutorService executionSubmitter = Executors.newCachedThreadPool();

        for (CommandExecution commandExecution : commandExecutionList) {
            Command command = commandExecution.getCommand();
            CountDownLatch latch = new CountDownLatch(commandExecution.getExecutionCount());
            ExecutionParameters executionParameters = new ExecutionParameters(workingDir, debugDir, commandExecution, variables, computationParameters, executionSubmitter,
                command, latch, errors, monitor);
            IntStream.range(0, commandExecution.getExecutionCount()).forEach(idx -> performSingleExecution(executionParameters, idx));
            latch.await();
        }

        // TODO remove duplicated code
        executionSubmitter.shutdown();
        if (!executionSubmitter.awaitTermination(20, TimeUnit.SECONDS)) {
            executionSubmitter.shutdownNow();
            if (!executionSubmitter.awaitTermination(20, TimeUnit.SECONDS)) {
                LOGGER.error("Thread pool did not terminate");
            }
        }

        return new DefaultExecutionReport(workingDir, errors);
    }

    private record ExecutionParameters(Path workingDir, Path debugDir, CommandExecution commandExecution,
                                       Map<String, String> variables, ComputationParameters computationParameters,
                                       ExecutorService executionSubmitter, Command command, CountDownLatch latch,
                                       List<ExecutionError> errors, ExecutionMonitor monitor) {
    }

    private void performSingleExecution(ExecutionParameters executionParameters, int idx) {
        executionParameters.executionSubmitter.execute(() -> {
            try {
                enter();
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Executing command {} in working directory {}",
                        executionParameters.command.toString(idx), executionParameters.workingDir);
                }
                preProcess(executionParameters.workingDir, executionParameters.command, idx);
                Stopwatch stopwatch = null;
                if (LOGGER.isDebugEnabled()) {
                    stopwatch = Stopwatch.createStarted();
                }
                int exitValue = process(executionParameters.workingDir, executionParameters.commandExecution, idx,
                    executionParameters.variables, executionParameters.computationParameters);
                if (stopwatch != null) {
                    stopwatch.stop();
                    LOGGER.debug("Command {} executed in {} ms",
                        executionParameters.command.toString(idx), stopwatch.elapsed(TimeUnit.MILLISECONDS));
                }
                postProcess(executionParameters.workingDir, executionParameters.commandExecution, idx, exitValue,
                    executionParameters.errors, executionParameters.monitor);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                LOGGER.warn(e.getMessage(), e);
            } catch (Exception e) {
                LOGGER.warn(e.getMessage(), e);
            } finally {
                //                if (executionParameters.debugDir != null) {
                if (true) {
                    try {
                        FileUtil.copyDir(executionParameters.workingDir, executionParameters.debugDir);
                    } catch (IOException e) {
                        LOGGER.warn(e.getMessage(), e);
                    }
                }
                executionParameters.latch.countDown();
                exit();
            }
        });
    }

    private void preProcess(Path workingDir, Command command, int executionIndex) throws IOException {
        // pre-processing
        for (InputFile file : command.getInputFiles()) {
            String fileName = file.getName(executionIndex);

            Path path = checkInputFileExistsInWorkingAndCommons(workingDir, fileName, file);
            if (file.getPreProcessor() != null) {
                switch (file.getPreProcessor()) {
                    case FILE_GUNZIP:
                        // gunzip the file
                        try (InputStream is = new GZIPInputStream(Files.newInputStream(path));
                             OutputStream os = Files.newOutputStream(workingDir.resolve(fileName.substring(0, fileName.length() - 3)))) {
                            ByteStreams.copy(is, os);
                        }
                        break;
                    case ARCHIVE_UNZIP:
                        // extract the archive
                        try (ZipFile zipFile = ZipFile.builder()
                            .setSeekableByteChannel(Files.newByteChannel(path))
                            .get()) {
                            for (ZipArchiveEntry ze : Collections.list(zipFile.getEntries())) {
                                Files.copy(zipFile.getInputStream(zipFile.getEntry(ze.getName())), workingDir.resolve(ze.getName()), REPLACE_EXISTING);
                            }
                        }
                        break;

                    default:
                        throw new IllegalStateException("Unexpected FilePreProcessor value: " + file.getPreProcessor());
                }
            }
        }
    }

    private int process(Path workingDir, CommandExecution commandExecution, int executionIndex, Map<String, String> variables, ComputationParameters computationParameters) throws IOException, InterruptedException {
        Command command = commandExecution.getCommand();
        int exitValue = 0;
        long timeout = -1;
        Path outFile = workingDir.resolve(command.getId() + "_" + executionIndex + ".out");
        Path errFile = workingDir.resolve(command.getId() + "_" + executionIndex + ".err");
        Map<String, String> executionVariables = CommandExecution.getExecutionVariables(variables, commandExecution);
        switch (command.getType()) {
            case SIMPLE:
                SimpleCommand simpleCmd = (SimpleCommand) command;
                timeout = computationParameters.getTimeout(simpleCmd.getId()).orElse(-1);
                exitValue = localCommandExecutor.execute(simpleCmd.getProgram(), timeout,
                        simpleCmd.getArgs(executionIndex),
                        outFile,
                        errFile,
                        workingDir,
                        executionVariables);
                break;
            case GROUP:
                // TODO timeout for group
                for (GroupCommand.SubCommand subCmd : ((GroupCommand) command).getSubCommands()) {
                    exitValue = localCommandExecutor.execute(subCmd.getProgram(),
                            subCmd.getArgs(executionIndex),
                            outFile,
                            errFile,
                            workingDir,
                            executionVariables);
                    if (exitValue != 0) {
                        break;
                    }
                }
                break;
            default:
                throw new IllegalStateException("Unexpected CommandType value: " + command.getType());
        }
        return exitValue;
    }

    private void postProcess(Path workingDir, CommandExecution commandExecution, int executionIndex, int exitValue, List<ExecutionError> errors, ExecutionMonitor monitor) throws IOException {
        Command command = commandExecution.getCommand();
        if (exitValue != 0) {
            errors.add(new ExecutionError(command, executionIndex, exitValue));
        } else {
            // post processing
            for (OutputFile file : command.getOutputFiles()) {
                String fileName = file.getName(executionIndex);
                Path path = workingDir.resolve(fileName);
                if (file.getPostProcessor() != null && Files.isRegularFile(path)) {
                    if (file.getPostProcessor() == FilePostProcessor.FILE_GZIP) { // gzip the file
                        try (InputStream is = Files.newInputStream(path);
                             OutputStream os = new GZIPOutputStream(Files.newOutputStream(workingDir.resolve(fileName + ".gz")))) {
                            ByteStreams.copy(is, os);
                        }

                    } else {
                        throw new IllegalStateException("Unexpected FilePostProcessor value: " + file.getPostProcessor());
                    }
                }
            }
        }

        if (monitor != null) {
            monitor.onProgress(commandExecution, executionIndex);
        }
    }

    private Path checkInputFileExistsInWorkingAndCommons(Path workingDir, String fileName, InputFile file) throws IOException {
        // first check if the file exists in the working directory
        Path path = workingDir.resolve(fileName);
        if (!Files.exists(path)) {
            // if not check if the file exists in the common directory
            path = commonDir.toPath().resolve(fileName);
            if (!Files.exists(path)) {
                throw new PowsyblException("Input file '" + fileName + "' not found in the working and common directory");
            }
            if (file.getPreProcessor() == null) {
                Files.copy(path, workingDir.resolve(path.getFileName()));
            }
        }
        return path;
    }

    private void enter() throws InterruptedException {
        permits.acquire();
        status.incrementNumberOfBusyCores();
    }

    private void exit() {
        status.decrementNumberOfBusyCores();
        permits.release();
    }

    @Override
    public <R> CompletableFuture<R> execute(ExecutionEnvironment environment, ExecutionHandler<R> handler) {
        return execute(environment, handler, ComputationParameters.empty());
    }

    @Override
    public <R> CompletableFuture<R> execute(ExecutionEnvironment environment, ExecutionHandler<R> handler, ComputationParameters parameters) {
        Objects.requireNonNull(environment);
        Objects.requireNonNull(handler);

        return CompletableFutureTask.runAsync(() -> doExecute(environment, handler, parameters), threadPool);
    }

    /**
     * Executes commands described by the specified handler. If the executing thread is interrupted,
     * for example by a call to {@link CompletableFutureTask#cancel(boolean)}, the underlying process
     * execution will be stopped.
     */
    private <R> R doExecute(ExecutionEnvironment environment, ExecutionHandler<R> handler, ComputationParameters parameters) throws IOException, InterruptedException {

        try (WorkingDirectory workingDir = new WorkingDirectory(config.getLocalDir(), environment.getWorkingDirPrefix(), environment.isDebug())) {

            List<CommandExecution> commandExecutionList = handler.before(workingDir.toPath());

            ExecutionReport report;

            try {
                report = execute(workingDir.toPath(), /*Path.of(environment.getDebugDir())*/Path.of("/tmp/toto"), commandExecutionList, environment.getVariables(), parameters, handler::onExecutionCompletion);
            } catch (InterruptedException exc) {
                localCommandExecutor.stop(workingDir.toPath());
                throw exc;
            }
            return handler.after(workingDir.toPath(), report);
        }
    }

    @Override
    public ComputationResourcesStatus getResourcesStatus() {
        return status;
    }

    @Override
    public Executor getExecutor() {
        return threadPool;
    }

    @Override
    public void close() {
        try {
            commonDir.close();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

}
