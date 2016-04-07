/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.offline.db.mmap;

import eu.itesla_project.commons.io.ModuleConfig;
import eu.itesla_project.commons.io.PlatformConfig;

import java.nio.file.Path;
import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class MMapOfflineDbConfig {

    private static final int DEFAULT_SAMPLE_CHUNK_SIZE = 1000;
    private static final int DEFAULT_MAX_SECURITY_INDEXES_COUNT = 1000;
    private static final int DEFAULT_MAX_NETWORK_ATTRIBUTES_COUNT = 40000;

    private Path directory;
    private int sampleChunkSize;
    private int maxSecurityIndexesCount;
    private int maxNetworkAttributesCount;

    public static MMapOfflineDbConfig load() {
        ModuleConfig config = PlatformConfig.defaultConfig().getModuleConfig("mmap-offlinedb");
        Path directory = config.getPathProperty("directory");
        int sampleChunkSize = config.getIntProperty("sampleChunkSize", DEFAULT_SAMPLE_CHUNK_SIZE);
        int maxSecurityIndexesCount = config.getIntProperty("maxSecurityIndexesCount", DEFAULT_MAX_SECURITY_INDEXES_COUNT);
        int maxNetworkAttributesCount = config.getIntProperty("maxNetworkAttributesCount", DEFAULT_MAX_NETWORK_ATTRIBUTES_COUNT);
        return new MMapOfflineDbConfig(directory, sampleChunkSize, maxSecurityIndexesCount, maxNetworkAttributesCount);
    }

    public MMapOfflineDbConfig(Path directory, int sampleChunkSize, int maxSecurityIndexesCount, int maxNetworkAttributesCount) {
        this.directory = Objects.requireNonNull(directory);
        this.sampleChunkSize = sampleChunkSize;
        this.maxSecurityIndexesCount = maxSecurityIndexesCount;
        this.maxNetworkAttributesCount = maxNetworkAttributesCount;
    }

    public MMapOfflineDbConfig(Path directory) {
        this(directory, DEFAULT_SAMPLE_CHUNK_SIZE, DEFAULT_MAX_SECURITY_INDEXES_COUNT, DEFAULT_MAX_NETWORK_ATTRIBUTES_COUNT);
    }

    public Path getDirectory() {
        return directory;
    }

    public void setDirectory(Path directory) {
        this.directory = directory;
    }

    public int getSampleChunkSize() {
        return sampleChunkSize;
    }

    public void setSampleChunkSize(int sampleChunkSize) {
        this.sampleChunkSize = sampleChunkSize;
    }

    public int getMaxSecurityIndexesCount() {
        return maxSecurityIndexesCount;
    }

    public void setMaxSecurityIndexesCount(int maxSecurityIndexesCount) {
        this.maxSecurityIndexesCount = maxSecurityIndexesCount;
    }

    public int getMaxNetworkAttributesCount() {
        return maxNetworkAttributesCount;
    }

    public void setMaxNetworkAttributesCount(int maxNetworkAttributesCount) {
        this.maxNetworkAttributesCount = maxNetworkAttributesCount;
    }

}
