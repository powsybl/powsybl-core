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
public abstract class AbstractAppStorageTest {

    private static final String FOLDER_PSEUDO_CLASS = "folder";

    private AppStorage storage;

    protected abstract AppStorage createStorage();

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
        NodeInfo testFolderInfo = storage.createNode(rootFolderInfo.getId(), "test", FOLDER_PSEUDO_CLASS, 0);
        storage.flush();
        assertEquals(rootFolderInfo.getId(), storage.getParentNode(testFolderInfo.getId()));
        assertEquals(rootFolderInfo.getId(), storage.getParentNodeInfo(testFolderInfo.getId()).getId());
        assertEquals(storage.getFileSystemName(), storage.getParentNodeInfo(testFolderInfo.getId()).getName());
        assertEquals(FOLDER_PSEUDO_CLASS, storage.getParentNodeInfo(testFolderInfo.getId()).getPseudoClass());
        assertEquals("test", storage.getNodeName(testFolderInfo.getId()));
        assertEquals(FOLDER_PSEUDO_CLASS, storage.getNodePseudoClass(testFolderInfo.getId()));
        assertEquals(testFolderInfo.getId(), storage.getNodeInfo(testFolderInfo.getId()).getId());
        assertEquals(0, storage.getNodeInfo(testFolderInfo.getId()).getVersion());
        assertEquals("test", storage.getNodeInfo(testFolderInfo.getId()).getName());
        assertEquals(FOLDER_PSEUDO_CLASS, storage.getNodeInfo(testFolderInfo.getId()).getPseudoClass());
        assertEquals(testFolderInfo.getId(), storage.fromString(testFolderInfo.getId().toString()));
        assertTrue(storage.getChildNodes(testFolderInfo.getId()).isEmpty());
        assertEquals(1, storage.getChildNodes(rootFolderInfo.getId()).size());
        assertEquals(testFolderInfo.getId(), storage.getChildNodes(rootFolderInfo.getId()).get(0));
        assertEquals(1, storage.getChildNodesInfo(rootFolderInfo.getId()).size());
        assertEquals(testFolderInfo.getId(), storage.getChildNodesInfo(rootFolderInfo.getId()).get(0).getId());
        assertEquals("test", storage.getChildNodesInfo(rootFolderInfo.getId()).get(0).getName());
        assertEquals(FOLDER_PSEUDO_CLASS, storage.getChildNodesInfo(rootFolderInfo.getId()).get(0).getPseudoClass());
        assertNull(storage.getChildNode(rootFolderInfo.getId(), "???"));
        assertNull(storage.getChildNodeInfo(rootFolderInfo.getId(), "???"));
        assertNotNull(storage.getChildNode(rootFolderInfo.getId(), "test"));
        assertEquals(testFolderInfo.getId(), storage.getChildNodeInfo(rootFolderInfo.getId(), "test").getId());
        assertEquals("test", storage.getChildNodeInfo(rootFolderInfo.getId(), "test").getName());
        assertEquals(FOLDER_PSEUDO_CLASS, storage.getChildNodeInfo(rootFolderInfo.getId(), "test").getPseudoClass());

        // description
        assertEquals("", testFolderInfo.getDescription());
        storage.setDescription(testFolderInfo.getId(), "hello");
        testFolderInfo = storage.getNodeInfo(testFolderInfo.getId());
        assertEquals("hello", testFolderInfo.getDescription());

