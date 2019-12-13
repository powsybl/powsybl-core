/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.storage;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.common.io.ByteStreams;
import com.powsybl.afs.storage.events.*;
import com.powsybl.commons.datasource.DataSource;
import com.powsybl.timeseries.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.threeten.extra.Interval;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public abstract class AbstractAppStorageTest {

    protected static final String FOLDER_PSEUDO_CLASS = "folder";
    static final String DATA_FILE_CLASS = "data";

    protected AppStorage storage;

    protected BlockingQueue<NodeEvent> eventStack;

    protected AppStorageListener l = eventList -> eventStack.addAll(eventList.getEvents());

    protected abstract AppStorage createStorage();

    @Before
    public void setUp() throws Exception {
        eventStack = new LinkedBlockingQueue<>();
        this.storage = createStorage();
        this.storage.getEventsBus().addListener(l);
    }

    @After
    public void tearDown() {
        storage.close();
    }

    private void assertEventStack(NodeEvent... events) throws InterruptedException {
        for (NodeEvent event : events) {
            assertEquals(event, eventStack.take());
        }

        // assert all events have been checked
        assertTrue(eventStack.isEmpty());
    }

    private void discardEvents(int count) throws InterruptedException {
        for (int i = 0; i < count; i++) {
            eventStack.take();
        }
    }

    @Test
    public void test() throws IOException, InterruptedException {
        // 1) create root folder
        NodeInfo rootFolderInfo = storage.createRootNodeIfNotExists(storage.getFileSystemName(), FOLDER_PSEUDO_CLASS);
        storage.flush();

        assertTrue(storage.isConsistent(rootFolderInfo.getId()));

        // check event
        assertEventStack(new NodeCreated(rootFolderInfo.getId(), null), new NodeConsistent(rootFolderInfo.getId()));

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

        assertFalse(storage.isConsistent(testFolderInfo.getId()));
        assertEquals(1, storage.getInconsistentNodes().size());
        assertEquals(testFolderInfo.getId(), storage.getInconsistentNodes().get(0).getId());

        storage.setConsistent(testFolderInfo.getId());
        storage.flush();

        assertTrue(storage.isConsistent(testFolderInfo.getId()));
        assertTrue(storage.getInconsistentNodes().isEmpty());

        // check event
        assertEventStack(new NodeCreated(testFolderInfo.getId(), rootFolderInfo.getId()), new NodeConsistent(testFolderInfo.getId()));

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
        assertEventStack(new NodeDescriptionUpdated(testFolderInfo.getId(), "hello"));

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

        storage.setConsistent(testDataInfo.getId());
        storage.setConsistent(testData2Info.getId());
        storage.setConsistent(testData3Info.getId());
        storage.flush();

        // check events
        assertEventStack(new NodeCreated(testDataInfo.getId(), testFolderInfo.getId()),
                     new NodeCreated(testData2Info.getId(), testFolderInfo.getId()),
                     new NodeCreated(testData3Info.getId(), testFolderInfo.getId()),
                     new NodeConsistent(testDataInfo.getId()),
                     new NodeConsistent(testData2Info.getId()),
                     new NodeConsistent(testData3Info.getId()));

        // check info are correctly stored even with metadata
        assertEquals(testData2Info, storage.getNodeInfo(testData2Info.getId()));

        // check test folder has 3 children
        assertEquals(3, storage.getChildNodes(testFolderInfo.getId()).size());

        // check data nodes initial dependency state
        assertTrue(storage.getDependencies(testDataInfo.getId()).isEmpty());
        assertTrue(storage.getDependencies(testData2Info.getId()).isEmpty());
        assertTrue(storage.getBackwardDependencies(testDataInfo.getId()).isEmpty());
        assertTrue(storage.getBackwardDependencies(testData2Info.getId()).isEmpty());

        // 5b) create named data items in test folder
        DataSource ds1 = new AppStorageDataSource(storage, testFolderInfo.getId(), testFolderInfo.getName());
        try (Writer writer = new OutputStreamWriter(storage.writeBinaryData(testFolderInfo.getId(), "testData1"), StandardCharsets.UTF_8)) {
            writer.write("Content for testData1");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        try (Writer writer = new OutputStreamWriter(storage.writeBinaryData(testFolderInfo.getId(), "testData2"), StandardCharsets.UTF_8)) {
            writer.write("Content for testData2");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        try (Writer writer = new OutputStreamWriter(storage.writeBinaryData(testFolderInfo.getId(), "dataTest3"), StandardCharsets.UTF_8)) {
            writer.write("Content for dataTest3");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        storage.flush();

        // check events
        assertEventStack(new NodeDataUpdated(testFolderInfo.getId(), "testData1"),
                new NodeDataUpdated(testFolderInfo.getId(), "testData2"),
                new NodeDataUpdated(testFolderInfo.getId(), "dataTest3"));

        // check data names
        assertEquals(ImmutableSet.of("testData2", "testData1", "dataTest3"), storage.getDataNames(testFolderInfo.getId()));

        // check data names seen from data source
        assertEquals(ImmutableSet.of("testData2", "testData1"), ds1.listNames("^testD.*"));

        // check children names (not data names)
        List<String> expectedChildrenNames = ImmutableList.of("data", "data2", "data3");
        List<String> actualChildrenNames = storage.getChildNodes(testFolderInfo.getId()).stream()
                .map(n -> n.getName()).collect(Collectors.toList());
        assertEquals(expectedChildrenNames, actualChildrenNames);

        // 6) create a dependency between data node and data node 2
        storage.addDependency(testDataInfo.getId(), "mylink", testData2Info.getId());
        storage.flush();

        // check event
        assertEventStack(new DependencyAdded(testDataInfo.getId(), "mylink"),
                         new BackwardDependencyAdded(testData2Info.getId(), "mylink"));

        // check dependency state
        assertEquals(ImmutableSet.of(new NodeDependency("mylink", testData2Info)), storage.getDependencies(testDataInfo.getId()));
        assertEquals(ImmutableSet.of(testDataInfo), storage.getBackwardDependencies(testData2Info.getId()));
        assertEquals(ImmutableSet.of(testData2Info), storage.getDependencies(testDataInfo.getId(), "mylink"));
        assertTrue(storage.getDependencies(testDataInfo.getId(), "mylink2").isEmpty());

        // 7) add then add a second dependency
        storage.addDependency(testDataInfo.getId(), "mylink2", testData2Info.getId());
        storage.flush();

        // check event
        assertEventStack(new DependencyAdded(testDataInfo.getId(), "mylink2"),
                         new BackwardDependencyAdded(testData2Info.getId(), "mylink2"));

        assertEquals(ImmutableSet.of(new NodeDependency("mylink", testData2Info), new NodeDependency("mylink2", testData2Info)), storage.getDependencies(testDataInfo.getId()));
        assertEquals(ImmutableSet.of(testDataInfo), storage.getBackwardDependencies(testData2Info.getId()));
        assertEquals(ImmutableSet.of(testData2Info), storage.getDependencies(testDataInfo.getId(), "mylink"));

        storage.removeDependency(testDataInfo.getId(), "mylink2", testData2Info.getId());
        storage.flush();

        // check event
        assertEventStack(new DependencyRemoved(testDataInfo.getId(), "mylink2"),
                         new BackwardDependencyRemoved(testData2Info.getId(), "mylink2"));

        assertEquals(ImmutableSet.of(new NodeDependency("mylink", testData2Info)), storage.getDependencies(testDataInfo.getId()));
        assertEquals(ImmutableSet.of(testDataInfo), storage.getBackwardDependencies(testData2Info.getId()));
        assertEquals(ImmutableSet.of(testData2Info), storage.getDependencies(testDataInfo.getId(), "mylink"));

        // 8) delete data node
        assertEquals(testFolderInfo.getId(), storage.deleteNode(testDataInfo.getId()));
        storage.flush();

        // check event
        assertEventStack(new NodeRemoved(testDataInfo.getId(), testFolderInfo.getId()));

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
        assertEventStack(new NodeDataUpdated(testData2Info.getId(), "blob"));

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
        assertEventStack(new NodeDataRemoved(testData2Info.getId(), "blob"));

        assertTrue(storage.getDataNames(testData2Info.getId()).isEmpty());
        assertFalse(storage.readBinaryData(testData2Info.getId(), "blob").isPresent());

        // 11) check data source using pattern api
        DataSource ds = new AppStorageDataSource(storage, testData2Info.getId(), testData2Info.getName());
        assertEquals(testData2Info.getName(), ds.getBaseName());
        assertFalse(ds.exists(null, "ext"));
        try (OutputStream os = ds.newOutputStream(null, "ext", false)) {
            os.write("word1".getBytes(StandardCharsets.UTF_8));
        }
        storage.flush();

        // check event
        assertEventStack(new NodeDataUpdated(testData2Info.getId(), "DATA_SOURCE_SUFFIX_EXT____ext"));

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
        assertEventStack(new NodeDataUpdated(testData2Info.getId(), "DATA_SOURCE_FILE_NAME__file1"));

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
        assertEventStack(new TimeSeriesCreated(testData2Info.getId(), "ts1"));

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
        storage.addDoubleTimeSeriesData(testData2Info.getId(), 0, "ts1", Arrays.asList(new UncompressedDoubleDataChunk(2, new double[] {1d, 2d}),
                                                                                       new UncompressedDoubleDataChunk(5, new double[] {3d})));
        storage.flush();

        // check event
        assertEventStack(new TimeSeriesDataUpdated(testData2Info.getId(), "ts1"));

        // check versions
        assertEquals(ImmutableSet.of(0), storage.getTimeSeriesDataVersions(testData2Info.getId()));
        assertEquals(ImmutableSet.of(0), storage.getTimeSeriesDataVersions(testData2Info.getId(), "ts1"));

        // check double time series data query
        Map<String, List<DoubleDataChunk>> doubleTimeSeriesData = storage.getDoubleTimeSeriesData(testData2Info.getId(), Sets.newHashSet("ts1"), 0);
        assertEquals(1, doubleTimeSeriesData.size());
        assertEquals(Arrays.asList(new UncompressedDoubleDataChunk(2, new double[] {1d, 2d}),
                                   new UncompressedDoubleDataChunk(5, new double[] {3d})),
                     doubleTimeSeriesData.get("ts1"));
        assertTrue(storage.getDoubleTimeSeriesData(testData3Info.getId(), Sets.newHashSet("ts1"), 0).isEmpty());

        // 15) create a second string time series
        TimeSeriesMetadata metadata2 = new TimeSeriesMetadata("ts2",
                                                              TimeSeriesDataType.STRING,
                                                              ImmutableMap.of(),
                                                              RegularTimeSeriesIndex.create(Interval.parse("2015-01-01T00:00:00Z/2015-01-01T01:15:00Z"),
                                                                                            Duration.ofMinutes(15)));
        storage.createTimeSeries(testData2Info.getId(), metadata2);
        storage.flush();

        // check event
        assertEventStack(new TimeSeriesCreated(testData2Info.getId(), "ts2"));

        // check string time series query
        assertEquals(Sets.newHashSet("ts1", "ts2"), storage.getTimeSeriesNames(testData2Info.getId()));
        metadataList = storage.getTimeSeriesMetadata(testData2Info.getId(), Sets.newHashSet("ts1"));
        assertEquals(1, metadataList.size());

        // 16) add data to double time series
        storage.addStringTimeSeriesData(testData2Info.getId(), 0, "ts2", Arrays.asList(new UncompressedStringDataChunk(2, new String[] {"a", "b"}),
                                                                                       new UncompressedStringDataChunk(5, new String[] {"c"})));
        storage.flush();

        // check event
        assertEventStack(new TimeSeriesDataUpdated(testData2Info.getId(), "ts2"));

        // check string time series data query
        Map<String, List<StringDataChunk>> stringTimeSeriesData = storage.getStringTimeSeriesData(testData2Info.getId(), Sets.newHashSet("ts2"), 0);
        assertEquals(1, stringTimeSeriesData.size());
        assertEquals(Arrays.asList(new UncompressedStringDataChunk(2, new String[] {"a", "b"}),
                                   new UncompressedStringDataChunk(5, new String[] {"c"})),
                     stringTimeSeriesData.get("ts2"));

        // 17) clear time series
        storage.clearTimeSeries(testData2Info.getId());
        storage.flush();

        // check event
        assertEventStack(new TimeSeriesCleared(testData2Info.getId()));

        // check there is no more time series
        assertTrue(storage.getTimeSeriesNames(testData2Info.getId()).isEmpty());

        // 18) change parent test
        NodeInfo folder1Info = storage.createNode(rootFolderInfo.getId(), "test1", FOLDER_PSEUDO_CLASS, "", 0, new NodeGenericMetadata());
        NodeInfo folder2Info = storage.createNode(rootFolderInfo.getId(), "test2", FOLDER_PSEUDO_CLASS, "", 0, new NodeGenericMetadata());
        storage.setConsistent(folder1Info.getId());
        storage.setConsistent(folder2Info.getId());
        storage.flush();

        discardEvents(4);

        // create a file in folder 1
        NodeInfo fileInfo = storage.createNode(folder1Info.getId(), "file", "file-type", "", 0, new NodeGenericMetadata());
        storage.setConsistent(fileInfo.getId());
        storage.flush();

        discardEvents(2);

        // check parent folder
        assertEquals(folder1Info, storage.getParentNode(fileInfo.getId()).orElseThrow(AssertionError::new));

        // change parent to folder 2
        storage.setParentNode(fileInfo.getId(), folder2Info.getId());
        storage.flush();

        // check event
        assertEventStack(new ParentChanged(fileInfo.getId()));

        // check parent folder change
        assertEquals(folder2Info, storage.getParentNode(fileInfo.getId()).orElseThrow(AssertionError::new));

        // 18) delete node test

        // create root node and 2 folders
        NodeInfo folder3Info = storage.createNode(rootFolderInfo.getId(), "test3", FOLDER_PSEUDO_CLASS, "", 0, new NodeGenericMetadata());
        NodeInfo folder4Info = storage.createNode(rootFolderInfo.getId(), "test4", FOLDER_PSEUDO_CLASS, "", 0, new NodeGenericMetadata());
        storage.setConsistent(folder3Info.getId());
        storage.setConsistent(folder4Info.getId());
        storage.flush();

        discardEvents(2);

        storage.addDependency(folder3Info.getId(), "dep", folder4Info.getId());
        storage.addDependency(folder3Info.getId(), "dep2", folder4Info.getId());
        storage.flush();

        discardEvents(2);

        assertEquals(Collections.singleton(folder4Info), storage.getDependencies(folder3Info.getId(), "dep"));
        assertEquals(Collections.singleton(folder4Info), storage.getDependencies(folder3Info.getId(), "dep2"));

        storage.deleteNode(folder4Info.getId());

        assertTrue(storage.getDependencies(folder3Info.getId(), "dep").isEmpty());
        assertTrue(storage.getDependencies(folder3Info.getId(), "dep2").isEmpty());

        // 19) rename node test
        NodeInfo folder5Info = storage.createNode(rootFolderInfo.getId(), "test5", FOLDER_PSEUDO_CLASS, "", 0, new NodeGenericMetadata());
        storage.setConsistent(folder5Info.getId());
        NodeInfo folder51Info = storage.createNode(folder5Info.getId(), "child_of_test5", FOLDER_PSEUDO_CLASS, "", 0, new NodeGenericMetadata());
        NodeInfo folder52Info = storage.createNode(folder5Info.getId(), "another_child_of_test5", FOLDER_PSEUDO_CLASS, "", 0, new NodeGenericMetadata());
        storage.setConsistent(folder51Info.getId());
        storage.setConsistent(folder52Info.getId());
        storage.flush();

        String newName = "newtest5";

        storage.renameNode(folder5Info.getId(), newName);
        storage.flush();
        folder5Info = storage.getNodeInfo(folder5Info.getId());
        assertEquals(newName, folder5Info.getName());
        assertEquals(2, storage.getChildNodes(folder5Info.getId()).size());
        assertTrue(storage.getChildNode(folder5Info.getId(), "child_of_test5").isPresent());
        assertTrue(storage.getChildNode(folder5Info.getId(), "another_child_of_test5").isPresent());

        NodeInfo folder6Info = storage.createNode(rootFolderInfo.getId(), "test6", FOLDER_PSEUDO_CLASS, "", 0, new NodeGenericMetadata());
        storage.setConsistent(folder6Info.getId());
        try {
            storage.renameNode(folder6Info.getId(), null);
            fail();
        } catch (Exception ignored) {
        }

        NodeInfo folder7Info = storage.createNode(rootFolderInfo.getId(), "test7", FOLDER_PSEUDO_CLASS, "", 0, new NodeGenericMetadata());
        storage.setConsistent(folder7Info.getId());
        try {
            storage.renameNode(folder7Info.getId(), "");
            fail();
        } catch (Exception ignored) {
        }

        testUpdateNodeMetadata(rootFolderInfo, storage);

        // 19 check that eventsBus is not null
        assertNotNull(storage.getEventsBus());

        storage.getEventsBus().pushEvent(new NodeCreated("test", "test"), "test useful for RemoteStorage event push");
    }

    protected void testUpdateNodeMetadata(NodeInfo rootFolderInfo, AppStorage storage) throws InterruptedException {
        NodeGenericMetadata metadata = new NodeGenericMetadata();
        NodeInfo node = storage.createNode(rootFolderInfo.getId(), "testNode", "unknownFile", "", 0, cloneMetadata(metadata));
        storage.setConsistent(node.getId());

        storage.flush();

        checkMetadataEquality(metadata, node.getGenericMetadata());

        discardEvents(18);

        storage.setMetadata(node.getId(), cloneMetadata(metadata));
        storage.flush();
        assertEventStack(new NodeMetadataUpdated(node.getId(), metadata));

        metadata.setString("test", "test");
        assertThat(node.getGenericMetadata().getStrings().keySet().size()).isEqualTo(0);

        storage.setMetadata(node.getId(), cloneMetadata(metadata));
        storage.flush();
        assertEventStack(new NodeMetadataUpdated(node.getId(), metadata));
        node = storage.getNodeInfo(node.getId());
        checkMetadataEquality(metadata, node.getGenericMetadata());
        node = storage.getChildNode(rootFolderInfo.getId(), "testNode").get();
        checkMetadataEquality(metadata, node.getGenericMetadata());

        metadata.setBoolean("test1", true);
        storage.setMetadata(node.getId(), cloneMetadata(metadata));
        storage.flush();
        assertEventStack(new NodeMetadataUpdated(node.getId(), metadata));
        node = storage.getNodeInfo(node.getId());
        checkMetadataEquality(metadata, node.getGenericMetadata());
        node = storage.getChildNode(rootFolderInfo.getId(), "testNode").get();
        checkMetadataEquality(metadata, node.getGenericMetadata());

        metadata.getStrings().remove("test");
        metadata.setDouble("test2", 0.1);
        storage.setMetadata(node.getId(), cloneMetadata(metadata));
        storage.flush();
        assertEventStack(new NodeMetadataUpdated(node.getId(), metadata));
        node = storage.getNodeInfo(node.getId());
        checkMetadataEquality(metadata, node.getGenericMetadata());
        node = storage.getChildNode(rootFolderInfo.getId(), "testNode").get();
        checkMetadataEquality(metadata, node.getGenericMetadata());

        metadata.setInt("test3", 1);
        storage.setMetadata(node.getId(), cloneMetadata(metadata));
        storage.flush();
        assertEventStack(new NodeMetadataUpdated(node.getId(), metadata));
        node = storage.getNodeInfo(node.getId());
        checkMetadataEquality(metadata, node.getGenericMetadata());
        node = storage.getChildNode(rootFolderInfo.getId(), "testNode").get();
        checkMetadataEquality(metadata, node.getGenericMetadata());

        storage.deleteNode(node.getId());
        storage.flush();
    }

    private void checkMetadataEquality(NodeGenericMetadata source, NodeGenericMetadata target) {
        assertThat(target).isNotNull();
        assertThat(source.getBooleans().keySet().size()).isEqualTo(target.getBooleans().keySet().size());
        source.getBooleans().forEach((key, val) -> {
            assertThat(target.getBooleans()).contains(new HashMap.SimpleEntry<>(key, val));
        });
        assertThat(source.getStrings().keySet().size()).isEqualTo(target.getStrings().keySet().size());
        source.getStrings().forEach((key, val) -> {
            assertThat(target.getStrings()).contains(new HashMap.SimpleEntry<>(key, val));
        });
        assertThat(source.getInts().keySet().size()).isEqualTo(target.getInts().keySet().size());
        source.getInts().forEach((key, val) -> {
            assertThat(target.getInts()).contains(new HashMap.SimpleEntry<>(key, val));
        });
        assertThat(source.getDoubles().keySet().size()).isEqualTo(target.getDoubles().keySet().size());
        source.getDoubles().forEach((key, val) -> {
            assertThat(target.getDoubles()).contains(new HashMap.SimpleEntry<>(key, val));
        });
    }

    private NodeGenericMetadata cloneMetadata(NodeGenericMetadata metadata) {
        NodeGenericMetadata clone = new NodeGenericMetadata();
        clone.getStrings().putAll(metadata.getStrings());
        clone.getBooleans().putAll(metadata.getBooleans());
        clone.getInts().putAll(metadata.getInts());
        clone.getDoubles().putAll(metadata.getDoubles());
        return clone;
    }
}
