/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.alternatives.test;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.cgmes.conformity.test.CgmesConformity1Catalog;
import com.powsybl.cgmes.model.CgmesModel;
import com.powsybl.cgmes.model.CgmesModelFactory;
import com.powsybl.cgmes.model.CgmesSubset;
import com.powsybl.cgmes.model.test.TestGridModelResources;
import com.powsybl.commons.datasource.CompressionFormat;
import com.powsybl.commons.datasource.DataSource;
import com.powsybl.commons.datasource.DataSourceUtil;
import com.powsybl.triplestore.api.TripleStoreFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Mathieu Bague <mathieu.bague@rte-france.com>
 */
public class TripleStoreWriteTest {

    private FileSystem fileSystem;

    @Before
    public void setup() {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
    }

    @After
    public void tearDown() throws IOException {
        fileSystem.close();
    }

    @Test
    public void testWriteSubset() throws IOException {
        TestGridModelResources resources = CgmesConformity1Catalog.microGridBaseCaseBE();

        for (String impl : TripleStoreFactory.allImplementations()) {
            CgmesModel model = CgmesModelFactory.create(resources.dataSource(), impl);
            assertEquals(9, model.tripleStore().contextNames().size());

            for (CgmesSubset subset : CgmesSubset.values()) {
                DataSource ds1 = DataSourceUtil.createDataSource(fileSystem.getPath("/"), "cgmes", CompressionFormat.ZIP, null);
                model.write(ds1, subset);

                // Assert that there is only one file in the archive
                Set<String> filenames = ds1.listNames(".*");
                assertEquals(1, ds1.listNames(".*").size());

                // Assert that the filename is correct
                assertTrue(resources.dataSource().exists(filenames.iterator().next()));

                // Remove temporary zip file
                Files.delete(fileSystem.getPath("/cgmes.zip"));
            }

        }
    }

}
