/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.storage;

import com.google.common.collect.ImmutableMap;
import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.math.timeseries.RegularTimeSeriesIndex;
import com.powsybl.math.timeseries.TimeSeriesDataType;
import com.powsybl.math.timeseries.TimeSeriesMetadata;
import com.powsybl.math.timeseries.UncompressedDoubleArrayChunk;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.threeten.extra.Interval;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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

        NodeInfo folder1Info = storage.createNode(rootFolderInfo.getId(), "folder1", AbstractAppStorageTest.FOLDER_PSEUDO_CLASS, "", 0, new NodeGenericMetadata());
        NodeInfo file1Info = storage.createNode(folder1Info.getId(), "file1", AbstractAppStorageTest.DATA_FILE_CLASS, "", 0, new NodeGenericMetadata().setInt("i1", 1));

        try (OutputStream os = storage.writeBinaryData(file1Info.getId(), "data1")) {
            os.write("hello".getBytes(StandardCharsets.UTF_8));
        }

        TimeSeriesMetadata metadata1 = new TimeSeriesMetadata("ts1",
                                                              TimeSeriesDataType.DOUBLE,
                                                              ImmutableMap.of("var1", "value1"),
                                                              RegularTimeSeriesIndex.create(Interval.parse("2015-01-01T00:00:00Z/2015-01-01T01:15:00Z"),
                                                                                            Duration.ofMinutes(15)));
        storage.createTimeSeries(file1Info.getId(), metadata1);
        storage.addDoubleTimeSeriesData(file1Info.getId(), 0, "ts1", Arrays.asList(new UncompressedDoubleArrayChunk(2, new double[] {1d, 2d}),
                                                                                   new UncompressedDoubleArrayChunk(5, new double[] {3d})));

        NodeInfo folder2Info = storage.createNode(rootFolderInfo.getId(), "folder2", AbstractAppStorageTest.FOLDER_PSEUDO_CLASS, "", 0, new NodeGenericMetadata());
        NodeInfo file2Info = storage.createNode(folder2Info.getId(), "file2", AbstractAppStorageTest.DATA_FILE_CLASS, "", 0, new NodeGenericMetadata()
                .setString("s1", "a"));

        storage.addDependency(file1Info.getId(), "dependency1", file2Info.getId());

        storage.flush();

        // archive
        Path workDir = fileSystem.getPath("/work");
        new AppStorageArchive(storage).archive(rootFolderInfo.getId(), workDir);

        Path rootPath = workDir.resolve(rootFolderInfo.getId());
        assertTrue(Files.exists(rootPath));

        // unarchive to the second storage
        new AppStorageArchive(storage2).unarchive(rootPath);

        // check we have same data in storage and storage2
        NodeInfo rootFolderInfo2 = storage2.createRootNodeIfNotExists(storage.getFileSystemName(), AbstractAppStorageTest.FOLDER_PSEUDO_CLASS);
        assertEquals(2, storage2.getChildNodes(rootFolderInfo2.getId()).size());

    }
}
