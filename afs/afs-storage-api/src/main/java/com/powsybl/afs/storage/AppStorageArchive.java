/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.storage;

import com.fasterxml.jackson.databind.ObjectWriter;
import com.powsybl.afs.storage.json.AppStorageJsonModule;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.math.timeseries.DoubleArrayChunk;
import com.powsybl.math.timeseries.StringArrayChunk;
import com.powsybl.math.timeseries.TimeSeriesMetadata;

import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class AppStorageArchive {

    private static class ArchiveDependency {

        private String nodeId;

        private String name;

        public ArchiveDependency() {
        }

        public ArchiveDependency(String nodeId, String name) {
            this.nodeId = nodeId;
            this.name = name;
        }

        public String getNodeId() {
            return nodeId;
        }

        public void setNodeId(String nodeId) {
            this.nodeId = nodeId;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    private final AppStorage storage;

    private final ObjectWriter objectWriter;

    public AppStorageArchive(AppStorage storage) {
        this.storage = Objects.requireNonNull(storage);
        objectWriter = JsonUtil.createObjectMapper()
                .registerModule(new AppStorageJsonModule())
                .writerWithDefaultPrettyPrinter();
    }

    private void writeNodeInfo(NodeInfo nodeInfo, Path nodeDir) throws IOException {
        try (Writer writer = Files.newBufferedWriter(nodeDir.resolve("info.json"), StandardCharsets.UTF_8)) {
            objectWriter.writeValue(writer, nodeInfo);
        }
    }

    private void writeDependencies(NodeInfo nodeInfo, Path nodeDir) throws IOException {
        List<ArchiveDependency> dependencies = storage.getDependencies(nodeInfo.getId())
                .stream()
                .map(nodeDependency -> new ArchiveDependency(nodeDependency.getNodeInfo().getId(), nodeDependency.getName()))
                .collect(Collectors.toList());
        if (dependencies.isEmpty()) {
            return;
        }
        try (Writer writer = Files.newBufferedWriter(nodeDir.resolve("dependencies.json"), StandardCharsets.UTF_8)) {
            objectWriter.writeValue(writer, dependencies);
        }
    }

    private void writeData(NodeInfo nodeInfo, Path nodeDir) throws IOException {
        Set<String> dataNames = storage.getDataNames(nodeInfo.getId());
        if (dataNames.isEmpty()) {
            return;
        }
        Path dataDir = nodeDir.resolve("data");
        Files.createDirectory(dataDir);

        for (String dataName : dataNames) {
            try (InputStream is = storage.readBinaryData(nodeInfo.getId(), dataName).orElseThrow(AssertionError::new)) {
                Files.copy(is, dataDir.resolve(dataName));
            }
        }
    }

    private void writeTimeSeries(NodeInfo nodeInfo, Path nodeDir) throws IOException {
        Set<String> timeSeriesNames = storage.getTimeSeriesNames(nodeInfo.getId());
        if (timeSeriesNames.isEmpty()) {
            return;
        }
        Path timeSeriesDir = nodeDir.resolve("time-series");
        Files.createDirectory(timeSeriesDir);

        for (TimeSeriesMetadata metadata : storage.getTimeSeriesMetadata(nodeInfo.getId(), timeSeriesNames)) {
            String timeSeriesFileName = URLEncoder.encode(metadata.getName(), StandardCharsets.UTF_8.name());
            Path timeSeriesNameDir = timeSeriesDir.resolve(timeSeriesFileName);
            Files.createDirectory(timeSeriesNameDir);

            // write metadata
            try (Writer writer = Files.newBufferedWriter(timeSeriesNameDir.resolve("metadata.json"), StandardCharsets.UTF_8)) {
                objectWriter.writeValue(writer, metadata);
            }

            // write chunks for each version
            for (int version : storage.getTimeSeriesDataVersions(nodeInfo.getId(), metadata.getName())) {
                switch (metadata.getDataType()) {
                    case DOUBLE:
                        List<DoubleArrayChunk> doubleChunks = storage.getDoubleTimeSeriesData(nodeInfo.getId(), Collections.singleton(metadata.getName()), version).get(metadata.getName());
                        try (OutputStream os = Files.newOutputStream(timeSeriesNameDir.resolve("chunks-" + version + ".json"))) {
                            objectWriter.writeValue(os, doubleChunks);
                        }
                        break;

                    case STRING:
                        List<StringArrayChunk> stringChunks = storage.getStringTimeSeriesData(nodeInfo.getId(), Collections.singleton(metadata.getName()), version).get(metadata.getName());
                        try (OutputStream os = Files.newOutputStream(timeSeriesNameDir.resolve("chunks-" + version + ".json"))) {
                            objectWriter.writeValue(os, stringChunks);
                        }
                        break;

                    default:
                        throw new AssertionError("Unsupported data type " + metadata.getDataType());
                }
            }
        }
    }

    private void writeChildren(NodeInfo nodeInfo, Path nodeDir) throws IOException {
        List<NodeInfo> childNodeInfos = storage.getChildNodes(nodeInfo.getId());
        if (!childNodeInfos.isEmpty()) {
            Path childrenDir = nodeDir.resolve("children");
            Files.createDirectory(childrenDir);
            for (NodeInfo childNodeInfo : childNodeInfos) {
                archive(childNodeInfo, childrenDir);
            }
        }
    }

    public void archive(String nodeId, Path parentDir) {
        Objects.requireNonNull(nodeId);
        Objects.requireNonNull(parentDir);

        try {
            archive(storage.getNodeInfo(nodeId), parentDir);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void archive(NodeInfo nodeInfo, Path parentDir) throws IOException {
        Objects.requireNonNull(nodeInfo);

        Path nodeDir = parentDir.resolve(nodeInfo.getId());
        Files.createDirectory(nodeDir);

        writeNodeInfo(nodeInfo, nodeDir);

        writeDependencies(nodeInfo, nodeDir);

        writeData(nodeInfo, nodeDir);

        writeTimeSeries(nodeInfo, nodeDir);

        writeChildren(nodeInfo, nodeDir);
    }

    public void unarchive(NodeInfo rootNodeInfo, Path parentDir) {

    }
}
