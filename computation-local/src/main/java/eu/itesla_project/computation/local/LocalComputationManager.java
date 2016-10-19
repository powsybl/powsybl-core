/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.computation.local;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.ByteStreams;
import eu.itesla_project.commons.io.WorkingDirectory;
import eu.itesla_project.computation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

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

    private static final Lock LOCK = new ReentrantLock();

    private static LocalComputationManager DEFAULT;

    public static ComputationManager getDefault() {
        LOCK.lock();
        try {
            if (DEFAULT == null) {
                try {
                    DEFAULT = new LocalComputationManager();
                    Runtime.getRuntime().addShutdownHook(new Thread() {
                        @Override
                        public void run() {
                            try {
                                DEFAULT.close();
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    });
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            return DEFAULT;
        } finally {
            LOCK.unlock();
        }
    }

    public LocalComputationManager() throws IOException {
        this(LocalComputationConfig.load());
    }

    public LocalComputationManager(Path localDir)  throws IOException {
        this(new LocalComputationConfig(localDir));
    }

    public LocalComputationManager(LocalComputationConfig config) throws IOException {
        this.config = Objects.requireNonNull(config, "config is null");
        status = new LocalComputationResourcesStatus(config.getAvailableCore());
        permits = new Semaphore(config.getAvailableCore());
        //make sure the localdir exists
        Files.createDirectories(config.getLocalDir());
        commonDir = new WorkingDirectory(config.getLocalDir(), "itesla_common_", false);
        LOGGER.info(config.toString());
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

    @Override
    public CommandExecutor newCommandExecutor(Map<String, String> env, String workingDirPrefix, boolean debug) throws Exception {
        Objects.requireNonNull(env);
        Objects.requireNonNull(workingDirPrefix);

        WorkingDirectory workingDir = new WorkingDirectory(config.getLocalDir(), workingDirPrefix, debug);
        return new CommandExecutor() {
            @Override
            public Path getWorkingDir() {
                return workingDir.toPath();
            }

            @Override
            public void start(CommandExecution execution, ExecutionListener listener) throws Exception {
                enter();
                try {
                    if (listener != null) {
                        listener.onExecutionStart(0, execution.getExecutionCount() - 1);
                    }
                    ExecutionReport report = execute(workingDir.toPath(), Arrays.asList(execution), env, (execution1, executionIndex) -> {
                        if (listener != null) {
                            listener.onExecutionCompletion(executionIndex);
                        }
                    });
                    if (listener != null) {
                        listener.onEnd(report);
                    }
                } finally {
                    exit();
                }
            }

            @Override
            public ExecutionReport start(CommandExecution execution) throws Exception {
                enter();
                try {
                    return execute(workingDir.toPath(), Arrays.asList(execution), env, null);
                } finally {
                    exit();
                }
            }

            @Override
            public void close() throws Exception {
                workingDir.close();
            }
        };
    }

    private int execute(String program, List<String> args, File out, Path workingDir, Map<String, String> env) throws IOException, InterruptedException {
        StringBuilder internalCmd = new StringBuilder();
        for (Map.Entry<String, String> entry : env.entrySet()) {
            String name = entry.getKey();
            String value = entry.getValue();
            internalCmd.append("export ").append(name).append("=").append(value);
            if (name.endsWith("PATH")) {
                internalCmd.append(":").append("$").append(name);
            }
            internalCmd.append("; ");
        }
        internalCmd.append(program);
        for (String arg : args) {
            internalCmd.append(" ").append(arg);
        }

        List<String> cmdLs = ImmutableList.<String>builder()
                .add("bash")
                .add("-c")
                .add(internalCmd.toString())
                .build();
        ProcessBuilder.Redirect redirect = ProcessBuilder.Redirect.appendTo(out);
        Process process = new ProcessBuilder(cmdLs)
                .directory(workingDir.toFile())
                .redirectOutput(redirect)
                .redirectError(redirect)
                .start();
        int exitValue = process.waitFor();

        // to avoid 'two many open files' exception
        process.getInputStream().close();
        process.getOutputStream().close();
        process.getErrorStream().close();

        if (exitValue != 0) {
            LOGGER.debug("Command '{}' has failed (exitValue={})", cmdLs, exitValue);
        }
        return exitValue;
    }

    private interface ExecutionMonitor {

        void onProgress(CommandExecution execution, int executionIndex);

    }

    private ExecutionReport execute(Path workingDir, List<CommandExecution> commandExecutionList, Map<String, String> variables, ExecutionMonitor monitor)
            throws IOException, InterruptedException {
        List<ExecutionError> errors = new ArrayList<>();

        // set TMPDIR to working dir to avoid issue with /tmp
        ImmutableMap variables2 = ImmutableMap.builder()
                .putAll(variables)
                .put("TMPDIR", workingDir.toAbsolutePath().toString())
                .build();

        for (CommandExecution commandExecution : commandExecutionList) {
            Command command = commandExecution.getCommand();

            for (int executionIndex = 0; executionIndex < commandExecution.getExecutionCount(); executionIndex++) {
                LOGGER.debug("Executing command {} in working directory {}",
                        command.toString(Integer.toString(executionIndex)), workingDir);

                // pre-processing
                for (InputFile file : command.getInputFiles(Integer.toString(executionIndex))) {
                    // first check if the file exists in the working directory
                    Path path = workingDir.resolve(file.getName());
                    if (!Files.exists(path)) {
                        // if not check if the file exists in the common directory
                        path = commonDir.toPath().resolve(file.getName());
                        if (!Files.exists(path)) {
                            throw new RuntimeException("Input file '" + file.getName() + "' not found in the working and common directory");
                        }
                        if (file.getPreProcessor() == null) {
                            Files.copy(path, workingDir.resolve(path.getFileName()));
                        }
                    }
                    if (file.getPreProcessor() != null) {
                        switch (file.getPreProcessor()) {
                            case FILE_GUNZIP:
                                // gunzip the file
                                try (InputStream is = new GZIPInputStream(Files.newInputStream(path));
                                     OutputStream os = Files.newOutputStream(workingDir.resolve(file.getName().substring(0, file.getName().length() - 3)))) {
                                    ByteStreams.copy(is, os);
                                }
                                break;
                            case ARCHIVE_UNZIP:
                                // extract the archive
                                try (ZipFile zipFile = new ZipFile(path.toFile())) {
                                    for (ZipEntry ze : Collections.list(zipFile.entries())) {
                                        Files.copy(zipFile.getInputStream(ze), workingDir.resolve(ze.getName()), REPLACE_EXISTING);
                                    }
                                }
                                break;

                            default:
                                throw new InternalError();
                        }
                    }
                }

                int exitValue = 0;
                File out = workingDir.resolve(command.getId() + "_" + executionIndex + ".out").toFile();
                switch (command.getType()) {
                    case SIMPLE:
                        SimpleCommand simpleCmd = (SimpleCommand) command;
                        exitValue = execute(simpleCmd.getProgram(),
                                simpleCmd.getArgs(Integer.toString(executionIndex)),
                                out,
                                workingDir,
                                variables2);
                        break;
                    case GROUP:
                        for (GroupCommand.SubCommand subCmd : ((GroupCommand) command).getSubCommands()) {
                            exitValue = execute(subCmd.getProgram(),
                                    subCmd.getArgs(Integer.toString(executionIndex)),
                                    out,
                                    workingDir,
                                    variables2);
                            if (exitValue != 0) {
                                break;
                            }
                        }
                        break;
                    default:
                        throw new InternalError();
                }

                if (exitValue != 0) {
                    errors.add(new ExecutionError(command, executionIndex, exitValue));
                } else {
                    // post processing
                    for (OutputFile file : command.getOutputFiles(Integer.toString(executionIndex))) {
                        Path path = workingDir.resolve(file.getName());
                        if (file.getPostProcessor() != null && Files.isRegularFile(path)) {
                            switch (file.getPostProcessor()) {
                                case FILE_GZIP:
                                    // gzip the file
                                    try (InputStream is = Files.newInputStream(path);
                                         OutputStream os = new GZIPOutputStream(Files.newOutputStream(workingDir.resolve(file.getName() + ".gz")))) {
                                        ByteStreams.copy(is, os);
                                    }
                                    break;

                                default:
                                    throw new InternalError();
                            }
                        }
                    }
                }

                if (monitor != null) {
                    monitor.onProgress(commandExecution, executionIndex);
                }
            }
        }
        return new ExecutionReport(errors);
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
        CompletableFuture<R> f = new CompletableFuture<>();
        getExecutor().execute(() -> {
            try {
                try (WorkingDirectory workingDir = new WorkingDirectory(config.getLocalDir(), environment.getWorkingDirPrefix(), environment.isDebug())) {
                    List<CommandExecution> commandExecutionList = handler.before(workingDir.toPath());
                    ExecutionReport report = null;
                    enter();
                    try {
                        report = execute(workingDir.toPath(), commandExecutionList, environment.getVariables(), (execution, executionIndex) -> handler.onProgress(execution, executionIndex));
                    } finally {
                        exit();
                    }
                    R result = handler.after(workingDir.toPath(), report);
                    f.complete(result);
                }
            } catch (Exception e) {
                f.completeExceptionally(e);
            }
        });
        return f;
    }

    @Override
    public ComputationResourcesStatus getResourcesStatus() {
        return status;
    }

    @Override
    public Executor getExecutor() {
        return ForkJoinPool.commonPool();
    }

    @Override
    public void close() throws IOException {
        commonDir.close();
    }

}
