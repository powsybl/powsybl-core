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
import com.powsybl.afs.storage.NodeInfo;
import com.powsybl.afs.storage.NodeGenericMetadata;
import com.powsybl.iidm.import_.ImportersLoader;
import com.powsybl.iidm.import_.ImportersLoaderList;
import com.powsybl.iidm.network.Network;
import org.junit.Before;
import org.junit.Test;

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
        return ImmutableList.of(new ImportedCaseExtension(createImportersLoader()), new ModificationScriptExtension(), new VirtualCaseExtension());
    }

    @Override
    protected List<ServiceExtension> getServiceExtensions() {
        return ImmutableList.of(new LocalNetworkServiceExtension());
    }

    @Before
    public void setup() {
        super.setup();
        NodeInfo rootFolderInfo = storage.createRootNodeIfNotExists("root", Folder.PSEUDO_CLASS);
        storage.createNode(rootFolderInfo.getId(), "network", Case.PSEUDO_CLASS, "", Case.VERSION,
                new NodeGenericMetadata().setString(Case.FORMAT, TestImporter.FORMAT));
    }

    @Test
    public void test() throws Exception {
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
        assertTrue(virtualCase.getCase().isPresent());
        assertTrue(virtualCase.getScript().isPresent());
        assertNotNull(virtualCase.getIcon());
        assertEquals(2, virtualCase.getDependencies().size());
        assertEquals(1, importedCase.getBackwardDependencies().size());
        assertEquals(1, script.getBackwardDependencies().size());
        assertNotNull(virtualCase.getNetwork());
        assertNull(virtualCase.getScriptError());

        // check script output
        assertEquals("hello", virtualCase.getScriptOutput());

        // test cache invalidation
        script.writeScript("print 'bye'");
        assertNotNull(virtualCase.getNetwork());
        assertNull(virtualCase.getScriptError());
        assertEquals("bye", virtualCase.getScriptOutput());

        virtualCase.delete();
        assertTrue(importedCase.getBackwardDependencies().isEmpty());
        assertTrue(script.getBackwardDependencies().isEmpty());

        // test script error
        folder.fileBuilder(ModificationScriptBuilder.class)
                .withName("scriptWithError")
                .withType(ScriptType.GROOVY)
                .withContent("prin 'hello'")
                .build();

        VirtualCase virtualCaseWithError = folder.fileBuilder(VirtualCaseBuilder.class)
                .withName("network2")
                .withCase("folder/network")
                .withScript("folder/scriptWithError")
                .build();

        Network networkWithError = virtualCaseWithError.getNetwork();
        assertNotNull(networkWithError);
        assertEquals(0, networkWithError.getSubstationCount());
        assertNotNull(virtualCaseWithError.getScriptError());
        assertTrue(virtualCaseWithError.getScriptError().getMessage().contains("No signature of method: test.prin() is applicable"));
        assertEquals("", virtualCaseWithError.getScriptOutput());
    }
}
