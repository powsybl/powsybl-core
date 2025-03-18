/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.datasource.DataSource;
import com.powsybl.commons.datasource.DirectoryDataSource;
import com.powsybl.commons.report.PowsyblCoreReportResourceBundle;
import com.powsybl.commons.test.PowsyblCoreTestReportResourceBundle;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.commons.test.TestUtil;
import com.powsybl.computation.ComputationManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class ImportersTest extends AbstractConvertersTest {

    private static final String WORK_DIR = "/work/";
    private static final String FOO_TST = "foo.tst";

    private final Importer testImporter = new TestImporter();
    private final ImportPostProcessor testImportPostProcessor = new TestImportPostProcessor();
    private final ImportersLoader loader = new ImportersLoaderList(Collections.singletonList(testImporter),
            Collections.singletonList(testImportPostProcessor));

    private final ComputationManager computationManager = Mockito.mock(ComputationManager.class);

    private final ImportConfig importConfigMock = Mockito.mock(ImportConfig.class);
    private final ImportConfig importConfigWithPostProcessor = new ImportConfig("test");
    private final NetworkFactory networkFactory = new NetworkFactoryMock();

    @BeforeEach
    void setUp() throws IOException {
        super.setUp();
        Files.createFile(fileSystem.getPath(WORK_DIR + FOO_TST));
        Files.createFile(fileSystem.getPath(WORK_DIR + "bar.tst"));
        Files.createFile(fileSystem.getPath(WORK_DIR + "baz.txt"));
    }

    @Test
    void getFormat() {
        Collection<String> formats = Importer.getFormats(loader);
        assertNotNull(formats);
        assertEquals(1, formats.size());
        assertTrue(formats.contains(TEST_FORMAT));
    }

    @Test
    void list() {
        Collection<Importer> importers = Importer.list(loader, computationManager, importConfigMock);
        assertNotNull(importers);
        assertEquals(1, importers.size());
        assertTrue(importers.contains(testImporter));
    }

    @Test
    void getImporter() {
        Importer importer = Importer.find(loader, TEST_FORMAT, computationManager, importConfigMock);
        assertNotNull(importer);
        assertSame(testImporter, importer);
    }

    @Test
    void getImporterWithImportConfig() {
        Importer importer = Importer.find(loader, TEST_FORMAT, computationManager, importConfigWithPostProcessor);
        assertNotNull(importer);
        Network network = importer.importData(null, networkFactory, null);
        assertNotNull(network);
        assertEquals(LoadType.FICTITIOUS, network.getLoad("LOAD").getLoadType());
    }

    @Test
    void getImporterWithImportConfigAndReportNode() throws IOException {
        Importer importer = Importer.find(loader, TEST_FORMAT, computationManager, importConfigWithPostProcessor);
        ReportNode reportNode = ReportNode.newRootReportNode()
                .withResourceBundles(PowsyblCoreTestReportResourceBundle.TEST_BASE_NAME, PowsyblCoreReportResourceBundle.BASE_NAME)
                .withMessageTemplate("testFunctionalLog")
                .build();
        assertNotNull(importer);
        Network network = importer.importData(null, networkFactory, null, reportNode);
        assertNotNull(network);
        assertEquals(LoadType.FICTITIOUS, network.getLoad("LOAD").getLoadType());

        // Check that the wrapped importer has received the functional logs reportNode and produced report items
        assertEquals(1, reportNode.getChildren().size());
        StringWriter sw = new StringWriter();
        reportNode.print(sw);
        String actual = TestUtil.normalizeLineSeparator(sw.toString());
        String expected = TestUtil.normalizeLineSeparator(
                "+ testFunctionalLogs\n" +
                "   Import model eurostagTutorialExample1\n");
        assertEquals(expected, actual);
    }

    @Test
    void getNullImporter() {
        Importer importer = Importer.find(loader, UNSUPPORTED_FORMAT, computationManager, importConfigMock);
        assertNull(importer);
    }

    @Test
    void getPostProcessorNames() {
        Collection<String> names = Importer.getPostProcessorNames(loader);
        assertNotNull(names);
        assertEquals(1, names.size());
        assertTrue(names.contains("test"));
    }

    @Test
    void addAndRemovePostProcessor() {
        Importer importer1 = Importer.addPostProcessors(loader, testImporter, computationManager, "test");
        Network network1 = importer1.importData(null, networkFactory, null);
        assertNotNull(network1);
        assertEquals(LoadType.FICTITIOUS, network1.getLoad("LOAD").getLoadType());

        Importer importer2 = Importer.removePostProcessors(importer1);
        Network network2 = importer2.importData(null, networkFactory, null);
        assertNotNull(network2);
        assertEquals(LoadType.UNDEFINED, network2.getLoad("LOAD").getLoadType());
    }

    @Test
    void setPostProcessor() {
        Importer importer = Importer.setPostProcessors(loader, testImporter, computationManager, "test");
        Network network = importer.importData(null, networkFactory, null);
        assertNotNull(network);
        assertEquals(LoadType.FICTITIOUS, network.getLoad("LOAD").getLoadType());
    }

    @Test
    void importBadData() {
        PowsyblException e = assertThrows(PowsyblException.class, () -> Importers.importData(loader, UNSUPPORTED_FORMAT, null, null, computationManager, importConfigMock));
        assertEquals("Import format " + UNSUPPORTED_FORMAT + " not supported", e.getMessage());
    }

    @Test
    void importAll() throws InterruptedException, ExecutionException, IOException {
        List<Boolean> isLoadPresent = new ArrayList<>();
        Importers.importAll(fileSystem.getPath(WORK_DIR), testImporter, false, null, n -> isLoadPresent.add(n.getLoad("LOAD") != null), null, networkFactory, ReportNode.NO_OP);
        assertEquals(2, isLoadPresent.size());
        isLoadPresent.forEach(Assertions::assertTrue);
    }

    @Test
    void importAllParallel() throws InterruptedException, ExecutionException, IOException {
        List<Boolean> isLoadPresent = Collections.synchronizedList(new ArrayList<>());
        Importers.importAll(fileSystem.getPath(WORK_DIR), testImporter, true, null, n -> isLoadPresent.add(n.getLoad("LOAD") != null), null, networkFactory, ReportNode.NO_OP);
        assertEquals(2, isLoadPresent.size());
        isLoadPresent.forEach(Assertions::assertTrue);
    }

    @Test
    void createDataSource1() throws IOException {
        DataSource dataSource = new DirectoryDataSource(fileSystem.getPath(WORK_DIR), "foo");
        assertTrue(dataSource.exists(FOO_TST));
    }

    @Test
    void createDataSource2() throws IOException {
        DataSource dataSource = DataSource.fromPath(path);
        assertTrue(dataSource.exists(FOO_TST));
    }

    @Test
    void findImporter() {
        Importer importer = Importer.find(DataSource.fromPath(path), loader, computationManager, importConfigMock);
        assertNotNull(importer);
        assertEquals(testImporter, importer);
    }

    @Test
    void findNullImporter() {
        Importer importer = Importer.find(DataSource.fromPath(badPath), loader, computationManager, importConfigMock);
        assertNull(importer);
    }

    @Test
    void loadNetwork1() {
        Network network = Network.read(path, computationManager, importConfigMock, null, networkFactory, loader, ReportNode.NO_OP);
        assertNotNull(network);
        assertNotNull(network.getLoad("LOAD"));
    }

    @Test
    void loadNullNetwork1() {
        PowsyblException e = assertThrows(PowsyblException.class, () -> Network.read(badPath, computationManager, importConfigMock, null, networkFactory, loader, ReportNode.NO_OP));
        assertEquals("Unsupported file format or invalid file.", e.getMessage());
    }

    @Test
    void loadNetwork2() throws IOException {
        try (var is = DataSource.fromPath(path).newInputStream(null, EXTENSION)) {
            Network network = Network.read(FOO_TST, is, computationManager, importConfigMock, null, networkFactory, loader, ReportNode.NO_OP);
            assertNotNull(network);
            assertNotNull(network.getLoad("LOAD"));
        }
    }

    @Test
    void loadNullNetwork2() throws IOException {
        InputStream is = DataSource.fromPath(badPath).newInputStream(null, "txt");
        PowsyblException e = assertThrows(PowsyblException.class, () -> Network.read("baz.txt", is, computationManager, importConfigMock, null, networkFactory, loader, ReportNode.NO_OP));
        assertEquals("Unsupported file format or invalid file.", e.getMessage());
    }

    @Test
    void loadNetworks() throws InterruptedException, ExecutionException, IOException {
        List<Boolean> isLoadPresent = new ArrayList<>();
        Network.readAll(fileSystem.getPath(WORK_DIR), false, loader, computationManager, importConfigMock, null, n -> isLoadPresent.add(n.getLoad("LOAD") != null), null, networkFactory, ReportNode.NO_OP);
        assertEquals(2, isLoadPresent.size());
        isLoadPresent.forEach(Assertions::assertTrue);
    }
}

