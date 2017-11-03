/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs;

import com.google.common.collect.ImmutableList;
import com.powsybl.afs.mapdb.storage.MapDbAppFileSystemStorage;
import com.powsybl.afs.storage.AppFileSystemStorage;
import com.powsybl.computation.ComputationManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.Collections;

import static org.junit.Assert.*;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class AfsBaseTest {

    private AppFileSystemStorage storage;

    private AppFileSystem afs;

    private AppData ad;

    @Before
    public void setup() throws IOException {
        storage = MapDbAppFileSystemStorage.createHeap("mem");

        ComputationManager computationManager = Mockito.mock(ComputationManager.class);
        afs = new AppFileSystem("mem", true, storage);
        ad = new AppData(computationManager, Collections.singletonList(computationManager1 -> Collections.singletonList(afs)),
                Collections.emptyList(), Collections.singletonList(new FooFileExtension()));
    }

    @After
    public void tearDown() throws Exception {
        storage.close();
    }

    @Test
    public void baseTest() throws IOException {
        assertSame(afs, ad.getFileSystem("mem"));
        assertNull(ad.getFileSystem("???"));
        assertEquals(Collections.singletonList("mem"), ad.getRemotelyAccessibleFileSystemNames());
        assertNotNull(ad.getRemotelyAccessibleStorage("mem"));
        assertEquals("mem", afs.getName());
        assertEquals(1, ad.getProjectFileClasses().size());
        Folder root = afs.getRootFolder();
        assertNotNull(root);
        Folder dir1 = root.createFolder("dir1");
        assertNotNull(dir1);
        dir1.createFolder("dir2");
        dir1.createFolder("dir3");
        dir1 = root.getFolder("dir1");
        assertNotNull(dir1);
        assertTrue(dir1.isFolder());
        assertTrue(dir1.isWritable());
        assertEquals("dir1", dir1.getName());
        assertEquals(dir1.getName(), dir1.toString());
        assertEquals("mem", dir1.getParent().getName());
        Folder dir2 = dir1.getFolder("dir2");
        assertNotNull(dir2);
        assertNotNull(dir2.getParent());
        assertEquals("mem:/dir1", dir2.getParent().getPath().toString());
        assertEquals(dir1.getChildren().size(), 2);
        Folder dir3 = root.getFolder("dir3");
        assertNull(dir3);
        String str = dir2.getPath().toString();
        assertEquals("mem:/dir1/dir2", str);
        Folder mayBeDir2 = afs.getRootFolder().getFolder("dir1/dir2");
        assertNotNull(mayBeDir2);
        assertEquals("dir2", mayBeDir2.getName());
        Folder mayBeDir2otherWay = afs.getRootFolder().getChild(Folder.class, "dir1", "dir2");
        assertNotNull(mayBeDir2otherWay);
        assertEquals("dir2", mayBeDir2otherWay.getName());

        Project project1 = dir2.createProject("project1");
        project1.setDescription("test project");
        assertNotNull(project1);
        assertEquals("project1", project1.getName());
        assertEquals("test project", project1.getDescription());
        assertNotNull(project1.getIcon());
        assertNotNull(project1.getParent());
        assertEquals("mem:/dir1/dir2", project1.getParent().getPath().toString());
        assertTrue(project1.getRootFolder().getChildren().isEmpty());
        assertTrue(project1.getFileSystem() == afs);

        ProjectFolder dir4 = project1.getRootFolder().createFolder("dir4");
        assertTrue(dir4.isFolder());
        assertEquals("dir4", dir4.getName());
        assertNotNull(dir4.getParent());
        assertTrue(dir4.getChildren().isEmpty());
        assertEquals(1, project1.getRootFolder().getChildren().size());

        dir4.delete();
        assertTrue(project1.getRootFolder().getChildren().isEmpty());
        try {
            dir4.getChildren();
            fail();
        } catch (Exception ignored) {
        }

        ProjectFolder dir5 = project1.getRootFolder().createFolder("dir5");
        ProjectFolder dir6 = dir5.createFolder("dir6");
        assertEquals(ImmutableList.of("dir5", "dir6"), dir6.getPath().toList().subList(1, 3));
        assertEquals("dir5/dir6", dir6.getPath().toString());
        assertEquals("dir6", project1.getRootFolder().getChild("dir5/dir6").getName());
    }

    @Test
    public void moveToTest() throws IOException {
        Project project = afs.getRootFolder().createProject("test");
        ProjectFolder test1 = project.getRootFolder().createFolder("test1");
        ProjectFolder test2 = project.getRootFolder().createFolder("test2");
        FooFile file = test1.fileBuilder(FooFileBuilder.class)
                .withName("foo")
                .build();
        assertEquals(test1.getId(), file.getParent().getId());
        assertEquals(1, test1.getChildren().size());
        assertTrue(test2.getChildren().isEmpty());
        file.moveTo(test2);
        assertEquals(test2.getId(), file.getParent().getId());
        assertTrue(test1.getChildren().isEmpty());
        assertEquals(1, test2.getChildren().size());
    }

}
