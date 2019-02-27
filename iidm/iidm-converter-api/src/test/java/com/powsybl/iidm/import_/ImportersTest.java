/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.import_;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.datasource.DataSource;
import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.AbstractConvertersTest;
import com.powsybl.iidm.network.LoadType;
import com.powsybl.iidm.network.Network;
import org.junit.*;
import org.mockito.Mockito;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ImportersTest extends AbstractConvertersTest {

    private final Importer testImporter = new TestImporter();
    private final ImportPostProcessor testImportPostProcessor = new TestImportPostProcessor();
    private final ImportersLoader loader = new ImportersLoaderList(Collections.singletonList(testImporter),
            Collections.singletonList(testImportPostProcessor));

    private final ComputationManager computationManager = Mockito.mock(ComputationManager.class);
    private final ImportConfig importConfig = Mockito.mock(ImportConfig.class);

    @Before
    public void setUp() throws IOException {
        super.setUp();
        Files.createFile(fileSystem.getPath("/work/foo.tst"));
        Files.createFile(fileSystem.getPath("/work/bar.tst"));
        Files.createFile(fileSystem.getPath("/work/baz.txt"));
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
        Collection<Importer> importers = Importers.list(loader, computationManager, importConfig);
        assertNotNull(importers);
        assertEquals(1, importers.size());
        assertTrue(importers.contains(testImporter));
    }

    @Test
    public void getImporter() {
        Importer importer = Importers.getImporter(loader, TEST_FORMAT, computationManager, importConfig);
        assertNotNull(importer);
        assertEquals(testImporter, importer);
    }

    @Test
    public void getNullImporter() {
        Importer importer = Importers.getImporter(loader, UNSUPPORTED_FORMAT, computationManager, importConfig);
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
        Network network1 = importer1.importData(null, null);
        assertNotNull(network1);
        assertEquals(LoadType.FICTITIOUS, network1.getLoad("LOAD").getLoadType());

        Importer importer2 = Importers.removePostProcessors(importer1);
        Network network2 = importer2.importData(null, null);
        assertNotNull(network2);
        assertEquals(LoadType.UNDEFINED, network2.getLoad("LOAD").getLoadType());
    }

    @Test
    public void setPostProcessor() {
        Importer importer = Importers.setPostProcessors(loader, testImporter, computationManager, "test");
        Network network = importer.importData(null, null);
        assertNotNull(network);
        assertEquals(LoadType.FICTITIOUS, network.getLoad("LOAD").getLoadType());
    }

    @Test
    public void importData() {
        Network network = Importers.importData(loader, TEST_FORMAT, null, null, computationManager, importConfig);
        assertNotNull(network);
        assertNotNull(network.getLoad("LOAD"));
    }

    @Test
    public void importBadData() {
        expected.expect(PowsyblException.class);
        expected.expectMessage("Import format " + UNSUPPORTED_FORMAT + " not supported");
        Importers.importData(loader, UNSUPPORTED_FORMAT, null, null, computationManager, importConfig);
    }

    @Test
    public void importAll() throws InterruptedException, ExecutionException, IOException {
        List<Boolean> isLoadPresent = new ArrayList<>();
        Importers.importAll(fileSystem.getPath("/work/"), testImporter, false, n -> isLoadPresent.add(n.getLoad("LOAD") != null), null);
        assertEquals(2, isLoadPresent.size());
        isLoadPresent.forEach(Assert::assertTrue);
    }

    @Test
    public void createDataSource1() throws IOException {
        DataSource dataSource = Importers.createDataSource(fileSystem.getPath("/work/"), "foo");
        assertTrue(dataSource.exists("foo.tst"));
    }

    @Test
    public void createDataSource2() throws IOException {
        DataSource dataSource = Importers.createDataSource(path);
        assertTrue(dataSource.exists("foo.tst"));
    }

    @Test
    public void findImporter() {
        Importer importer = Importers.findImporter(Importers.createDataSource(path), loader, computationManager, importConfig);
        assertNotNull(importer);
        assertEquals(testImporter, importer);
    }

    @Test
    public void findNullImporter() {
        Importer importer = Importers.findImporter(Importers.createDataSource(badPath), loader, computationManager, importConfig);
        assertNull(importer);
    }

    @Test
    public void loadNetwork1() {
        Network network = Importers.loadNetwork(path, computationManager, importConfig, null, loader);
        assertNotNull(network);
        assertNotNull(network.getLoad("LOAD"));
    }

    @Test
    public void loadNullNetwork1() {
        Network network = Importers.loadNetwork(badPath, computationManager, importConfig, null, loader);
        assertNull(network);
    }

    @Test
    public void loadNetwork2() throws IOException {
        Network network = Importers.loadNetwork("foo.tst", Importers.createDataSource(path).newInputStream(null, EXTENSION), computationManager, importConfig, null, loader);
        assertNotNull(network);
        assertNotNull(network.getLoad("LOAD"));
    }

    @Test
    public void loadNullNetwork2() throws IOException {
        Network network = Importers.loadNetwork("baz.txt", Importers.createDataSource(badPath).newInputStream(null, "txt"), computationManager, importConfig, null, loader);
        assertNull(network);
    }

    @Test
    public void loadNetworks() throws InterruptedException, ExecutionException, IOException {
        List<Boolean> isLoadPresent = new ArrayList<>();
        Importers.loadNetworks(fileSystem.getPath("/work/"), false, loader, computationManager, importConfig, n -> isLoadPresent.add(n.getLoad("LOAD") != null), null);
        assertEquals(2, isLoadPresent.size());
        isLoadPresent.forEach(Assert::assertTrue);
    }
}
