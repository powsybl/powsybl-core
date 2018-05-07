/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.commons.config.InMemoryPlatformConfig;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.commons.datasource.FileDataSource;
import com.powsybl.iidm.network.Network;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.util.Properties;

import static org.junit.Assert.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class XMLImporterTest {

    private FileSystem fileSystem;

    private XMLImporter importer;

    private void writeNetwork(String fileName, boolean writeExt) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(fileSystem.getPath(fileName), StandardCharsets.UTF_8)) {
            writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            writer.write("<iidm:network xmlns:iidm=\"http://www.itesla_project.eu/schema/iidm/1_0\" id=\"test\" caseDate=\"2013-01-15T18:45:00.000+01:00\" forecastDistance=\"0\" sourceFormat=\"test\">");
            writer.newLine();
            writer.write("    <iidm:substation id=\"P1\" country=\"FR\"/>");
            writer.newLine();
            if (writeExt) {
                writer.write("    <iidm:extension id=\"P1\">");
                writer.write("    <foo/>");
                writer.write("    </iidm:extension>");
            }
            writer.write("</iidm:network>");
            writer.newLine();
        }
    }

    private void writeNetworkWithComment(String fileName) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(fileSystem.getPath(fileName), StandardCharsets.UTF_8)) {
            writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            writer.newLine();
            writer.write("<!--sfsfs-->");
            writer.write("<iidm:network xmlns:iidm=\"http://www.itesla_project.eu/schema/iidm/1_0\" id=\"test\" caseDate=\"2013-01-15T18:45:00.000+01:00\" forecastDistance=\"0\" sourceFormat=\"test\">");
            writer.newLine();
            writer.write("    <iidm:substation id=\"P1\" country=\"FR\"/>");
            writer.newLine();
            writer.write("</iidm:network>");
            writer.newLine();
        }
    }

    @Before
    public void setUp() throws Exception {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());

        // create test files
        //   /test0.xiidm
        //   /test1.iidm
        //   /test2.xml
        //   /test3.txt
        //   /test5.xiidm that contains unsupported extensions
        //   /test6.xiidm + /test6_mapping.csv
        //   /test7.xiidm that contains a comment after xml prolog
        writeNetwork("/test0.xiidm", false);
        writeNetwork("/test1.iidm", false);
        writeNetwork("/test2.xml", false);
        writeNetwork("/test3.txt", false);
        writeNetwork("/test5.xiidm", true);
        writeNetwork("/test6.xiidm", false);
        try (BufferedWriter writer = Files.newBufferedWriter(fileSystem.getPath("/test6_mapping.csv"), StandardCharsets.UTF_8)) {
            writer.write("X1;P1");
            writer.newLine();
        }
        writeNetworkWithComment("/test7.xiidm");

        PlatformConfig platformConfig = new InMemoryPlatformConfig(fileSystem);
        importer = new XMLImporter(platformConfig);
    }

    @After
    public void tearDown() throws Exception {
        fileSystem.close();
    }

    @Test
    public void getFormat() throws Exception {
        assertEquals("XIIDM", importer.getFormat());
    }

    @Test
    public void getParameters() throws Exception {
        assertEquals(1, importer.getParameters().size());
        assertEquals("throwExceptionIfExtensionNotFound", importer.getParameters().get(0).getName());
    }

    @Test
    public void getComment() throws Exception {
        assertEquals("IIDM XML v 1.0 importer", importer.getComment());
    }

    @Test
    public void exists() throws Exception {
        assertTrue(importer.exists(new FileDataSource(fileSystem.getPath("/"), "test0")));
        assertTrue(importer.exists(new FileDataSource(fileSystem.getPath("/"), "test1")));
        assertTrue(importer.exists(new FileDataSource(fileSystem.getPath("/"), "test2")));
        assertFalse(importer.exists(new FileDataSource(fileSystem.getPath("/"), "test3"))); // wrong extension
        assertFalse(importer.exists(new FileDataSource(fileSystem.getPath("/"), "test4"))); // does not exist
    }

    @Test
    public void copy() throws Exception {
        importer.copy(new FileDataSource(fileSystem.getPath("/"), "test0"), new FileDataSource(fileSystem.getPath("/"), "test0_copy"));
        assertTrue(Files.exists(fileSystem.getPath("/test0_copy.xiidm")));
        assertEquals(Files.readAllLines(fileSystem.getPath("/test0.xiidm"), StandardCharsets.UTF_8),
                     Files.readAllLines(fileSystem.getPath("/test0_copy.xiidm"), StandardCharsets.UTF_8));

        // test copy with id mapping file
        importer.copy(new FileDataSource(fileSystem.getPath("/"), "test6"), new FileDataSource(fileSystem.getPath("/"), "test6_copy"));
        assertTrue(Files.exists(fileSystem.getPath("/test6_copy.xiidm")));
        assertTrue(Files.exists(fileSystem.getPath("/test6_copy_mapping.csv")));
        assertEquals(Files.readAllLines(fileSystem.getPath("/test6.xiidm"), StandardCharsets.UTF_8),
                     Files.readAllLines(fileSystem.getPath("/test6_copy.xiidm"), StandardCharsets.UTF_8));
        assertEquals(Files.readAllLines(fileSystem.getPath("/test6_mapping.csv"), StandardCharsets.UTF_8),
                     Files.readAllLines(fileSystem.getPath("/test6_copy_mapping.csv"), StandardCharsets.UTF_8));
    }

    @Test
    public void importData() throws Exception {
        // should be ok
        assertNotNull(importer.importData(new FileDataSource(fileSystem.getPath("/"), "test0"), null));

        // should fail because file that does not exist
        try {
            importer.importData(new FileDataSource(fileSystem.getPath("/"), "test4"), null);
            fail();
        } catch (RuntimeException ignored) {
        }

        // extension plugin will be not found but default option just warn
        assertNotNull(importer.importData(new FileDataSource(fileSystem.getPath("/"), "test5"), null));

        // extension plugin will be not found but option is set to throw an exception
        Properties params = new Properties();
        params.put("throwExceptionIfExtensionNotFound", "true");
        try {
            importer.importData(new FileDataSource(fileSystem.getPath("/"), "test5"), params);
            fail();
        } catch (RuntimeException ignored) {
        }

        // read file with id mapping
        Network network = importer.importData(new FileDataSource(fileSystem.getPath("/"), "test6"), params);
        assertNotNull(network.getSubstation("X1")); // and not P1 !!!!!

        Network network2 = importer.importData(new FileDataSource(fileSystem.getPath("/"), "test7"), null);
        assertNotNull(network2.getSubstation("P1"));
    }
}
