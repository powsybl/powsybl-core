/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.local;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.afs.AppFileSystem;
import com.powsybl.afs.AppFileSystemProviderContext;
import com.powsybl.afs.local.storage.LocalCaseScanner;
import com.powsybl.afs.local.storage.LocalFileScanner;
import com.powsybl.afs.storage.InMemoryEventsBus;
import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.import_.ImportConfig;
import com.powsybl.iidm.import_.ImportersLoaderList;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.nio.file.FileSystem;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class LocalAppFileSystemProviderTest {

    private FileSystem fileSystem;

    @Before
    public void setUp() throws Exception {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
    }

    @After
    public void tearDown() throws Exception {
        fileSystem.close();
    }

    @Test
    public void test() {
        ComputationManager computationManager = Mockito.mock(ComputationManager.class);
        LocalAppFileSystemConfig config = new LocalAppFileSystemConfig("drive", true, fileSystem.getPath("/work"));
        LocalFileScanner extension = new LocalCaseScanner(new ImportConfig(), new ImportersLoaderList());
        List<AppFileSystem> fileSystems = new LocalAppFileSystemProvider(Collections.singletonList(config),
                                                                         Collections.singletonList(extension),
                                                                         Collections.emptyList())
                .getFileSystems(new AppFileSystemProviderContext(computationManager, null, new InMemoryEventsBus()));
        assertEquals(1, fileSystems.size());
        assertTrue(fileSystems.get(0) instanceof LocalAppFileSystem);
        assertEquals("drive", fileSystems.get(0).getName());
        assertTrue(fileSystems.get(0).isRemotelyAccessible());
    }
}
