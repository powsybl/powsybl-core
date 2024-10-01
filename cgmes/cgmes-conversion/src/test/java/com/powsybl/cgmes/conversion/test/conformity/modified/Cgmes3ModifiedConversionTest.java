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
import com.powsybl.cgmes.conformity.Cgmes3ModifiedCatalog;
import com.powsybl.cgmes.conformity.CgmesConformity3Catalog;
import com.powsybl.cgmes.conformity.CgmesConformity3ModifiedCatalog;
import com.powsybl.cgmes.conversion.CgmesImport;
import com.powsybl.cgmes.conversion.CgmesModelExtension;
import com.powsybl.cgmes.conversion.Conversion;
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
        Network network = Importers.importData("CGMES", Cgmes3ModifiedCatalog.microGridBaseCaseBESingleFile().dataSource(), importParams);
        assertEquals(6, network.getExtension(CgmesModelExtension.class).getCgmesModel().boundaryNodes().size());
        assertEquals(5, network.getDanglingLineCount());
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

    @Test
    void testMultipleUnpairedLinesAtSameBoundary() {
        // Dangling lines at Brussels substation
        final String line5Id = "b18cd1aa-7808-49b9-a7cf-605eaf07b006";
        final String line4Id = "ed0c5d75-4a54-43c8-b782-b20d7431630b";
        final String line3Id = "78736387-5f60-4832-b3fe-d50daf81b0a6";
        final String nodeGY11 = "b67c8340-cb6e-11e1-bcee-406c8f32ef58";
        final String nodeMA11 = "b67cf870-cb6e-11e1-bcee-406c8f32ef58";
        final String nodeAL11 = "b675a570-cb6e-11e1-bcee-406c8f32ef58";

        // In the base case each line is connected to a different boundary node
        // All have different values of p0, q0
        Network networkBase = Network.read(CgmesConformity3Catalog.microGridBaseCaseBE().dataSource(), importParams);
        assertEquals(nodeGY11, Conversion.getDanglingLineBoundaryNode(networkBase.getDanglingLine(line5Id)));
        assertEquals(-27.0286, networkBase.getDanglingLine(line5Id).getP0(), 1e-4);
        assertEquals(120.7887, networkBase.getDanglingLine(line5Id).getQ0(), 1e-4);
        assertEquals(nodeMA11, Conversion.getDanglingLineBoundaryNode(networkBase.getDanglingLine(line4Id)));
        assertEquals(-8.9532, networkBase.getDanglingLine(line4Id).getP0(), 1e-4);
        assertEquals(67.2335, networkBase.getDanglingLine(line4Id).getQ0(), 1e-4);
        assertEquals(nodeAL11, Conversion.getDanglingLineBoundaryNode(networkBase.getDanglingLine(line3Id)));
        assertEquals(-14.0675, networkBase.getDanglingLine(line3Id).getP0(), 1e-4);
        assertEquals(63.9583, networkBase.getDanglingLine(line3Id).getQ0(), 1e-4);

        // We have prepared a modified case where lines 4 and 5 both connect to the same node
        // p0, q0 is adjusted for connected dangling lines
        // p0, q0 of the disconnected is not modified
        Network network3dls = Network.read(CgmesConformity3ModifiedCatalog.microGridBE3DanglingLinesSameBoundary1Disconnected().dataSource(), importParams);
        assertEquals(nodeGY11, Conversion.getDanglingLineBoundaryNode(network3dls.getDanglingLine(line5Id)));
        assertEquals(-13.5143, network3dls.getDanglingLine(line5Id).getP0(), 1e-4);
        assertEquals(60.3944, network3dls.getDanglingLine(line5Id).getQ0(), 1e-4);
        assertEquals(nodeGY11, Conversion.getDanglingLineBoundaryNode(network3dls.getDanglingLine(line4Id)));
        assertEquals(-13.5143, network3dls.getDanglingLine(line4Id).getP0(), 1e-4);
        assertEquals(60.3944, network3dls.getDanglingLine(line4Id).getQ0(), 1e-4);
        assertEquals(nodeGY11, Conversion.getDanglingLineBoundaryNode(network3dls.getDanglingLine(line3Id)));
        assertEquals(-27.0286, network3dls.getDanglingLine(line3Id).getP0(), 1e-4);
        assertEquals(120.7887, network3dls.getDanglingLine(line3Id).getQ0(), 1e-4);
    }

    private FileSystem fileSystem;
    private Properties importParams;
}
