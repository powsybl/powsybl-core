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
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ModificationScriptTest extends AbstractProjectFileTest {

    @Override
    protected AppStorage createStorage() {
        return MapDbAppStorage.createHeap("mem");
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
    public void test() {
        Project project = afs.getRootFolder().createProject("project");
        ProjectFolder rootFolder = project.getRootFolder();

        // create groovy script
        try {
            rootFolder.fileBuilder(ModificationScriptBuilder.class)
                    .withType(ScriptType.GROOVY)
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
                    .withType(ScriptType.GROOVY)
                    .build();
            fail();
        } catch (AfsException ignored) {
        }
        ModificationScript script = rootFolder.fileBuilder(ModificationScriptBuilder.class)
                .withName("script")
                .withType(ScriptType.GROOVY)
                .withContent("println 'hello'")
                .build();
        assertNotNull(script);
        assertEquals("script", script.getName());
        assertFalse(script.isFolder());
        assertTrue(script.getDependencies().isEmpty());
        assertEquals("println 'hello'", script.readScript());
        boolean[] scriptUpdated = new boolean[1];
        scriptUpdated[0] = false;
        ScriptListener listener = () -> scriptUpdated[0] = true;
        script.addListener(listener);
        script.writeScript("println 'bye'");
        assertEquals("println 'bye'", script.readScript());
        assertTrue(scriptUpdated[0]);
        script.removeListener(listener);

        // check script file is correctly scanned
        assertEquals(1, rootFolder.getChildren().size());
        ProjectNode firstNode = rootFolder.getChildren().get(0);
        assertTrue(firstNode instanceof ModificationScript);
        assertEquals("script", firstNode.getName());
    }
}
