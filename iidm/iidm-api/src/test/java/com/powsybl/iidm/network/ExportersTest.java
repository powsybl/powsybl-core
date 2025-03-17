/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network;

import com.google.common.io.ByteStreams;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.datasource.DataSource;
import com.powsybl.commons.datasource.DirectoryDataSource;
import com.powsybl.commons.report.PowsyblCoreReportResourceBundle;
import com.powsybl.commons.test.PowsyblCoreTestReportResourceBundle;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.commons.test.TestUtil;
import com.powsybl.iidm.network.tools.ExporterMockWithReportNode;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
 */
class ExportersTest extends AbstractConvertersTest {

    private final Exporter testExporter = new TestExporter();
    private final ExportersLoader loader = new ExportersLoaderList(testExporter);

    @Test
    void getFormats() {
        Collection<String> formats = Exporter.getFormats(loader);
        assertEquals(1, formats.size());
        assertTrue(formats.contains(TEST_FORMAT));
    }

    @Test
    void getExporter() {
        Exporter exporter = Exporter.find(loader, TEST_FORMAT);
        assertNotNull(exporter);
        assertSame(testExporter, exporter);
    }

    @Test
    void getNullExporter() {
        Exporter exporter = Exporter.find(loader, UNSUPPORTED_FORMAT);
        assertNull(exporter);
    }

    @Test
    void createDataSource1() throws IOException {
        Files.createFile(path);
        DataSource dataSource = new DirectoryDataSource(fileSystem.getPath(WORK_FOLDER), FOO_BASENAME);
        assertTrue(dataSource.exists(FOO_TST));
    }

    @Test
    void createDataSource2() throws IOException {
        Files.createFile(path);
        DataSource dataSource = Exporters.createDataSource(path, null);
        assertTrue(dataSource.exists(FOO_TST));
    }

    @Test
    void createDataSource3() throws IOException {
        Files.createFile(path);
        DataSource dataSource = Exporters.createDataSource(path);
        assertTrue(dataSource.exists(FOO_TST));
    }

    @Test
    void createDataSourceFolder() throws IOException {
        Path dataFolder = fileSystem.getPath(WORK_FOLDER).resolve("dataFolder");
        Files.createDirectories(dataFolder);
        Files.createFile(dataFolder.resolve(FOO_TST));
        DataSource dataSource = Exporters.createDataSource(dataFolder, null);
        assertTrue(dataSource.exists(FOO_TST));
        try (Writer writer = new OutputStreamWriter(dataSource.newOutputStream("_suffix", "txt", false), StandardCharsets.UTF_8)) {
            writer.write("foo");
        }
        Path outFile = dataFolder.resolve("dataFolder_suffix.txt");
        assertTrue(Files.exists(outFile));
        assertEquals(List.of("foo"), Files.readAllLines(outFile));
    }

    @Test
    void failExport() {
        Network network = Mockito.spy(Network.class);
        PowsyblException e = assertThrows(PowsyblException.class, () -> network.write(loader, UNSUPPORTED_FORMAT, null, Exporters.createDataSource(path)));
        assertEquals("Export format " + UNSUPPORTED_FORMAT + " not supported", e.getMessage());
    }

    @Test
    void export1() throws IOException {
        DataSource dataSource = Exporters.createDataSource(path);
        Network network = Mockito.spy(Network.class);
        network.write(loader, TEST_FORMAT, null, dataSource);
        try (var is = dataSource.newInputStream(null, EXTENSION)) {
            assertEquals(1, is.read());
        }
    }

    @Test
    void export2() throws IOException {
        Network network = Mockito.spy(Network.class);
        network.write(loader, TEST_FORMAT, null, path);
        DataSource dataSource = Exporters.createDataSource(path);
        try (var is = dataSource.newInputStream(null, EXTENSION)) {
            assertEquals(1, is.read());
        }
    }

    @Test
    void export3() throws IOException {
        Path dir = Files.createTempDirectory("tmp-export");
        Network network = Mockito.spy(Network.class);
        network.write(loader, TEST_FORMAT, null, dir.toString(), "tmp");
        try (InputStream is = Files.newInputStream(dir.resolve("tmp.tst"))) {
            assertEquals(1, is.read());
        }
    }

    @Test
    void exportWithReportNode() throws Exception {
        Exporter testExporter = new ExporterMockWithReportNode();
        DataSource dataSource = Exporters.createDataSource(path);
        ReportNode rootReportNode = ReportNode.newRootReportNode(PowsyblCoreTestReportResourceBundle.TEST_BASE_NAME, PowsyblCoreReportResourceBundle.BASE_NAME)
                .withMessageTemplate("reportTest")
                .build();
        testExporter.export(null, null, dataSource, rootReportNode);
        Optional<ReportNode> reportNode = rootReportNode.getChildren().stream().findFirst();
        assertTrue(reportNode.isPresent());
        assertInstanceOf(ReportNode.class, reportNode.get());

        StringWriter sw = new StringWriter();
        rootReportNode.print(sw);

        InputStream refStream = getClass().getResourceAsStream("/exportReportNodeTest.txt");
        String refLogExport = TestUtil.normalizeLineSeparator(new String(ByteStreams.toByteArray(refStream), StandardCharsets.UTF_8));
        String logExport = TestUtil.normalizeLineSeparator(sw.toString());
        assertEquals(refLogExport, logExport);
    }
}
