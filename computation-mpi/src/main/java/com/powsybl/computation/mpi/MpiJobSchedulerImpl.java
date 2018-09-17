/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.computation.mpi;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.ByteStreams;
import com.google.protobuf.ByteString;
import com.powsybl.commons.PowsyblException;
import com.powsybl.computation.*;
import com.powsybl.computation.mpi.generated.Messages;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class MpiJobSchedulerImpl implements MpiJobScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(MpiJobSchedulerImpl.class);

    private static final int TIMEOUT = 10; // ms

    private static final Map<String, String> ZIP_FS_ENV = ImmutableMap.of("create", "true");

    private final MpiNativeServices nativeServices;

    private final MpiStatistics statistics;

    private final Future<?> future;

    private final List<CommonFile> newCommonFiles = new ArrayList<>();

    private final Lock newCommonFileLock = new ReentrantLock();

    private final Set<String> commonFiles = new HashSet<>();

    private final List<MpiJob> newJobs = new ArrayList<>();

    private final Lock newJobsLock = new ReentrantLock();

    // active contexts ordered by priority
    private final Set<MpiJob> jobs = new TreeSet<>((c1, c2) -> {
        if (c1.getExecution().getPriority() != c2.getExecution().getPriority()) {
            return c1.getExecution().getPriority() - c2.getExecution().getPriority();
        } else {
            return System.identityHashCode(c1) - System.identityHashCode(c2);
        }
    });

    private volatile boolean stopRequested = false;

    private String mpiVersion;

    private MpiResources resources;

    private final AtomicInteger startedTasks = new AtomicInteger();

    private int taskId = 0;

    private int jobId = 0;

    private final Path stdOutArchive;

    // profiling
    private long startTasksTime;
    private long startTasksJniTime;
    private long processCompletedTasksTime;
    private long checkTaskCompletionTime;

    MpiJobSchedulerImpl(MpiNativeServices nativeServices, MpiStatisticsFactory statisticsFactory, Path statisticsDbDir, String statisticsDbName,
                        int coresPerRank, boolean verbose, ExecutorService executor, Path stdOutArchive) throws InterruptedException, IOException {
        this.nativeServices = Objects.requireNonNull(nativeServices);
        this.statistics = Objects.requireNonNull(statisticsFactory).create(statisticsDbDir, statisticsDbName);
        this.stdOutArchive = checkOutputArchive(stdOutArchive);
        final CountDownLatch initialized = new CountDownLatch(1);
        future = executor.submit(() -> {
            LOGGER.trace("Job scheduler started");
            try {
                nativeServices.initMpi(coresPerRank, verbose);

                mpiVersion = nativeServices.getMpiVersion();
                resources = new MpiResources(nativeServices.getMpiCommSize(), coresPerRank);

                initialized.countDown();

                long time = System.currentTimeMillis();

                List<MpiTask> completedTasks = new ArrayList<>();
                while (!stopRequested || !jobs.isEmpty()) {
                    // check performances
                    long oldTime = time;
                    time = System.currentTimeMillis();
                    long diff = time - oldTime;
                    if (diff > 1000) { // 1s
                        LOGGER.warn("Slowness ({} ms) has been detected in the job scheduler (startTasksTime={}, startTasksJniTime={}, processCompletedTasksTime={}, checkTaskCompletionTime={})",
                                diff, startTasksTime, startTasksJniTime, processCompletedTasksTime, checkTaskCompletionTime);
                    }
                    startTasksTime = 0;
                    startTasksJniTime = 0;
                    processCompletedTasksTime = 0;
                    checkTaskCompletionTime = 0;

                    sendCommonFilesChunks();

                    // add new context
                    newJobsLock.lock();
                    try {
                        jobs.addAll(newJobs);
                        newJobs.clear();
                    } finally {
                        newJobsLock.unlock();
                    }

                    // just sleep in the case nothing has been done in the loop
                    if (processJobs(completedTasks)) {
                        TimeUnit.MILLISECONDS.sleep(TIMEOUT);
                    }
                }

                nativeServices.terminateMpi();

            } catch (Throwable t) {
                LOGGER.error(t.toString(), t);
                System.exit(-1);
            }

            LOGGER.trace("Job scheduler stopped");
        });

        initialized.await();
    }

    private static Path checkOutputArchive(Path stdOutArchive) throws IOException {
        if (stdOutArchive != null) {
            if (Files.exists(stdOutArchive)) {
                if (!Files.isRegularFile(stdOutArchive)) {
                    throw new IllegalArgumentException("Bad standard output archive file");
                }
                Files.deleteIfExists(stdOutArchive);
            }
            LOGGER.info("Standard output of failed commands will be archived {}", stdOutArchive);
        }
        return stdOutArchive;
    }

    @Override
    public MpiResources getResources() {
        return resources;
    }

    @Override
    public String getVersion() {
        return mpiVersion;
    }

    @Override
    public int getStartedTasksAndReset() {
        return startedTasks.getAndSet(0);
    }

    private static Messages.Task.InputFile.PreProcessor createPreProcessor(FilePreProcessor preProcessor) {
        return preProcessor != null ? Messages.Task.InputFile.PreProcessor.valueOf(preProcessor.name()) : Messages.Task.InputFile.PreProcessor.NONE;
    }

    private static Messages.Task.OutputFile.PostProcessor createPostProcessor(FilePostProcessor postProcessor) {
        return postProcessor != null ? Messages.Task.OutputFile.PostProcessor.valueOf(postProcessor.name()) : Messages.Task.OutputFile.PostProcessor.NONE;
    }

    private static Messages.Task.Environment createEnvMessage(Map<String, String> variables) {
        Messages.Task.Environment.Builder builder = Messages.Task.Environment.newBuilder();
        for (Map.Entry<String, String> var : variables.entrySet()) {
            builder.addVariable(Messages.Task.Variable.newBuilder()
                    .setName(var.getKey())
                    .setValue(var.getValue())
                    .build());
        }
        return builder.build();
    }

    private static Messages.Task.Command createCommand(SimpleCommand command, int taskIndex) {
        Messages.Task.Command.Builder builder = Messages.Task.Command.newBuilder()
                .setProgram(command.getProgram())
                .addAllArgument(command.getArgs(taskIndex));
        if (command.getTimeout() != -1) {
            builder.setTimeout(command.getTimeout());
        }
        return builder.build();
    }

    private static Messages.Task.Command createCommand(GroupCommand.SubCommand subCmd, int taskIndex) {
        Messages.Task.Command.Builder builder = Messages.Task.Command.newBuilder()
                .setProgram(subCmd.getProgram())
                .addAllArgument(subCmd.getArgs(taskIndex));
        if (subCmd.getTimeout() != -1) {
            builder.setTimeout(subCmd.getTimeout());
        }
        return builder.build();
    }

    private Messages.Task createTaskMessage(MpiJob job, MpiRank rank, Command command, int taskIndex) throws IOException {

        // job scoped file will be sent only one time to each slave
        boolean initJob = rank.jobs.add(job);

        Messages.Task.Builder builder = Messages.Task.newBuilder()
                .setJobId(job.getId())
                .setIndex(taskIndex)
                .setCmdId(command.getId())
                .setInitJob(initJob);

        //
        // select which files have to be send with the message
        //
        for (InputFile file : command.getInputFiles()) {
            if (file.dependsOnExecutionNumber()) {
                addInputFile(builder, job, file, taskIndex);
            } else {
                addInputFile(builder, job, file, taskIndex, initJob);
            }
        }

        builder.setEnv(createEnvMessage(job.getExecutionVariables()));

        switch (command.getType()) {
            case SIMPLE:
                builder.addCommand(createCommand((SimpleCommand) command, taskIndex));
                break;

            case GROUP:
                GroupCommand groupCommand = (GroupCommand) command;
                groupCommand.getSubCommands().forEach(subCmd -> builder.addCommand(createCommand(subCmd, taskIndex)));
                break;

            default:
                throw new AssertionError("Unexpected CommandType value: " + command.getType());
        }

        command.getOutputFiles().forEach(outputFile ->
                builder.addOutputFile(Messages.Task.OutputFile.newBuilder()
                        .setName(outputFile.getName(taskIndex))
                        .setPostProcessor(createPostProcessor(outputFile.getPostProcessor()))
                        .build()));

        for (Iterator<MpiJob> it = rank.jobs.iterator(); it.hasNext(); ) {
            MpiJob otherJob = it.next();
            if (otherJob.isCompleted()) {
                it.remove();
                builder.addCompletedJobId(otherJob.getId());
            }
        }

        return builder.build();
    }

    private void addInputFile(Messages.Task.Builder builder, MpiJob job, InputFile file, int taskIndex) throws IOException {
        //
        // The file name depends on the execution instance, it has
        // to be sent with the message
        //
        String fileName = file.getName(taskIndex);
        Path path = job.getWorkingDir().resolve(fileName);
        if (Files.exists(path)) {
            try (InputStream is = Files.newInputStream(path)) {
                builder.addInputFile(Messages.Task.InputFile.newBuilder()
                        .setName(fileName)
                        .setScope(Messages.Task.InputFile.Scope.TASK)
                        .setPreProcessor(createPreProcessor(file.getPreProcessor()))
                        .setData(ByteString.readFrom(is))
                        .build());
            }
        } else if (commonFiles.contains(fileName)) {
            builder.addInputFile(Messages.Task.InputFile.newBuilder()
                    .setName(fileName)
                    .setScope(Messages.Task.InputFile.Scope.RUN)
                    .setPreProcessor(createPreProcessor(file.getPreProcessor()))
                    .build());
        } else {
            throw new PowsyblException("Input file " + fileName
                    + " not found in the working directory nor in the common file list");
        }
    }

    private void addInputFile(Messages.Task.Builder builder, MpiJob job, InputFile file, int taskIndex, boolean initJob) throws IOException {
        String fileName = file.getName(taskIndex);
        Path path = job.getWorkingDir().resolve(fileName);
        if (Files.exists(path)) {
            //
            // case 2: the file name does not depend on the execution instance
            // and exists in the working directory
            //
            if (initJob) {
                //
                // This is the first task of the job executed by
                // the slave with specified rank, we pack the file with
                // the message
                //
                try (InputStream is = Files.newInputStream(path)) {
                    builder.addInputFile(Messages.Task.InputFile.newBuilder()
                            .setName(fileName)
                            .setScope(Messages.Task.InputFile.Scope.JOB)
                            .setPreProcessor(createPreProcessor(file.getPreProcessor()))
                            .setData(ByteString.readFrom(is))
                            .build());
                }
            } else {
                //
                // Another task of the job has already been
                // handled by the slave with the specified rank, so the file
                // is already cached on the slave node and there is no need
                // the pack it with the message
                //
                builder.addInputFile(Messages.Task.InputFile.newBuilder()
                        .setName(fileName)
                        .setScope(Messages.Task.InputFile.Scope.JOB)
                        .setPreProcessor(createPreProcessor(file.getPreProcessor()))
                        .build());
            }
        } else if (commonFiles.contains(fileName)) {
            //
            // The file name does not depend on the execution instance
            // and belongs to the common file list, it has already been
            // sent to all slave so no need to pack it in the message
            //
            builder.addInputFile(Messages.Task.InputFile.newBuilder()
                    .setName(fileName)
                    .setScope(Messages.Task.InputFile.Scope.RUN)
                    .setPreProcessor(createPreProcessor(file.getPreProcessor()))
                    .build());
        } else {
            throw new PowsyblException("Input file " + fileName
                    + " not found in the common file list");
        }
    }

    private boolean processJobs(List<MpiTask> completedTasks) throws IOException {
        boolean sleep = true;

        for (Iterator<MpiJob> it = jobs.iterator(); it.hasNext(); ) {
            MpiJob job = it.next();

            sleep = startTasks(job);

            long t0 = System.currentTimeMillis();
            try {
                nativeServices.checkTasksCompletion(job.getRunningTasks(), completedTasks);
            } finally {
                checkTaskCompletionTime += System.currentTimeMillis() - t0;
            }
            if (!completedTasks.isEmpty()) {
                DateTime endTime = DateTime.now();

                // release cores as fast as possible
                completedTasks.forEach(task -> {
                    MpiJobSchedulerImpl.this.resources.releaseCore(task.getCore());
                    task.setEndTime(endTime);
                });

                // ...and re-use immediatly free cores
                startTasks(job);

                // ...and then post process terminated tasks
                processCompletedTasks(job, completedTasks);

                // ...no more tasks to start or running, we can remove the context
                if (job.isCompleted()) {
                    // remove the job
                    it.remove();

                    processCompletedJob(job);
                }

                sleep = false;
            }
        }

        return sleep;
    }

    private boolean startTasks(MpiJob job) throws IOException {
        long t0 = System.currentTimeMillis();
        try {
            CommandExecution execution = job.getExecution();
            Command command = execution.getCommand();

            int taskIndex = job.getTaskIndex();

            if (taskIndex < execution.getExecutionCount()) {
                // reserve one core for each of the execution instances
                List<Core> allocatedCores = resources.reserveCores(execution.getExecutionCount() - taskIndex, job.getUsedRanks());
                if (allocatedCores == null || allocatedCores.isEmpty()) {
                    return true;
                }

                if (taskIndex == 0) {
                    statistics.logJobStart(job.getId(), command.getId(), execution.getTags());
                }

                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Sending commands {} to slaves {} using working directory {}",
                            command.toString(-1), allocatedCores, job.getWorkingDir());
                }

                DateTime startTime = DateTime.now();

                // encode task messages
                int oldTaskIndex = taskIndex;
                List<MpiTask> tasks = new ArrayList<>(allocatedCores.size());
                for (Core core : allocatedCores) {
                    byte[] message = createTaskMessage(job, core.rank, command, taskIndex).toByteArray();

                    MpiTask task = new MpiTask(taskId++, core, taskIndex, message, startTime);
                    tasks.add(task);

                    statistics.logTaskStart(task.getId(),
                            job.getId(),
                            taskIndex,
                            startTime,
                            core.rank.num,
                            core.thread,
                            message.length);

                    taskIndex++;

                    // update used ranks
                    // TODO c'est completement bugge, ne pas reactiver!!!!
//                        job.getUsedRanks().add(core.rank.num);
                }

                if (LOGGER.isTraceEnabled()) {
                    LOGGER.trace("Starting tasks {} of job {}",
                            tasks.stream().map(MpiTask::getIndex).collect(Collectors.toList()), job.getId());
                }

                // run tasks on slaves nodes
                long t1 = System.currentTimeMillis();
                try {
                    nativeServices.startTasks(tasks);
                } finally {
                    startTasksJniTime += System.currentTimeMillis() - t1;
                }

                startedTasks.addAndGet(allocatedCores.size());

                job.setTaskIndex(taskIndex);
                job.getRunningTasks().addAll(tasks);

                // notify execution start
                try {
                    job.getListener().onExecutionStart(oldTaskIndex, taskIndex);
                } catch (Exception e) {
                    LOGGER.error(e.toString(), e);
                }
                return false;
            }
            return true;
        } finally {
            startTasksTime += System.currentTimeMillis() - t0;
        }
    }

    private void processCompletedTasks(MpiJob job, List<MpiTask> completedTasks) throws IOException {
        long t0 = System.currentTimeMillis();
        try {
            CommandExecution execution = job.getExecution();
            Command command = execution.getCommand();

            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("Tasks {} of job {} completed",
                        completedTasks.stream().map(MpiTask::getIndex).collect(Collectors.toList()), job.getId());
            }

            for (MpiTask task : completedTasks) {
                // duration of the task seen by the master in ms
                long taskDurationSeenByMaster = new Duration(task.getStartTime(), task.getEndTime()).getMillis();

                // decode task result messages
                Messages.TaskResult message = Messages.TaskResult.parseFrom(task.getResultMessage());

                // duration of the task seen by the slave in ms
                long taskDurationSeenBySlave = message.getTaskDuration(); // number of ms

                // write output files to working dir
                String stdOutGzFileName = job.getExecution().getCommand().getId() + "_" + task.getIndex() + ".out.gz";
                for (Messages.TaskResult.OutputFile outputFile : message.getOutputFileList()) {
                    // std out file special case directly unzip it on working dir
                    copyFileToWorkingDirectory(outputFile, job, stdOutGzFileName);

                    // archive standard output of problematic tasks
                    if ((stdOutArchive != null) && (message.getExitCode() != 0) && outputFile.getName().equals(stdOutGzFileName)) {
                        try (FileSystem archiveFileSystem = FileSystems.newFileSystem(URI.create("jar:file:" + stdOutArchive.toUri().getPath()), ZIP_FS_ENV)) {
                            Path dir = archiveFileSystem.getPath("/").resolve("job-" + job.getId());
                            Files.createDirectories(dir);
                            try (InputStream is = outputFile.getData().newInput();
                                 OutputStream os = Files.newOutputStream(dir.resolve(outputFile.getName()))) {
                                ByteStreams.copy(is, os);
                            }
                        }
                    }
                }

                if (message.getExitCode() != 0) {
                    job.getErrors().add(new ExecutionError(command,
                            task.getIndex(),
                            message.getExitCode()));
                }

                // notify execution completion
                try {
                    job.getListener().onExecutionCompletion(task.getIndex());
                } catch (Exception e) {
                    LOGGER.error(e.toString(), e);
                }

                // update execution statistics
                statistics.logTaskEnd(task.getId(),
                        taskDurationSeenByMaster,
                        message.getCommandDurationList(),
                        taskDurationSeenByMaster - taskDurationSeenBySlave,
                        task.getResultMessage().length,
                        message.getWorkingDataSize(),
                        message.getExitCode());
            }

            job.getRunningTasks().removeAll(completedTasks);
            completedTasks.clear();
        } finally {
            processCompletedTasksTime += System.currentTimeMillis() - t0;
        }
    }

    private void copyFileToWorkingDirectory(Messages.TaskResult.OutputFile outputFile, MpiJob job, String stdOutGzFileName) throws IOException {
        if (outputFile.getName().equals(stdOutGzFileName)) {
            Path path = job.getWorkingDir().resolve(outputFile.getName().substring(0, outputFile.getName().length() - 3));
            try (InputStream is = new GZIPInputStream(outputFile.getData().newInput());
                 OutputStream os = Files.newOutputStream(path)) {
                ByteStreams.copy(is, os);
            }
        } else {
            Path path = job.getWorkingDir().resolve(outputFile.getName());
            try (InputStream is = outputFile.getData().newInput();
                 OutputStream os = Files.newOutputStream(path)) {
                ByteStreams.copy(is, os);
            }
        }
    }

    private void processCompletedJob(MpiJob job) {
        ExecutionReport report = new ExecutionReport(job.getErrors());
        try {
            job.getListener().onEnd(report);
        } catch (Exception e) {
            LOGGER.error(e.toString(), e);
        }
        job.getFuture().complete(report);

        MpiJobSchedulerImpl.this.statistics.logJobEnd(job.getId());
    }

    @Override
    public void sendCommonFile(CommonFile commonFile) {
        newCommonFileLock.lock();
        try {
            newCommonFiles.add(commonFile);
        } finally {
            newCommonFileLock.unlock();
        }
    }

    private void sendCommonFilesChunks() throws IOException {
        newCommonFileLock.lock();
        try {
            for (CommonFile commonFile : newCommonFiles) {
                LOGGER.info("Sending chunk {} of common file '{}' (last={})",
                        commonFile.getChunk(), commonFile.getName(), commonFile.isLast());
                List<Core> allCores = resources.reserveAllCoresOrFail();
                try {
                    try (ByteArrayInputStream is = new ByteArrayInputStream(commonFile.getData())) {
                        // no pre-processor let it as it is
                        Messages.CommonFile message = Messages.CommonFile.newBuilder()
                                .setName(commonFile.getName())
                                .setChunk(commonFile.getChunk())
                                .setLast(commonFile.isLast())
                                .setData(ByteString.readFrom(is))
                                .build();
                        long t1 = System.currentTimeMillis();
                        nativeServices.sendCommonFile(message.toByteArray());
                        long t2 = System.currentTimeMillis();
                        commonFiles.add(commonFile.getName());
                        MpiJobSchedulerImpl.this.statistics.logCommonFileTransfer(commonFile.getName(), commonFile.getChunk(), commonFile.getData().length, t2 - t1);
                    }
                } finally {
                    resources.releaseCores(allCores);
                }
            }
            newCommonFiles.clear();
        } finally {
            newCommonFileLock.unlock();
        }
    }

    @Override
    public CompletableFuture<ExecutionReport> execute(CommandExecution execution, Path workingDir, Map<String, String> variables, ExecutionListener listener) {
        CompletableFuture<ExecutionReport> report = new CompletableFuture<>();
        newJobsLock.lock();
        try {
            MpiJob job = new MpiJob(jobId++, execution, workingDir, variables, listener, report);
            newJobs.add(job);
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("Job {} scheduled ({} tasks)", job.getId(), job.getExecution().getExecutionCount());
            }
        } finally {
            newJobsLock.unlock();
        }
        return report;
    }

    @Override
    public void shutdown() throws Exception {
        try {
            statistics.close();
        } catch (Exception e) {
            LOGGER.error(e.toString(), e);
        }
        stopRequested = true;
        future.get();
    }

}
