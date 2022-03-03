/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion.test.export;

import com.powsybl.cgmes.conversion.CgmesExport;
import com.powsybl.cgmes.conversion.CgmesImport;
import com.powsybl.cgmes.extensions.CimCharacteristics;
import com.powsybl.cgmes.model.test.cim14.Cim14SmallCasesCatalog;
import com.powsybl.commons.AbstractConverterTest;
import com.powsybl.commons.datasource.MemDataSource;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.commons.datasource.ZipFileDataSource;
import com.powsybl.iidm.import_.Importers;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.NetworkFactory;
import com.powsybl.iidm.network.test.NetworkTest1Factory;
import org.junit.Test;

import java.io.IOException;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import static org.junit.Assert.assertEquals;

/**
 * @author Luma <zamarrenolm at aia.es>
 */
public class ExportToCimVersionTest extends AbstractConverterTest {

    @Test
    public void testExportDataSourceEmptyBaseName() throws IOException {
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
    public void testExportIEEE14Cim14ToCim16() {
        testExportIEEE14Cim14ToCim(16, tempDir());
    }

    @Test
    public void testExportIEEE14Cim14ToCim100() {
        // Testing export to CGMES 3
        // FIXME(Luma) verify that namespace in headers are correct
        // FIXME(Luma) verify that all classes and attributes are valid against profiles (using CIMdesk)
        testExportIEEE14Cim14ToCim(100, tempDir());
    }

    private void testExportIEEE14Cim14ToCim(int cimVersion, Path tempDir) {
        ReadOnlyDataSource dataSource = Cim14SmallCasesCatalog.ieee14().dataSource();
        Network networkCim14 = new CgmesImport().importData(dataSource, NetworkFactory.findDefault(), null);
        CimCharacteristics cim14 = networkCim14.getExtension(CimCharacteristics.class);

        Properties params = new Properties();
        params.put(CgmesExport.CIM_VERSION, Integer.toString(cimVersion));
        ZipFileDataSource zip = new ZipFileDataSource(tempDir.resolve("."), "IEEE14_CIM" + cimVersion);
        new CgmesExport().export(networkCim14, params, zip);

        Network networkCimVersion = Importers.loadNetwork(tempDir.resolve("IEEE14_CIM" + cimVersion + ".zip"));
        CimCharacteristics cim = networkCimVersion.getExtension(CimCharacteristics.class);

        assertEquals(14, cim14.getCimVersion());
        assertEquals(cimVersion, cim.getCimVersion());
    }

    private Path tempDir() {
        boolean localEnvironment = false;
        if (localEnvironment) {
            return Paths.get("/Users/zamarrenolm/work/temp/cgmes3");
        } else {
            return tmpDir;
        }
    }
}
