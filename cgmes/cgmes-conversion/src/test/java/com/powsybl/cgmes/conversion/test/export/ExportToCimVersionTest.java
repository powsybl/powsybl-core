/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion.test.export;

import com.powsybl.cgmes.conversion.CgmesExport;
import com.powsybl.cgmes.conversion.CgmesImport;
import com.powsybl.cgmes.conversion.CgmesModelExtension;
import com.powsybl.cgmes.extensions.CimCharacteristics;
import com.powsybl.cgmes.model.CgmesModel;
import com.powsybl.cgmes.model.test.cim14.Cim14SmallCasesCatalog;
import com.powsybl.commons.AbstractConverterTest;
import com.powsybl.commons.datasource.MemDataSource;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.commons.datasource.ZipFileDataSource;
import com.powsybl.iidm.network.Importers;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.NetworkFactory;
import com.powsybl.iidm.network.test.NetworkTest1Factory;
import org.junit.Test;

import java.io.IOException;
import java.util.Properties;

import static org.junit.Assert.*;

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
        testExportToCim(ieee14Cim14(), "IEEE14", 16);
    }

    @Test
    public void testExportIEEE14Cim14ToCim100() {
        // Testing export to CGMES 3
        // TODO(Luma) verify that all classes and attributes are valid against profiles (using CIMdesk)
        // TODO(Luma) Check mRID is exported
        // TODO(Luma) Check GeneratingUnit.initialP (removed in CIM100)
        // TODO(Luma) Check the way OperationalLimit TypeName is read
        // TODO(Luma) Check OperationalLimit value (renamed to normalValue in CIM100)
        Network network = ieee14Cim14();
        assertEquals(14, network.getExtension(CimCharacteristics.class).getCimVersion());
        testExportToCim(network, "IEEE14", 100);
    }

    @Test
    public void testExportIEEE14ToCim100CheckIsNodeBreaker() {
        // Testing export to CGMES 3 is interpreted as a node/breaker CGMES model
        // Input was a bus/branch model

        Network network = ieee14Cim14();
        CgmesModel cgmesModel14 = network.getExtension(CgmesModelExtension.class).getCgmesModel();
        assertFalse(cgmesModel14.isNodeBreaker());

        String cimZipFilename = "ieee14_CIM100";
        Properties params = new Properties();
        params.put(CgmesExport.CIM_VERSION, "100");
        ZipFileDataSource zip = new ZipFileDataSource(tmpDir.resolve("."), cimZipFilename);
        new CgmesExport().export(network, params, zip);
        Network network100 = Importers.loadNetwork(tmpDir.resolve(cimZipFilename + ".zip"));
        CgmesModel cgmesModel100 = network100.getExtension(CgmesModelExtension.class).getCgmesModel();
        assertTrue(cgmesModel100.isNodeBreaker());
    }

    private static Network ieee14Cim14() {
        ReadOnlyDataSource dataSource = Cim14SmallCasesCatalog.ieee14().dataSource();
        return new CgmesImport().importData(dataSource, NetworkFactory.findDefault(), null);
    }

    private void testExportToCim(Network network, String name, int cimVersion) {
        String cimZipFilename = name + "_CIM" + cimVersion;
        Properties params = new Properties();
        params.put(CgmesExport.CIM_VERSION, Integer.toString(cimVersion));
        ZipFileDataSource zip = new ZipFileDataSource(tmpDir.resolve("."), cimZipFilename);
        new CgmesExport().export(network, params, zip);

        // Reimport and verify contents of Network
        Network networkCimVersion = Importers.loadNetwork(tmpDir.resolve(cimZipFilename + ".zip"));
        CimCharacteristics cim = networkCimVersion.getExtension(CimCharacteristics.class);

        assertEquals(cimVersion, cim.getCimVersion());
        // Initial verification: check that we have the same number of elements in both networks
        // TODO(Luma) compare the networks
        // If the original was bus-branch (like IEEE14) and the exported is node-breaker at least compare attributes
        // or select another network for verification (SmallGrid ?)
        assertEquals(network.getSubstationCount(), networkCimVersion.getSubstationCount());
        assertEquals(network.getVoltageLevelCount(), networkCimVersion.getVoltageLevelCount());
        assertEquals(network.getLineCount(), networkCimVersion.getLineCount());
        assertEquals(network.getTwoWindingsTransformerCount(), networkCimVersion.getTwoWindingsTransformerCount());
        assertEquals(network.getThreeWindingsTransformerCount(), networkCimVersion.getThreeWindingsTransformerCount());
        assertEquals(network.getGeneratorCount(), networkCimVersion.getGeneratorCount());
        assertEquals(network.getLoadCount(), networkCimVersion.getLoadCount());
        assertEquals(network.getShuntCompensatorCount(), networkCimVersion.getShuntCompensatorCount());
    }
}
