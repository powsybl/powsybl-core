/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.cgmes.alternatives.test;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.cgmes.conformity.CgmesConformity1Catalog;
import com.powsybl.cgmes.model.CgmesModel;
import com.powsybl.cgmes.model.CgmesModelFactory;
import com.powsybl.cgmes.model.CgmesSubset;
import com.powsybl.cgmes.model.GridModelReferenceResources;
import com.powsybl.commons.datasource.DataSource;
import com.powsybl.commons.datasource.DataSourceUtil;
import com.powsybl.triplestore.api.TripleStoreFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Mathieu Bague {@literal <mathieu.bague@rte-france.com>}
 */
class TripleStoreWriteTest {

    private FileSystem fileSystem;

    @BeforeEach
    void setup() {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
    }

    @AfterEach
    void tearDown() throws IOException {
        fileSystem.close();
    }

    @Test
    void testWriteSubset() throws IOException {
        GridModelReferenceResources resources = CgmesConformity1Catalog.microGridBaseCaseBE();

        for (String impl : TripleStoreFactory.allImplementations()) {
            CgmesModel model = CgmesModelFactory.create(resources.dataSource(), impl);
            assertEquals(9, model.tripleStore().contextNames().size());

            for (CgmesSubset subset : CgmesSubset.values()) {
                if (subset == CgmesSubset.UNKNOWN) {
                    continue;
                }
                DataSource ds1 = DataSourceUtil.createDataSource(fileSystem.getPath("/cgmes.zip"));
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
