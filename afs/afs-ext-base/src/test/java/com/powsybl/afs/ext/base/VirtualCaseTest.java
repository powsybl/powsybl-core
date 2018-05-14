/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.ext.base;

import com.google.common.collect.ImmutableList;
import com.powsybl.afs.*;
import com.powsybl.afs.mapdb.storage.MapDbAppStorage;
import com.powsybl.afs.storage.AppStorage;
import com.powsybl.afs.storage.NodeGenericMetadata;
import com.powsybl.afs.storage.NodeInfo;
import com.powsybl.iidm.import_.ImportConfig;
import com.powsybl.iidm.import_.ImportersLoader;
import com.powsybl.iidm.import_.ImportersLoaderList;
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
    protected AppStorage createStorage() {
        return MapDbAppStorage.createHeap("mem");
    }

    @Override
    protected List<FileExtension> getFileExtensions() {
        return ImmutableList.of(new CaseExtension(createImportersLoader()));
    }

    @Override
    protected List<ProjectFileExtension> getProjectFileExtensions() {
        return ImmutableList.of(new ImportedCaseExtension(createImportersLoader(), new ImportConfig()),
                                new ModificationScriptExtension(),
                                new VirtualCaseExtension());
    }

    @Override
    protected List<ServiceExtension> getServiceExtensions() {
        return ImmutableList.of(new LocalNetworkCacheServiceExtension());
    }

    @Before
    public void setup() throws IOException {
        super.setup();
        NodeInfo rootFolderInfo = storage.createRootNodeIfNotExists("root", Folder.PSEUDO_CLASS);
        storage.createNode(rootFolderInfo.getId(), "network", Case.PSEUDO_CLASS, "", Case.VERSION,
                new NodeGenericMetadata().setString(Case.FORMAT, TestImporter.FORMAT));
    }

    @Test
    public void test() {
        // get case
        Case aCase = (Case) afs.getRootFolder().getChildren().get(0);

        // create project
        Project project = afs.getRootFolder().createProject("project");

        // create project folder
        ProjectFolder folder = project.getRootFolder().createFolder("folder");

        // import case into project
        ImportedCase importedCase = folder.fileBuilder(ImportedCaseBuilder.class)
                .withCase(aCase)
                .build();

        // create groovy script
        ModificationScript script = folder.fileBuilder(ModificationScriptBuilder.class)
                .withName("script")
                .withType(ScriptType.GROOVY)
                .withContent("print 'hello'")
                .build();

        // create virtual by applying groovy script on imported case
        try {
            VirtualCase virtualCase = folder.fileBuilder(VirtualCaseBuilder.class)
                    .withCase(importedCase)
                    .withScript(script)
                    .build();
            fail();
        } catch (AfsException ignored) {
        }

        try {
            VirtualCase virtualCase = folder.fileBuilder(VirtualCaseBuilder.class)
                    .withName("network2")
                    .withScript(script)
                    .build();
            fail();
        } catch (AfsException ignored) {
        }

        try {
            VirtualCase virtualCase = folder.fileBuilder(VirtualCaseBuilder.class)
                    .withName("network2")
                    .withCase(importedCase)
                    .build();
            fail();
        } catch (AfsException ignored) {
        }

        VirtualCase virtualCase = folder.fileBuilder(VirtualCaseBuilder.class)
                .withName("network2")
                .withCase(importedCase)
                .withScript(script)
                .build();

        assertEquals("network2", virtualCase.getName());
        assertTrue(virtualCase.getCase().isPresent());
        assertTrue(virtualCase.getScript().isPresent());
        assertEquals(2, virtualCase.getDependencies().size());
        assertEquals(1, importedCase.getBackwardDependencies().size());
        assertEquals(1, script.getBackwardDependencies().size());
        assertNotNull(virtualCase.getNetwork());

        // test cache invalidation
        script.writeScript("print 'bye'");
        assertNotNull(virtualCase.getNetwork());

        virtualCase.delete();
        assertTrue(importedCase.getBackwardDependencies().isEmpty());
        assertTrue(script.getBackwardDependencies().isEmpty());

        // test script error
        ModificationScript scriptWithError = folder.fileBuilder(ModificationScriptBuilder.class)
                .withName("scriptWithError")
                .withType(ScriptType.GROOVY)
                .withContent("prin 'hello'")
                .build();

        VirtualCase virtualCaseWithError = folder.fileBuilder(VirtualCaseBuilder.class)
                .withName("network2")
                .withCase(importedCase)
                .withScript(scriptWithError)
                .build();

        try {
            virtualCaseWithError.getNetwork();
            fail();
        } catch (ScriptException e) {
            assertNotNull(e.getError());
            assertTrue(e.getError().getMessage().contains("No signature of method: test.prin() is applicable"));
        }
    }
}
