/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.storage;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.google.common.io.ByteStreams;
import com.google.common.io.CharStreams;
import com.powsybl.commons.datasource.DataSource;
import com.powsybl.math.timeseries.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.threeten.extra.Interval;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public abstract class AbstractAppFileSystemStorageTest {

    private static final String FOLDER_PSEUDO_CLASS = "folder";

    private AppFileSystemStorage storage;

    protected abstract AppFileSystemStorage createStorage();

    @Before
    public void setUp() throws Exception {
        storage = createStorage();
    }

    @After
    public void tearDown() throws Exception {
        storage.close();
    }

    @Test
    public void test() throws IOException {
        // folder and create tests
        NodeInfo rootFolderInfo = storage.createRootNodeIfNotExists(storage.getFileSystemName(), FOLDER_PSEUDO_CLASS);
        assertNotNull(rootFolderInfo.getId());
        assertNull(storage.getParentNode(rootFolderInfo.getId()));
        assertNull(storage.getParentNodeInfo(rootFolderInfo.getId()));
        assertEquals(FOLDER_PSEUDO_CLASS, storage.getNodePseudoClass(rootFolderInfo.getId()));
        assertTrue(storage.getChildNodes(rootFolderInfo.getId()).isEmpty());
        assertTrue(storage.getChildNodesInfo(rootFolderInfo.getId()).isEmpty());
        NodeId testFolderId = storage.createNode(rootFolderInfo.getId(), "test", FOLDER_PSEUDO_CLASS);
        storage.flush();
        assertEquals(rootFolderInfo.getId(), storage.getParentNode(testFolderId));
        assertEquals(new NodeInfo(rootFolderInfo.getId(), storage.getFileSystemName(), FOLDER_PSEUDO_CLASS), storage.getParentNodeInfo(testFolderId));
        assertEquals("test", storage.getNodeName(testFolderId));
        assertEquals(FOLDER_PSEUDO_CLASS, storage.getNodePseudoClass(testFolderId));
        assertEquals(new NodeInfo(testFolderId, "test", FOLDER_PSEUDO_CLASS), storage.getNodeInfo(testFolderId));
        assertEquals(testFolderId, storage.fromString(testFolderId.toString()));
        assertTrue(storage.getChildNodes(testFolderId).isEmpty());
        assertEquals(1, storage.getChildNodes(rootFolderInfo.getId()).size());
        assertEquals(testFolderId, storage.getChildNodes(rootFolderInfo.getId()).get(0));
        assertEquals(Collections.singletonList(new NodeInfo(testFolderId, "test", FOLDER_PSEUDO_CLASS)), storage.getChildNodesInfo(rootFolderInfo.getId()));
        assertNull(storage.getChildNode(rootFolderInfo.getId(), "???"));
        assertNull(storage.getChildNodeInfo(rootFolderInfo.getId(), "???"));
        assertNotNull(storage.getChildNode(rootFolderInfo.getId(), "test"));
        assertEquals(new NodeInfo(testFolderId, "test", FOLDER_PSEUDO_CLASS), storage.getChildNodeInfo(rootFolderInfo.getId(), "test"));

        // dependency tests
        NodeId testDataId = storage.createNode(testFolderId, "data", "data");
        NodeId testData2Id = storage.createNode(testFolderId, "data2", "data");
        storage.flush();
        assertEquals(2, storage.getChildNodes(testFolderId).size());
        assertTrue(storage.getDependencies(testDataId).isEmpty());
        assertTrue(storage.getDependenciesInfo(testDataId).isEmpty());
        assertTrue(storage.getBackwardDependencies(testData2Id).isEmpty());
        assertTrue(storage.getBackwardDependenciesInfo(testData2Id).isEmpty());
        storage.addDependency(testDataId, "mylink", testData2Id);
        assertEquals(Collections.singletonList(testData2Id), storage.getDependencies(testDataId));
        assertEquals(Collections.singletonList(new NodeInfo(testData2Id, "data2", "data")), storage.getDependenciesInfo(testDataId));
        assertEquals(Collections.singletonList(testDataId), storage.getBackwardDependencies(testData2Id));
        assertEquals(Collections.singletonList(new NodeInfo(testDataId, "data", "data")), storage.getBackwardDependenciesInfo(testData2Id));
        assertEquals(testData2Id, storage.getDependency(testDataId, "mylink"));
        assertEquals(new NodeInfo(testData2Id, "data2", "data"), storage.getDependencyInfo(testDataId, "mylink"));
        assertNull(storage.getDependency(testDataId, "mylink2"));
        assertNull(storage.getDependencyInfo(testDataId, "mylink2"));
        storage.deleteNode(testDataId);
        storage.flush();
        assertEquals(1, storage.getChildNodes(testFolderId).size());

        // attribute tests

        // set string attribute
        assertNull(storage.getStringAttribute(testData2Id, "str"));
        storage.setStringAttribute(testData2Id, "str", "test");
        storage.flush();
        assertEquals("test", storage.getStringAttribute(testData2Id, "str"));

        // unset string attribute
        storage.setStringAttribute(testData2Id, "str", null);
        storage.flush();
        assertNull(storage.getStringAttribute(testData2Id, "str"));

        // set int attribute
        assertFalse(storage.getIntAttribute(testData2Id, "int").isPresent());
        storage.setIntAttribute(testData2Id, "int", 3);
        storage.flush();
        assertTrue(storage.getIntAttribute(testData2Id, "int").isPresent());
        assertEquals(3, storage.getIntAttribute(testData2Id, "int").getAsInt());

        // set double attribute
        assertFalse(storage.getDoubleAttribute(testData2Id, "double").isPresent());
        storage.setDoubleAttribute(testData2Id, "double", 5d);
        storage.flush();
        assertTrue(storage.getDoubleAttribute(testData2Id, "double").isPresent());
        assertEquals(5d, storage.getDoubleAttribute(testData2Id, "double").getAsDouble(), 0d);

        // set boolean attribute
        assertFalse(storage.getBooleanAttribute(testData2Id, "bool").isPresent());
        storage.setBooleanAttribute(testData2Id, "bool", true);
        storage.flush();
        assertTrue(storage.getBooleanAttribute(testData2Id, "bool").isPresent());
        assertTrue(storage.getBooleanAttribute(testData2Id, "bool").get());

        try (Writer writer = storage.writeStringAttribute(testData2Id, "str")) {
            writer.write("word1");
        }
        storage.flush();
        try (Reader reader = storage.readStringAttribute(testData2Id, "str")) {
            assertEquals("word1", CharStreams.toString(reader));
        }

        DataSource ds = storage.getDataSourceAttribute(testData2Id, "ds");
        assertEquals("", ds.getBaseName());
        assertFalse(ds.exists(null, "ext"));
        try (OutputStream os = ds.newOutputStream(null, "ext", false)) {
            os.write("word1".getBytes(StandardCharsets.UTF_8));
        }

        storage.flush();
        assertTrue(ds.exists(null, "ext"));
        try (InputStream ignored = ds.newInputStream(null, "ext2")) {
            fail();
        } catch (Exception ignored) {
        }
        try (InputStream is = ds.newInputStream(null, "ext")) {
            assertEquals("word1", new String(ByteStreams.toByteArray(is), StandardCharsets.UTF_8));
        }
        try (OutputStream os = ds.newOutputStream(null, "ext", true)) {
            os.write("word2".getBytes(StandardCharsets.UTF_8));
        }
        storage.flush();
        try (InputStream is = ds.newInputStream(null, "ext")) {
            assertEquals("word1word2", new String(ByteStreams.toByteArray(is), StandardCharsets.UTF_8));
        }

        assertFalse(ds.exists("file1"));
        try (OutputStream os = ds.newOutputStream("file1", false)) {
            os.write("word1".getBytes(StandardCharsets.UTF_8));
        }
        storage.flush();
        assertTrue(ds.exists("file1"));
        try (InputStream ignored = ds.newInputStream("file2")) {
            fail();
        } catch (Exception ignored) {
        }
        try (InputStream is = ds.newInputStream("file1")) {
            assertEquals("word1", new String(ByteStreams.toByteArray(is), StandardCharsets.UTF_8));
        }
        try (OutputStream os = ds.newOutputStream("file1", true)) {
            os.write("word2".getBytes(StandardCharsets.UTF_8));
        }
        storage.flush();
        try (InputStream is = ds.newInputStream("file1")) {
            assertEquals("word1word2", new String(ByteStreams.toByteArray(is), StandardCharsets.UTF_8));
        }

        // time series test
        TimeSeriesMetadata metadata1 = new TimeSeriesMetadata("ts1",
                                                              TimeSeriesDataType.DOUBLE,
                                                              ImmutableMap.of("var1", "value1"),
                                                              RegularTimeSeriesIndex.create(Interval.parse("2015-01-01T00:00:00Z/2015-01-01T01:15:00Z"),
                                                                                            Duration.ofMinutes(15), 1, 1));
        storage.createTimeSeries(testData2Id, metadata1);
        storage.flush();
        assertEquals(Sets.newHashSet("ts1"), storage.getTimeSeriesNames(testData2Id));
        List<TimeSeriesMetadata> metadataList = storage.getTimeSeriesMetadata(testData2Id, Sets.newHashSet("ts1"));
        assertEquals(1, metadataList.size());
        assertEquals(metadata1, metadataList.get(0));
        storage.addDoubleTimeSeriesData(testData2Id, 0, "ts1", Arrays.asList(new UncompressedDoubleArrayChunk(2, new double[] {1d, 2d}),
                                                                             new UncompressedDoubleArrayChunk(5, new double[] {3d})));
        storage.flush();
        List<DoubleTimeSeries> doubleTimeSeries = storage.getDoubleTimeSeries(testData2Id, Sets.newHashSet("ts1"), 0);
        assertEquals(1, doubleTimeSeries.size());
        DoubleTimeSeries ts1 = doubleTimeSeries.get(0);
        assertArrayEquals(new double[] {Double.NaN, Double.NaN, 1d, 2d, Double.NaN, 3d}, ts1.toArray(), 0d);

        TimeSeriesMetadata metadata2 = new TimeSeriesMetadata("ts2",
                                                              TimeSeriesDataType.STRING,
                                                              ImmutableMap.of(),
                                                              RegularTimeSeriesIndex.create(Interval.parse("2015-01-01T00:00:00Z/2015-01-01T01:15:00Z"),
                                                                                            Duration.ofMinutes(15), 1, 1));
        storage.createTimeSeries(testData2Id, metadata2);
        storage.flush();
        assertEquals(Sets.newHashSet("ts1", "ts2"), storage.getTimeSeriesNames(testData2Id));
        metadataList = storage.getTimeSeriesMetadata(testData2Id, Sets.newHashSet("ts1"));
        assertEquals(1, metadataList.size());
        storage.addStringTimeSeriesData(testData2Id, 0, "ts2", Arrays.asList(new UncompressedStringArrayChunk(2, new String[] {"a", "b"}),
                                                                             new UncompressedStringArrayChunk(5, new String[] {"c"})));
        storage.flush();
        List<StringTimeSeries> stringTimeSeries = storage.getStringTimeSeries(testData2Id, Sets.newHashSet("ts2"), 0);
        assertEquals(1, stringTimeSeries.size());
        StringTimeSeries ts2 = stringTimeSeries.get(0);
        assertArrayEquals(new String[] {null, null, "a", "b", null, "c"}, ts2.toArray());

        storage.removeAllTimeSeries(testData2Id);
        storage.flush();
        assertTrue(storage.getTimeSeriesNames(testData2Id).isEmpty());

        // test cache
        byte[] data = "data".getBytes(StandardCharsets.UTF_8);
        try (OutputStream os = storage.writeToCache(testData2Id, "cache1")) {
            os.write(data);
        }
        storage.flush();

        try (InputStream is = storage.readFromCache(testData2Id, "cache1")) {
            assertArrayEquals(data, ByteStreams.toByteArray(is));
        }

        storage.invalidateCache(testData2Id, "cache1");
        storage.flush();
        assertNull(storage.readFromCache(testData2Id, "cache1"));

        try (OutputStream os = storage.writeToCache(testData2Id, "cache2")) {
            os.write("data2".getBytes(StandardCharsets.UTF_8));
        }
        storage.invalidateCache();
        storage.flush();
        assertNull(storage.readFromCache(testData2Id, "cache2"));
    }

    @Test
    public void setParentTest() throws IOException {
        NodeInfo rootFolderInfo = storage.createRootNodeIfNotExists(storage.getFileSystemName(), FOLDER_PSEUDO_CLASS);
        NodeId folder1Id = storage.createNode(rootFolderInfo.getId(), "test1", FOLDER_PSEUDO_CLASS);
        NodeId folder2Id = storage.createNode(rootFolderInfo.getId(), "test2", FOLDER_PSEUDO_CLASS);
        storage.flush();
        NodeId fileId = storage.createNode(folder1Id, "file", "file-type");
        storage.flush();
        assertEquals(folder1Id, storage.getParentNode(fileId));
        storage.setParentNode(fileId, folder2Id);
        storage.flush();
        assertEquals(folder2Id, storage.getParentNode(fileId));
    }

}
