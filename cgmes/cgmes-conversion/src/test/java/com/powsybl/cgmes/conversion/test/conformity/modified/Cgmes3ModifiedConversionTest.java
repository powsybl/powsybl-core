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
import com.powsybl.iidm.network.Importers;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Substation;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.FileSystem;

import static org.junit.Assert.*;

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

    @Test
    public void microGridGeographicalRegionInBoundary() {
        Network network = Importers.importData("CGMES", Cgmes3ModifiedCatalog.microGridBaseCaseGeographicalRegionInBoundary().dataSource(), null);
        Substation anvers = network.getSubstation("87f7002b-056f-4a6a-a872-1744eea757e3");
        Substation brussels = network.getSubstation("37e14a0f-5e34-4647-a062-8bfd9305fa9d");
        assertNotNull(anvers);
        assertNotNull(brussels);
        assertTrue(anvers.getGeographicalTags().contains("ELIA-Anvers"));
        assertTrue(brussels.getGeographicalTags().contains("ELIA-Brussels"));
        assertNotNull(anvers.getNullableCountry());
        assertNotNull(brussels.getNullableCountry());
        assertEquals("BE", anvers.getNullableCountry().toString());
        assertEquals("BE", brussels.getNullableCountry().toString());
    }

    private FileSystem fileSystem;
}
