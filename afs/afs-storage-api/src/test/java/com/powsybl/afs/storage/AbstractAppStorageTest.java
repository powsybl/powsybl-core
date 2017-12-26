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
    private static final String DATA_FILE_CLASS = "data";

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
    public void baseTest() throws IOException {
        // 1) create root folder
        NodeInfo rootFolderInfo = storage.createRootNodeIfNotExists(storage.getFileSystemName(), FOLDER_PSEUDO_CLASS);
        storage.flush();
        assertNotNull(rootFolderInfo.getId());

        // assert root folder parent is null
        assertNull(storage.getParentNode(rootFolderInfo.getId()));

        // check root folder name and pseudo class is correct
        assertEquals(storage.getFileSystemName(), storage.getNodeInfo(rootFolderInfo.getId()).getName());
        assertEquals(FOLDER_PSEUDO_CLASS, storage.getNodeInfo(rootFolderInfo.getId()).getPseudoClass());

        // assert root folder is empty
        assertTrue(storage.getChildNodes(rootFolderInfo.getId()).isEmpty());

        // 2) create a test folder
        NodeInfo testFolderInfo = storage.createNode(rootFolderInfo.getId(), "test", FOLDER_PSEUDO_CLASS, "", 0,
                Collections.singletonMap("k", "v"), Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap());
        storage.flush();

        // assert parent of test folder is root folder
        assertEquals(rootFolderInfo, storage.getParentNode(testFolderInfo.getId()));

        // check test folder infos are corrects
        assertEquals(testFolderInfo.getId(), storage.getNodeInfo(testFolderInfo.getId()).getId());
        assertEquals("test", storage.getNodeInfo(testFolderInfo.getId()).getName());
        assertEquals(FOLDER_PSEUDO_CLASS, storage.getNodeInfo(testFolderInfo.getId()).getPseudoClass());
        assertEquals(0, storage.getNodeInfo(testFolderInfo.getId()).getVersion());
        assertEquals(Collections.singletonMap("k", "v"), storage.getNodeInfo(testFolderInfo.getId()).getStringMetadata());
        assertTrue(storage.getNodeInfo(testFolderInfo.getId()).getDoubleMetadata().isEmpty());
        assertTrue(storage.getNodeInfo(testFolderInfo.getId()).getIntMetadata().isEmpty());
        assertTrue(storage.getNodeInfo(testFolderInfo.getId()).getBooleanMetadata().isEmpty());
        assertEquals("", storage.getNodeInfo(testFolderInfo.getId()).getDescription());
        assertTrue(storage.getNodeInfo(testFolderInfo.getId()).getCreationTime() > 0);
        assertTrue(storage.getNodeInfo(testFolderInfo.getId()).getModificationTime() > 0);

        // check NodeId -> String -> NodeId
        assertEquals(testFolderInfo.getId(), storage.fromString(testFolderInfo.getId().toString()));

        // check test folder is empty
        assertTrue(storage.getChildNodes(testFolderInfo.getId()).isEmpty());

        // check root folder has one child (test folder)
        assertEquals(1, storage.getChildNodes(rootFolderInfo.getId()).size());
        assertEquals(testFolderInfo, storage.getChildNodes(rootFolderInfo.getId()).get(0));
        assertNotNull(storage.getChildNode(rootFolderInfo.getId(), "test"));
        assertEquals(testFolderInfo, storage.getChildNode(rootFolderInfo.getId(), "test"));

        // check getChildNode return null if child does not exist
        assertNull(storage.getChildNode(rootFolderInfo.getId(), "???"));

        // 3) check description initial value and update
        assertEquals("", testFolderInfo.getDescription());
        storage.setDescription(testFolderInfo.getId(), "hello");
        storage.flush();
        assertEquals("hello", storage.getNodeInfo(testFolderInfo.getId()).getDescription());

        // 4) create 2 data nodes in test folder
        NodeInfo testDataInfo = storage.createNode(testFolderInfo.getId(), "data", DATA_FILE_CLASS, "", 0, Collections.emptyMap(),
                Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap());
        NodeInfo testData2Info = storage.createNode(testFolderInfo.getId(), "data2", DATA_FILE_CLASS, "", 0, Collections.singletonMap("s1", "v1"),
                Collections.singletonMap("d1", 1d), Collections.singletonMap("i1", 2), Collections.singletonMap("b1", false));
        storage.flush();

        // check info are correctly stored even with metadata
        assertEquals(testData2Info, storage.getNodeInfo(testData2Info.getId()));

        // check test folder has 2 children
        assertEquals(2, storage.getChildNodes(testFolderInfo.getId()).size());

        // check data nodes initial dependency state
        assertTrue(storage.getDependencies(testDataInfo.getId()).isEmpty());
        assertTrue(storage.getDependencies(testData2Info.getId()).isEmpty());
        assertTrue(storage.getBackwardDependencies(testDataInfo.getId()).isEmpty());
        assertTrue(storage.getBackwardDependencies(testData2Info.getId()).isEmpty());

        // 5) create a dependency between data node and data node 2
        storage.addDependency(testDataInfo.getId(), "mylink", testData2Info.getId());
        storage.flush();

        // check dependency state
        assertEquals(1, storage.getDependencies(testDataInfo.getId()).size());
        assertEquals(testData2Info, storage.getDependencies(testDataInfo.getId()).get(0));
        assertEquals(1, storage.getBackwardDependencies(testData2Info.getId()).size());
        assertEquals(testDataInfo, storage.getBackwardDependencies(testData2Info.getId()).get(0));
        assertEquals(testData2Info, storage.getDependency(testDataInfo.getId(), "mylink"));
        assertNull(storage.getDependency(testDataInfo.getId(), "mylink2"));

        // 6) delete data node
        storage.deleteNode(testDataInfo.getId());
        storage.flush();

        // check test folder children have been correctly updated
        assertEquals(1, storage.getChildNodes(testFolderInfo.getId()).size());

        // check data node 2 backward dependency has been correctly updated
        assertTrue(storage.getBackwardDependencies(testData2Info.getId()).isEmpty());

        // 7) check data node 2 metadata value
        assertEquals(ImmutableMap.of("s1", "v1"), testData2Info.getStringMetadata());
        assertEquals(ImmutableMap.of("d1", 1d), testData2Info.getDoubleMetadata());
        assertEquals(ImmutableMap.of("i1", 2), testData2Info.getIntMetadata());
        assertEquals(ImmutableMap.of("b1", false), testData2Info.getBooleanMetadata());

        // check data node 2 string data write
        try (Writer writer = storage.writeStringData(testData2Info.getId(), "str")) {
            writer.write("word1");
        }
        storage.flush();
        try (Reader reader = storage.readStringData(testData2Info.getId(), "str")) {
            assertEquals("word1", CharStreams.toString(reader));
        }
        assertTrue(storage.dataExists(testData2Info.getId(), "str"));
        assertFalse(storage.dataExists(testData2Info.getId(), "str2"));

        // 8) check data node 2 binary data write
        try (OutputStream os = storage.writeBinaryData(testData2Info.getId(), "blob")) {
            os.write("word2".getBytes(StandardCharsets.UTF_8));
        }
        storage.flush();
        try (InputStream is = storage.readBinaryData(testData2Info.getId(), "blob")) {
            assertEquals("word2", new String(ByteStreams.toByteArray(is), StandardCharsets.UTF_8));

        }
        assertTrue(storage.dataExists(testData2Info.getId(), "blob"));
        assertFalse(storage.dataExists(testData2Info.getId(), "blob2"));

        // 9) check data source using pattern api
        DataSource ds = new AppStorageDataSource(storage, testData2Info.getId());
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

        assertFalse(ds.exists("file1"));

        // 10) check data source using file name api
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

        // 11) create double time series
        TimeSeriesMetadata metadata1 = new TimeSeriesMetadata("ts1",
                                                              TimeSeriesDataType.DOUBLE,
                                                              ImmutableMap.of("var1", "value1"),
                                                              RegularTimeSeriesIndex.create(Interval.parse("2015-01-01T00:00:00Z/2015-01-01T01:15:00Z"),
                                                                                            Duration.ofMinutes(15), 1, 1));
        storage.createTimeSeries(testData2Info.getId(), metadata1);
        storage.flush();

        // check double time series query
        assertEquals(Sets.newHashSet("ts1"), storage.getTimeSeriesNames(testData2Info.getId()));
        List<TimeSeriesMetadata> metadataList = storage.getTimeSeriesMetadata(testData2Info.getId(), Sets.newHashSet("ts1"));
        assertEquals(1, metadataList.size());
        assertEquals(metadata1, metadataList.get(0));

        // 12) add data to double time series
        storage.addDoubleTimeSeriesData(testData2Info.getId(), 0, "ts1", Arrays.asList(new UncompressedDoubleArrayChunk(2, new double[] {1d, 2d}),
                                                                                       new UncompressedDoubleArrayChunk(5, new double[] {3d})));
        storage.flush();

        // check double time series data query
        List<DoubleTimeSeries> doubleTimeSeries = storage.getDoubleTimeSeries(testData2Info.getId(), Sets.newHashSet("ts1"), 0);
        assertEquals(1, doubleTimeSeries.size());
        DoubleTimeSeries ts1 = doubleTimeSeries.get(0);
        assertArrayEquals(new double[] {Double.NaN, Double.NaN, 1d, 2d, Double.NaN, 3d}, ts1.toArray(), 0d);

        // 13) create a second string time series
        TimeSeriesMetadata metadata2 = new TimeSeriesMetadata("ts2",
                                                              TimeSeriesDataType.STRING,
                                                              ImmutableMap.of(),
                                                              RegularTimeSeriesIndex.create(Interval.parse("2015-01-01T00:00:00Z/2015-01-01T01:15:00Z"),
                                                                                            Duration.ofMinutes(15), 1, 1));
        storage.createTimeSeries(testData2Info.getId(), metadata2);
        storage.flush();

        // check string time series query
        assertEquals(Sets.newHashSet("ts1", "ts2"), storage.getTimeSeriesNames(testData2Info.getId()));
        metadataList = storage.getTimeSeriesMetadata(testData2Info.getId(), Sets.newHashSet("ts1"));
        assertEquals(1, metadataList.size());

        // 14) add data to double time series
        storage.addStringTimeSeriesData(testData2Info.getId(), 0, "ts2", Arrays.asList(new UncompressedStringArrayChunk(2, new String[] {"a", "b"}),
                                                                                       new UncompressedStringArrayChunk(5, new String[] {"c"})));
        storage.flush();

        // check string time series data query
        List<StringTimeSeries> stringTimeSeries = storage.getStringTimeSeries(testData2Info.getId(), Sets.newHashSet("ts2"), 0);
        assertEquals(1, stringTimeSeries.size());
        StringTimeSeries ts2 = stringTimeSeries.get(0);
        assertArrayEquals(new String[] {null, null, "a", "b", null, "c"}, ts2.toArray());

        // 15) remove all time series
        storage.removeAllTimeSeries(testData2Info.getId());
        storage.flush();

        // check there is no more time series
        assertTrue(storage.getTimeSeriesNames(testData2Info.getId()).isEmpty());
    }

    @Test
    public void parentChangeTest() throws IOException {
        // create root node and 2 folders
        NodeInfo rootFolderInfo = storage.createRootNodeIfNotExists(storage.getFileSystemName(), FOLDER_PSEUDO_CLASS);
        NodeInfo folder1Info = storage.createNode(rootFolderInfo.getId(), "test1", FOLDER_PSEUDO_CLASS, "", 0,
                Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap());
        NodeInfo folder2Info = storage.createNode(rootFolderInfo.getId(), "test2", FOLDER_PSEUDO_CLASS, "", 0,
                Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap());
        storage.flush();

        // create a file in folder 1
        NodeInfo fileInfo = storage.createNode(folder1Info.getId(), "file", "file-type", "", 0, Collections.emptyMap(),
                Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap());
        storage.flush();

        // check parent folder
        assertEquals(folder1Info, storage.getParentNode(fileInfo.getId()));

        // change parent to folder 2
        storage.setParentNode(fileInfo.getId(), folder2Info.getId());
        storage.flush();

        // check parent folder change
        assertEquals(folder2Info, storage.getParentNode(fileInfo.getId()));
    }

}
