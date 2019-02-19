/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.import_;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.commons.datasource.DataSource;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.network.LoadType;
import com.powsybl.iidm.network.Network;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.util.Collection;
import java.util.Collections;

import static org.junit.Assert.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ImportersTest {

    private FileSystem fileSystem;
    private final ImportersLoader loader =
            new ImportersLoaderList(Collections.singletonList(new TestImporter()), Collections.singletonList(new TestImportPostProcessor()));
    private final ComputationManager computationManager = Mockito.mock(ComputationManager.class);

    @Before
    public void setUp() throws IOException {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
        Files.createFile(fileSystem.getPath("/work/foo.tst"));
        Files.createFile(fileSystem.getPath("/work/bar.tst"));
        Files.createFile(fileSystem.getPath("/work/baz.tst"));
    }

    @After
    public void tearDown() throws IOException {
        fileSystem.close();
    }

    private static void checkNetwork(Importer importer, DataSource dataSource) {
        assertNotNull(importer);
        Network network = importer.importData(dataSource, null);
        assertEquals(LoadType.UNDEFINED, network.getLoad("LOAD").getLoadType());
    }

    private static void checkProcessedNetwork(Importer importer, DataSource dataSource) {
        assertNotNull(importer);
        Network network = importer.importData(dataSource, null);
        assertEquals(LoadType.FICTITIOUS, network.getLoad("LOAD").getLoadType());
    }

    @Test
    public void getFormat() {
        Collection<String> formats = Importers.getFormats(loader);
        assertEquals(1, formats.size());
        assertTrue(formats.contains("TST"));
    }

    @Test
    public void getImporter() {
        Importer importer = Importers.getImporter(loader, "TST", computationManager, new ImportConfig());
        assertNotNull(importer);
    }

    @Test
    public void getNullImporter() {
        Importer importer = Importers.getImporter(loader, "UNSUPPORTED", computationManager, new ImportConfig());
        assertNull(importer);
    }

    @Test
    public void getPostProcessorNames() {
        Collection<String> postProcessorNames = Importers.getPostProcessorNames(loader);
        assertEquals(1, postProcessorNames.size());
        assertTrue(postProcessorNames.contains("testPostProcessor"));
    }

    @Test
    public void findImporter() {
        DataSource dataSource = Importers.createDataSource(fileSystem.getPath("/work/bar.tst"));
        Importer importer = Importers.findImporter(dataSource, loader, computationManager, new ImportConfig("testPostProcessor"));
        assertNotNull(importer);
        checkProcessedNetwork(importer, dataSource);
    }

    @Test
    public void addPostProcessor() {
        DataSource dataSource = Importers.createDataSource(fileSystem.getPath("/work/bar.tst"));
        Importer importer = Importers.findImporter(dataSource, loader, computationManager, new ImportConfig());
        checkNetwork(importer, dataSource);
        Importer processedImporter = Importers.addPostProcessors(loader, importer, computationManager, "testPostProcessor");
        checkProcessedNetwork(processedImporter, dataSource);
    }

    @Test
    public void removePostProcessor() {
        DataSource dataSource = Importers.createDataSource(fileSystem.getPath("/work/baz.tst"));
        Importer processedImporter = Importers.addPostProcessors(loader, Importers.findImporter(dataSource, loader, computationManager, new ImportConfig()), computationManager, "testPostProcessor");
        checkProcessedNetwork(processedImporter, dataSource);
        Importer importer = Importers.removePostProcessors(processedImporter);
        checkNetwork(importer, dataSource);
    }

    @Test
    public void setPostProcessors() {
        DataSource dataSource = Importers.createDataSource(fileSystem.getPath("/work/bar.tst"));
        Importer importer = Importers.findImporter(dataSource, loader, computationManager, new ImportConfig());
        checkNetwork(importer, dataSource);
        Importer processedImporter = Importers.setPostProcessors(loader, importer, computationManager, "testPostProcessor");
        checkProcessedNetwork(processedImporter, dataSource);
    }

    @Test
    public void createReadOnlyWithRelativePath() throws IOException {
        ReadOnlyDataSource dataSource = Importers.createDataSource(fileSystem.getPath("/work/foo.tst"));
        assertTrue(dataSource.exists("/work/foo.tst"));
    }
}
