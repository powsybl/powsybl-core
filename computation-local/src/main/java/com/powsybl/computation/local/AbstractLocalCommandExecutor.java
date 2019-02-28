/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.computation.local;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public abstract class AbstractLocalCommandExecutor implements LocalCommandExecutor {

    protected final Map<Path, Process> processMap = new HashMap<>();
    protected final ReadWriteLock lock = new ReentrantReadWriteLock();

    protected static final String NON_ZERO_LOG_PATTERN = "Command '{}' has failed (exitValue={})";

    private static long DEFAULT_FAST_POLLING = 500_000_000; // 0.5 seconds
    private static long BEGINING_NANO = 3_000_000_000L; // 3 seconds

    @Override
    public void stop(Path workingDir) {
        Process process = getProcessByWorkingDir(workingDir);
        if (process != null) {
            process.destroy(); // kill -15 PID
        }
    }

    @Override
    public void stopForcibly(Path workingDir) throws InterruptedException {
        Process process = getProcessByWorkingDir(workingDir);
        if (process != null) {
            Process killProcess = process.destroyForcibly(); // kill -9 PID
            killProcess.waitFor();
        }
    }

    private Process getProcessByWorkingDir(Path workingDir) {
        try {
            lock.readLock().lock();
            return processMap.get(workingDir);
        } finally {
            lock.readLock().unlock();
        }
    }

    protected int execute(List<String> cmdLs, Path workingDir, Path outFile, Path errFile, long timeout) throws IOException, InterruptedException {
        ProcessBuilder.Redirect outRedirect = ProcessBuilder.Redirect.appendTo(outFile.toFile());
        ProcessBuilder.Redirect errRedirect = ProcessBuilder.Redirect.appendTo(errFile.toFile());
        Process process = new ProcessBuilder(cmdLs)
                .directory(workingDir.toFile())
                .redirectOutput(outRedirect)
                .redirectError(errRedirect)
                .start();

        long startTime = System.nanoTime();

        try {
            lock.writeLock().lock();
            processMap.put(workingDir, process);
        } finally {
            lock.writeLock().unlock();
        }

        int exitCode;
        if (timeout == -1) {
            exitCode = process.waitFor();
        } else {
            while (true) {
                int tried = 0;
                try {
                    exitCode = process.exitValue();
                    closeStream(process);
                    if (exitCode != 0) {
                        nonZeroLog(cmdLs, exitCode);
                    }
                    return exitCode;
                } catch (IllegalThreadStateException ex) {
                    long running = System.nanoTime() - startTime;
                    if (running > TimeUnit.SECONDS.toNanos(timeout)) {
                        break;
                    }
                    TimeUnit.NANOSECONDS.sleep(smartWait(running, startTime, timeout));
                }
            }
            process.destroy();
            exitCode = 124;
        }

        closeStream(process);
        if (exitCode != 0) {
            nonZeroLog(cmdLs, exitCode);
        }
        return exitCode;
    }

    private static long smartWait(long running, long startTime, long timeout) {
        if (running < BEGINING_NANO || running > 0.8 * TimeUnit.SECONDS.toNanos(timeout)) {
            return DEFAULT_FAST_POLLING;
        }
        return (long) (TimeUnit.SECONDS.toNanos(timeout) * 0.8) - running;
    }

    private void closeStream(Process process) throws IOException {
        // to avoid 'too many open files' exception
        process.getInputStream().close();
        process.getOutputStream().close();
        process.getErrorStream().close();
    }

    abstract void nonZeroLog(List<String> cmdLs, int exitCode);
}
