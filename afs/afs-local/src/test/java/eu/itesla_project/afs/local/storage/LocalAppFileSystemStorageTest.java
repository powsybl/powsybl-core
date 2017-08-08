/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.afs.local.storage;

import com.google.common.collect.ImmutableList;
import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import eu.itesla_project.afs.ext.base.Case;
import eu.itesla_project.afs.ext.base.TestImporter;
import eu.itesla_project.afs.storage.NodeId;
import eu.itesla_project.afs.storage.PseudoClass;
import eu.itesla_project.commons.datasource.DataSource;
import eu.itesla_project.computation.ComputationManager;
import eu.itesla_project.iidm.import_.ImportConfig;
import eu.itesla_project.iidm.import_.ImportersLoaderList;
import eu.itesla_project.iidm.network.Network;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;

import static org.junit.Assert.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class LocalAppFileSystemStorageTest {

    private FileSystem fileSystem;

    private Path path1;

    private Path path2;

    private LocalAppFileSystemStorage storage;

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
        storage = new LocalAppFileSystemStorage(rootDir, "mem", computationManager,
                new ImportConfig(), new ImportersLoaderList(Collections.singletonList(new TestImporter(network)),
                                                            Collections.emptyList()));
    }

    @After
    public void tearDown() throws Exception {
        storage.close();
        fileSystem.close();
    }

    @Test
    public void test() {
        NodeId rootNode = storage.getRootNode();
        assertEquals("mem", storage.getNodeName(rootNode));
        assertFalse(storage.isWritable(rootNode));
        assertNull(storage.getParentNode(rootNode));
        assertEquals(ImmutableList.of(new PathNodeId(path1), new PathNodeId(path2)), storage.getChildNodes(rootNode));
        NodeId case1 = storage.getChildNode(rootNode, "n.tst");
        assertNotNull(case1);
        assertEquals(rootNode, storage.getParentNode(case1));
        NodeId case2 = storage.getChildNode(rootNode, "n2.tst");
        assertNotNull(case2);
        assertEquals("/cases/n.tst", case1.toString());
        assertEquals(case1, storage.fromString(case1.toString()));
        assertNull(storage.getChildNode(rootNode, "n3.tst"));
        assertEquals(PseudoClass.FOLDER_PSEUDO_CLASS, storage.getNodePseudoClass(rootNode));
        assertEquals(Case.PSEUDO_CLASS, storage.getNodePseudoClass(case1));
        assertEquals("TEST", storage.getStringAttribute(case1, "format"));
        assertEquals("Test format", storage.getStringAttribute(case1, "description"));
        DataSource ds = storage.getDataSourceAttribute(case1, "dataSource");
        assertNotNull(ds);
    }
}
