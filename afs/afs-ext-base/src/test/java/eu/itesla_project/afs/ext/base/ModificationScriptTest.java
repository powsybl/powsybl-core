/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.afs.ext.base;

import com.google.common.collect.ImmutableList;
import eu.itesla_project.afs.core.*;
import eu.itesla_project.afs.storage.AppFileSystemStorage;
import eu.itesla_project.afs.mapdb.storage.MapDbAppFileSystemStorage;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ModificationScriptTest extends AbstractProjectFileTest {

    @Override
    protected AppFileSystemStorage createStorage() {
        return MapDbAppFileSystemStorage.createHeap("mem");
    }

    @Override
    protected List<FileExtension> getFileExtensions() {
        return ImmutableList.of(new CaseExtension());
    }

    @Override
    protected List<ProjectFileExtension> getProjectFileExtensions() {
        return ImmutableList.of(new ModificationScriptExtension());
    }

    @Test
    public void test() throws Exception {
        Project project = afs.getRootFolder().createProject("project", "");
        ProjectFolder rootFolder = project.getRootFolder();

        // create groovy script
        try {
            rootFolder.fileBuilder(ModificationScriptBuilder.class)
                    .withType(ModificationScript.ScriptType.GROOVY)
                    .withContent("println 'hello'")
                    .build();
            fail();
        } catch (AfsException ignored) {
        }
        try {
            rootFolder.fileBuilder(ModificationScriptBuilder.class)
                    .withName("script")
                    .withContent("println 'hello'")
                    .build();
            fail();
        } catch (AfsException ignored) {
        }
        try {
            rootFolder.fileBuilder(ModificationScriptBuilder.class)
                    .withName("script")
                    .withType(ModificationScript.ScriptType.GROOVY)
                    .build();
            fail();
        } catch (AfsException ignored) {
        }
        ModificationScript script = rootFolder.fileBuilder(ModificationScriptBuilder.class)
                .withName("script")
                .withType(ModificationScript.ScriptType.GROOVY)
                .withContent("println 'hello'")
                .build();
        assertNotNull(script);
        assertEquals("script", script.getName());
        assertNotNull(script.getIcon());
        assertFalse(script.isFolder());
        assertTrue(script.getDependencies().isEmpty());
        assertEquals("println 'hello'", script.read());
        script.write("println 'bye'");
        assertEquals("println 'bye'", script.read());

        // check script file is correctly scanned
        assertEquals(1, rootFolder.getChildren().size());
        ProjectNode firstNode = rootFolder.getChildren().get(0);
        assertTrue(firstNode instanceof ModificationScript);
        assertEquals("script", firstNode.getName());
    }
}