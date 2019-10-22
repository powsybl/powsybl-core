/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.storage;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.google.common.io.ByteStreams;
import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.timeseries.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.threeten.extra.Interval;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public abstract class AbstractAppStorageArchiveTest {

    private AppStorage storage;

    private AppStorage storage2;

    private FileSystem fileSystem;

    @Before
    public void setUp() {
        storage = createStorage();
        storage2 = createStorage();
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
    }

    @After
    public void tearDown() throws IOException {
        fileSystem.close();
        storage.close();
        storage2.close();
    }

    protected abstract AppStorage createStorage();

    @Test
    public void archive() throws IOException  {
        // create test case
        NodeInfo rootFolderInfo = storage.createRootNodeIfNotExists(storage.getFileSystemName(), AbstractAppStorageTest.FOLDER_PSEUDO_CLASS);
        storage.setConsistent(rootFolderInfo.getId());

        NodeInfo folder1Info = storage.createNode(rootFolderInfo.getId(), "folder1", AbstractAppStorageTest.FOLDER_PSEUDO_CLASS, "", 0, new NodeGenericMetadata());
        storage.setConsistent(folder1Info.getId());
        NodeInfo file1Info = storage.createNode(folder1Info.getId(), "file1", AbstractAppStorageTest.DATA_FILE_CLASS, "", 0, new NodeGenericMetadata().setInt("i1", 1));
        storage.setConsistent(file1Info.getId());

        try (OutputStream os = storage.writeBinaryData(file1Info.getId(), "data1")) {
            os.write("hello".getBytes(StandardCharsets.UTF_8));
        }

        TimeSeriesMetadata metadata1 = new TimeSeriesMetadata("ts1 hello",
                                                              TimeSeriesDataType.DOUBLE,
                                                              ImmutableMap.of("var1", "value1"),
                                                              RegularTimeSeriesIndex.create(Interval.parse("2015-01-01T00:00:00Z/2015-01-01T01:15:00Z"),
                                                                                            Duration.ofMinutes(15)));
        storage.createTimeSeries(file1Info.getId(), metadata1);
        List<DoubleDataChunk> chunks = Arrays.asList(new UncompressedDoubleDataChunk(2, new double[]{1d, 2d}),
                                                     new UncompressedDoubleDataChunk(5, new double[]{3d}));
        storage.addDoubleTimeSeriesData(file1Info.getId(), 0, "ts1 hello", chunks);

        NodeInfo folder2Info = storage.createNode(rootFolderInfo.getId(), "folder2", AbstractAppStorageTest.FOLDER_PSEUDO_CLASS, "", 0, new NodeGenericMetadata());
        storage.setConsistent(folder2Info.getId());
        NodeInfo file2Info = storage.createNode(folder2Info.getId(), "file2", AbstractAppStorageTest.DATA_FILE_CLASS, "", 0, new NodeGenericMetadata()
                .setString("s1", "a"));
        storage.setConsistent(file2Info.getId());

        storage.addDependency(file1Info.getId(), "dependency1", file2Info.getId());

        storage.flush();

        // archive
        Path workDir = fileSystem.getPath("/work");
        new AppStorageArchive(storage).archiveChildren(rootFolderInfo, workDir);

        // unarchive to the second storage
        NodeInfo newRootFolderInfo = storage2.createRootNodeIfNotExists(storage2.getFileSystemName(), AbstractAppStorageTest.FOLDER_PSEUDO_CLASS);
        storage2.setConsistent(newRootFolderInfo.getId());

        new AppStorageArchive(storage2).unarchiveChildren(newRootFolderInfo, workDir);

        // check we have same data in storage and storage2
        NodeInfo rootFolderInfo2 = storage2.createRootNodeIfNotExists(storage.getFileSystemName(), AbstractAppStorageTest.FOLDER_PSEUDO_CLASS);
        storage2.setConsistent(rootFolderInfo2.getId());

        assertEquals(2, storage2.getChildNodes(rootFolderInfo2.getId()).size());
        NodeInfo newFolder1Info = storage2.getChildNode(rootFolderInfo2.getId(), "folder1").orElse(null);
        assertNotNull(newFolder1Info);
        assertEquals("folder1", newFolder1Info.getName());
        assertEquals(AbstractAppStorageTest.FOLDER_PSEUDO_CLASS, newFolder1Info.getPseudoClass());
        assertTrue(newFolder1Info.getDescription().isEmpty());
        assertEquals(0, newFolder1Info.getVersion());
        assertEquals(new NodeGenericMetadata(), newFolder1Info.getGenericMetadata());
        NodeInfo newFolder2Info = storage2.getChildNode(rootFolderInfo2.getId(), "folder2").orElse(null);
        assertNotNull(newFolder2Info);
        assertEquals("folder2", newFolder2Info.getName());
        assertEquals(AbstractAppStorageTest.FOLDER_PSEUDO_CLASS, newFolder2Info.getPseudoClass());
        assertTrue(newFolder2Info.getDescription().isEmpty());
        assertEquals(0, newFolder2Info.getVersion());
        assertEquals(new NodeGenericMetadata(), newFolder1Info.getGenericMetadata());
        assertEquals(1, storage2.getChildNodes(newFolder1Info.getId()).size());
        NodeInfo newFile1Info = storage2.getChildNode(newFolder1Info.getId(), "file1").orElse(null);
        assertNotNull(newFile1Info);
        assertEquals("file1", newFile1Info.getName());
        assertEquals(AbstractAppStorageTest.DATA_FILE_CLASS, newFile1Info.getPseudoClass());
        assertTrue(newFile1Info.getDescription().isEmpty());
        assertEquals(0, newFile1Info.getVersion());
        assertEquals(new NodeGenericMetadata().setInt("i1", 1), newFile1Info.getGenericMetadata());
        assertEquals(Sets.newHashSet("data1"), storage2.getDataNames(newFile1Info.getId()));
        try (InputStream is = storage2.readBinaryData(newFile1Info.getId(), "data1").orElseThrow(AssertionError::new)) {
            assertArrayEquals("hello".getBytes(StandardCharsets.UTF_8), ByteStreams.toByteArray(is));
        }
        assertEquals(Sets.newHashSet("ts1 hello"), storage2.getTimeSeriesNames(newFile1Info.getId()));
        assertEquals(Collections.singletonList(metadata1), storage2.getTimeSeriesMetadata(newFile1Info.getId(), Sets.newHashSet("ts1 hello")));
        assertTrue(storage2.getStringTimeSeriesData(newFile1Info.getId(), Sets.newHashSet("ts1 hello"), 0).isEmpty());
        Map<String, List<DoubleDataChunk>> data = storage2.getDoubleTimeSeriesData(newFile1Info.getId(), Sets.newHashSet("ts1 hello"), 0);
        assertEquals(1, data.size());
        assertEquals(chunks, data.get("ts1 hello"));
        NodeInfo newFile2Info = storage2.getChildNode(newFolder2Info.getId(), "file2").orElse(null);
        assertNotNull(newFile2Info);
        assertEquals("file2", newFile2Info.getName());
        assertEquals(AbstractAppStorageTest.DATA_FILE_CLASS, newFile2Info.getPseudoClass());
        assertTrue(newFile2Info.getDescription().isEmpty());
        assertEquals(0, newFile2Info.getVersion());
        assertEquals(new NodeGenericMetadata().setString("s1", "a"), newFile2Info.getGenericMetadata());
        assertEquals(1, storage2.getDependencies(newFile1Info.getId()).size());
        assertEquals(1, storage2.getDependencies(newFile1Info.getId(), "dependency1").size());
        assertEquals(newFile2Info.getId(), storage2.getDependencies(newFile1Info.getId(), "dependency1").iterator().next().getId());
    }
}
