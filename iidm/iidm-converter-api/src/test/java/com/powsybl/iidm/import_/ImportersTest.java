/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.import_;

import com.google.common.io.ByteStreams;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.TestUtil;
import com.powsybl.commons.datasource.DataSource;
import com.powsybl.commons.reporter.Report;
import com.powsybl.commons.reporter.ReporterModel;
import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.AbstractConvertersTest;
import com.powsybl.iidm.network.LoadType;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.NetworkFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ImportersTest extends AbstractConvertersTest {

    private static final String WORK_DIR = "/work/";
    private static final String FOO_TST = "foo.tst";

    private final Importer testImporter = new TestImporter();
    private final ImportPostProcessor testImportPostProcessor = new TestImportPostProcessor();
    private final ImportersLoader loader = new ImportersLoaderList(Collections.singletonList(testImporter),
            Collections.singletonList(testImportPostProcessor));

    private final ComputationManager computationManager = Mockito.mock(ComputationManager.class);

    private final ImportConfig importConfigMock = Mockito.mock(ImportConfig.class);
    private final ImportConfig importConfigWithPostProcessor = new ImportConfig("test");

    @Before
    public void setUp() throws IOException {
        super.setUp();
        Files.createFile(fileSystem.getPath(WORK_DIR + FOO_TST));
        Files.createFile(fileSystem.getPath(WORK_DIR + "bar.tst"));
        Files.createFile(fileSystem.getPath(WORK_DIR + "baz.txt"));
    }

    @Test
    public void getFormat() {
        Collection<String> formats = Importers.getFormats(loader);
        assertNotNull(formats);
        assertEquals(1, formats.size());
        assertTrue(formats.contains(TEST_FORMAT));
    }

    @Test
    public void list() {
        Collection<Importer> importers = Importers.list(loader, computationManager, importConfigMock);
        assertNotNull(importers);
        assertEquals(1, importers.size());
        assertTrue(importers.contains(testImporter));
    }

    @Test
    public void getImporter() {
        Importer importer = Importers.getImporter(loader, TEST_FORMAT, computationManager, importConfigMock);
        assertNotNull(importer);
        assertSame(testImporter, importer);
    }

    @Test
    public void getImporterWithImportConfig() {
        Importer importer = Importers.getImporter(loader, TEST_FORMAT, computationManager, importConfigWithPostProcessor);
        assertNotNull(importer);
        Network network = importer.importData(null, NetworkFactory.findDefault(), null);
        assertNotNull(network);
        assertEquals(LoadType.FICTITIOUS, network.getLoad("LOAD").getLoadType());
    }

    @Test
    public void getNullImporter() {
        Importer importer = Importers.getImporter(loader, UNSUPPORTED_FORMAT, computationManager, importConfigMock);
        assertNull(importer);
    }

    @Test
    public void getPostProcessorNames() {
        Collection<String> names = Importers.getPostProcessorNames(loader);
        assertNotNull(names);
        assertEquals(1, names.size());
        assertTrue(names.contains("test"));
    }

    @Test
    public void addAndRemovePostProcessor() {
        Importer importer1 = Importers.addPostProcessors(loader, testImporter, computationManager, "test");
        Network network1 = importer1.importData(null, NetworkFactory.findDefault(), null);
        assertNotNull(network1);
        assertEquals(LoadType.FICTITIOUS, network1.getLoad("LOAD").getLoadType());

        Importer importer2 = Importers.removePostProcessors(importer1);
        Network network2 = importer2.importData(null, NetworkFactory.findDefault(), null);
        assertNotNull(network2);
        assertEquals(LoadType.UNDEFINED, network2.getLoad("LOAD").getLoadType());
    }

    @Test
    public void setPostProcessor() {
        Importer importer = Importers.setPostProcessors(loader, testImporter, computationManager, "test");
        Network network = importer.importData(null, NetworkFactory.findDefault(), null);
        assertNotNull(network);
        assertEquals(LoadType.FICTITIOUS, network.getLoad("LOAD").getLoadType());
    }

    @Test
    public void importData() {
        Network network = Importers.importData(loader, TEST_FORMAT, null, null, computationManager, importConfigMock);
        assertNotNull(network);
        assertNotNull(network.getLoad("LOAD"));
    }

    @Test
    public void importBadData() {
        expected.expect(PowsyblException.class);
        expected.expectMessage("Import format " + UNSUPPORTED_FORMAT + " not supported");
        Importers.importData(loader, UNSUPPORTED_FORMAT, null, null, computationManager, importConfigMock);
    }

    @Test
    public void importAll() throws InterruptedException, ExecutionException, IOException {
        List<Boolean> isLoadPresent = new ArrayList<>();
        Importers.importAll(fileSystem.getPath(WORK_DIR), testImporter, false, n -> isLoadPresent.add(n.getLoad("LOAD") != null));
        assertEquals(2, isLoadPresent.size());
        isLoadPresent.forEach(Assert::assertTrue);
    }

    @Test
    public void importAllParallel() throws InterruptedException, ExecutionException, IOException {
        List<Boolean> isLoadPresent = Collections.synchronizedList(new ArrayList<>());
        Importers.importAll(fileSystem.getPath(WORK_DIR), testImporter, true, n -> isLoadPresent.add(n.getLoad("LOAD") != null));
        assertEquals(2, isLoadPresent.size());
        isLoadPresent.forEach(Assert::assertTrue);
    }

    @Test
    public void createDataSource1() throws IOException {
        DataSource dataSource = Importers.createDataSource(fileSystem.getPath(WORK_DIR), "foo");
        assertTrue(dataSource.exists(FOO_TST));
    }

    @Test
    public void createDataSource2() throws IOException {
        DataSource dataSource = Importers.createDataSource(path);
        assertTrue(dataSource.exists(FOO_TST));
    }

    @Test
    public void findImporter() {
        Importer importer = Importers.findImporter(Importers.createDataSource(path), loader, computationManager, importConfigMock);
        assertNotNull(importer);
        assertEquals(testImporter, importer);
    }

    @Test
    public void findNullImporter() {
        Importer importer = Importers.findImporter(Importers.createDataSource(badPath), loader, computationManager, importConfigMock);
        assertNull(importer);
    }

    @Test
    public void loadNetwork1() {
        Network network = Importers.loadNetwork(path, computationManager, importConfigMock, null, loader);
        assertNotNull(network);
        assertNotNull(network.getLoad("LOAD"));
    }

    @Test
    public void loadNullNetwork1() {
        expected.expect(PowsyblException.class);
        expected.expectMessage("Unsupported file format or invalid file.");
        Importers.loadNetwork(badPath, computationManager, importConfigMock, null, loader);
    }

    @Test
    public void loadNetwork2() throws IOException {
        Network network = Importers.loadNetwork(FOO_TST, Importers.createDataSource(path).newInputStream(null, EXTENSION), computationManager, importConfigMock, null, loader);
        assertNotNull(network);
        assertNotNull(network.getLoad("LOAD"));
    }

    @Test
    public void loadNullNetwork2() throws IOException {
        expected.expect(PowsyblException.class);
        expected.expectMessage("Unsupported file format or invalid file.");
        Importers.loadNetwork("baz.txt", Importers.createDataSource(badPath).newInputStream(null, "txt"), computationManager, importConfigMock, null, loader);
    }

    @Test
    public void loadNetworks() throws InterruptedException, ExecutionException, IOException {
        List<Boolean> isLoadPresent = new ArrayList<>();
        Importers.loadNetworks(fileSystem.getPath(WORK_DIR), false, loader, computationManager, importConfigMock, n -> isLoadPresent.add(n.getLoad("LOAD") != null), null);
        assertEquals(2, isLoadPresent.size());
        isLoadPresent.forEach(Assert::assertTrue);
    }

    @Test
    public void postProcessorWithReporter() throws IOException {
        ImportPostProcessorMock importPostProcessorMock = new ImportPostProcessorMock();
        ImportersLoader loader = new ImportersLoaderList(Collections.singletonList(testImporter), Collections.singletonList(importPostProcessorMock));
        Importer importer1 = Importers.addPostProcessors(loader, testImporter, computationManager, "testReporter");

        ReporterModel reporter = new ReporterModel("testPostProcessor", "Test importer post processor");
        Network network1 = importer1.importData(null, NetworkFactory.findDefault(), null, reporter);
        assertNotNull(network1);

        Optional<Report> report = reporter.getReports().stream().findFirst();
        assertTrue(report.isPresent());

        StringWriter sw = new StringWriter();
        reporter.export(sw);
        InputStream refStream = getClass().getResourceAsStream("/postProcessorReporterTest.txt");
        String refLogExport = TestUtil.normalizeLineSeparator(new String(ByteStreams.toByteArray(refStream), StandardCharsets.UTF_8));
        String logExport = TestUtil.normalizeLineSeparator(sw.toString());
        assertEquals(refLogExport, logExport);
    }

}

