/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.computation.mpi;

import com.google.common.base.Joiner;
import com.google.common.base.Joiner.MapJoiner;
import com.google.common.base.Splitter;
import com.google.common.base.Splitter.MapSplitter;
import com.powsybl.commons.PowsyblException;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class CsvMpiStatistics implements MpiStatistics {

    private static final Logger LOGGER = LoggerFactory.getLogger(CsvMpiStatistics.class);

    private static final String COMMON_FILE_TRANSFER_KEY = "COMMON_FILE_TRANSFER";
    private static final String JOB_START_KEY = "JOB_START";
    private static final String JOB_END_KEY = "JOB_END";
    private static final String TASK_START_KEY = "TASK_START";
    private static final String TASK_END_KEY = "TASK_END";

    private static final String CSV_SEPARATOR = ";";

    private final Path dbDir;
    private final String dbName;
    private final BufferedWriter internalWriter;

    private final Joiner blankJoiner = Joiner.on(' ');
    private final MapJoiner mapJoiner = blankJoiner.withKeyValueSeparator("=");

    private static final class StatisticsReader implements AutoCloseable {

        private final BufferedReader reader;

        Map<Integer, TaskExecution> tasks = new HashMap<>();

        Map<Integer, JobExecution> jobs = new HashMap<>();

        private static final class JobExecution {
            final int jobId;
            final String commandId;
            final Map<String, String> tags;
            private JobExecution(int jobId, String commandId, Map<String, String> tags) {
                this.jobId = jobId;
                this.commandId = commandId;
                this.tags = tags;
            }
        }

        private static final class TaskExecution {

            final int taskId;
            final int jobId;
            final int taskIndex;

            final DateTime startTime;
            final int slaveRank;
            final int slaveThread;
            final long inputMessageSize;
            Long taskDuration;
            List<Long> commandsDuration;
            Long dataTransferDuration;
            Long outputMessageSize;
            Long workingDataSize;
            Integer exitCode;

            private TaskExecution(int taskId, int jobId, int taskIndex, DateTime startTime, int slaveRank, int slaveThread, long inputMessageSize) {
                this.taskId = taskId;
                this.jobId = jobId;
                this.taskIndex = taskIndex;
                this.startTime = startTime;
                this.slaveRank = slaveRank;
                this.slaveThread = slaveThread;
                this.inputMessageSize = inputMessageSize;
            }
        }

        private static final class CommonFileTransfer {

            String fileName;
            long size;
            long duration;

            private CommonFileTransfer(String fileName, long size, long duration) {
                this.fileName = fileName;
                this.size = size;
                this.duration = duration;
            }
        }

        private interface Handler {

            void onTaskEnd(TaskExecution task, JobExecution job);

            void onCommonFileTransfer(CommonFileTransfer commonFileTransfer);

        }

        private abstract static class AbstractHandler implements Handler {

            @Override
            public void onTaskEnd(TaskExecution task, JobExecution job) {
            }

            @Override
            public void onCommonFileTransfer(CommonFileTransfer commonFileTransfer) {
            }

        }

        private StatisticsReader(Path tasksCsv) throws IOException {
            reader = Files.newBufferedReader(tasksCsv, StandardCharsets.UTF_8);
        }

        private void read(Handler handler) throws IOException {
            Splitter blankSplitter = Splitter.on(' ');
            MapSplitter mapSplitter = blankSplitter.withKeyValueSeparator('=');
            String line;
            int jobId;
            int taskId;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                String[] tokens = line.split(CSV_SEPARATOR);
                if (tokens.length < 1) {
                    throw new PowsyblException("Cannot detect line key");
                }
                String key = tokens[0];
                switch (key) {
                    case COMMON_FILE_TRANSFER_KEY:
                        checkTokenSize(4, tokens.length, line, key);
                        String fileName = tokens[1];
                        long size = Long.parseLong(tokens[2]);
                        long duration = Long.parseLong(tokens[3]);
                        handler.onCommonFileTransfer(new CommonFileTransfer(fileName, size, duration));
                        break;

                    case JOB_START_KEY:
                        checkTokenSize(3, 4, tokens.length, line, key);
                        jobId = Integer.parseInt(tokens[1]);
                        String commandId = tokens[2];
                        Map<String, String> tags = null;
                        if (tokens.length == 4) {
                            tags = mapSplitter.split(tokens[3]);
                        }
                        jobs.put(jobId, new JobExecution(jobId, commandId, tags));
                        break;

                    case JOB_END_KEY:
                        checkTokenSize(2, tokens.length, line, key);
                        jobId = Integer.parseInt(tokens[1]);
                        jobs.remove(jobId);
                        break;

                    case TASK_START_KEY:
                        checkTokenSize(8, tokens.length, line, key);
                        taskId = Integer.parseInt(tokens[1]);
                        jobId = Integer.parseInt(tokens[2]);
                        int taskIndex = Integer.parseInt(tokens[3]);
                        DateTime startTime = DateTime.parse(tokens[4]);
                        int slaveRank = Integer.parseInt(tokens[5]);
                        int slaveThread = Integer.parseInt(tokens[6]);
                        long inputMessageSize = Long.parseLong(tokens[7]);
                        tasks.put(taskId, new TaskExecution(taskId, jobId, taskIndex, startTime, slaveRank, slaveThread, inputMessageSize));
                        break;

                    case TASK_END_KEY:
                        checkTokenSize(8, tokens.length, line, key);
                        taskId = Integer.parseInt(tokens[1]);
                        TaskExecution task = tasks.get(taskId);
                        task.taskDuration = Long.parseLong(tokens[2]);
                        task.commandsDuration = new ArrayList<>();
                        for (String s : blankSplitter.split(tokens[3])) {
                            task.commandsDuration.add(Long.parseLong(s));
                        }
                        task.dataTransferDuration = Long.parseLong(tokens[4]);
                        task.outputMessageSize = Long.parseLong(tokens[5]);
                        task.workingDataSize = Long.parseLong(tokens[6]);
                        task.exitCode = Integer.parseInt(tokens[7]);

                        JobExecution job = jobs.get(task.jobId);

                        handler.onTaskEnd(task, job);

                        tasks.remove(taskId);
                        break;

                    default:
                        throw new PowsyblException("Unknown key " + key);
                }
            }
        }

        private static PowsyblException createIncorrectLineException(String key, String line) {
            return new PowsyblException("Incorrect " + key + " line '" + line + "'");
        }

        private static void checkTokenSize(int expected, int actual, String line, String key) {
            if (actual != expected) {
                throw createIncorrectLineException(key, line);
            }
        }

        private static void checkTokenSize(int expected1, int expected2, int actual, String line, String key) {
            if (actual != expected1 && actual != expected2) {
                throw createIncorrectLineException(key, line);
            }
        }

        @Override
        public void close() throws IOException {
            reader.close();
        }

    }

    public CsvMpiStatistics(Path dbDir, String dbName) throws IOException {
        Objects.requireNonNull(dbDir);
        Objects.requireNonNull(dbName);
        this.dbDir = dbDir;
        this.dbName = dbName;
        Path csvFile = dbDir.resolve(dbName + ".csv");
        // if file already exists, create a backup
        if (Files.exists(csvFile)) {
            int i = 1;
            Path savCsvFile;
            do {
                savCsvFile = csvFile.getParent().resolve(csvFile.getFileName() + "." + i++);
            } while (Files.exists(savCsvFile));
            Files.move(csvFile, savCsvFile);
        }
        internalWriter = Files.newBufferedWriter(csvFile, StandardCharsets.UTF_8);
    }

    @Override
    public void logCommonFileTransfer(String fileName, int chunk, long size, long duration) {
        try {
            internalWriter.write(COMMON_FILE_TRANSFER_KEY);
            internalWriter.write(CSV_SEPARATOR);
            internalWriter.write(fileName);
            internalWriter.write(CSV_SEPARATOR);
            internalWriter.write(Long.toString(size));
            internalWriter.write(CSV_SEPARATOR);
            internalWriter.write(Long.toString(duration));
            internalWriter.newLine();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void logJobStart(int jobId, String commandId, Map<String, String> tags) {
        try {
            internalWriter.write(JOB_START_KEY);
            internalWriter.write(CSV_SEPARATOR);
            internalWriter.write(Integer.toString(jobId));
            internalWriter.write(CSV_SEPARATOR);
            internalWriter.write(commandId);
            internalWriter.write(CSV_SEPARATOR);
            internalWriter.write(tags != null ? mapJoiner.join(tags) : "");
            internalWriter.newLine();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void logJobEnd(int jobId) {
        try {
            internalWriter.write(JOB_END_KEY);
            internalWriter.write(CSV_SEPARATOR);
            internalWriter.write(Integer.toString(jobId));
            internalWriter.newLine();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void logTaskStart(int taskId, int jobId, int taskIndex, DateTime startTime, int slaveRank, int slaveThread, long inputMessageSize) {
        try {
            internalWriter.write(TASK_START_KEY);
            internalWriter.write(CSV_SEPARATOR);
            internalWriter.write(Integer.toString(taskId));
            internalWriter.write(CSV_SEPARATOR);
            internalWriter.write(Integer.toString(jobId));
            internalWriter.write(CSV_SEPARATOR);
            internalWriter.write(Integer.toString(taskIndex));
            internalWriter.write(CSV_SEPARATOR);
            internalWriter.write(startTime.toString());
            internalWriter.write(CSV_SEPARATOR);
            internalWriter.write(Integer.toString(slaveRank));
            internalWriter.write(CSV_SEPARATOR);
            internalWriter.write(Integer.toString(slaveThread));
            internalWriter.write(CSV_SEPARATOR);
            internalWriter.write(Long.toString(inputMessageSize));
            internalWriter.newLine();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void logTaskEnd(int taskId, long taskDuration, List<Long> commandsDuration, long dataTransferDuration, long outputMessageSize, long workingDataSize, int exitCode) {
        try {
            internalWriter.write(TASK_END_KEY);
            internalWriter.write(CSV_SEPARATOR);
            internalWriter.write(Integer.toString(taskId));
            internalWriter.write(CSV_SEPARATOR);
            internalWriter.write(Long.toString(taskDuration));
            internalWriter.write(CSV_SEPARATOR);
            internalWriter.write(blankJoiner.join(commandsDuration));
            internalWriter.write(CSV_SEPARATOR);
            internalWriter.write(Long.toString(dataTransferDuration));
            internalWriter.write(CSV_SEPARATOR);
            internalWriter.write(Long.toString(outputMessageSize));
            internalWriter.write(CSV_SEPARATOR);
            internalWriter.write(Long.toString(workingDataSize));
            internalWriter.write(CSV_SEPARATOR);
            internalWriter.write(Integer.toString(exitCode));
            internalWriter.newLine();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void exportTasksToCsv(final Writer writer) {
        try {
            Path csv = dbDir.resolve(dbName + ".csv");

            writer.write("Task Id " + CSV_SEPARATOR +
                         "Job Id" + CSV_SEPARATOR +
                         "Task index" + CSV_SEPARATOR +
                         "Command Id" + CSV_SEPARATOR +
                         "Tags" + CSV_SEPARATOR +
                         "Start time" + CSV_SEPARATOR +
                         "Slave rank" + CSV_SEPARATOR +
                         "Slave thread" + CSV_SEPARATOR +
                         "Input message size (bytes)" + CSV_SEPARATOR +
                         "Task duration (ms)" + CSV_SEPARATOR +
                         "Commands duration (ms)" + CSV_SEPARATOR +
                         "Data transfer duration (ms)" + CSV_SEPARATOR +
                         "Output message size (bytes)" + CSV_SEPARATOR +
                         "Working data size (bytes)" + CSV_SEPARATOR +
                         "Exit code");
            writer.write("\n");

            try (StatisticsReader reader = new StatisticsReader(csv)) {
                reader.read(new StatisticsReader.AbstractHandler() {

                    @Override
                    public void onTaskEnd(StatisticsReader.TaskExecution task, StatisticsReader.JobExecution job) {
                        try {
                            String taskDuration = Objects.toString(task.taskDuration, "");
                            String commandsDuration = "";
                            if (task.commandsDuration != null) {
                                commandsDuration = blankJoiner.join(task.commandsDuration);
                            }
                            String dataTransferDuration = Objects.toString(task.dataTransferDuration, "");
                            String outputMessageSize = Objects.toString(task.outputMessageSize, "");
                            String workingDataSize = Objects.toString(task.workingDataSize, "");
                            String exitCode = Objects.toString(task.exitCode, "");
                            writer.write(task.taskId + CSV_SEPARATOR +
                                         task.jobId + CSV_SEPARATOR +
                                         task.taskIndex + CSV_SEPARATOR +
                                         job.commandId + CSV_SEPARATOR +
                                         (job.tags != null ? mapJoiner.join(job.tags) : "") + CSV_SEPARATOR +
                                         task.startTime.toString("dd/MM/YYYY HH:mm:ss") + CSV_SEPARATOR +
                                         task.slaveRank + CSV_SEPARATOR +
                                         task.slaveThread + CSV_SEPARATOR +
                                         task.inputMessageSize + CSV_SEPARATOR +
                                         taskDuration + CSV_SEPARATOR +
                                         commandsDuration + CSV_SEPARATOR +
                                         dataTransferDuration + CSV_SEPARATOR +
                                         outputMessageSize + CSV_SEPARATOR +
                                         workingDataSize + CSV_SEPARATOR +
                                         exitCode);
                            writer.write("\n");
                        } catch (IOException e) {
                            LOGGER.error(e.toString(), e);
                        }
                    }
                });
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static void exportCommonFileTransferDuration(Path dbDir, String dbName) throws IOException {
        Objects.requireNonNull(dbDir);
        Objects.requireNonNull(dbName);

        Path csv = dbDir.resolve(dbName + ".csv");

        Path commonFilesTransferCsv = dbDir.resolve("common-files-transfer.csv");

        logWritingPath(commonFilesTransferCsv);

        try (BufferedWriter commonFilesTransferWriter = Files.newBufferedWriter(commonFilesTransferCsv, StandardCharsets.UTF_8)) {
            commonFilesTransferWriter.write("File name" + CSV_SEPARATOR + "File size (bytes)" + CSV_SEPARATOR + "Transfer duration (ms)");
            commonFilesTransferWriter.newLine();

            try (StatisticsReader reader = new StatisticsReader(csv)) {
                reader.read(new StatisticsReader.AbstractHandler() {

                    @Override
                    public void onCommonFileTransfer(StatisticsReader.CommonFileTransfer transfer) {
                        try {
                            commonFilesTransferWriter.write(transfer.fileName + CSV_SEPARATOR + transfer.size + CSV_SEPARATOR + transfer.duration);
                            commonFilesTransferWriter.newLine();
                        } catch (IOException e) {
                            LOGGER.error(e.toString(), e);
                        }
                    }
                });
            }
        }
    }

    private static void logWritingPath(Path path) {
        LOGGER.info("Writing {}", path);
    }

    public static void exportTaskCount(Path dbDir, String dbName) throws IOException {
        Objects.requireNonNull(dbDir);
        Objects.requireNonNull(dbName);

        Path csv = dbDir.resolve(dbName + ".csv");

        class CommandStats {
            int count = 0;
            int ok = 0;
        }

        Path tasksCountCsv = dbDir.resolve("tasks-count.csv");
        logWritingPath(tasksCountCsv);

        final Map<String, CommandStats> tasksPerCommandId = new HashMap<>();
        try (StatisticsReader reader = new StatisticsReader(csv)) {
            reader.read(new StatisticsReader.AbstractHandler() {

                @Override
                public void onTaskEnd(StatisticsReader.TaskExecution task, StatisticsReader.JobExecution job) {
                    CommandStats stats = tasksPerCommandId.get(job.commandId);
                    if (stats == null) {
                        stats = new CommandStats();
                        tasksPerCommandId.put(job.commandId, stats);
                    }
                    stats.count++;
                    if (task.exitCode == 0) {
                        stats.ok++;
                    }
                }

            });
        }

        try (BufferedWriter writer = Files.newBufferedWriter(tasksCountCsv, StandardCharsets.UTF_8)) {
            writer.write("Command Id" + CSV_SEPARATOR + "Executions" + CSV_SEPARATOR + "OK rate");
            writer.newLine();
            for (Map.Entry<String, CommandStats> entry : tasksPerCommandId.entrySet()) {
                String commandId = entry.getKey();
                CommandStats stats = entry.getValue();
                writer.write(commandId + CSV_SEPARATOR + stats.count + CSV_SEPARATOR + ((float) stats.ok / stats.count));
                writer.newLine();
            }
        }
    }

    public static void exportBusyCores(Path dbDir, String dbName) throws IOException {
        Objects.requireNonNull(dbDir);
        Objects.requireNonNull(dbName);

        Path csv = dbDir.resolve(dbName + ".csv");

        Path busyCoresCsv = dbDir.resolve("busy-cores.csv");
        logWritingPath(busyCoresCsv);

        final DateTime[] min = new DateTime[1];
        final DateTime[] max = new DateTime[1];
        try (StatisticsReader reader = new StatisticsReader(csv)) {
            reader.read(new StatisticsReader.AbstractHandler() {

                @Override
                public void onTaskEnd(StatisticsReader.TaskExecution task, StatisticsReader.JobExecution job) {
                    if (min[0] == null || task.startTime.compareTo(min[0]) < 0) {
                        min[0] = task.startTime;
                    }
                    if (task.taskDuration != null) {
                        DateTime endTime = task.startTime.plusMillis((int) (long) task.taskDuration);
                        if (max[0] == null || endTime.compareTo(max[0]) > 0) {
                            max[0] = endTime;
                        }
                    }
                }

            });
        }

        final int secs = (int) new Duration(min[0], max[0]).getStandardSeconds() + 1;
        final int[] busyCores = new int[secs];
        Arrays.fill(busyCores, 0);
        try (StatisticsReader reader = new StatisticsReader(csv)) {
            reader.read(new StatisticsReader.AbstractHandler() {

                @Override
                public void onTaskEnd(StatisticsReader.TaskExecution task, StatisticsReader.JobExecution job) {
                    int s1 = (int) new Duration(min[0], task.startTime).getStandardSeconds();
                    int s2;
                    if (task.taskDuration != null) {
                        s2 = s1 + (int) ((float) task.taskDuration / 1000);
                    } else {
                        s2 = secs - 1;
                    }
                    for (int s = s1; s <= s2; s++) {
                        busyCores[s]++;
                    }
                }

            });
        }
        try (BufferedWriter writer = Files.newBufferedWriter(busyCoresCsv, StandardCharsets.UTF_8)) {
            writer.write("Second" + CSV_SEPARATOR + "Busy cores");
            writer.newLine();
            for (int s = 0; s < busyCores.length; s++) {
                writer.write(Integer.toString(s));
                writer.write(CSV_SEPARATOR);
                writer.write(Integer.toString(busyCores[s]));
                writer.newLine();
            }
        }
    }

    public static void exportWorkingDataSize(Path dbDir, String dbName) throws IOException {
        Objects.requireNonNull(dbDir);
        Objects.requireNonNull(dbName);

        Path csv = dbDir.resolve(dbName + ".csv");

        Path workingDataSizeCsv = dbDir.resolve("working-data-size.csv");
        logWritingPath(workingDataSizeCsv);

        final Map<String, AtomicLong> workingDataSizePerSlave = new HashMap<>();
        final long[] totalWorkingDataSize = new long[1];
        try (StatisticsReader reader = new StatisticsReader(csv)) {
            reader.read(new StatisticsReader.AbstractHandler() {

                @Override
                public void onTaskEnd(StatisticsReader.TaskExecution task, StatisticsReader.JobExecution job) {
                    if (task.workingDataSize != null) {
                        String slaveId = task.slaveRank + "_" + task.slaveThread;
                        AtomicLong workingDataSize = workingDataSizePerSlave.computeIfAbsent(slaveId, k -> new AtomicLong());
                        workingDataSize.addAndGet(task.workingDataSize);
                        totalWorkingDataSize[0] += task.workingDataSize;
                    }
                }

            });
        }

        try (BufferedWriter writer = Files.newBufferedWriter(workingDataSizeCsv, StandardCharsets.UTF_8)) {
            writer.write("Slave" + CSV_SEPARATOR + "Working data size (bytes)");
            writer.newLine();
            for (Map.Entry<String, AtomicLong> entry : workingDataSizePerSlave.entrySet()) {
                String slaveId = entry.getKey();
                long workingDataSize = entry.getValue().get();
                writer.write(slaveId + CSV_SEPARATOR + workingDataSize);
                writer.newLine();
            }
            writer.write("Total" + CSV_SEPARATOR + totalWorkingDataSize[0]);
            writer.newLine();
        }
    }

    @Override
    public void close() {
        try {
            internalWriter.close();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

}

