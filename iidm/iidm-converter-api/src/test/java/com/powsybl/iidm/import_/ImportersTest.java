/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.import_;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.computation.ComputationManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.util.Collections;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ImportersTest {

    private FileSystem fileSystem;
    private final ImportersLoader loader = new ImportersLoaderList(Collections.singletonList(new TestImporter()));

    @Before
    public void setUp() throws IOException {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
        Files.createFile(fileSystem.getPath("/work/foo.txt"));
    }

    @After
    public void tearDown() throws IOException {
        fileSystem.close();
    }

    @Test
    public void getImporter() {
        Importer importer = Importers.getImporter(loader, "TST", Mockito.mock(ComputationManager.class), new ImportConfig());
        assertNotNull(importer);
    }

    @Test
    public void getNullImporter() {
        Importer importer = Importers.getImporter(loader, "UNSUPPORTED", Mockito.mock(ComputationManager.class), new ImportConfig());
        assertNull(importer);
    }

    @Test
    public void createReadOnlyWithRelativePath() throws IOException {
        ReadOnlyDataSource dataSource = Importers.createDataSource(fileSystem.getPath("foo.txt"));
        assertTrue(dataSource.exists("foo.txt"));
    }
}
