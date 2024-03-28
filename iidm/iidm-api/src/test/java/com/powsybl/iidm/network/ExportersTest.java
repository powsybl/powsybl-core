/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network;

import com.google.common.io.ByteStreams;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.datasource.DataSource;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.commons.test.TestUtil;
import com.powsybl.iidm.network.tools.ExporterMockWithReportNode;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
 */
class ExportersTest extends AbstractConvertersTest {

    private static final String FOO_TST = "foo.tst";
    private static final String WORK_FOO_TST = "/work/" + FOO_TST;

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
        Files.createFile(fileSystem.getPath(WORK_FOO_TST));
        DataSource dataSource = Exporters.createDataSource(fileSystem.getPath("/work/"), "foo", null);
        assertTrue(dataSource.exists(FOO_TST));
    }

    @Test
    void createDataSource2() throws IOException {
        Files.createFile(fileSystem.getPath(WORK_FOO_TST));
        DataSource dataSource = Exporters.createDataSource(path, null);
        assertTrue(dataSource.exists(FOO_TST));
    }

    @Test
    void createDataSource3() throws IOException {
        Files.createFile(fileSystem.getPath(WORK_FOO_TST));
        DataSource dataSource = Exporters.createDataSource(path);
        assertTrue(dataSource.exists(FOO_TST));
    }

    @Test
    void failExport() {
        Network network = Mockito.spy(Network.class);
        DataSource dataSource = Exporters.createDataSource(path);
        PowsyblException e = assertThrows(PowsyblException.class, () -> network.write(loader, UNSUPPORTED_FORMAT, null, dataSource));
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
        ReportNode rootReportNode = ReportNode.newRootReportNode().withMessageTemplate("reportTest", "Testing exporter reporting").build();
        testExporter.export(null, null, dataSource, rootReportNode);
        Optional<ReportNode> reportNode = rootReportNode.getChildren().stream().findFirst();
        assertTrue(reportNode.isPresent());
        assertTrue(reportNode.get() instanceof ReportNode);

        StringWriter sw = new StringWriter();
        rootReportNode.print(sw);

        InputStream refStream = getClass().getResourceAsStream("/exportReportNodeTest.txt");
        String refLogExport = TestUtil.normalizeLineSeparator(new String(ByteStreams.toByteArray(refStream), StandardCharsets.UTF_8));
        String logExport = TestUtil.normalizeLineSeparator(sw.toString());
        assertEquals(refLogExport, logExport);
    }
}
