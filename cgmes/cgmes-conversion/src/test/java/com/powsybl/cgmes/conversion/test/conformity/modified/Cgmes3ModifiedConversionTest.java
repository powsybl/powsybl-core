/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion.test.conformity.modified;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.cgmes.conformity.Cgmes3Catalog;
import com.powsybl.cgmes.conformity.Cgmes3ModifiedCatalog;
import com.powsybl.cgmes.conversion.CgmesImport;
import com.powsybl.iidm.network.Importers;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Substation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
class Cgmes3ModifiedConversionTest {

    @BeforeEach
    void setUp() {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
        importParams = new Properties();
        importParams.put(CgmesImport.IMPORT_CGM_WITH_SUBNETWORKS, "false");
    }

    @AfterEach
    void tearDown() throws IOException {
        fileSystem.close();
    }

    @Test
    void microGridSingleFile() {
        Network network0 = Importers.importData("CGMES", Cgmes3Catalog.microGrid().dataSource(), importParams);
        Network network = Importers.importData("CGMES", Cgmes3ModifiedCatalog.microGridBaseCaseBESingleFile().dataSource(), importParams);
        assertEquals(7, network.getVoltageLevelStream().mapToInt(voltageLevel -> voltageLevel.getBusBreakerView().getBusCount()).sum());
        assertEquals(5, network.getBoundaryLineCount());
    }

    @Test
    void microGridGeographicalRegionInBoundary() {
        Network network = Importers.importData("CGMES", Cgmes3ModifiedCatalog.microGridBaseCaseGeographicalRegionInBoundary().dataSource(), importParams);
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
    private Properties importParams;
}
