/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.local.storage;

import com.google.common.collect.ImmutableList;
import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.afs.Folder;
import com.powsybl.afs.ext.base.Case;
import com.powsybl.afs.ext.base.TestImporter;
import com.powsybl.afs.storage.NodeId;
import com.powsybl.afs.storage.NodeInfo;
import com.powsybl.commons.datasource.DataSource;
import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.import_.ImportConfig;
import com.powsybl.iidm.import_.ImportersLoaderList;
import com.powsybl.iidm.network.Network;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class LocalAppStorageTest {

    private FileSystem fileSystem;

    private Path path1;

    private Path path2;

    private LocalAppStorage storage;

    @Before
    public void setUp() throws Exception {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
        Path rootDir = fileSystem.getPath("/cases");
        Files.createDirectories(rootDir);
        path1 = rootDir.resolve("n.tst");
        path2 = rootDir.resolve("n2.tst");
        Files.createFile(path1);
        Files.createFile(path2);
        ComputationManager computationManager = Mockito.mock(ComputationManager.class);
        Network network = Mockito.mock(Network.class);
        List<LocalFileScanner> fileExtensions
                = Collections.singletonList(new LocalCaseScanner(new ImportConfig(),
                                                                          new ImportersLoaderList(Collections.singletonList(new TestImporter(network)),
                                                                                                  Collections.emptyList())));
        storage = new LocalAppStorage(rootDir, "mem", fileExtensions, Collections.emptyList(), computationManager);
    }

    @After
    public void tearDown() throws Exception {
        storage.close();
        fileSystem.close();
    }

    @Test
    public void test() {
        NodeInfo rootNodeInfo = storage.createRootNodeIfNotExists("mem", Folder.PSEUDO_CLASS);
        assertEquals("mem", storage.getNodeName(rootNodeInfo.getId()));
        assertFalse(storage.isWritable(rootNodeInfo.getId()));
        assertNull(storage.getParentNode(rootNodeInfo.getId()));
        assertEquals(ImmutableList.of(new PathNodeId(path1), new PathNodeId(path2)), storage.getChildNodes(rootNodeInfo.getId()));
        NodeId case1 = storage.getChildNode(rootNodeInfo.getId(), "n.tst");
        assertNotNull(case1);
        assertEquals(rootNodeInfo.getId(), storage.getParentNode(case1));
        NodeId case2 = storage.getChildNode(rootNodeInfo.getId(), "n2.tst");
        assertNotNull(case2);
        assertEquals("/cases/n.tst", case1.toString());
        assertEquals(case1, storage.fromString(case1.toString()));
        assertNull(storage.getChildNode(rootNodeInfo.getId(), "n3.tst"));
        assertEquals(Folder.PSEUDO_CLASS, storage.getNodePseudoClass(rootNodeInfo.getId()));
        assertEquals(Case.PSEUDO_CLASS, storage.getNodePseudoClass(case1));
        assertEquals("TEST", storage.getStringAttribute(case1, "format"));
        assertEquals("Test format", storage.getStringAttribute(case1, "description"));
        DataSource ds = storage.getDataSourceAttribute(case1, "dataSource");
        assertNotNull(ds);
    }
}