        // dependency tests
        NodeInfo testDataInfo = storage.createNode(testFolderInfo.getId(), "data", "data", 0);
        NodeInfo testData2Info = storage.createNode(testFolderInfo.getId(), "data2", "data", 0);
        storage.flush();
        assertEquals(2, storage.getChildNodes(testFolderInfo.getId()).size());
        assertTrue(storage.getDependencies(testDataInfo.getId()).isEmpty());
        assertTrue(storage.getDependenciesInfo(testDataInfo.getId()).isEmpty());
        assertTrue(storage.getBackwardDependencies(testData2Info.getId()).isEmpty());
        assertTrue(storage.getBackwardDependenciesInfo(testData2Info.getId()).isEmpty());
        storage.addDependency(testDataInfo.getId(), "mylink", testData2Info.getId());
        assertEquals(Collections.singletonList(testData2Info.getId()), storage.getDependencies(testDataInfo.getId()));
        assertEquals(1, storage.getDependenciesInfo(testDataInfo.getId()).size());
        assertEquals(testData2Info.getId(), storage.getDependenciesInfo(testDataInfo.getId()).get(0).getId());
        assertEquals("data2", storage.getDependenciesInfo(testDataInfo.getId()).get(0).getName());
        assertEquals("data", storage.getDependenciesInfo(testDataInfo.getId()).get(0).getPseudoClass());
        assertEquals(Collections.singletonList(testDataInfo.getId()), storage.getBackwardDependencies(testData2Info.getId()));
        assertEquals(1, storage.getBackwardDependenciesInfo(testData2Info.getId()).size());
        assertEquals(testDataInfo.getId(), storage.getBackwardDependenciesInfo(testData2Info.getId()).get(0).getId());
        assertEquals("data", storage.getBackwardDependenciesInfo(testData2Info.getId()).get(0).getName());
        assertEquals("data", storage.getBackwardDependenciesInfo(testData2Info.getId()).get(0).getPseudoClass());
        assertEquals(testData2Info.getId(), storage.getDependency(testDataInfo.getId(), "mylink"));
        assertEquals(testData2Info.getId(), storage.getDependencyInfo(testDataInfo.getId(), "mylink").getId());
        assertEquals("data2", storage.getDependencyInfo(testDataInfo.getId(), "mylink").getName());
        assertEquals("data", storage.getDependencyInfo(testDataInfo.getId(), "mylink").getPseudoClass());
        assertNull(storage.getDependency(testDataInfo.getId(), "mylink2"));
        assertNull(storage.getDependencyInfo(testDataInfo.getId(), "mylink2"));
        storage.deleteNode(testDataInfo.getId());
        storage.flush();
        assertEquals(1, storage.getChildNodes(testFolderInfo.getId()).size());

        // attribute tests

        // set string attribute
        assertNull(storage.getStringAttribute(testData2Info.getId(), "str"));
        storage.setStringAttribute(testData2Info.getId(), "str", "test");
        storage.flush();
        assertEquals("test", storage.getStringAttribute(testData2Info.getId(), "str"));

        // unset string attribute
        storage.setStringAttribute(testData2Info.getId(), "str", null);
        storage.flush();
        assertNull(storage.getStringAttribute(testData2Info.getId(), "str"));

        // set int attribute
        assertFalse(storage.getIntAttribute(testData2Info.getId(), "int").isPresent());
        storage.setIntAttribute(testData2Info.getId(), "int", 3);
        storage.flush();
        assertTrue(storage.getIntAttribute(testData2Info.getId(), "int").isPresent());
        assertEquals(3, storage.getIntAttribute(testData2Info.getId(), "int").getAsInt());

        // set double attribute
        assertFalse(storage.getDoubleAttribute(testData2Info.getId(), "double").isPresent());
        storage.setDoubleAttribute(testData2Info.getId(), "double", 5d);
        storage.flush();
        assertTrue(storage.getDoubleAttribute(testData2Info.getId(), "double").isPresent());
        assertEquals(5d, storage.getDoubleAttribute(testData2Info.getId(), "double").getAsDouble(), 0d);

        // set boolean attribute
        assertFalse(storage.getBooleanAttribute(testData2Info.getId(), "bool").isPresent());
        storage.setBooleanAttribute(testData2Info.getId(), "bool", true);
        storage.flush();
        assertTrue(storage.getBooleanAttribute(testData2Info.getId(), "bool").isPresent());
        assertTrue(storage.getBooleanAttribute(testData2Info.getId(), "bool").get());

        try (Writer writer = storage.writeStringAttribute(testData2Info.getId(), "str")) {
            writer.write("word1");
        }
        storage.flush();
        try (Reader reader = storage.readStringAttribute(testData2Info.getId(), "str")) {
            assertEquals("word1", CharStreams.toString(reader));
        }

