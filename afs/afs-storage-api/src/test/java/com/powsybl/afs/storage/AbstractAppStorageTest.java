/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.storage;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.common.io.ByteStreams;
import com.powsybl.afs.storage.events.*;
import com.powsybl.commons.datasource.DataSource;
import com.powsybl.math.timeseries.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.threeten.extra.Interval;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static org.junit.Assert.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public abstract class AbstractAppStorageTest {

    private static final String FOLDER_PSEUDO_CLASS = "folder";
    private static final String DATA_FILE_CLASS = "data";

    private ListenableAppStorage storage;

    private BlockingQueue<NodeEvent> eventStack;

    private AppStorageListener l = eventList -> eventStack.addAll(eventList.getEvents());

    protected abstract AppStorage createStorage();

    @Before
    public void setUp() throws Exception {
        eventStack = new LinkedBlockingQueue<>();

        AppStorage storage = createStorage();
        if (storage instanceof ListenableAppStorage) {
            this.storage = (ListenableAppStorage) storage;
        } else {
            this.storage = new DefaultListenableAppStorage(storage);
        }
        this.storage.addListener(l);
    }

    @After
    public void tearDown() {
        storage.close();
    }

    @Test
    public void baseTest() throws IOException, InterruptedException {
        // 1) create root folder
        NodeInfo rootFolderInfo = storage.createRootNodeIfNotExists(storage.getFileSystemName(), FOLDER_PSEUDO_CLASS);
        storage.flush();

        // check event
        assertEquals(new NodeCreated(rootFolderInfo.getId(), null), eventStack.take());

        assertNotNull(rootFolderInfo);

        // assert root folder is writable
        assertTrue(storage.isWritable(rootFolderInfo.getId()));

        // assert root folder parent is null
        assertFalse(storage.getParentNode(rootFolderInfo.getId()).isPresent());

        // check root folder name and pseudo class is correct
        assertEquals(storage.getFileSystemName(), storage.getNodeInfo(rootFolderInfo.getId()).getName());
        assertEquals(FOLDER_PSEUDO_CLASS, storage.getNodeInfo(rootFolderInfo.getId()).getPseudoClass());

        // assert root folder is empty
        assertTrue(storage.getChildNodes(rootFolderInfo.getId()).isEmpty());

        // 2) create a test folder
        NodeInfo testFolderInfo = storage.createNode(rootFolderInfo.getId(), "test", FOLDER_PSEUDO_CLASS, "", 0,
                new NodeGenericMetadata().setString("k", "v"));
        storage.flush();

        // check event
        assertEquals(new NodeCreated(testFolderInfo.getId(), rootFolderInfo.getId()), eventStack.take());

        // assert parent of test folder is root folder
        assertEquals(rootFolderInfo, storage.getParentNode(testFolderInfo.getId()).orElseThrow(AssertionError::new));

        // check test folder infos are corrects
        assertEquals(testFolderInfo.getId(), storage.getNodeInfo(testFolderInfo.getId()).getId());
        assertEquals("test", storage.getNodeInfo(testFolderInfo.getId()).getName());
        assertEquals(FOLDER_PSEUDO_CLASS, storage.getNodeInfo(testFolderInfo.getId()).getPseudoClass());
        assertEquals(0, storage.getNodeInfo(testFolderInfo.getId()).getVersion());
        assertEquals(Collections.singletonMap("k", "v"), storage.getNodeInfo(testFolderInfo.getId()).getGenericMetadata().getStrings());
        assertTrue(storage.getNodeInfo(testFolderInfo.getId()).getGenericMetadata().getDoubles().isEmpty());
        assertTrue(storage.getNodeInfo(testFolderInfo.getId()).getGenericMetadata().getInts().isEmpty());
        assertTrue(storage.getNodeInfo(testFolderInfo.getId()).getGenericMetadata().getBooleans().isEmpty());
        assertEquals("", storage.getNodeInfo(testFolderInfo.getId()).getDescription());
        assertTrue(storage.getNodeInfo(testFolderInfo.getId()).getCreationTime() > 0);
        assertTrue(storage.getNodeInfo(testFolderInfo.getId()).getModificationTime() > 0);

        // check test folder is empty
        assertTrue(storage.getChildNodes(testFolderInfo.getId()).isEmpty());

        // check root folder has one child (test folder)
        assertEquals(1, storage.getChildNodes(rootFolderInfo.getId()).size());
        assertEquals(testFolderInfo, storage.getChildNodes(rootFolderInfo.getId()).get(0));
        assertTrue(storage.getChildNode(rootFolderInfo.getId(), "test").isPresent());
        assertEquals(testFolderInfo, storage.getChildNode(rootFolderInfo.getId(), "test").orElseThrow(AssertionError::new));

        // check getChildNode return null if child does not exist
        assertFalse(storage.getChildNode(rootFolderInfo.getId(), "???").isPresent());

        // 3) check description initial value and update
        assertEquals("", testFolderInfo.getDescription());
        storage.setDescription(testFolderInfo.getId(), "hello");
        storage.flush();

        // check event
        assertEquals(new NodeDescriptionUpdated(testFolderInfo.getId(), "hello"), eventStack.take());

        assertEquals("hello", storage.getNodeInfo(testFolderInfo.getId()).getDescription());

        // 4) check modifiation time update
        long oldModificationTime = testFolderInfo.getModificationTime();
        storage.updateModificationTime(testFolderInfo.getId());
        storage.flush();
        assertTrue(storage.getNodeInfo(testFolderInfo.getId()).getModificationTime() >= oldModificationTime);

        // 5) create 2 data nodes in test folder
        NodeInfo testDataInfo = storage.createNode(testFolderInfo.getId(), "data", DATA_FILE_CLASS, "", 0, new NodeGenericMetadata());
        NodeInfo testData2Info = storage.createNode(testFolderInfo.getId(), "data2", DATA_FILE_CLASS, "", 0,
                new NodeGenericMetadata().setString("s1", "v1")
                                         .setDouble("d1", 1d)
                                         .setInt("i1", 2)
                                         .setBoolean("b1", false));
        NodeInfo testData3Info = storage.createNode(testFolderInfo.getId(), "data3", DATA_FILE_CLASS, "", 0, new NodeGenericMetadata());
        storage.flush();

        // check events
        assertEquals(new NodeCreated(testDataInfo.getId(), testFolderInfo.getId()), eventStack.take());
        assertEquals(new NodeCreated(testData2Info.getId(), testFolderInfo.getId()), eventStack.take());
        assertEquals(new NodeCreated(testData3Info.getId(), testFolderInfo.getId()), eventStack.take());

        // check info are correctly stored even with metadata
        assertEquals(testData2Info, storage.getNodeInfo(testData2Info.getId()));

        // check test folder has 2 children
        assertEquals(3, storage.getChildNodes(testFolderInfo.getId()).size());

        // check data nodes initial dependency state
        assertTrue(storage.getDependencies(testDataInfo.getId()).isEmpty());
        assertTrue(storage.getDependencies(testData2Info.getId()).isEmpty());
        assertTrue(storage.getBackwardDependencies(testDataInfo.getId()).isEmpty());
        assertTrue(storage.getBackwardDependencies(testData2Info.getId()).isEmpty());

        // 6) create a dependency between data node and data node 2
        storage.addDependency(testDataInfo.getId(), "mylink", testData2Info.getId());
        storage.flush();

        // check event
        assertEquals(new DependencyAdded(testDataInfo.getId(), "mylink"), eventStack.take());
        assertEquals(new BackwardDependencyAdded(testData2Info.getId(), "mylink"), eventStack.take());

        // check dependency state
        assertEquals(ImmutableSet.of(new NodeDependency("mylink", testData2Info)), storage.getDependencies(testDataInfo.getId()));
        assertEquals(ImmutableSet.of(testDataInfo), storage.getBackwardDependencies(testData2Info.getId()));
        assertEquals(ImmutableSet.of(testData2Info), storage.getDependencies(testDataInfo.getId(), "mylink"));
        assertTrue(storage.getDependencies(testDataInfo.getId(), "mylink2").isEmpty());

        // 7) add then add a second dependency
        storage.addDependency(testDataInfo.getId(), "mylink2", testData2Info.getId());
        storage.flush();

        // check event
        assertEquals(new DependencyAdded(testDataInfo.getId(), "mylink2"), eventStack.take());
        assertEquals(new BackwardDependencyAdded(testData2Info.getId(), "mylink2"), eventStack.take());

        assertEquals(ImmutableSet.of(new NodeDependency("mylink", testData2Info), new NodeDependency("mylink2", testData2Info)), storage.getDependencies(testDataInfo.getId()));
        assertEquals(ImmutableSet.of(testDataInfo), storage.getBackwardDependencies(testData2Info.getId()));
        assertEquals(ImmutableSet.of(testData2Info), storage.getDependencies(testDataInfo.getId(), "mylink"));

        storage.removeDependency(testDataInfo.getId(), "mylink2", testData2Info.getId());
        storage.flush();

        // check event
        assertEquals(new DependencyRemoved(testDataInfo.getId(), "mylink2"), eventStack.take());
        assertEquals(new BackwardDependencyRemoved(testData2Info.getId(), "mylink2"), eventStack.take());

        assertEquals(ImmutableSet.of(new NodeDependency("mylink", testData2Info)), storage.getDependencies(testDataInfo.getId()));
        assertEquals(ImmutableSet.of(testDataInfo), storage.getBackwardDependencies(testData2Info.getId()));
        assertEquals(ImmutableSet.of(testData2Info), storage.getDependencies(testDataInfo.getId(), "mylink"));

        // 8) delete data node
        assertEquals(testFolderInfo.getId(), storage.deleteNode(testDataInfo.getId()));
        storage.flush();

        // check event
        assertEquals(new NodeRemoved(testDataInfo.getId(), testFolderInfo.getId()), eventStack.take());

        // check test folder children have been correctly updated
        assertEquals(2, storage.getChildNodes(testFolderInfo.getId()).size());

        // check data node 2 backward dependency has been correctly updated
        assertTrue(storage.getBackwardDependencies(testData2Info.getId()).isEmpty());

        // 9) check data node 2 metadata value
        assertEquals(ImmutableMap.of("s1", "v1"), testData2Info.getGenericMetadata().getStrings());
        assertEquals(ImmutableMap.of("d1", 1d), testData2Info.getGenericMetadata().getDoubles());
        assertEquals(ImmutableMap.of("i1", 2), testData2Info.getGenericMetadata().getInts());
        assertEquals(ImmutableMap.of("b1", false), testData2Info.getGenericMetadata().getBooleans());

        // 10) check data node 2 binary data write
        try (OutputStream os = storage.writeBinaryData(testData2Info.getId(), "blob")) {
            os.write("word2".getBytes(StandardCharsets.UTF_8));
        }
        storage.flush();

        // check event
        assertEquals(new NodeDataUpdated(testData2Info.getId(), "blob"), eventStack.take());

        try (InputStream is = storage.readBinaryData(testData2Info.getId(), "blob").orElseThrow(AssertionError::new)) {
            assertEquals("word2", new String(ByteStreams.toByteArray(is), StandardCharsets.UTF_8));

        }
        assertTrue(storage.dataExists(testData2Info.getId(), "blob"));
        assertFalse(storage.dataExists(testData2Info.getId(), "blob2"));
        assertEquals(ImmutableSet.of("blob"), storage.getDataNames(testData2Info.getId()));

        // 10 bis) check data remove
        assertFalse(storage.removeData(testData2Info.getId(), "blob2"));
        assertTrue(storage.removeData(testData2Info.getId(), "blob"));
        storage.flush();

        // check event
        assertEquals(new NodeDataRemoved(testData2Info.getId(), "blob"), eventStack.take());

        assertTrue(storage.getDataNames(testData2Info.getId()).isEmpty());
        assertFalse(storage.readBinaryData(testData2Info.getId(), "blob").isPresent());

        // 11) check data source using pattern api
        DataSource ds = new AppStorageDataSource(storage, testData2Info.getId());
        assertEquals("", ds.getBaseName());
        assertFalse(ds.exists(null, "ext"));
        try (OutputStream os = ds.newOutputStream(null, "ext", false)) {
            os.write("word1".getBytes(StandardCharsets.UTF_8));
        }
        storage.flush();

        // check event
        assertEquals(new NodeDataUpdated(testData2Info.getId(), "DATA_SOURCE_SUFFIX_EXT____ext"), eventStack.take());

        assertTrue(ds.exists(null, "ext"));
        try (InputStream ignored = ds.newInputStream(null, "ext2")) {
            fail();
        } catch (Exception ignored) {
        }
        try (InputStream is = ds.newInputStream(null, "ext")) {
            assertEquals("word1", new String(ByteStreams.toByteArray(is), StandardCharsets.UTF_8));
        }

        assertFalse(ds.exists("file1"));

        // 12) check data source using file name api
        try (OutputStream os = ds.newOutputStream("file1", false)) {
            os.write("word1".getBytes(StandardCharsets.UTF_8));
        }
        storage.flush();

        // check event
        assertEquals(new NodeDataUpdated(testData2Info.getId(), "DATA_SOURCE_FILE_NAME__file1"), eventStack.take());

        assertTrue(ds.exists("file1"));
        try (InputStream ignored = ds.newInputStream("file2")) {
            fail();
        } catch (Exception ignored) {
        }
        try (InputStream is = ds.newInputStream("file1")) {
            assertEquals("word1", new String(ByteStreams.toByteArray(is), StandardCharsets.UTF_8));
        }

        // 13) create double time series
        TimeSeriesMetadata metadata1 = new TimeSeriesMetadata("ts1",
                                                              TimeSeriesDataType.DOUBLE,
                                                              ImmutableMap.of("var1", "value1"),
                                                              RegularTimeSeriesIndex.create(Interval.parse("2015-01-01T00:00:00Z/2015-01-01T01:15:00Z"),
                                                                                            Duration.ofMinutes(15)));
        storage.createTimeSeries(testData2Info.getId(), metadata1);
        storage.flush();

        // check event
        assertEquals(new TimeSeriesCreated(testData2Info.getId(), "ts1"), eventStack.take());

        // check double time series query
        assertEquals(Sets.newHashSet("ts1"), storage.getTimeSeriesNames(testData2Info.getId()));
        assertTrue(storage.timeSeriesExists(testData2Info.getId(), "ts1"));
        assertFalse(storage.timeSeriesExists(testData2Info.getId(), "ts9"));
        assertFalse(storage.timeSeriesExists(testData3Info.getId(), "ts1"));
        List<TimeSeriesMetadata> metadataList = storage.getTimeSeriesMetadata(testData2Info.getId(), Sets.newHashSet("ts1"));
        assertEquals(1, metadataList.size());
        assertEquals(metadata1, metadataList.get(0));
        assertTrue(storage.getTimeSeriesMetadata(testData3Info.getId(), Sets.newHashSet("ts1")).isEmpty());

        // 14) add data to double time series
        storage.addDoubleTimeSeriesData(testData2Info.getId(), 0, "ts1", Arrays.asList(new UncompressedDoubleArrayChunk(2, new double[] {1d, 2d}),
                                                                                       new UncompressedDoubleArrayChunk(5, new double[] {3d})));
        storage.flush();

        // check event
        assertEquals(new TimeSeriesDataUpdated(testData2Info.getId(), "ts1"), eventStack.take());

        // check versions
        assertEquals(ImmutableSet.of(0), storage.getTimeSeriesDataVersions(testData2Info.getId()));
        assertEquals(ImmutableSet.of(0), storage.getTimeSeriesDataVersions(testData2Info.getId(), "ts1"));

        // check double time series data query
        List<DoubleTimeSeries> doubleTimeSeries = storage.getDoubleTimeSeries(testData2Info.getId(), Sets.newHashSet("ts1"), 0);
        assertEquals(1, doubleTimeSeries.size());
        DoubleTimeSeries ts1 = doubleTimeSeries.get(0);
        assertArrayEquals(new double[] {Double.NaN, Double.NaN, 1d, 2d, Double.NaN, 3d}, ts1.toArray(), 0d);
        assertTrue(storage.getDoubleTimeSeries(testData3Info.getId(), Sets.newHashSet("ts1"), 0).isEmpty());

        // 15) create a second string time series
        TimeSeriesMetadata metadata2 = new TimeSeriesMetadata("ts2",
                                                              TimeSeriesDataType.STRING,
                                                              ImmutableMap.of(),
                                                              RegularTimeSeriesIndex.create(Interval.parse("2015-01-01T00:00:00Z/2015-01-01T01:15:00Z"),
                                                                                            Duration.ofMinutes(15)));
        storage.createTimeSeries(testData2Info.getId(), metadata2);
        storage.flush();

        // check event
        assertEquals(new TimeSeriesCreated(testData2Info.getId(), "ts2"), eventStack.take());

        // check string time series query
        assertEquals(Sets.newHashSet("ts1", "ts2"), storage.getTimeSeriesNames(testData2Info.getId()));
        metadataList = storage.getTimeSeriesMetadata(testData2Info.getId(), Sets.newHashSet("ts1"));
        assertEquals(1, metadataList.size());

        // 16) add data to double time series
        storage.addStringTimeSeriesData(testData2Info.getId(), 0, "ts2", Arrays.asList(new UncompressedStringArrayChunk(2, new String[] {"a", "b"}),
                                                                                       new UncompressedStringArrayChunk(5, new String[] {"c"})));
        storage.flush();

        // check event
        assertEquals(new TimeSeriesDataUpdated(testData2Info.getId(), "ts2"), eventStack.take());

        // check string time series data query
        List<StringTimeSeries> stringTimeSeries = storage.getStringTimeSeries(testData2Info.getId(), Sets.newHashSet("ts2"), 0);
        assertEquals(1, stringTimeSeries.size());
        StringTimeSeries ts2 = stringTimeSeries.get(0);
        assertArrayEquals(new String[] {null, null, "a", "b", null, "c"}, ts2.toArray());

        // 17) clear time series
        storage.clearTimeSeries(testData2Info.getId());
        storage.flush();

        // check event
        assertEquals(new TimeSeriesCleared(testData2Info.getId()), eventStack.take());

        // check there is no more time series
        assertTrue(storage.getTimeSeriesNames(testData2Info.getId()).isEmpty());

        // assert all events have been checked
        assertTrue(eventStack.isEmpty());
    }

    @Test
    public void parentChangeTest() throws InterruptedException {
        // create root node and 2 folders
        NodeInfo rootFolderInfo = storage.createRootNodeIfNotExists(storage.getFileSystemName(), FOLDER_PSEUDO_CLASS);
        NodeInfo folder1Info = storage.createNode(rootFolderInfo.getId(), "test1", FOLDER_PSEUDO_CLASS, "", 0, new NodeGenericMetadata());
        NodeInfo folder2Info = storage.createNode(rootFolderInfo.getId(), "test2", FOLDER_PSEUDO_CLASS, "", 0, new NodeGenericMetadata());
        storage.flush();

        eventStack.take();
        eventStack.take();
        eventStack.take();

        // create a file in folder 1
        NodeInfo fileInfo = storage.createNode(folder1Info.getId(), "file", "file-type", "", 0, new NodeGenericMetadata());
        storage.flush();

        eventStack.take();

        // check parent folder
        assertEquals(folder1Info, storage.getParentNode(fileInfo.getId()).orElseThrow(AssertionError::new));

        // change parent to folder 2
        storage.setParentNode(fileInfo.getId(), folder2Info.getId());
        storage.flush();

        // check event
        assertEquals(new ParentChanged(fileInfo.getId()), eventStack.take());

        // check parent folder change
        assertEquals(folder2Info, storage.getParentNode(fileInfo.getId()).orElseThrow(AssertionError::new));

        // assert all events have been checked
        assertTrue(eventStack.isEmpty());
    }

    @Test
    public void deleteNodeTest() throws InterruptedException {
        // create root node and 2 folders
        NodeInfo rootFolderInfo = storage.createRootNodeIfNotExists(storage.getFileSystemName(), FOLDER_PSEUDO_CLASS);
        NodeInfo folder1Info = storage.createNode(rootFolderInfo.getId(), "test1", FOLDER_PSEUDO_CLASS, "", 0, new NodeGenericMetadata());
        NodeInfo folder2Info = storage.createNode(rootFolderInfo.getId(), "test2", FOLDER_PSEUDO_CLASS, "", 0, new NodeGenericMetadata());
        storage.flush();

        eventStack.take();
        eventStack.take();
        eventStack.take();

        storage.addDependency(folder1Info.getId(), "dep", folder2Info.getId());
        storage.addDependency(folder1Info.getId(), "dep2", folder2Info.getId());
        storage.flush();

        eventStack.take();
        eventStack.take();

        assertEquals(Collections.singleton(folder2Info), storage.getDependencies(folder1Info.getId(), "dep"));
        assertEquals(Collections.singleton(folder2Info), storage.getDependencies(folder1Info.getId(), "dep2"));

        storage.deleteNode(folder2Info.getId());

        assertTrue(storage.getDependencies(folder1Info.getId(), "dep").isEmpty());
        assertTrue(storage.getDependencies(folder1Info.getId(), "dep2").isEmpty());
    }
}
