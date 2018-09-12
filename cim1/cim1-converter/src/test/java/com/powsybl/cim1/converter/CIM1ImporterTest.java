/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cim1.converter;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.commons.datasource.DataSource;
import com.powsybl.commons.datasource.FileDataSource;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.commons.datasource.ZipFileDataSource;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
public class CIM1ImporterTest {

    private FileSystem fileSystem;

    private DataSource zdsMerged;
    private DataSource zdsSplit;
    private DataSource fdsMerged;
    private DataSource fdsUnzippedMerged;
    private DataSource fdsSplit;
    private DataSource fdsUnzippedSplit;

    private CIM1Importer importer;

    private void copyFile(DataSource dataSource, String filename) throws IOException {
        try (OutputStream stream = dataSource.newOutputStream(filename, false)) {
            IOUtils.copy(getClass().getResourceAsStream("/" + filename), stream);
        }
    }

    @Before
    public void setUp() throws IOException {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());

        Path test1 = Files.createDirectory(fileSystem.getPath("test1"));
        fdsMerged = new FileDataSource(test1, "ieee14bus");
        fdsUnzippedMerged = new FileDataSource(test1, "ieee14bus_ME");
        copyFile(fdsMerged, "ieee14bus_ME.xml");
        copyFile(fdsMerged, "ENTSO-E_Boundary_Set_EU_EQ.xml");
        copyFile(fdsMerged, "ENTSO-E_Boundary_Set_EU_TP.xml");

        Path test2 = Files.createDirectory(fileSystem.getPath("test2"));
        zdsMerged = new ZipFileDataSource(test2, "ieee14bus");
        copyFile(zdsMerged, "ieee14bus_ME.xml");
        copyFile(fdsMerged, "ENTSO-E_Boundary_Set_EU_EQ.xml");
        copyFile(fdsMerged, "ENTSO-E_Boundary_Set_EU_TP.xml");

        Path test3 = Files.createDirectory(fileSystem.getPath("test3"));
        fdsSplit = new FileDataSource(test3, "ieee14bus");
        fdsUnzippedSplit = new FileDataSource(test3, "ieee14bus_EQ");
        copyFile(fdsSplit, "ieee14bus_EQ.xml");
        copyFile(fdsSplit, "ieee14bus_TP.xml");
        copyFile(fdsSplit, "ieee14bus_SV.xml");
        copyFile(fdsSplit, "ENTSO-E_Boundary_Set_EU_EQ.xml");
        copyFile(fdsSplit, "ENTSO-E_Boundary_Set_EU_TP.xml");

        Path test4 = Files.createDirectory(fileSystem.getPath("test4"));
        zdsSplit = new ZipFileDataSource(test4, "ieee14bus");
        copyFile(zdsSplit, "ieee14bus_EQ.xml");
        copyFile(zdsSplit, "ieee14bus_TP.xml");
        copyFile(zdsSplit, "ieee14bus_SV.xml");
        copyFile(zdsSplit, "ENTSO-E_Boundary_Set_EU_EQ.xml");
        copyFile(zdsSplit, "ENTSO-E_Boundary_Set_EU_TP.xml");

        importer = new CIM1Importer();
    }

    @After
    public void tearDown() throws IOException {
        fileSystem.close();
    }

    @Test
    public void exists() {
        assertTrue(importer.exists(fdsMerged));
        assertTrue(importer.exists(fdsUnzippedMerged));
        assertTrue(importer.exists(zdsMerged));
        assertTrue(importer.exists(fdsSplit));
        assertTrue(importer.exists(fdsUnzippedSplit));
        assertTrue(importer.exists(zdsSplit));
    }

    @Test
    public void testImport() {
        testImport(fdsMerged);
        testImport(fdsUnzippedMerged);
        testImport(fdsSplit);
        testImport(fdsUnzippedSplit);
    }

    private void testImport(ReadOnlyDataSource dataSource) {
        try {
            importer.importData(dataSource, new Properties());
            //fail();
        } catch (RuntimeException ignored) {
        }
    }

    @Test
    public void copy() throws Exception {
        Path testCopyDir = Files.createDirectory(fileSystem.getPath("test_copy"));
        importer.copy(zdsSplit, new FileDataSource(testCopyDir, "newbasename"));
        assertTrue(Files.exists(testCopyDir.resolve("newbasename_EQ.xml")));
        assertTrue(Files.exists(testCopyDir.resolve("newbasename_TP.xml")));
        assertTrue(Files.exists(testCopyDir.resolve("newbasename_SV.xml")));
        assertTrue(Files.exists(testCopyDir.resolve("ENTSO-E_Boundary_Set_EU_EQ.xml")));
        assertTrue(Files.exists(testCopyDir.resolve("ENTSO-E_Boundary_Set_EU_TP.xml")));
    }
}
