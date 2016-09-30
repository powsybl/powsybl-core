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
import eu.itesla_project.offline.db.util.MemoryMappedFileFactory;
import eu.itesla_project.offline.db.util.PersistentCounter;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class OfflineDbTable implements AutoCloseable {

    private static final String SAMPLE_COUNT_FILE_NAME = "sample-count";

    private final Path workflowDir;

    private final OfflineDbTableDescription description;

    private final MemoryMappedFileFactory memoryMappedFileFactory;

    private final PersistentCounter sampleCount;

    private final List<OfflineDbTableChunk> openedTableChunks = new ArrayList<>();

    private final Lock tableChunkLock = new ReentrantLock();

    public OfflineDbTable(Path workflowDir, OfflineDbTableDescription description, MemoryMappedFileFactory memoryMappedFileFactory) throws IOException {
        this.workflowDir = workflowDir;
        this.description = description;
        this.memoryMappedFileFactory = memoryMappedFileFactory;
        sampleCount = new PersistentCounter(memoryMappedFileFactory.create(workflowDir.resolve(SAMPLE_COUNT_FILE_NAME)), 0);
    }

    private int getChunk(int sample) {
        return sample / description.getSampleChunkSize();
    }

    private int getSampleInChunk(int sample) {
        return sample % description.getSampleChunkSize();
    }

    private OfflineDbTableChunk getTableChunk(int sample) throws IOException {
        int chunk = getChunk(sample);
        tableChunkLock.lock();
        try {
            if (chunk >= openedTableChunks.size()) {
                for (int i = openedTableChunks.size(); i <= chunk; i++) {
                    openedTableChunks.add(new OfflineDbTableChunk(workflowDir, description, memoryMappedFileFactory, chunk));
                }
            }
            return openedTableChunks.get(chunk);
        } finally {
            tableChunkLock.unlock();
        }
    }

    PersistentCounter getSampleCount() {
        return sampleCount;
    }

    OfflineDbTableDescription getDescription() {
        return description;
    }

    public void writeTaskStatus(int sample, OfflineTaskType taskType, OfflineTaskStatus taskStatus) throws IOException {
        getTableChunk(sample).writeTaskStatus(getSampleInChunk(sample), taskType, taskStatus);
    }

    public void writeSecurityIndex(int sample, SecurityIndexId securityIndexId, boolean ok) throws IOException {
        getTableChunk(sample).writeSecurityIndex(getSampleInChunk(sample), securityIndexId, ok);
    }

    void writeSecurityIndex(int sample, SecurityIndex securityIndex) throws IOException {
        getTableChunk(sample).writeSecurityIndex(getSampleInChunk(sample), securityIndex);
    }

    void writeSecurityIndexes(int sample, Collection<SecurityIndex> securityIndexes) throws IOException {
        getTableChunk(sample).writeSecurityIndexes(getSampleInChunk(sample), securityIndexes);
    }

    public void writeNetworkAttributeValue(int sample, HistoDbNetworkAttributeId attributeId, float value) throws IOException {
        getTableChunk(sample).writeNetworkAttributeValue(getSampleInChunk(sample), attributeId, value);
    }

    void writeNetworkAttributesValue(int sample, Map<HistoDbNetworkAttributeId, Float> values) throws IOException {
        getTableChunk(sample).writeNetworkAttributesValue(getSampleInChunk(sample), values);
    }

    OfflineTaskStatus getTaskStatus(int sample, OfflineTaskType taskType) throws IOException {
        return getTableChunk(sample).getTaskStatus(getSampleInChunk(sample), taskType);
    }

    void getTasksStatus(int sample, Map<OfflineTaskType, OfflineTaskStatus> tasksStatus) throws IOException {
        getTableChunk(sample).getTasksStatus(getSampleInChunk(sample), tasksStatus);
    }

    Boolean isSecurityIndexOk(int sample, SecurityIndexId securityIndexId) throws IOException {
        return getTableChunk(sample).isSecurityIndexOk(getSampleInChunk(sample), securityIndexId);
    }

    void getSecurityIndexesOk(int sample, Map<SecurityIndexId, Boolean> securityIndexesOk) throws IOException {
        getTableChunk(sample).getSecurityIndexesOk(getSampleInChunk(sample), securityIndexesOk);
    }

    float getNetworkAttributeValue(int sample, HistoDbNetworkAttributeId attributeId) throws IOException {
        return getTableChunk(sample).getNetworkAttributeValue(getSampleInChunk(sample), attributeId);
    }

    void getNetworkAttributesValue(int sample, Map<HistoDbNetworkAttributeId, Float> networkAttributesValue) throws IOException {
        getTableChunk(sample).getNetworkAttributesValue(getSampleInChunk(sample), networkAttributesValue);
    }

    @Override
    public void close() throws IOException {
        tableChunkLock.lock();
        try {
            for (OfflineDbTableChunk chunk : openedTableChunks) {
                chunk.close();
            }
            openedTableChunks.clear();
        } finally {
            tableChunkLock.unlock();
        }
    }
}
