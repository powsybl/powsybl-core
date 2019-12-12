/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.ext.base;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.afs.*;
import com.powsybl.afs.mapdb.storage.MapDbAppStorage;
import com.powsybl.afs.storage.AppStorage;
import com.powsybl.afs.storage.InMemoryEventsBus;
import com.powsybl.afs.storage.NodeGenericMetadata;
import com.powsybl.afs.storage.NodeInfo;
import com.powsybl.iidm.export.ExportersLoader;
import com.powsybl.iidm.export.ExportersLoaderList;
import com.powsybl.iidm.import_.ImportConfig;
import com.powsybl.iidm.import_.ImportersLoader;
import com.powsybl.iidm.import_.ImportersLoaderList;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.xml.XMLExporter;
import com.powsybl.iidm.xml.XMLImporter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ImportedCaseTest extends AbstractProjectFileTest {

    private FileSystem fileSystem;

    @Override
    protected AppStorage createStorage() {
        return MapDbAppStorage.createMem("mem", new InMemoryEventsBus());
    }

    private ExportersLoader createExportersLoader() {
        return new ExportersLoaderList(new XMLExporter());
    }

    private ImportersLoader createImportersLoader() {
        return new ImportersLoaderList(Arrays.asList(new TestImporter(network), new XMLImporter()), Collections.emptyList());
    }

    @Override
    protected List<FileExtension> getFileExtensions() {
        return ImmutableList.of(new CaseExtension(createImportersLoader()));
    }

    @Override
    protected List<ProjectFileExtension> getProjectFileExtensions() {
        return ImmutableList.of(new ImportedCaseExtension(createExportersLoader(), createImportersLoader(), new ImportConfig()));
    }

    @Override
    protected List<ServiceExtension> getServiceExtensions() {
        return ImmutableList.of(new LocalNetworkCacheServiceExtension());
    }

    @Override
    @Before
    public void setup() throws IOException {
        super.setup();
        NodeInfo rootFolderInfo = storage.createRootNodeIfNotExists("root", Folder.PSEUDO_CLASS);

        NodeInfo nodeInfo = storage.createNode(rootFolderInfo.getId(), "network", Case.PSEUDO_CLASS, "Test format", Case.VERSION,
                new NodeGenericMetadata().setString("format", TestImporter.FORMAT));
        storage.setConsistent(nodeInfo.getId());

        fileSystem = Jimfs.newFileSystem(Configuration.unix());
        Files.createFile(fileSystem.getPath("/work/network.tst"));
    }

    @Override
    @After
    public void tearDown() throws IOException {
        fileSystem.close();

        super.tearDown();
    }

    @Test
    public void test() {
        Folder root = afs.getRootFolder();

        // check case exist
        assertEquals(1, root.getChildren().size());
        assertTrue(root.getChildren().get(0) instanceof Case);
        Case aCase = (Case) root.getChildren().get(0);
        assertEquals("network", aCase.getName());
        assertEquals("Test format", aCase.getDescription());
        assertFalse(aCase.isFolder());

        // create project
        Project project = root.createProject("project");
        assertNotNull(project);

        // create project folder
        ProjectFolder folder = project.getRootFolder().createFolder("folder");
        assertTrue(folder.getChildren().isEmpty());

        // import case into project
        try {
            folder.fileBuilder(ImportedCaseBuilder.class)
                    .build();
            fail();
        } catch (AfsException ignored) {
        }
        ImportedCase importedCase = folder.fileBuilder(ImportedCaseBuilder.class)
                .withCase(aCase)
                .withParameter("param1", "true")
                .withParameters(ImmutableMap.of("param2", "1"))
                .build();
        assertNotNull(importedCase);
        assertFalse(importedCase.isFolder());
        assertNotNull(importedCase.getNetwork());
        assertTrue(importedCase.getDependencies().isEmpty());

        // test network query
        assertEquals("[\"s1\"]", importedCase.queryNetwork(ScriptType.GROOVY, "network.substations.collect { it.id }"));

        // try to reload the imported case
        assertEquals(1, folder.getChildren().size());
        ProjectNode projectNode = folder.getChildren().get(0);
        assertNotNull(projectNode);
        assertTrue(projectNode instanceof ImportedCase);
        ImportedCase importedCase2 = (ImportedCase) projectNode;
        assertEquals(TestImporter.FORMAT, importedCase2.getImporter().getFormat());
        assertEquals(2, importedCase2.getParameters().size());
        assertEquals("true", importedCase2.getParameters().getProperty("param1"));

        assertTrue(folder.getChild(ImportedCase.class, "network").isPresent());

        // delete imported case
        projectNode.delete();
        assertTrue(folder.getChildren().isEmpty());
        try {
            projectNode.getName();
        } catch (Exception ignored) {
        }
    }

    @Test
    public void testFile() {
        Folder root = afs.getRootFolder();

        // create project
        Project project = root.createProject("project");
        assertNotNull(project);

        // create project folder
        ProjectFolder folder = project.getRootFolder().createFolder("folder");
        assertTrue(folder.getChildren().isEmpty());

        ImportedCase importedCase = folder.fileBuilder(ImportedCaseBuilder.class)
                .withFile(fileSystem.getPath("/work/network.tst"))
                .withName("test")
                .build();
        assertNotNull(importedCase);
        assertEquals("test", importedCase.getName());

        ImportedCase importedCase2 = folder.fileBuilder(ImportedCaseBuilder.class)
                .withFile(fileSystem.getPath("/work/network.tst"))
                .build();
        assertNotNull(importedCase2);
        assertEquals("network", importedCase2.getName());
    }

    @Test
    public void testNetwork() {
        Folder root = afs.getRootFolder();

        // create project
        Project project = root.createProject("project");
        assertNotNull(project);

        // create project folder
        ProjectFolder folder = project.getRootFolder().createFolder("folder");
        assertTrue(folder.getChildren().isEmpty());

        Network network = Network.create("NetworkID", "scripting");
        ImportedCase importedCase1 = folder.fileBuilder(ImportedCaseBuilder.class)
                .withName("test")
                .withNetwork(network)
                .build();
        assertNotNull(importedCase1);
        assertEquals("test", importedCase1.getName());

        ImportedCase importedCase2 = folder.fileBuilder(ImportedCaseBuilder.class)
                .withNetwork(network)
                .build();
        assertNotNull(importedCase2);
        assertEquals("NetworkID", importedCase2.getName());
    }
}
