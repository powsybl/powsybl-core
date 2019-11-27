/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.storage.buffer;

import com.powsybl.timeseries.DoubleDataChunk;
import com.powsybl.timeseries.StringDataChunk;
import com.powsybl.timeseries.TimeSeriesMetadata;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class StorageChangeBuffer {

    private final StorageChangeSet changeSet = new StorageChangeSet();

    private final Lock lock = new ReentrantLock();

    private final StorageChangeFlusher flusher;

    private final int maximumChange;

    private final long maximumSize;

    public StorageChangeBuffer(StorageChangeFlusher flusher, int maximumChange, long maximumSize) {
        this.flusher = Objects.requireNonNull(flusher);
        if (maximumChange <= 0) {
            throw new IllegalArgumentException("Bad buffer maximum change " + maximumChange);
        }
        if (maximumSize <= 0) {
            throw new IllegalArgumentException("Bad buffer maximum size " + maximumSize);
        }
        this.maximumChange = maximumChange;
        this.maximumSize = maximumSize;
    }

    private void addChange(StorageChange change) {
        changeSet.getChanges().add(change);
        if (changeSet.getChanges().size() >= maximumChange ||
                changeSet.getEstimatedSize() >= maximumSize) {
            flush();
        }
    }

    public void createTimeSeries(String nodeId, TimeSeriesMetadata metadata) {
        Objects.requireNonNull(nodeId);
        Objects.requireNonNull(metadata);
        lock.lock();
        try {
            addChange(new TimeSeriesCreation(nodeId, metadata));
        } finally {
            lock.unlock();
        }
    }

    public void addDoubleTimeSeriesData(String nodeId, int version, String timeSeriesName, List<DoubleDataChunk> chunks) {
        lock.lock();
        try {
            addChange(new DoubleTimeSeriesChunksAddition(nodeId, version, timeSeriesName, chunks));
        } finally {
            lock.unlock();
        }
    }

    public void addStringTimeSeriesData(String nodeId, int version, String timeSeriesName, List<StringDataChunk> chunks) {
        lock.lock();
        try {
            addChange(new StringTimeSeriesChunksAddition(nodeId, version, timeSeriesName, chunks));
        } finally {
            lock.unlock();
        }
    }

    public boolean isEmpty() {
        lock.lock();
        try {
            return changeSet.getChanges().isEmpty();
        } finally {
            lock.unlock();
        }
    }

    public void flush() {
        lock.lock();
        try {
            flusher.flush(changeSet);
        } finally {
            changeSet.getChanges().clear();
            lock.unlock();
        }
    }
}
