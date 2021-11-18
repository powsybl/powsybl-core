/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.export;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.datasource.DataSource;
import com.powsybl.commons.reporter.Report;
import com.powsybl.commons.reporter.ReporterModel;
import com.powsybl.iidm.AbstractConvertersTest;

import com.powsybl.iidm.tools.ExporterMockWithReporter;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import java.util.Collection;
import java.util.Optional;

import static org.junit.Assert.*;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
public class ExportersTest extends AbstractConvertersTest {

    private static final String FOO_TST = "foo.tst";
    private static final String WORK_FOO_TST = "/work/" + FOO_TST;

    private final Exporter testExporter = new TestExporter();
    private final ExportersLoader loader = new ExportersLoaderList(testExporter);

    @Test
    public void getFormats() {
        Collection<String> formats = Exporters.getFormats(loader);
        assertEquals(1, formats.size());
        assertTrue(formats.contains(TEST_FORMAT));
    }

    @Test
    public void getExporter() {
        Exporter exporter = Exporters.getExporter(loader, TEST_FORMAT);
        assertNotNull(exporter);
        assertSame(testExporter, exporter);
    }

    @Test
    public void getNullExporter() {
        Exporter exporter = Exporters.getExporter(loader, UNSUPPORTED_FORMAT);
        assertNull(exporter);
    }

    @Test
    public void createDataSource1() throws IOException {
        Files.createFile(fileSystem.getPath(WORK_FOO_TST));
        DataSource dataSource = Exporters.createDataSource(fileSystem.getPath("/work/"), "foo", null);
        assertTrue(dataSource.exists(FOO_TST));
    }

    @Test
    public void createDataSource2() throws IOException {
        Files.createFile(fileSystem.getPath(WORK_FOO_TST));
        DataSource dataSource = Exporters.createDataSource(path, null);
        assertTrue(dataSource.exists(FOO_TST));
    }

    @Test
    public void createDataSource3() throws IOException {
        Files.createFile(fileSystem.getPath(WORK_FOO_TST));
        DataSource dataSource = Exporters.createDataSource(path);
        assertTrue(dataSource.exists(FOO_TST));
    }

    @Test
    public void failExport() {
        expected.expect(PowsyblException.class);
        expected.expectMessage("Export format " + UNSUPPORTED_FORMAT + " not supported");
        Exporters.export(loader, UNSUPPORTED_FORMAT, null, null, Exporters.createDataSource(path));
    }

    @Test
    public void export1() throws IOException {
        DataSource dataSource = Exporters.createDataSource(path);
        Exporters.export(loader, TEST_FORMAT, null, null, dataSource);
        assertEquals(Byte.BYTES, dataSource.newInputStream(null, EXTENSION).read());
    }

    @Test
    public void export2() throws IOException {
        Exporters.export(loader, TEST_FORMAT, null, null, path);
        DataSource dataSource = Exporters.createDataSource(path);
        assertEquals(Byte.BYTES, dataSource.newInputStream(null, EXTENSION).read());
    }

    @Test
    public void export3() throws IOException {
        Path dir = Files.createTempDirectory("tmp-export");
        Exporters.export(loader, TEST_FORMAT, null, null,  dir.toString(), "tmp");
        try (InputStream is = Files.newInputStream(dir.resolve("tmp.tst"))) {
            assertEquals(Byte.BYTES, is.read());
        }
    }

    @Test
    public void exportWithReporter() {
        Exporter testExporter = new ExporterMockWithReporter();
        DataSource dataSource = Exporters.createDataSource(path);
        ReporterModel reporter = new ReporterModel("reportTest", "Testing exporter reporter");
        testExporter.export(null, null, dataSource, reporter);
        Optional<Report> report = reporter.getReports().stream().findFirst();
        assertTrue(report.isPresent());

    }
}
