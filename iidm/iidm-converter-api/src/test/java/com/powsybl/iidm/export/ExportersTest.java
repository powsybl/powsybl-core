/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.export;

import com.google.common.jimfs.Jimfs;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.datasource.DataSource;
import com.powsybl.commons.datasource.FileDataSource;
import com.powsybl.iidm.network.NetworkFactory;
import org.junit.*;
import org.junit.rules.ExpectedException;

import java.io.*;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
public class ExportersTest {

    @Rule
    public final ExpectedException expected = ExpectedException.none();

    private ExportersLoader loader = new ExportersLoaderList(Collections.singletonList(new TestExporter()));
    private FileSystem fileSystem;
    private Path tmpDir;

    @Before
    public void setUp() throws IOException {
        fileSystem = Jimfs.newFileSystem();
        tmpDir = Files.createDirectory(fileSystem.getPath("tmp"));
    }

    @After
    public void tearDown() throws IOException {
        fileSystem.close();
    }

    @Test
    public void getFormat() {
        Collection<String> formats = Exporters.getFormats(loader);
        assertEquals(1, formats.size());
        assertTrue(formats.contains("TST"));
    }

    @Test
    public void exportWithNullExporter() {
        expected.expect(PowsyblException.class);
        expected.expectMessage("Export format UNSUPPORTED not supported");
        Exporters.export("UNSUPPORTED", NetworkFactory.create("test", "test"),
                null, new FileDataSource(tmpDir, "test0.tst"), loader);
    }

    @Test
    public void createDataSource() throws IOException {
        Files.createFile(tmpDir.resolve("test1.tst"));
        DataSource ds = Exporters.createDataSource(tmpDir.resolve("test1.tst"));
        assertTrue(ds.exists("test1.tst"));
    }

    @Test
    public void createDataSourceWhenFileExistsAndIsNotARegularFile() {
        expected.expect(UncheckedIOException.class);
        expected.expectMessage("File tmp already exists and is not a regular file");
        Exporters.createDataSource(tmpDir);
    }

    @Test
    public void export() throws IOException {
        Exporters.export("TST", NetworkFactory.create("test", "test"),
                null, tmpDir.resolve("test2.tst"), loader);
        try (BufferedReader reader = Files.newBufferedReader(tmpDir.resolve("test2.tst"))) {
            assertEquals("This is a test", reader.lines().collect(Collectors.joining()));
        }
    }
}
