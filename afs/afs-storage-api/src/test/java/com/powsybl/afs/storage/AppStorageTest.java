/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.storage;

import com.powsybl.commons.PowsyblException;
import com.powsybl.timeseries.DoubleDataChunk;
import com.powsybl.timeseries.StringDataChunk;
import com.powsybl.timeseries.TimeSeriesMetadata;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;


import static org.junit.Assert.*;

/**
 * @author Chamseddine Benhamed <chamseddine.benhamed at rte-france.com>
 */
public class AppStorageTest {
    private AppStorage appStorageImpl;

    @Before
    public void setUp() {
        appStorageImpl = new AppStorage() {
            @Override
            public String getFileSystemName() {
                return null;
            }

            @Override
            public boolean isRemote() {
                return false;
            }

            @Override
            public NodeInfo createRootNodeIfNotExists(String name, String nodePseudoClass) {
                return null;
            }

            @Override
            public NodeInfo createNode(String parentNodeId, String name, String nodePseudoClass, String description, int version, NodeGenericMetadata genericMetadata) {
                return null;
            }

            @Override
            public boolean isWritable(String nodeId) {
                return false;
            }

            @Override
            public NodeInfo getNodeInfo(String nodeId) {
                return null;
            }

            @Override
            public void setDescription(String nodeId, String description) {

            }

            @Override
            public void updateModificationTime(String nodeId) {

            }

            @Override
            public List<NodeInfo> getChildNodes(String nodeId) {
                return null;
            }

            @Override
            public Optional<NodeInfo> getChildNode(String nodeId, String name) {
                return Optional.empty();
            }

            @Override
            public Optional<NodeInfo> getParentNode(String nodeId) {
                return Optional.empty();
            }

            @Override
            public void setParentNode(String nodeId, String newParentNodeId) {

            }

            @Override
            public String deleteNode(String nodeId) {
                return null;
            }

            @Override
            public Optional<InputStream> readBinaryData(String nodeId, String name) {
                return Optional.empty();
            }

            @Override
            public OutputStream writeBinaryData(String nodeId, String name) {
                return null;
            }

            @Override
            public boolean dataExists(String nodeId, String name) {
                return false;
            }

            @Override
            public Set<String> getDataNames(String nodeId) {
                return null;
            }

            @Override
            public boolean removeData(String nodeId, String name) {
                return false;
            }

            @Override
            public void createTimeSeries(String nodeId, TimeSeriesMetadata metadata) {

            }

            @Override
            public Set<String> getTimeSeriesNames(String nodeId) {
                return null;
            }

            @Override
            public boolean timeSeriesExists(String nodeId, String timeSeriesName) {
                return false;
            }

            @Override
            public List<TimeSeriesMetadata> getTimeSeriesMetadata(String nodeId, Set<String> timeSeriesNames) {
                return null;
            }

            @Override
            public Set<Integer> getTimeSeriesDataVersions(String nodeId) {
                return null;
            }

            @Override
            public Set<Integer> getTimeSeriesDataVersions(String nodeId, String timeSeriesName) {
                return null;
            }

            @Override
            public Map<String, List<DoubleDataChunk>> getDoubleTimeSeriesData(String nodeId, Set<String> timeSeriesNames, int version) {
                return null;
            }

            @Override
            public void addDoubleTimeSeriesData(String nodeId, int version, String timeSeriesName, List<DoubleDataChunk> chunks) {

            }

            @Override
            public Map<String, List<StringDataChunk>> getStringTimeSeriesData(String nodeId, Set<String> timeSeriesNames, int version) {
                return null;
            }

            @Override
            public void addStringTimeSeriesData(String nodeId, int version, String timeSeriesName, List<StringDataChunk> chunks) {

            }

            @Override
            public void clearTimeSeries(String nodeId) {

            }

            @Override
            public void addDependency(String nodeId, String name, String toNodeId) {

            }

            @Override
            public Set<NodeInfo> getDependencies(String nodeId, String name) {
                return null;
            }

            @Override
            public Set<NodeDependency> getDependencies(String nodeId) {
                return null;
            }

            @Override
            public Set<NodeInfo> getBackwardDependencies(String nodeId) {
                return null;
            }

            @Override
            public void removeDependency(String nodeId, String name, String toNodeId) {

            }

            @Override
            public void flush() {

            }

            @Override
            public boolean isClosed() {
                return false;
            }

            @Override
            public void close() {

            }
        };
    }

    @Test
    public void testGetInconsistentNodes() throws IOException {
        assertTrue(appStorageImpl.getInconsistentNodes().isEmpty());
    }

    @Test(expected = PowsyblException.class)
    public void testSetConsistent() throws IOException {
        appStorageImpl.setConsistent("node1");
    }

    @Test(expected = PowsyblException.class)
    public void testIsConsistent() throws IOException {
        appStorageImpl.isConsistent("node1");
    }
}