        DataSource ds = storage.getDataSourceAttribute(testData2Info.getId(), "ds");
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
        storage.createTimeSeries(testData2Info.getId(), metadata1);
        storage.flush();
        assertEquals(Sets.newHashSet("ts1"), storage.getTimeSeriesNames(testData2Info.getId()));
        List<TimeSeriesMetadata> metadataList = storage.getTimeSeriesMetadata(testData2Info.getId(), Sets.newHashSet("ts1"));
        assertEquals(1, metadataList.size());
        assertEquals(metadata1, metadataList.get(0));
        storage.addDoubleTimeSeriesData(testData2Info.getId(), 0, "ts1", Arrays.asList(new UncompressedDoubleArrayChunk(2, new double[] {1d, 2d}),
                                                                                       new UncompressedDoubleArrayChunk(5, new double[] {3d})));
        storage.flush();
        List<DoubleTimeSeries> doubleTimeSeries = storage.getDoubleTimeSeries(testData2Info.getId(), Sets.newHashSet("ts1"), 0);
        assertEquals(1, doubleTimeSeries.size());
        DoubleTimeSeries ts1 = doubleTimeSeries.get(0);
        assertArrayEquals(new double[] {Double.NaN, Double.NaN, 1d, 2d, Double.NaN, 3d}, ts1.toArray(), 0d);

        TimeSeriesMetadata metadata2 = new TimeSeriesMetadata("ts2",
                                                              TimeSeriesDataType.STRING,
                                                              ImmutableMap.of(),
                                                              RegularTimeSeriesIndex.create(Interval.parse("2015-01-01T00:00:00Z/2015-01-01T01:15:00Z"),
                                                                                            Duration.ofMinutes(15), 1, 1));
        storage.createTimeSeries(testData2Info.getId(), metadata2);
        storage.flush();
        assertEquals(Sets.newHashSet("ts1", "ts2"), storage.getTimeSeriesNames(testData2Info.getId()));
        metadataList = storage.getTimeSeriesMetadata(testData2Info.getId(), Sets.newHashSet("ts1"));
        assertEquals(1, metadataList.size());
        storage.addStringTimeSeriesData(testData2Info.getId(), 0, "ts2", Arrays.asList(new UncompressedStringArrayChunk(2, new String[] {"a", "b"}),
                                                                                       new UncompressedStringArrayChunk(5, new String[] {"c"})));
        storage.flush();
        List<StringTimeSeries> stringTimeSeries = storage.getStringTimeSeries(testData2Info.getId(), Sets.newHashSet("ts2"), 0);
        assertEquals(1, stringTimeSeries.size());
        StringTimeSeries ts2 = stringTimeSeries.get(0);
        assertArrayEquals(new String[] {null, null, "a", "b", null, "c"}, ts2.toArray());

        storage.removeAllTimeSeries(testData2Info.getId());
        storage.flush();
        assertTrue(storage.getTimeSeriesNames(testData2Info.getId()).isEmpty());

        // test cache
        byte[] data = "data".getBytes(StandardCharsets.UTF_8);
        try (OutputStream os = storage.writeToCache(testData2Info.getId(), "cache1")) {
            os.write(data);
        }
        storage.flush();

        try (InputStream is = storage.readFromCache(testData2Info.getId(), "cache1")) {
            assertArrayEquals(data, ByteStreams.toByteArray(is));
        }

        storage.invalidateCache(testData2Info.getId(), "cache1");
        storage.flush();
        assertNull(storage.readFromCache(testData2Info.getId(), "cache1"));

        try (OutputStream os = storage.writeToCache(testData2Info.getId(), "cache2")) {
            os.write("data2".getBytes(StandardCharsets.UTF_8));
        }
        storage.invalidateCache();
        storage.flush();
        assertNull(storage.readFromCache(testData2Info.getId(), "cache2"));
    }

    @Test
    public void setParentTest() throws IOException {
        NodeInfo rootFolderInfo = storage.createRootNodeIfNotExists(storage.getFileSystemName(), FOLDER_PSEUDO_CLASS);
        NodeInfo folder1Info = storage.createNode(rootFolderInfo.getId(), "test1", FOLDER_PSEUDO_CLASS, 0);
        NodeInfo folder2Info = storage.createNode(rootFolderInfo.getId(), "test2", FOLDER_PSEUDO_CLASS, 0);
        storage.flush();
        NodeInfo fileInfo = storage.createNode(folder1Info.getId(), "file", "file-type", 0);
        storage.flush();
        assertEquals(folder1Info.getId(), storage.getParentNode(fileInfo.getId()));
        storage.setParentNode(fileInfo.getId(), folder2Info.getId());
        storage.flush();
        assertEquals(folder2Info.getId(), storage.getParentNode(fileInfo.getId()));
    }

}
