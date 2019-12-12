/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs;

import com.google.common.collect.ImmutableList;
import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.afs.mapdb.storage.MapDbAppStorage;
import com.powsybl.afs.storage.AppStorage;
import com.powsybl.afs.storage.InMemoryEventsBus;
import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.network.NetworkFactoryService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class AfsBaseTest {

    private FileSystem fileSystem;

    private AppStorage storage;

    private AppFileSystem afs;

    private AppData ad;

    @Before
    public void setup() {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
        ComputationManager computationManager = Mockito.mock(ComputationManager.class);
        ad = new AppData(computationManager, computationManager, Collections.emptyList(),
                Collections.emptyList(), Collections.singletonList(new FooFileExtension()), Collections.emptyList());

        storage = MapDbAppStorage.createMem("mem", ad.getEventsBus());

        afs = new AppFileSystem("mem", true, storage);
        afs.setData(ad);
        ad.addFileSystem(afs);
    }

    @After
    public void tearDown() throws Exception {
        storage.close();
        fileSystem.close();
    }

    @Test
    public void baseTest() {
        assertSame(InMemoryEventsBus.class, ad.getEventsBus().getClass());
        assertSame(afs, ad.getFileSystem("mem"));
        assertNull(ad.getFileSystem("???"));
        assertEquals(Collections.singletonList("mem"), ad.getRemotelyAccessibleFileSystemNames());
        assertNotNull(ad.getRemotelyAccessibleStorage("mem"));
        assertEquals("mem", afs.getName());
        assertEquals(1, ad.getProjectFileClasses().size());
        Folder root = afs.getRootFolder();
        assertNotNull(root);
        Folder dir1 = root.createFolder("dir1");
        assertTrue(storage.isConsistent(dir1.getId()));
        assertNotNull(dir1);
        dir1.createFolder("dir2");
        dir1.createFolder("dir3");
        dir1 = root.getFolder("dir1").orElse(null);
        assertNotNull(dir1);
        assertTrue(dir1.isFolder());
        assertTrue(dir1.isWritable());
        assertEquals("dir1", dir1.getName());
        assertNotNull(dir1.getCreationDate());
        assertNotNull(dir1.getModificationDate());
        assertEquals(0, dir1.getVersion());
        assertFalse(dir1.isAheadOfVersion());
        assertEquals(dir1.getName(), dir1.toString());
        assertEquals("mem", dir1.getParent().orElseThrow(AssertionError::new).getName());
        Folder dir2 = dir1.getFolder("dir2").orElse(null);
        assertNotNull(dir2);
        assertNotNull(dir2.getParent());
        assertEquals("mem:/dir1", dir2.getParent().orElseThrow(AssertionError::new).getPath().toString());
        assertEquals(2, dir1.getChildren().size());
        Folder dir3 = root.getFolder("dir3").orElse(null);
        assertNull(dir3);
        String str = dir2.getPath().toString();
        assertEquals("mem:/dir1/dir2", str);
        Folder mayBeDir2 = afs.getRootFolder().getFolder("dir1/dir2").orElse(null);
        assertNotNull(mayBeDir2);
        assertEquals("dir2", mayBeDir2.getName());
        Folder mayBeDir2otherWay = afs.getRootFolder().getChild(Folder.class, "dir1", "dir2").orElse(null);
        assertNotNull(mayBeDir2otherWay);
        assertEquals("dir2", mayBeDir2otherWay.getName());

        Project project1 = dir2.createProject("project1");
        project1.setDescription("test project");
        assertNotNull(project1);
        assertEquals("project1", project1.getName());
        assertEquals("test project", project1.getDescription());
        assertNotNull(project1.getParent());
        assertEquals("mem:/dir1/dir2", project1.getParent().orElseThrow(AssertionError::new).getPath().toString());
        assertTrue(project1.getRootFolder().getChildren().isEmpty());
        assertSame(project1.getFileSystem(), afs);

        Project project2 = dir2.createProject("project2");
        project2.rename("project22");
        assertEquals("project22", project2.getName());

        Project projet101 = dir2.createProject("project5");
        Project project102 = dir2.createProject("project6");
        try {
            project102.rename("project5");
            fail();
        } catch (AfsException ignored) {

        }

        Folder dir41 = dir2.createFolder("dir41");
        Project project3 = dir41.createProject("project3");
        project3.delete();
        assertTrue(dir41.getChildren().isEmpty());

        Folder dir51 = dir2.createFolder("dir51");
        Project project5 = dir51.createProject("project5");
        try {
            dir51.delete();
            fail();
        } catch (AfsException ignored) {
        }

        Folder dir71 = root.createFolder("dir7");
        Project project4 = dir41.createProject("projet4");
        project4.moveTo(dir71);
        assertFalse(dir71.getChildren().isEmpty());

        Folder dir81 = root.createFolder("dir8");
        Folder dir82 = dir81.createFolder("dir9");
        try {
            dir81.moveTo(dir82);
            fail();
        } catch (AfsException ignored) {
        }

        assertTrue(dir81.isParentOf(dir82));
        assertTrue(root.isAncestorOf(dir82));
        dir82.moveTo(dir81); // Does nothing
        assertTrue(dir81.isParentOf(dir82));
        assertTrue(root.isAncestorOf(dir82));

        List<String> added = new ArrayList<>();
        List<String> removed = new ArrayList<>();
        ProjectFolderListener l = new ProjectFolderListener() {
            @Override
            public void childAdded(String nodeId) {
                added.add(nodeId);
            }

            @Override
            public void childRemoved(String nodeId) {
                removed.add(nodeId);
            }
        };
        ProjectFolder rootFolder = project1.getRootFolder();
        rootFolder.addListener(l);
        ProjectFolder dir4 = rootFolder.createFolder("dir4");
        assertTrue(dir4.isFolder());
        assertEquals("dir4", dir4.getName());
        assertNotNull(dir4.getParent());
        assertTrue(dir4.getChildren().isEmpty());
        assertEquals(1, rootFolder.getChildren().size());

        dir4.delete();
        assertTrue(rootFolder.getChildren().isEmpty());
        try {
            dir4.getChildren();
            fail();
        } catch (Exception ignored) {
        }

        ProjectFolder dir5 = rootFolder.createFolder("dir5");
        ProjectFolder dir6 = dir5.createFolder("dir6");
        assertEquals(ImmutableList.of("dir5", "dir6"), dir6.getPath().toList().subList(1, 3));
        assertEquals("dir5/dir6", dir6.getPath().toString());
        assertEquals("dir6", rootFolder.getChild("dir5/dir6").orElseThrow(AssertionError::new).getName());

        assertEquals(Arrays.asList(dir4.getId(), dir5.getId()), added);
        assertEquals(Collections.singletonList(dir4.getId()), removed);

        ProjectFolder dir7 = rootFolder.createFolder("dir7");
        dir7.rename("dir77");
        assertEquals("dir77", dir7.getName());

        Path rootDir = fileSystem.getPath("/root");
        try {
            Files.createDirectories(rootDir);
        } catch (IOException ignored) {
        }

        dir7.archive(rootDir);
        Path child = rootDir.resolve(dir7.getId());
        assertTrue(Files.exists(child));

        ProjectFolder dir8 = rootFolder.createFolder("dir8");
        assertEquals(0, dir8.getChildren().size());
        dir8.unarchive(child);
        assertEquals(1, dir8.getChildren().size());
        assertEquals("dir77", dir8.getChildren().get(0).getName());

        Path testDirNotExists = rootDir.resolve("testDirNotExists");
        try {
            dir7.archive(testDirNotExists);
            fail();
        } catch (UncheckedIOException ignored) {
        }

        try {
            dir8.findService(NetworkFactoryService.class);
            fail();
        } catch (AfsException ignored) {
        }
    }

    @Test
    public void moveToTest() {
        Project project = afs.getRootFolder().createProject("test");
        ProjectFolder test1 = project.getRootFolder().createFolder("test1");
        ProjectFolder test2 = project.getRootFolder().createFolder("test2");
        FooFile file = test1.fileBuilder(FooFileBuilder.class)
                .withName("foo")
                .build();
        assertEquals(test1.getId(), file.getParent().orElseThrow(AssertionError::new).getId());
        assertEquals(1, test1.getChildren().size());
        assertTrue(test2.getChildren().isEmpty());
        file.moveTo(test2);
        assertEquals(test2.getId(), file.getParent().orElseThrow(AssertionError::new).getId());
        assertTrue(test1.getChildren().isEmpty());
        assertEquals(1, test2.getChildren().size());
    }

    @Test
    public void findProjectFolderTest() {

        Project project = afs.getRootFolder().createProject("test");
        ProjectFolder test1 = project.getRootFolder().createFolder("test1");
        ProjectFolder projectFolderResult = afs.findProjectFolder(test1.getId());
        assertNotNull(projectFolderResult);
        assertEquals(test1.getId(), projectFolderResult.getId());
        assertEquals(test1.getParentInfo(), projectFolderResult.getParentInfo());
        assertEquals(test1.getCreationDate(), projectFolderResult.getCreationDate());
        assertEquals(test1.getModificationDate(), projectFolderResult.getModificationDate());

    }

    @Test
    public void findProjectFileTest() {
        Project project = afs.getRootFolder().createProject("test");
        FooFile createdFile = project.getRootFolder().fileBuilder(FooFileBuilder.class)
                .withName("foo")
                .build();
        ProjectFile foundFile = afs.findProjectFile(createdFile.getId(), FooFile.class);
        assertNotNull(foundFile);
        assertEquals(createdFile.getId(), foundFile.getId());
        assertEquals(createdFile.getName(), foundFile.getName());
        assertEquals(createdFile.getDescription(), foundFile.getDescription());
        assertEquals(createdFile.getCreationDate(), foundFile.getCreationDate());
        assertEquals(createdFile.getModificationDate(), foundFile.getModificationDate());
        assertEquals(createdFile.getFileSystem(), foundFile.getFileSystem());
        assertEquals(createdFile.getDependencies(), foundFile.getDependencies());
        assertEquals(createdFile.getCodeVersion(), foundFile.getCodeVersion());
    }

    @Test
    public void findProjectTest() {
        Project project = afs.getRootFolder().createProject("test");
        Project foundProject = afs.findProject(project.getId()).orElse(null);
        assertNotNull(foundProject);
        assertEquals(project.getId(), foundProject.getId());
        assertEquals(project.getName(), foundProject.getName());
        assertEquals(project.getDescription(), foundProject.getDescription());
        assertEquals(project.getCreationDate(), foundProject.getCreationDate());
        assertEquals(project.getModificationDate(), foundProject.getModificationDate());
        assertEquals(project.getFileSystem(), foundProject.getFileSystem());
        assertEquals(project.getCodeVersion(), foundProject.getCodeVersion());
    }
}
