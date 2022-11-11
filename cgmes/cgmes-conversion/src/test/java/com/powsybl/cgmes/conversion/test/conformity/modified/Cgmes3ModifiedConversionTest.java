/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion.test.conformity.modified;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.cgmes.conformity.Cgmes3Catalog;
import com.powsybl.cgmes.conformity.Cgmes3ModifiedCatalog;
import com.powsybl.cgmes.conversion.CgmesModelExtension;
import com.powsybl.iidm.import_.Importers;
import com.powsybl.iidm.network.Network;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.FileSystem;

import static org.junit.Assert.assertEquals;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class Cgmes3ModifiedConversionTest {

    @Before
    public void setUp() {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
    }

    @After
    public void tearDown() throws IOException {
        fileSystem.close();
    }

    @Test
    public void microGridSingleFile() {
        Network network0 = Importers.importData("CGMES", Cgmes3Catalog.microGrid().dataSource(), null);
        System.out.println(network0.getExtension(CgmesModelExtension.class).getCgmesModel().boundaryNodes().tabulateLocals());
        Network network = Importers.importData("CGMES", Cgmes3ModifiedCatalog.microGridBaseCaseBESingleFile().dataSource(), null);
        System.out.println(network.getExtension(CgmesModelExtension.class).getCgmesModel().boundaryNodes().tabulateLocals());
        assertEquals(6, network.getExtension(CgmesModelExtension.class).getCgmesModel().boundaryNodes().size());
        assertEquals(5, network.getDanglingLineCount());
    }

    private FileSystem fileSystem;
}
