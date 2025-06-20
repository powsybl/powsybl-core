/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion.test.export;

import com.powsybl.cgmes.conversion.CgmesExport;
import com.powsybl.commons.datasource.*;
import com.powsybl.commons.test.AbstractSerDeTest;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.NetworkTest1Factory;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Luma {@literal <zamarrenolm at aia.es>}
 */
class ExportToCimVersionTest extends AbstractSerDeTest {

    @Test
    void testExportDataSourceEmptyBaseName() throws IOException {
        MemDataSource ds = new MemDataSource();
        Network n = NetworkTest1Factory.create();
        // The export is not given baseName as parameter, and the data source has an empty basename
        new CgmesExport().export(n, null, ds);
        String nameEQ = ds.listNames(".*EQ.*xml").iterator().next();
        String baseName = nameEQ.replace("_EQ.xml", "");
        // The baseName of the output files should be taken from the network name or id
        assertEquals(n.getNameOrId(), baseName);
    }

    @Test
    void testExportMasterResourceIdentifierOnlyForCim100OrGreater() throws IOException {
        Network n = NetworkTest1Factory.create();

        Properties params = new Properties();
        params.put(CgmesExport.PROFILES, "EQ");
        String basename = "network";
        String eqFilename = basename + "_EQ.xml";
        String version;
        Path outputFolder;
        String eqContent;
        String mRIDTag = "<cim:IdentifiedObject.mRID>";

        // The exported EQ for CGMES 2.4 (CIM 16) should not contain the mRID tag
        version = "16";
        outputFolder = tmpDir.resolve(version);
        params.put(CgmesExport.CIM_VERSION, version);
        Files.createDirectories(outputFolder);
        n.write("CGMES", params, outputFolder.resolve(basename));
        eqContent = Files.readString(outputFolder.resolve(eqFilename));
        assertFalse(eqContent.contains(mRIDTag));

        // The exported EQ for CGMES 3 (CIM 100) must contain the mRID tag
        version = "100";
        params.put(CgmesExport.CIM_VERSION, version);
        Files.createDirectories(outputFolder);
        n.write("CGMES", params, outputFolder.resolve(basename));
        eqContent = Files.readString(outputFolder.resolve(eqFilename));
        assertTrue(eqContent.contains("<cim:IdentifiedObject.mRID>"));
    }

}
