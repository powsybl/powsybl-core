/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.itools;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.itools.util.DirectoriesAndFilesProcessing;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.Objects;

import static org.junit.Assert.*;

/**
 * @author François Nicot <francois.nicot@rte-france.com>
 */
public class PackageMojoTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(PackageMojoTest.class);

    private FileSystem fileSystem;
    private String jimfsSeparator;

    private String packageNameNotNull = "powsybl";
    private String virtualResourcesFolderName = "/work/resources";
    private Path virtualPackageDir;
    private Path virtualTargetDir;

    private DirectoriesAndFilesProcessing dirFilesProcessing;

    public FileSystem getJimfsFileSystem() {
        return this.fileSystem;
    }

    @Before
    public void setUp() throws Exception {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
        Objects.requireNonNull(fileSystem);

        jimfsSeparator = fileSystem.getSeparator();
        dirFilesProcessing = new DirectoriesAndFilesProcessing(jimfsSeparator);
    }

    @After
    public void tearDown() throws Exception {
        fileSystem.close();
    }

    // Prerequisites for these tests:
    // We must have the simplified directory 'powsybl' in .../powsybl-core/itools-packager/src/test/resources.

    @Test
    public void createPackage() throws IOException {
        if (initialization()) {
            testZipFileWithoutExt1();
            testZipFileWithoutExt2();
            testZipFileWithZipExt();
            testZipFileWithGzExt();
            testZipFileWithBz2Ext();
        } else {
            LOGGER.info("Warning: Abandoned tests.");
        }
    }

    public void testZipFileWithoutExt1() throws IOException {
        String archiveName = null;
        String archiveNameNotNull = archiveName != null ? archiveName : packageNameNotNull;
        assertNotNull(archiveNameNotNull);

        LOGGER.info("====================================");
        LOGGER.info("Generate zip file package : test n°1");
        LOGGER.info("test zip file without extension (archiveName = {} and archiveNameNotNull = {})", archiveName, archiveNameNotNull);

        new PackageMojo().createPackage(virtualPackageDir, virtualTargetDir, archiveNameNotNull);

        if (!dirFilesProcessing.check(virtualTargetDir, archiveNameNotNull, packageNameNotNull)) {
            Assert.assertTrue("Error : Generate zip file package, test n°1", false);
        }
    }

    public void testZipFileWithoutExt2() throws IOException {
        String archiveName = "file2";
        String archiveNameNotNull = archiveName != null ? archiveName : packageNameNotNull;
        assertNotNull(archiveNameNotNull);

        LOGGER.info("====================================");
        LOGGER.info("Generate zip file package : test n°2");
        LOGGER.info("test zip file without extension (archiveName = archiveNameNotNull = {})", archiveNameNotNull);

        new PackageMojo().createPackage(virtualPackageDir, virtualTargetDir, archiveNameNotNull);

        if (!dirFilesProcessing.check(virtualTargetDir, archiveNameNotNull, packageNameNotNull)) {
            Assert.assertTrue("Error : Generate zip file package, test n°2", false);
        }
    }

    public void testZipFileWithZipExt() throws IOException {
        String archiveName = "file3.zip";
        String archiveNameNotNull = archiveName != null ? archiveName : packageNameNotNull;
        assertNotNull(archiveNameNotNull);

        LOGGER.info("====================================");
        LOGGER.info("Generate zip file package : test n°3");
        LOGGER.info("test zip file with extension (archiveName = archiveNameNotNull = {})", archiveNameNotNull);

        new PackageMojo().createPackage(virtualPackageDir, virtualTargetDir, archiveNameNotNull);

        if (!dirFilesProcessing.check(virtualTargetDir, archiveNameNotNull, packageNameNotNull)) {
            Assert.assertTrue("Error : Generate zip file package, test n°3", false);
        }
    }

    public void testZipFileWithGzExt() throws IOException {
        String archiveName = "file3.tar.gz";
        String archiveNameNotNull = archiveName != null ? archiveName : packageNameNotNull;
        assertNotNull(archiveNameNotNull);

        LOGGER.info("====================================");
        LOGGER.info("Generate zip file package : test n°4");
        LOGGER.info("test zip file with extension (archiveName = archiveNameNotNull = {})", archiveNameNotNull);

        new PackageMojo().createPackage(virtualPackageDir, virtualTargetDir, archiveNameNotNull);

        if (!dirFilesProcessing.check(virtualTargetDir, archiveNameNotNull, packageNameNotNull)) {
            Assert.assertTrue("Error : Generate zip file package, test n°4", false);
        }
    }

    public void testZipFileWithBz2Ext() throws IOException {
        String archiveName = "file3.tar.bz2";
        String archiveNameNotNull = archiveName != null ? archiveName : packageNameNotNull;
        assertNotNull(archiveNameNotNull);

        LOGGER.info("====================================");
        LOGGER.info("Generate zip file package : test n°5");
        LOGGER.info("test zip file with extension (archiveName = archiveNameNotNull = {})", archiveNameNotNull);

        new PackageMojo().createPackage(virtualPackageDir, virtualTargetDir, archiveNameNotNull);

        if (!dirFilesProcessing.check(virtualTargetDir, archiveNameNotNull, packageNameNotNull)) {
            Assert.assertTrue("Error : Generate zip file package, test n°5", false);
        }
    }

    public boolean initialization() throws IOException {
        LOGGER.info("Initialization :");

        String sourceFolderName = "src" + File.separator + "test" + File.separator + "resources";
        sourceFolderName = Paths.get(sourceFolderName).toAbsolutePath().toString();
        final Path targetDir = Paths.get(sourceFolderName).toAbsolutePath();
        Path packageDir = targetDir.resolve(packageNameNotNull);

        if (!Files.exists(targetDir)) {
            LOGGER.info("Warning for these tests: No found '{}' repertoire.", sourceFolderName);
            return false;
        }

        if (!Files.exists(packageDir)) {
            LOGGER.info("Warning for these tests: No found '{}' packageFile in '{}' repertoire.", packageNameNotNull, sourceFolderName);
            return false;
        }

        final Path virtualResourcesDir = getJimfsFileSystem().getPath(virtualResourcesFolderName);
        assertFalse(Files.exists(virtualResourcesDir));

        virtualPackageDir = getJimfsFileSystem().getPath(virtualResourcesFolderName + jimfsSeparator + packageNameNotNull);
        virtualTargetDir = getJimfsFileSystem().getPath(virtualResourcesFolderName);

        // Copy files and directories from "targetDir" directory to the "virtualTargetDir" virtual directory
        Files.deleteIfExists(virtualTargetDir);
        dirFilesProcessing.copyIntoVirtualWorkDir(targetDir, virtualTargetDir, jimfsSeparator);
        return true;
    }
}
