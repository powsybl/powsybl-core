/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.offline.db.mmap;

import eu.itesla_project.modules.histo.HistoDbNetworkAttributeId;
import eu.itesla_project.modules.offline.OfflineTaskStatus;
import eu.itesla_project.modules.offline.OfflineTaskType;
import eu.itesla_project.simulation.securityindexes.SecurityIndex;
import eu.itesla_project.simulation.securityindexes.SecurityIndexId;
import eu.itesla_project.offline.db.util.MemoryMappedFile;
import eu.itesla_project.offline.db.util.MemoryMappedFileFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class OfflineDbTableChunk implements AutoCloseable {

    private final OfflineDbTableDescription description;

    private final MemoryMappedFile memoryMappedFile;

    private final ByteBuffer buffer;

    private final Lock bufferLock = new ReentrantLock();

    OfflineDbTableChunk(Path workflowDir, OfflineDbTableDescription description, MemoryMappedFileFactory memoryMappedFileFactory, int chunk) throws IOException {
        this.description = description;
        memoryMappedFile = memoryMappedFileFactory.create(workflowDir.resolve("offlinedb." + chunk));
        boolean exists = memoryMappedFile.exists();
        buffer = memoryMappedFile.getBuffer(description.getRowSize() * description.getSampleChunkSize());
        if (!exists) {
            resetBuffer();
        }
    }

    private void resetBuffer() {
        // init the buffer with default values
        for (int sampleId = 0; sampleId < description.getSampleChunkSize(); sampleId++) {
            for (OfflineTaskType ignored : OfflineTaskType.values()) {
                buffer.put((byte) -1);
            }
            for (int i = 0; i < description.getMaxSecurityIndexesCount(); i++) {
                buffer.put((byte) -1);
            }
            for (int i = 0; i < description.getMaxNetworkAttributesCount(); i++) {
                buffer.putFloat(Float.NaN);
            }
        }

        buffer.rewind();
    }

    void writeTaskStatus(int sample, OfflineTaskType taskType, OfflineTaskStatus taskStatus) {
        bufferLock.lock();
        try {
            buffer.put(description.getTaskTypeBufferPosition(sample, taskType), (byte) taskStatus.ordinal());
        } finally {
            bufferLock.unlock();
        }
    }

    private static byte okToByte(boolean ok) {
        return ok ? (byte) 1 : 0;
    }

    void writeSecurityIndex(int sample, SecurityIndexId securityIndexId, boolean ok) {
        bufferLock.lock();
        try {
            buffer.put(description.getSecurityIndexBufferPosition(sample, securityIndexId), okToByte(ok));
        } finally {
            bufferLock.unlock();
        }
    }

    void writeSecurityIndex(int sample, SecurityIndex securityIndex) {
        writeSecurityIndex(sample, securityIndex.getId(), securityIndex.isOk());
    }

    void writeSecurityIndexes(int sample, Collection<SecurityIndex> securityIndexes) {
        bufferLock.lock();
        try {
            for (SecurityIndex securityIndex : securityIndexes) {
                buffer.put(description.getSecurityIndexBufferPosition(sample, securityIndex.getId()), okToByte(securityIndex.isOk()));
            }
        } finally {
            bufferLock.unlock();
        }
    }

    void writeNetworkAttributeValue(int sample, HistoDbNetworkAttributeId attributeId, float value) {
        bufferLock.lock();
        try {
            buffer.putFloat(description.getNetworkAttributeBufferPosition(sample, attributeId), value);
        } finally {
            bufferLock.unlock();
        }
    }

    void writeNetworkAttributesValue(int sample, Map<HistoDbNetworkAttributeId, Float> values) {
        bufferLock.lock();
        try {
            for (Map.Entry<HistoDbNetworkAttributeId, Float> entry : values.entrySet()) {
                HistoDbNetworkAttributeId attributeId = entry.getKey();
                float value = entry.getValue();
                buffer.putFloat(description.getNetworkAttributeBufferPosition(sample, attributeId), value);
            }
        } finally {
            bufferLock.unlock();
        }
    }

    private static OfflineTaskStatus intToTaskStatus(int i) {
        return i == -1 ? null : OfflineTaskStatus.values()[i];
    }

    OfflineTaskStatus getTaskStatus(int sample, OfflineTaskType taskType) {
        bufferLock.lock();
        try {
            int i = buffer.get(description.getTaskTypeBufferPosition(sample, taskType));
            return intToTaskStatus(i);
        } finally {
            bufferLock.unlock();
        }
    }

    void getTasksStatus(int sample, Map<OfflineTaskType, OfflineTaskStatus> tasksStatus) {
        bufferLock.lock();
        try {
            for (OfflineTaskType taskType : OfflineTaskType.values()) {
                int i = buffer.get(description.getTaskTypeBufferPosition(sample, taskType));
                tasksStatus.put(taskType, intToTaskStatus(i));
            }
        } finally {
            bufferLock.unlock();
        }
    }

    private static Boolean intToSecurityIndexOk(int i) {
        switch (i) {
            case -1: return null;
            case 0: return Boolean.FALSE;
            case 1: return Boolean.TRUE;
            default: throw new AssertionError();
        }
    }

    Boolean isSecurityIndexOk(int sample, SecurityIndexId securityIndexId) {
        bufferLock.lock();
        try {
            return intToSecurityIndexOk(buffer.get(description.getSecurityIndexBufferPosition(sample, securityIndexId)));
        } finally {
            bufferLock.unlock();
        }
    }

    void getSecurityIndexesOk(int sample, Map<SecurityIndexId, Boolean> securityIndexesOk) {
        bufferLock.lock();
        try {
            for (Map.Entry<SecurityIndexId, Boolean> e : securityIndexesOk.entrySet()) {
                SecurityIndexId securityIndexId = e.getKey();
                Boolean ok = intToSecurityIndexOk(buffer.get(description.getSecurityIndexBufferPosition(sample, securityIndexId)));
                e.setValue(ok);
            }
        } finally {
            bufferLock.unlock();
        }
    }

    float getNetworkAttributeValue(int sample, HistoDbNetworkAttributeId attributeId) {
        bufferLock.lock();
        try {
            return buffer.getFloat(description.getNetworkAttributeBufferPosition(sample, attributeId));
        } finally {
            bufferLock.unlock();
        }
    }

    void getNetworkAttributesValue(int sample, Map<HistoDbNetworkAttributeId, Float> networkAttributesValue) {
        bufferLock.lock();
        try {
            for (Map.Entry<HistoDbNetworkAttributeId, Float> e : networkAttributesValue.entrySet()) {
                HistoDbNetworkAttributeId attributeId = e.getKey();
                float value = buffer.getFloat(description.getNetworkAttributeBufferPosition(sample, attributeId));
                e.setValue(value);
            }
        } finally {
            bufferLock.unlock();
        }
    }

    @Override
    public void close() throws IOException {
        memoryMappedFile.close();
    }
}
