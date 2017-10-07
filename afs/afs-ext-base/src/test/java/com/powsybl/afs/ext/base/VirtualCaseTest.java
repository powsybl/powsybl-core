/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.ext.base;

import com.google.common.collect.ImmutableList;
import com.google.common.io.CharStreams;
import com.powsybl.afs.*;
import com.powsybl.afs.mapdb.storage.MapDbAppFileSystemStorage;
import com.powsybl.afs.storage.AppFileSystemStorage;
import com.powsybl.afs.storage.NodeId;
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
public class VirtualCaseTest extends AbstractProjectFileTest {

    private ImportersLoader createImportersLoader() {
        return new ImportersLoaderList(Collections.singletonList(new TestImporter(network)), Collections.emptyList());
    }

    @Override
    protected AppFileSystemStorage createStorage() {
        return MapDbAppFileSystemStorage.createHeap("mem");
    }

    @Override
    protected List<FileExtension> getFileExtensions() {
        return ImmutableList.of(new CaseExtension(createImportersLoader()));
    }

    @Override
    protected List<ProjectFileExtension> getProjectFileExtensions() {
        return ImmutableList.of(new ImportedCaseExtension(createImportersLoader()), new ModificationScriptExtension(), new VirtualCaseExtension());
    }

    @Before
    public void setup() throws IOException {
        super.setup();
        NodeId rootFolderId = storage.getRootNode();
        NodeId caseId = storage.createNode(rootFolderId, "network", Case.PSEUDO_CLASS);
        storage.setStringAttribute(caseId, Case.FORMAT, TestImporter.FORMAT);
    }

    @Test
    public void test() throws Exception {
        // get case
        Case aCase = (Case) afs.getRootFolder().getChildren().get(0);

        // create project
        Project project = afs.getRootFolder().createProject("project", "");

        // create project folder
        ProjectFolder folder = project.getRootFolder().createFolder("folder");

        // import case into project
        ImportedCase importedCase = folder.fileBuilder(ImportedCaseBuilder.class)
                .withCase(aCase)
                .build();

        // create groovy script
        ModificationScript script = folder.fileBuilder(ModificationScriptBuilder.class)
                .withName("script")
                .withType(ModificationScript.ScriptType.GROOVY)
                .withContent("print 'hello'")
                .build();

        // create virtual by applying groovy script on imported case
        try {
            VirtualCase virtualCase = folder.fileBuilder(VirtualCaseBuilder.class)
                    .withCase("folder/network")
                    .withScript("folder/script")
                    .build();
            fail();
        } catch (AfsException ignored) {
        }

        try {
            VirtualCase virtualCase = folder.fileBuilder(VirtualCaseBuilder.class)
                    .withName("network2")
                    .withScript("folder/script")
                    .build();
            fail();
        } catch (AfsException ignored) {
        }

        try {
            VirtualCase virtualCase = folder.fileBuilder(VirtualCaseBuilder.class)
                    .withName("network2")
                    .withCase("folder/network")
                    .build();
            fail();
        } catch (AfsException ignored) {
        }

        try {
            VirtualCase virtualCase = folder.fileBuilder(VirtualCaseBuilder.class)
                    .withName("network2")
                    .withCase("folder/???")
                    .withScript("folder/script")
                    .build();
            fail();
        } catch (AfsException ignored) {
        }

        try {
            VirtualCase virtualCase = folder.fileBuilder(VirtualCaseBuilder.class)
                    .withName("network2")
                    .withCase("folder/network")
                    .withScript("folder/???")
                    .build();
            fail();
        } catch (AfsException ignored) {
        }

        VirtualCase virtualCase = folder.fileBuilder(VirtualCaseBuilder.class)
                .withName("network2")
                .withCase("folder/network")
                .withScript("folder/script")
                .build();

        assertEquals("network2", virtualCase.getName());
        assertNotNull(virtualCase.getCase());
        assertNotNull(virtualCase.getScript());
        assertNotNull(virtualCase.getIcon());
        assertEquals(2, virtualCase.getDependencies().size());
        assertEquals(1, importedCase.getBackwardDependencies().size());
        assertEquals(1, script.getBackwardDependencies().size());
        Network network = virtualCase.loadNetwork();
        assertNotNull(network);

        // check script output
        assertEquals("hello", CharStreams.toString(virtualCase.getScriptOutputReader()));

        // test cache invalidation
        script.write("print 'bye'");
        assertNull(virtualCase.getScriptOutputReader());
        Network network2 = virtualCase.loadNetwork();
        assertNotNull(network2);
        assertEquals("bye", CharStreams.toString(virtualCase.getScriptOutputReader()));

        virtualCase.delete();
        assertTrue(importedCase.getBackwardDependencies().isEmpty());
        assertTrue(script.getBackwardDependencies().isEmpty());

    }
}
