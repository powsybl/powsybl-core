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
import com.powsybl.afs.mapdb.storage.MapDbAppFileSystemStorage;
import com.powsybl.afs.storage.AppFileSystemStorage;
import com.powsybl.afs.storage.NodeId;
import com.powsybl.afs.storage.NodeInfo;
import com.powsybl.iidm.import_.ImportersLoader;
import com.powsybl.iidm.import_.ImportersLoaderList;
import com.powsybl.iidm.network.Network;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ImportedCaseTest extends AbstractProjectFileTest {

    @Override
    protected AppFileSystemStorage createStorage() {
        return MapDbAppFileSystemStorage.createHeap("mem");
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

    @Before
    public void setup() throws IOException {
        super.setup();
        NodeInfo rootFolderInfo = storage.createRootNodeIfNotExists("root", Folder.PSEUDO_CLASS);
        NodeId caseId = storage.createNode(rootFolderInfo.getId(), "network", Case.PSEUDO_CLASS);
        storage.setStringAttribute(caseId, "description", "Test format");
        storage.setStringAttribute(caseId, "format", TestImporter.FORMAT);
    }

    @Test
    public void test() throws Exception {
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
        Network network = importedCase.loadNetwork();
        assertNotNull(network);
        assertTrue(importedCase.getDependencies().isEmpty());

        // try to reload the imported case
        assertEquals(1, folder.getChildren().size());
        ProjectNode projectNode = folder.getChildren().get(0);
        assertNotNull(projectNode);
        assertTrue(projectNode instanceof ImportedCase);
        ImportedCase importedCase2 = (ImportedCase) projectNode;
        assertEquals(TestImporter.FORMAT, importedCase2.getImporter().getFormat());
        assertEquals(2, importedCase2.getParameters().size());
        assertEquals("true", importedCase2.getParameters().getProperty("param1"));

        assertNotNull(folder.getChild(ImportedCase.class, "network"));

        // deleteProjectNode imported case
        projectNode.delete();
        assertTrue(folder.getChildren().isEmpty());
        try {
            projectNode.getName();
        } catch (Exception ignored) {
        }
    }
}
