package com.powsybl.commons.io;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Sylvain Leclerc {@literal <sylvain.leclerc at rte-france.com>}
 */
class WorkingDirectoryTest {

    private FileSystem fileSystem;

    private Path path;

    @BeforeEach
    void setUp() throws Exception {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
        path = fileSystem.getPath("/tmp");
        Files.createDirectories(path);
    }

    @Test
    void testDebug() throws IOException {
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
    void testDirDeletion() throws IOException {
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
    void closeShouldBeIdemPotent() throws IOException {
        Path workingDirPath;
        try (WorkingDirectory dir = new WorkingDirectory(path, "test-", false)) {
            workingDirPath = dir.toPath();
            assertTrue(Files.isDirectory(workingDirPath));
            //Must not throw
            dir.close();
        }
    }
}
