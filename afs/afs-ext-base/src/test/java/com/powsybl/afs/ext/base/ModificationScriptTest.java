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
import com.powsybl.afs.storage.InMemoryEventsBus;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ModificationScriptTest extends AbstractProjectFileTest {

    @Override
    protected AppStorage createStorage() {
        return MapDbAppStorage.createMem("mem", new InMemoryEventsBus());
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
        AtomicBoolean scriptUpdated = new AtomicBoolean(false);
        ScriptListener listener = () -> scriptUpdated.set(true);
        script.addListener(listener);
        script.writeScript("println 'bye'");
        assertEquals("println 'bye'", script.readScript());
        assertTrue(scriptUpdated.get());
        script.removeListener(listener);

        // check script file is correctly scanned
        assertEquals(1, rootFolder.getChildren().size());
        ProjectNode firstNode = rootFolder.getChildren().get(0);
        assertTrue(firstNode instanceof ModificationScript);
        assertEquals("script", firstNode.getName());

        ModificationScript include1 = rootFolder.fileBuilder(ModificationScriptBuilder.class)
                .withName("include_script1")
                .withType(ScriptType.GROOVY)
                .withContent("var foo=\"bar\"")
                .build();
        assertNotNull(include1);
        script.addScript(include1);
        String contentWithInclude = script.readScript(true);
        assertEquals(contentWithInclude, "var foo=\"bar\"\n\nprintln 'bye'");

        script.addScript(include1);
        contentWithInclude = script.readScript(true);
        assertEquals(contentWithInclude, "var foo=\"bar\"\n\nvar foo=\"bar\"\n\nprintln 'bye'");

        ModificationScript include2 = rootFolder.fileBuilder(ModificationScriptBuilder.class)
                .withName("include_script2")
                .withType(ScriptType.GROOVY)
                .withContent("var p0=1")
                .build();
        script.removeScript(include1.getId());
        script.addScript(include1);
        script.addScript(include2);
        contentWithInclude = script.readScript(true);
        assertEquals(contentWithInclude, "var foo=\"bar\"\n\nvar p0=1\n\nprintln 'bye'");

        ModificationScript include3 = rootFolder.fileBuilder(ModificationScriptBuilder.class)
                .withName("include_script3")
                .withType(ScriptType.GROOVY)
                .withContent("var pmax=2")
                .build();
        script.addScript(include3);
        script.removeScript(include2.getId());
        contentWithInclude = script.readScript(true);
        assertEquals(contentWithInclude, "var foo=\"bar\"\n\nvar pmax=2\n\nprintln 'bye'");

        include3.addScript(include2);
        contentWithInclude = script.readScript(true);
        assertEquals(contentWithInclude, "var foo=\"bar\"\n\nvar p0=1\n\nvar pmax=2\n\nprintln 'bye'");

        List<AbstractScript> includes = script.getIncludedScripts();
        assertEquals(includes.size(), 2);
        assertEquals(includes.get(0).getId(), include1.getId());
        assertEquals(includes.get(1).getId(), include3.getId());
    }
}
