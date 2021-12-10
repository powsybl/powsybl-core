package com.powsybl.commons.io;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Sylvain Leclerc <sylvain.leclerc at rte-france.com>
 */
public class WorkingDirectoryTest {

    private FileSystem fileSystem;

    private Path path;

    @Before
    public void setUp() throws Exception {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
        path = fileSystem.getPath("/tmp");
        Files.createDirectories(path);
    }

    @Test
    public void testDebug() throws IOException {
        Path workingDirPath;
        try (WorkingDirectory dir = new WorkingDirectory(path, "test-", true)) {
            workingDirPath = dir.toPath();
            assertTrue(workingDirPath.toString().startsWith("/tmp/test-"));
            assertTrue(Files.isDirectory(workingDirPath));
        }
        //must still exist
        assertTrue(Files.isDirectory(workingDirPath));
    }

    @Test
    public void testDirDeletion() throws IOException {
        Path workingDirPath;
        try (WorkingDirectory dir = new WorkingDirectory(path, "test-", false)) {
            workingDirPath = dir.toPath();
            assertTrue(workingDirPath.toString().startsWith("/tmp/test-"));
            assertTrue(Files.isDirectory(workingDirPath));
        }
        //must be deleted
        assertFalse(Files.exists(workingDirPath));
    }

    @Test
    public void closeShouldBeIdemPotent() throws IOException {
        Path workingDirPath;
        try (WorkingDirectory dir = new WorkingDirectory(path, "test-", false)) {
            workingDirPath = dir.toPath();
            assertTrue(Files.isDirectory(workingDirPath));
            //Must not throw
            dir.close();
        }
    }
}
