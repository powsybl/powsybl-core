/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.ext.base;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.powsybl.afs.*;
import com.powsybl.afs.mapdb.storage.MapDbAppStorage;
import com.powsybl.afs.storage.AppStorage;
import com.powsybl.afs.storage.NodeInfo;
import com.powsybl.afs.storage.NodeGenericMetadata;
import com.powsybl.iidm.import_.ImportersLoader;
import com.powsybl.iidm.import_.ImportersLoaderList;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ImportedCaseTest extends AbstractProjectFileTest {

    @Override
    protected AppStorage createStorage() {
        return MapDbAppStorage.createHeap("mem");
    }

    private ImportersLoader createImportersLoader() {
        return new ImportersLoaderList(Collections.singletonList(new TestImporter(network)), Collections.emptyList());
    }

    @Override
    protected List<FileExtension> getFileExtensions() {
        return ImmutableList.of(new CaseExtension(createImportersLoader()));
    }

    @Override
    protected List<ProjectFileExtension> getProjectFileExtensions() {
        return ImmutableList.of(new ImportedCaseExtension(createImportersLoader()));
    }

    @Override
    protected List<ServiceExtension> getServiceExtensions() {
        return ImmutableList.of(new LocalNetworkCacheServiceExtension());
    }

    @Before
    public void setup() {
        super.setup();
        NodeInfo rootFolderInfo = storage.createRootNodeIfNotExists("root", Folder.PSEUDO_CLASS);
        NodeInfo caseInfo = storage.createNode(rootFolderInfo.getId(), "network", Case.PSEUDO_CLASS, "Test format", Case.VERSION,
                new NodeGenericMetadata().setString("format", TestImporter.FORMAT));
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
        assertNotNull(aCase.getIcon());

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
        assertNotNull(importedCase.getIcon());
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
}
