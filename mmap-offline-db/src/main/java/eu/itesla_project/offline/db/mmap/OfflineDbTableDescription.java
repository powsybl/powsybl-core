/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.offline.db.mmap;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import eu.itesla_project.modules.histo.HistoDbAttributeIdParser;
import eu.itesla_project.modules.histo.HistoDbNetworkAttributeId;
import eu.itesla_project.modules.offline.OfflineTaskStatus;
import eu.itesla_project.modules.offline.OfflineTaskType;
import eu.itesla_project.modules.securityindexes.SecurityIndexId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class OfflineDbTableDescription {

    private static final Logger LOGGER = LoggerFactory.getLogger(OfflineDbTableDescription.class);

    private static final String TABLE_DESCRIPTION_FILE_NAME = "table-description.json";
    private static final int TASK_STATUS_CELL_SIZE = Byte.BYTES;
    private static final int SECURITY_INDEX_CELL_SIZE = Byte.BYTES;
    private static final int NETWORK_ATTRIBUTE_CELL_SIZE = Float.BYTES;

    private final int sampleChunkSize;
    private final int maxSecurityIndexesCount;
    private final int maxNetworkAttributesCount;

    private final List<SecurityIndexId> securityIndexes;
    private final Map<SecurityIndexId, Integer> securityIndexesColumnIndex;
    private final Lock securityIndexesLock = new ReentrantLock();

    private final List<HistoDbNetworkAttributeId> networkAttributes;
    private final Map<HistoDbNetworkAttributeId, Integer> networkAttributesColumnIndex;
    private final Lock networkAttributesLock = new ReentrantLock();

    private volatile boolean changed = false;

    static OfflineDbTableDescription load(Path workflowDir, MMapOfflineDbConfig config) throws IOException {
        int sampleChunkSize = config.getSampleChunkSize();
        int maxSecurityIndexesCount = config.getMaxSecurityIndexesCount();
        int maxNetworkAttributesCount = config.getMaxNetworkAttributesCount();
        List<SecurityIndexId> securityIndexes = new ArrayList<>();
        List<HistoDbNetworkAttributeId> networkAttributes = new ArrayList<>();

        Path file = workflowDir.resolve(TABLE_DESCRIPTION_FILE_NAME);
        boolean exists = Files.exists(file);
        if (exists) {
            try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8);
                 JsonParser parser = MMapOfflineDb.JSON_FACTORY.get().createParser(reader)) {
                parser.nextToken();
                while (parser.nextToken() != JsonToken.END_OBJECT) {
                    String fieldname = parser.getCurrentName();
                    switch (fieldname) {
                        case "sampleChunkSize":
                            parser.nextToken();
                            sampleChunkSize = parser.getIntValue();
                            break;

                        case "maxSecurityIndexesCount":
                            parser.nextToken();
                            maxSecurityIndexesCount = parser.getIntValue();
                            break;

                        case "maxNetworkAttributesCount":
                            parser.nextToken();
                            maxNetworkAttributesCount = parser.getIntValue();
                            break;

                        case "securityIndexes":
                            parser.nextToken();
                            while (parser.nextToken() != JsonToken.END_ARRAY) {
                                securityIndexes.add(SecurityIndexId.fromString(parser.getText()));
                            }
                            break;

                        case "networkAttributes":
                            parser.nextToken();
                            while (parser.nextToken() != JsonToken.END_ARRAY) {
                                networkAttributes.add((HistoDbNetworkAttributeId) HistoDbAttributeIdParser.parse(parser.getText()));
                            }
                            break;

                        default:
                            throw new AssertionError();
                    }
                }
            }
        }

        OfflineDbTableDescription tableDescription =  new OfflineDbTableDescription(sampleChunkSize, maxSecurityIndexesCount, maxNetworkAttributesCount, securityIndexes, networkAttributes);
        if (!exists) {
            tableDescription.save(workflowDir);
        }
        return tableDescription;
    }

    OfflineDbTableDescription(int sampleChunkSize, int maxSecurityIndexesCount, int maxNetworkAttributesCount) {
        this(sampleChunkSize, maxSecurityIndexesCount, maxNetworkAttributesCount, new ArrayList<>(), new ArrayList<>());
    }

    public OfflineDbTableDescription(int sampleChunkSize, int maxSecurityIndexesCount, int maxNetworkAttributesCount,
                                      List<SecurityIndexId> securityIndexes, List<HistoDbNetworkAttributeId> networkAttributes) {
        this.sampleChunkSize = sampleChunkSize;
        this.maxSecurityIndexesCount = maxSecurityIndexesCount;
        this.maxNetworkAttributesCount = maxNetworkAttributesCount;
        this.securityIndexes = Objects.requireNonNull(securityIndexes);
        securityIndexesColumnIndex = new HashMap<>(securityIndexes.size());
        for (int i = 0; i < securityIndexes.size(); i++) {
            securityIndexesColumnIndex.put(securityIndexes.get(i), i);
        }
        this.networkAttributes = Objects.requireNonNull(networkAttributes);
        networkAttributesColumnIndex = new HashMap<>(networkAttributes.size());
        for (int i = 0; i < networkAttributes.size(); i++) {
            networkAttributesColumnIndex.put(networkAttributes.get(i), i);
        }
        if (OfflineTaskStatus.values().length > Byte.MAX_VALUE) {
            throw new AssertionError();
        }
    }

    int getSampleChunkSize() {
        return sampleChunkSize;
    }

    int getMaxSecurityIndexesCount() {
        return maxSecurityIndexesCount;
    }

    int getMaxNetworkAttributesCount() {
        return maxNetworkAttributesCount;
    }

    Collection<SecurityIndexId> getSecurityIndexIds() {
        securityIndexesLock.lock();
        try {
            return new TreeSet<>(securityIndexes);
        } finally {
            securityIndexesLock.unlock();
        }
    }

    Collection<HistoDbNetworkAttributeId> getNetworkAttributeIds() {
        networkAttributesLock.lock();
        try {
            return new TreeSet<>(networkAttributes);
        } finally {
            networkAttributesLock.unlock();
        }
    }

    int getSecurityIndexesCount() {
        securityIndexesLock.lock();
        try {
            return securityIndexes.size();
        } finally {
            securityIndexesLock.unlock();
        }
    }

    int getNetworkAttributeCount() {
        networkAttributesLock.lock();
        try {
            return networkAttributes.size();
        } finally {
            networkAttributesLock.unlock();
        }
    }

    int getColumnIndex(OfflineTaskType taskType) {
        return taskType.ordinal();
    }

    int getColumnIndex(SecurityIndexId securityIndexId) {
        securityIndexesLock.lock();
        try {
            Integer index = securityIndexesColumnIndex.get(securityIndexId);
            if (index == null) {
                if (securityIndexes.size() > maxSecurityIndexesCount) {
                    throw new RuntimeException("Max number of security indexes reached (" + maxSecurityIndexesCount + ")");
                }
                index = securityIndexes.size();
                securityIndexes.add(securityIndexId);
                securityIndexesColumnIndex.put(securityIndexId, index);
                changed = true;
            }
            return index;
        } finally {
            securityIndexesLock.unlock();
        }
    }

    SecurityIndexId getSecurityIndexId(int columnIndex) {
        securityIndexesLock.lock();
        try {
            if (columnIndex >= securityIndexes.size()) {
                throw new RuntimeException("No security index at column " + columnIndex);
            }
            return securityIndexes.get(columnIndex);
        } finally {
            securityIndexesLock.unlock();
        }
    }

    int getColumnIndex(HistoDbNetworkAttributeId attributeId) {
        networkAttributesLock.lock();
        try {
            Integer index = networkAttributesColumnIndex.get(attributeId);
            if (index == null) {
                if (networkAttributes.size() > maxNetworkAttributesCount) {
                    throw new RuntimeException("Max number of network attributes reached (" + maxNetworkAttributesCount + ")");
                }
                index = networkAttributesColumnIndex.size();
                networkAttributes.add(attributeId);
                networkAttributesColumnIndex.put(attributeId, index);
                changed = true;
            }
            return index;
        } finally {
            networkAttributesLock.unlock();
        }
    }

    HistoDbNetworkAttributeId getNetworkAttributeId(int columnIndex) {
        networkAttributesLock.lock();
        try {
            if (columnIndex >= networkAttributes.size()) {
                throw new RuntimeException("No network attribute at column " + columnIndex);
            }
            return networkAttributes.get(columnIndex);
        } finally {
            networkAttributesLock.unlock();
        }
    }

    static int getTasksStatusRowSize() {
        return OfflineTaskType.values().length * TASK_STATUS_CELL_SIZE;
    }

    int getSecurityIndexesRowSize() {
        return maxSecurityIndexesCount * SECURITY_INDEX_CELL_SIZE;
    }

    int getNetworkAttributesRowSize() {
        return maxNetworkAttributesCount * NETWORK_ATTRIBUTE_CELL_SIZE;
    }

    int getRowSize() {
        return getTasksStatusRowSize() + getSecurityIndexesRowSize() + getNetworkAttributesRowSize();
    }

    int getTaskTypeBufferPosition(int sample, OfflineTaskType taskType) {
        return sample * getRowSize() + getColumnIndex(taskType) * TASK_STATUS_CELL_SIZE;
    }

    int getSecurityIndexBufferPosition(int sample, SecurityIndexId securityIndexId) {
        return sample * getRowSize() + getTasksStatusRowSize() + getColumnIndex(securityIndexId) * SECURITY_INDEX_CELL_SIZE;
    }

    int getNetworkAttributeBufferPosition(int sample, HistoDbNetworkAttributeId attributeId) {
        return sample * getRowSize() + getTasksStatusRowSize() + getSecurityIndexesRowSize() + getColumnIndex(attributeId) * NETWORK_ATTRIBUTE_CELL_SIZE;
    }

    boolean hasChanged() {
        return changed;
    }

    void saveIfChanged(Path workflowDir) throws IOException {
        if (changed) {
            save(workflowDir);
        }
    }

    public void save(Path workflowDir) throws IOException {
        Path file = workflowDir.resolve(TABLE_DESCRIPTION_FILE_NAME);
        LOGGER.debug("Saving " + file);
        Files.deleteIfExists(file);
        try (Writer writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8);
             JsonGenerator generator = MMapOfflineDb.JSON_FACTORY.get().createGenerator(writer)) {
            generator.useDefaultPrettyPrinter();
            generator.writeStartObject();

            generator.writeNumberField("sampleChunkSize", sampleChunkSize);
            generator.writeNumberField("maxSecurityIndexesCount", maxSecurityIndexesCount);
            generator.writeNumberField("maxNetworkAttributesCount", maxNetworkAttributesCount);

            generator.writeFieldName("securityIndexes");
            generator.writeStartArray();
            securityIndexesLock.lock();
            try {
                for (SecurityIndexId securityIndex : securityIndexes) {
                    generator.writeString(securityIndex.toString());
                }
            } finally {
                securityIndexesLock.unlock();
            }
            generator.writeEndArray();

            generator.writeFieldName("networkAttributes");
            generator.writeStartArray();
            networkAttributesLock.lock();
            try {
                for (HistoDbNetworkAttributeId attributeId : networkAttributes) {
                    generator.writeString(attributeId.toString());
                }
            } finally {
                networkAttributesLock.unlock();
            }
            generator.writeEndArray();

            generator.writeEndObject();
        }
        changed = false;
    }

}
