/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.computation.local;

import com.google.common.io.ByteStreams;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.commons.io.WorkingDirectory;
import com.powsybl.computation.*;
import net.java.truevfs.comp.zip.ZipEntry;
import net.java.truevfs.comp.zip.ZipFile;
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
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class LocalComputationManager implements ComputationManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(LocalComputationManager.class);

    private final LocalComputationConfig config;

    private final WorkingDirectory commonDir;

    private final LocalComputationResourcesStatus status;

    private final Semaphore permits;

    private final Executor threadPools;

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

    public LocalComputationManager(Path localDir)  throws IOException {
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
        this.threadPools = Objects.requireNonNull(executor);
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

    private ExecutionReport execute(Path workingDir, List<CommandExecution> commandExecutionList, Map<String, String> variables, ExecutionMonitor monitor)
            throws InterruptedException {
        // TODO concurrent
        List<ExecutionError> errors = new ArrayList<>();
        ExecutorService executionSubmitter = Executors.newCachedThreadPool();

        for (CommandExecution commandExecution : commandExecutionList) {
            Command command = commandExecution.getCommand();
            CountDownLatch latch = new CountDownLatch(commandExecution.getExecutionCount());
            IntStream.range(0, commandExecution.getExecutionCount()).forEach(idx ->
                    executionSubmitter.execute(() -> {
                        try {
                            enter();
                            logExecutingCommand(workingDir, command, idx);
                            preProcess(workingDir, command, idx);
                            int exitValue = process(workingDir, commandExecution, idx, variables);
                            postProcess(workingDir, commandExecution, idx, exitValue, errors, monitor);
                        } catch (Exception e) {
                            LOGGER.warn(e.getMessage());
                        } finally {
                            latch.countDown();
                            exit();
                        }
                    })
            );
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

        return new ExecutionReport(errors);
    }

    private void logExecutingCommand(Path workingDir, Command command, int executionIndex) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Executing command {} in working directory {}",
                    command.toString(executionIndex), workingDir);
        }
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
                        try (ZipFile zipFile = new ZipFile(path)) {
                            for (ZipEntry ze : Collections.list(zipFile.entries())) {
                                Files.copy(zipFile.getInputStream(ze.getName()), workingDir.resolve(ze.getName()), REPLACE_EXISTING);
                            }
                        }
                        break;

                    default:
                        throw new AssertionError("Unexpected FilePreProcessor value: " + file.getPreProcessor());
                }
            }
        }
    }

    private int process(Path workingDir, CommandExecution commandExecution, int executionIndex, Map<String, String> variables) throws IOException, InterruptedException {
        Command command = commandExecution.getCommand();
        int exitValue = 0;
        Path outFile = workingDir.resolve(command.getId() + "_" + executionIndex + ".out");
        Path errFile = workingDir.resolve(command.getId() + "_" + executionIndex + ".err");
        Map<String, String> executionVariables = CommandExecution.getExecutionVariables(variables, commandExecution);
        switch (command.getType()) {
            case SIMPLE:
                SimpleCommand simpleCmd = (SimpleCommand) command;
                exitValue = localCommandExecutor.execute(simpleCmd.getProgram(),
                        simpleCmd.getArgs(executionIndex),
                        outFile,
                        errFile,
                        workingDir,
                        executionVariables);
                break;
            case GROUP:
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
                throw new AssertionError("Unexpected CommandType value: " + command.getType());
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
                        throw new AssertionError("Unexpected FilePostProcessor value: " + file.getPostProcessor());
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
        Objects.requireNonNull(environment);
        Objects.requireNonNull(handler);
        MyCf<R> f = new MyCf<>();
        threadPools.execute(() -> {
            f.setThread(Thread.currentThread());
            try {
                try (WorkingDirectory workingDir = new WorkingDirectory(config.getLocalDir(), environment.getWorkingDirPrefix(), environment.isDebug())) {
                    f.setWorkingDir(workingDir.toPath());
                    List<CommandExecution> commandExecutionList = handler.before(workingDir.toPath());
                    ExecutionReport report = execute(workingDir.toPath(), commandExecutionList, environment.getVariables(), handler::onExecutionCompletion);
                    R result = handler.after(workingDir.toPath(), report);
                    f.complete(result);
                }
            } catch (Exception e) {
                f.completeExceptionally(e);
            }
        });
        return f;
    }

    private class MyCf<R> extends ThreadInterruptedCompletableFuture<R> {

        private Path workingDir;

        private void setWorkingDir(Path workingDir) {
            this.workingDir = workingDir;
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            super.cancel(mayInterruptIfRunning);
            if (mayInterruptIfRunning) {
                localCommandExecutor.stop(workingDir);
            }
            return true;
        }
    }

    @Override
    public ComputationResourcesStatus getResourcesStatus() {
        return status;
    }

    @Override
    public Executor getExecutor() {
        return threadPools;
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
