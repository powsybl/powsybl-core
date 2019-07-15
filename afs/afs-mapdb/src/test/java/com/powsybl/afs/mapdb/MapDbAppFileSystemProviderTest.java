/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.mapdb;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.afs.AppFileSystem;
import com.powsybl.afs.AppFileSystemProviderContext;
import com.powsybl.afs.mapdb.storage.MapDbAppStorage;
import com.powsybl.afs.storage.InMemoryEventsBus;
import com.powsybl.computation.ComputationManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class MapDbAppFileSystemProviderTest {

    private FileSystem fileSystem;

    private Path dbFile;

    @Before
    public void setUp() throws Exception {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
        dbFile = fileSystem.getPath("/db");
        Files.createFile(dbFile);
    }

    @After
    public void tearDown() throws Exception {
        fileSystem.close();
    }

    @Test
    public void test() {
        ComputationManager computationManager = Mockito.mock(ComputationManager.class);
        MapDbAppFileSystemConfig config = new MapDbAppFileSystemConfig("drive", true, dbFile);
        List<AppFileSystem> fileSystems = new MapDbAppFileSystemProvider(Collections.singletonList(config),
            (name, file, eventsBus) -> MapDbAppStorage.createMem(name, eventsBus))
                .getFileSystems(new AppFileSystemProviderContext(computationManager, null, new InMemoryEventsBus()));
        assertEquals(1, fileSystems.size());
        assertTrue(fileSystems.get(0) instanceof MapDbAppFileSystem);
        assertEquals("drive", fileSystems.get(0).getName());
        assertTrue(fileSystems.get(0).isRemotelyAccessible());
    }
}
