/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.cgmes.conversion.test.cim14;

import com.powsybl.cgmes.conversion.CgmesImport;
import com.powsybl.cgmes.conversion.CgmesModelExtension;
import com.powsybl.cgmes.conversion.test.ConversionTester;
import com.powsybl.cgmes.conversion.test.network.compare.ComparisonConfig;
import com.powsybl.cgmes.model.CgmesModel;
import com.powsybl.cgmes.model.test.Cim14SmallCasesCatalog;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.commons.datasource.ResourceDataSource;
import com.powsybl.commons.datasource.ResourceSet;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Importers;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.impl.NetworkFactoryImpl;
import com.powsybl.triplestore.api.TripleStoreFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import static com.powsybl.cgmes.conversion.test.ConversionUtil.readCgmesResources;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
class Cim14SmallCasesConversionTest {
    @BeforeAll
    static void setUp() {
        Properties importParams = new Properties();
        importParams.put(CgmesImport.IMPORT_CGM_WITH_SUBNETWORKS, "false");
        tester = new ConversionTester(
                importParams,
                TripleStoreFactory.onlyDefaultImplementation(),
                new ComparisonConfig()
                        .checkNetworkId(false)
                        // Expected cases are read using CIM1Importer, that uses floats to read numbers
                        // IIDM and CGMES now stores numbers as doubles
                        .tolerance(2.4e-4));
    }

    @Test
    void txMicroBEAdapted() throws IOException {
        tester.testConversion(Cim14SmallCasesNetworkCatalog.txMicroBEAdapted(), Cim14SmallCasesCatalog.txMicroBEAdapted());
    }

    @Test
    void smallcase1() throws IOException {
        tester.testConversion(Cim14SmallCasesNetworkCatalog.smallcase1(), Cim14SmallCasesCatalog.small1());
    }

    @Test
    void ieee14() throws IOException {
        tester.testConversion(Cim14SmallCasesNetworkCatalog.ieee14(), Cim14SmallCasesCatalog.ieee14());
    }

    @Test
    void nordic32() throws IOException {
        tester.testConversion(Cim14SmallCasesNetworkCatalog.nordic32(), Cim14SmallCasesCatalog.nordic32());
    }

    @Test
    void m7buses() throws IOException {
        tester.testConversion(Cim14SmallCasesNetworkCatalog.m7buses(), Cim14SmallCasesCatalog.m7buses());
    }

    @Test
    void m7busesNoSequenceNumbers() {
        Properties importParams = new Properties();
        importParams.put(CgmesImport.IMPORT_CGM_WITH_SUBNETWORKS, "false");
        Network networkSeq = Importers.importData("CGMES", Cim14SmallCasesCatalog.m7buses().dataSource(), importParams);
        Network networkNoSeq = Importers.importData("CGMES", Cim14SmallCasesCatalog.m7busesNoSequenceNumbers().dataSource(), importParams);
        // Make sure we have not lost any line or switch
        assertEquals(networkSeq.getLineCount(), networkNoSeq.getLineCount());
        assertEquals(networkSeq.getSwitchCount(), networkNoSeq.getSwitchCount());

        // Check terminal ids have been sorted properly
        // In m7buses sequenceNumber ordering is just the opposite of Identifier ordering
        // Terminal with sequenceNumber 2 ends in _EX
        // Terminal with sequenceNumber 1 ends in _OR
        CgmesModel cgmesSeq = networkSeq.getExtension(CgmesModelExtension.class).getCgmesModel();
        Map<String, String> aclsSeqTerminal1 = cgmesSeq.acLineSegments().stream().collect(Collectors.toMap(acls -> acls.getId("ACLineSegment"), acls -> acls.getId("Terminal1")));
        CgmesModel cgmesNoSeq = networkNoSeq.getExtension(CgmesModelExtension.class).getCgmesModel();
        cgmesNoSeq.acLineSegments().forEach(aclsNoSeq -> {
            String aclsId = aclsNoSeq.getId("ACLineSegment");
            String aclsSeqTerminal1Id = aclsSeqTerminal1.get(aclsId);
            String aclsNoSeqTerminal2Id = aclsNoSeq.getId("Terminal2");
            assertEquals(aclsSeqTerminal1Id, aclsNoSeqTerminal2Id);
        });
    }

    @Test
    void condenser() {
        ReadOnlyDataSource ds = new ResourceDataSource("condenser",
                new ResourceSet("/cim14/condenser", "condenser_EQ.xml", "condenser_TP.xml"));
        Network network = new CgmesImport().importData(ds, new NetworkFactoryImpl(), new Properties());
        assertEquals(1, network.getGeneratorCount());
        Generator c = network.getGenerator("CONDENSER_1");
        assertEquals(0, c.getMinP());
        assertEquals(0, c.getMaxP());
        assertTrue(c.isCondenser());
    }

    @Test
    void voltageLevelWithoutName() {
        // CGMES network:
        //   Voltage level of ID "VoltageLevel1" has no name
        // IIDM network:
        //   The voltage level imported without name, and can be retrieved via its id

        ReadOnlyDataSource ds = new ResourceDataSource("vl_without_name",
                new ResourceSet("/cim14/", "vl_without_name.xml"));
        Network network = new CgmesImport().importData(ds, new NetworkFactoryImpl(), new Properties());

        assertNotNull(network);
        assertEquals(2, network.getVoltageLevelCount());
        assertNotNull(network.getVoltageLevel("VoltageLevel1"));
    }

    private static ConversionTester tester;
}
