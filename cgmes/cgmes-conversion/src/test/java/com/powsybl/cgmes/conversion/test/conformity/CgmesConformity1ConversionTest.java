/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.cgmes.conversion.test.conformity;

import com.google.common.collect.ImmutableSet;
import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.cgmes.conformity.CgmesConformity1Catalog;
import com.powsybl.cgmes.conformity.CgmesConformity1NetworkCatalog;
import com.powsybl.cgmes.conversion.CgmesExport;
import com.powsybl.cgmes.conversion.CgmesImport;
import com.powsybl.cgmes.conversion.Conversion;
import com.powsybl.cgmes.conversion.test.ConversionTester;
import com.powsybl.cgmes.conversion.test.network.compare.ComparisonConfig;
import com.powsybl.cgmes.model.CgmesModel;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.cgmes.model.CgmesSubset;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.ActivePowerControl;
import com.powsybl.iidm.network.extensions.ReferencePriorities;
import com.powsybl.iidm.network.extensions.ReferencePriority;
import com.powsybl.triplestore.api.TripleStoreFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
class CgmesConformity1ConversionTest {

    @BeforeAll
    static void setUpBeforeClass() {
        Properties importParams = new Properties();
        importParams.put(CgmesImport.IMPORT_CGM_WITH_SUBNETWORKS, "false");
        tester = new ConversionTester(
                importParams,
                TripleStoreFactory.onlyDefaultImplementation(),
                new ComparisonConfig());
    }

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
    void microGridBaseCaseBEReport() throws IOException {
        ConversionTester t = new ConversionTester(importParams, TripleStoreFactory.onlyDefaultImplementation(),
            new ComparisonConfig());
        Map<String, TxData> actual = new HashMap<>();
        t.setOnlyReport(true);
        t.setReportConsumer(line -> {
            String[] cols = line.split("\\t");
            String rowType = cols[0];
            if (rowType.equals("TapChanger")) {
                String tx = cols[3];
                TxData d = new TxData(cols[5], cols[23], cols[24], cols[25], cols[26]);
                actual.put(tx, d);
            }
        });
        t.testConversion(null, CgmesConformity1Catalog.microGridBaseCaseBE());

        Map<String, TxData> expected = new HashMap<>();
        expected.put("84ed55f4-61f5-4d9d-8755-bba7b877a246", new TxData(3, 0, 0, 1, 0));
        expected.put("a708c3bc-465d-4fe7-b6ef-6fa6408a62b0", new TxData(2, 0, 1, 0, 0));
        expected.put("b94318f6-6d24-4f56-96b9-df2531ad6543", new TxData(2, 1, 0, 0, 0));
        expected.put("e482b89a-fa84-4ea9-8e70-a83d44790957", new TxData(2, 0, 0, 1, 0));
        actual.keySet().forEach(tx -> assertEquals(expected.get(tx), actual.get(tx)));
    }

    @Test
    void microGridBaseCaseBERoundtripBoundary() throws IOException {
        importParams.put(CgmesImport.CONVERT_BOUNDARY, "true");
        Properties exportParams = new Properties();
        exportParams.put(CgmesExport.PROFILES, "SSH,SV");
        exportParams.put(CgmesExport.MODELING_AUTHORITY_SET, "http://elia.be/CGMES/2.4.15");
        ConversionTester t = new ConversionTester(
            importParams, exportParams,
            TripleStoreFactory.onlyDefaultImplementation(),
            new ComparisonConfig()
                    .tolerance(1e-5)
                    .checkNetworkId(false)
                    .exportedSubset(Set.of(CgmesSubset.STEADY_STATE_HYPOTHESIS, CgmesSubset.STATE_VARIABLES)));
        t.setTestExportImportCgmes(true);
        Network expected = null;
        t.testConversion(expected, CgmesConformity1Catalog.microGridBaseCaseBE());
    }

    @Test
    void microGridBaseCaseBERoundtrip() throws IOException {
        // TODO When we convert boundaries values for P0, Q0 at dangling lines
        // are recalculated and we need to increase the tolerance
        Properties exportParams = new Properties();
        exportParams.put(CgmesExport.PROFILES, List.of("SSH", "SV"));
        exportParams.put(CgmesExport.MODELING_AUTHORITY_SET, "http://elia.be/CGMES/2.4.15");
        ConversionTester t = new ConversionTester(importParams, exportParams,
            TripleStoreFactory.onlyDefaultImplementation(),
            new ComparisonConfig()
                    .tolerance(1e-5)
                    .checkNetworkId(false)
                    .exportedSubset(Set.of(CgmesSubset.STEADY_STATE_HYPOTHESIS, CgmesSubset.STATE_VARIABLES)));
        t.setTestExportImportCgmes(true);
        t.testConversion(CgmesConformity1NetworkCatalog.microBaseCaseBE(), CgmesConformity1Catalog.microGridBaseCaseBE());
    }

    @Test
    void microGridBaseCaseBEBusBalanceValidation() throws IOException {
        // Check bus balance mismatches are low if we use SV voltages
        // MicroGrid BaseCase BE contains an RTC defined at transformerEnd1
        // with step != neutralStep,
        // resulting in a significant ratio (far from 1.0).
        // Validating bus balance of buses after conversion verifies that
        // the interpretation of the location of tap changer
        // relative to the transmission impedance is correct
        importParams.put(CgmesImport.PROFILE_FOR_INITIAL_VALUES_SHUNT_SECTIONS_TAP_POSITIONS, "SV");
        importParams.put(CgmesImport.IMPORT_CGM_WITH_SUBNETWORKS, "false");
        ConversionTester t = new ConversionTester(
            importParams,
            TripleStoreFactory.onlyDefaultImplementation(),
            new ComparisonConfig());
        t.setValidateBusBalancesUsingThreshold(1.2);
        t.testConversion(null, CgmesConformity1Catalog.microGridBaseCaseBE());
    }

    @Test
    void microGridBaseCaseBE() throws IOException {
        tester.testConversion(CgmesConformity1NetworkCatalog.microBaseCaseBE(), CgmesConformity1Catalog.microGridBaseCaseBE());
    }

    @Test
    void microGridType4BE() throws IOException {
        tester.testConversion(CgmesConformity1NetworkCatalog.microType4BE(), CgmesConformity1Catalog.microGridType4BE());
    }

    @Test
    void microGridType4BEOnlyEqTpSsh() throws IOException {
        tester.testConversion(null, CgmesConformity1Catalog.microGridType4BEOnlyEqTpSsh());
    }

    @Test
    void microGridBaseCaseNL() throws IOException {
        tester.testConversion(null, CgmesConformity1Catalog.microGridBaseCaseNL());
    }

    @Test
    void microGridBaseCaseAssembled() throws IOException {
        tester.testConversion(null, CgmesConformity1Catalog.microGridBaseCaseAssembled());
    }

    @Test
    void miniBusBranch() throws IOException {
        tester.testConversion(null, CgmesConformity1Catalog.miniBusBranch());
        // This generator has a regulating control that is enabled
        // But the SSH data says the synchronous machine has control disabled
        // So the generator is not participating in the voltage regulation
        // Voltage regulating must be off
        assertFalse(tester.lastConvertedNetwork().getGenerator("2970a2b7-b840-4e9c-b405-0cb854cd2318").isVoltageRegulatorOn());
    }

    @Test
    void miniNodeBreakerBusBalanceValidation() throws IOException {
        // This test will check that IIDM buses,
        // that will be computed by IIDM from CGMES node-breaker ConnectivityNodes,
        // have proper balances from SV values
        importParams.put(CgmesImport.PROFILE_FOR_INITIAL_VALUES_SHUNT_SECTIONS_TAP_POSITIONS, "SV");
        importParams.put(CgmesImport.IMPORT_CGM_WITH_SUBNETWORKS, "false");
        ConversionTester t = new ConversionTester(
            importParams,
            TripleStoreFactory.onlyDefaultImplementation(),
            new ComparisonConfig());
        t.setValidateBusBalances(true);
        t.testConversion(null, CgmesConformity1Catalog.miniNodeBreaker());
        t.lastConvertedNetwork().getVoltageLevels()
            .forEach(vl -> assertEquals(TopologyKind.NODE_BREAKER, vl.getTopologyKind()));
    }

    @Test
    void miniNodeBreakerAsBusBranchBusBalanceValidation() throws IOException {
        // This test will check that IIDM buses,
        // that will be created during conversion from CGMES TopologicalNodes,
        // have proper balances from SV values
        CgmesModel expected = CgmesConformity1Catalog.miniNodeBreaker().expected();

        importParams.put(CgmesImport.PROFILE_FOR_INITIAL_VALUES_SHUNT_SECTIONS_TAP_POSITIONS, "SV");
        importParams.put(CgmesImport.IMPORT_NODE_BREAKER_AS_BUS_BREAKER, "true");
        ConversionTester t = new ConversionTester(
                importParams,
                TripleStoreFactory.onlyDefaultImplementation(),
                new ComparisonConfig());
        t.setValidateBusBalances(true);
        t.testConversion(null, CgmesConformity1Catalog.miniNodeBreaker());

        Network network = t.lastConvertedNetwork();

        // All voltage levels must have bus/breaker topology kind
        network.getVoltageLevels()
                .forEach(vl -> assertEquals(TopologyKind.BUS_BREAKER, vl.getTopologyKind()));

        // All bus identifiers in the bus/breaker view must correspond to Topological Nodes of CGMES model
        List<String> iidmBusIds = network.getBusBreakerView().getBusStream().map(Identifiable::getId).sorted().toList();

        List<String> cgmesTNIds = expected.topologicalNodes().pluckIdentifiers(CgmesNames.TOPOLOGICAL_NODE).stream().sorted().toList();
        // Boundary nodes of CGMES model are not mapped to buses in IIDM
        List<String> cgmesBoundaryTNIds = network.getDanglingLineStream().map(dl -> dl.getProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TOPOLOGICAL_NODE_BOUNDARY)).toList();

        List<String> expectedBusIds = new ArrayList<>(cgmesTNIds);
        expectedBusIds.removeAll(cgmesBoundaryTNIds);

        assertEquals(expectedBusIds, iidmBusIds);
    }

    @Test
    void microNodeBreakerBoundary() throws IOException {
        importParams.put(CgmesImport.CONVERT_BOUNDARY, "true");
        ConversionTester t = new ConversionTester(
            importParams,
            TripleStoreFactory.onlyDefaultImplementation(),
            new ComparisonConfig());
        Network expected = null;
        t.testConversion(expected, CgmesConformity1Catalog.microGridBaseCaseBE());
        assertEquals(
            ImmutableSet.of(
                Country.AT,
                Country.BE,
                Country.ES,
                Country.NL),
            t.lastConvertedNetwork().getSubstationStream()
                .map(Substation::getCountry)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet()));
    }

    @Test
    void miniNodeBreakerBoundary() throws IOException {
        importParams.put(CgmesImport.CONVERT_BOUNDARY, "true");
        ConversionTester t = new ConversionTester(
            importParams,
            TripleStoreFactory.onlyDefaultImplementation(),
            new ComparisonConfig());
        Network expected = null;
        t.testConversion(expected, CgmesConformity1Catalog.miniNodeBreaker());
        Substation substation = t.lastConvertedNetwork().getSubstation("183d126d-2522-4ff2-a8cd-c5016cf09c1b_S");
        assertNotNull(substation);
        assertEquals("boundary", substation.getOptionalName().orElse(null));
        VoltageLevel voltageLevel = t.lastConvertedNetwork().getVoltageLevel("183d126d-2522-4ff2-a8cd-c5016cf09c1b_VL");
        assertNotNull(voltageLevel);
        assertEquals("boundary", voltageLevel.getOptionalName().orElse(null));
    }

    @Test
    void smallBusBranch() throws IOException {
        tester.testConversion(null, CgmesConformity1Catalog.smallBusBranch());
    }

    @Test
    void smallNodeBreaker() throws IOException {
        tester.testConversion(null, CgmesConformity1Catalog.smallNodeBreaker());
    }

    @Test
    void smallNodeBreakerHvdc() {
        // Small Grid Node Breaker HVDC should be imported without errors
        assertNotNull(new CgmesImport().importData(CgmesConformity1Catalog.smallNodeBreakerHvdc().dataSource(), NetworkFactory.findDefault(), importParams));

    }

    @Test
    // This is to test that we have stable Identifiers for calculated buses
    // If no topology change has been made, running a LoadFlow (even a Mock
    // LoadFlow)
    // must produce identical identifiers for calculated buses
    void smallNodeBreakerStableBusNaming() {
        Network network = new CgmesImport().importData(CgmesConformity1Catalog.smallNodeBreaker().dataSource(), NetworkFactory.findDefault(), importParams);

        // Initial bus identifiers
        List<String> initialBusIds = network.getBusView().getBusStream()
            .map(Bus::getId).collect(Collectors.toList());

        // Compute a "mock" LoadFlow and obtain bus identifiers
        String lfVariantId = "lf";
        network.getVariantManager()
            .cloneVariant(network.getVariantManager().getWorkingVariantId(),
                lfVariantId);
        network.getVariantManager().setWorkingVariant(lfVariantId);
        List<String> afterLoadFlowBusIds = network.getBusView().getBusStream()
            .map(Bus::getId).collect(Collectors.toList());

        assertEquals(initialBusIds, afterLoadFlowBusIds);
    }

    @Test
    void miniNodeBreakerOnlyEQ() {
        assertNotNull(new CgmesImport().importData(CgmesConformity1Catalog.miniNodeBreakerOnlyEQ().dataSource(), NetworkFactory.findDefault(), importParams));
    }

    @Test
    void smallNodeBreakerOnlyEQ() {
        assertNotNull(new CgmesImport().importData(CgmesConformity1Catalog.smallNodeBreakerOnlyEQ().dataSource(), NetworkFactory.findDefault(), importParams));
    }

    @Test
    void smallNodeBreakerHvdcOnlyEQ() {
        assertNotNull(new CgmesImport().importData(CgmesConformity1Catalog.smallNodeBreakerHvdcOnlyEQ().dataSource(), NetworkFactory.findDefault(), importParams));
    }

    @Test
    void microNLActivePowerControlExtensionByDefault() {
        // We need to explicitly set that the extension does not have to be created
        importParams.put(CgmesImport.CREATE_ACTIVE_POWER_CONTROL_EXTENSION, "false");
        Network network = new CgmesImport().importData(CgmesConformity1Catalog.microGridBaseCaseNL().dataSource(), NetworkFactory.findDefault(), importParams);
        Generator g = network.getGenerator("9c3b8f97-7972-477d-9dc8-87365cc0ad0e");
        ActivePowerControl<Generator> ext = g.getExtension(ActivePowerControl.class);
        assertNull(ext);
    }

    @Test
    void microNLActivePowerControlExtension() {
        // The extension is created by default
        Network network = new CgmesImport().importData(CgmesConformity1Catalog.microGridBaseCaseNL().dataSource(), NetworkFactory.findDefault(), importParams);
        Generator g = network.getGenerator("9c3b8f97-7972-477d-9dc8-87365cc0ad0e");
        ActivePowerControl<Generator> ext = g.getExtension(ActivePowerControl.class);
        assertNotNull(ext);
        assertTrue(Double.isNaN(ext.getDroop()));
        assertEquals(1.0, ext.getParticipationFactor(), 0.0);
        assertTrue(ext.isParticipate());
    }

    @Test
    void microNLReferencePriorityExtension() {
        Network network = new CgmesImport().importData(CgmesConformity1Catalog.microGridBaseCaseNL().dataSource(), NetworkFactory.findDefault(), importParams);
        ReferencePriority referencePriority = ReferencePriorities.get(network).iterator().next();
        assertNotNull(referencePriority);
        assertEquals(1, referencePriority.getPriority());
        assertEquals("9c3b8f97-7972-477d-9dc8-87365cc0ad0e", referencePriority.getTerminal().getConnectable().getId());
    }

    private static class TxData {
        TxData(int numEnds, int rtc1, int ptc1, int rtc2, int ptc2) {
            this.numEnds = numEnds;
            this.rtc1 = rtc1;
            this.ptc1 = ptc1;
            this.rtc2 = rtc2;
            this.ptc2 = ptc2;
        }

        TxData(String numEnds, String rtc1, String ptc1, String rtc2, String ptc2) {
            this.numEnds = Integer.parseInt(numEnds);
            this.rtc1 = Integer.parseInt(rtc1);
            this.ptc1 = Integer.parseInt(ptc1);
            this.rtc2 = Integer.parseInt(rtc2);
            this.ptc2 = Integer.parseInt(ptc2);
        }

        @Override
        public int hashCode() {
            return Objects.hash(numEnds, rtc1, ptc1, rtc2, ptc2);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof TxData)) {
                return false;
            }
            TxData d = (TxData) obj;
            return numEnds == d.numEnds && rtc1 == d.rtc1 && ptc1 == d.ptc1 && rtc2 == d.rtc2 && ptc2 == d.ptc2;
        }

        @Override
        public String toString() {
            return String.format("(%d %d %d %d %d)", numEnds, rtc1, ptc1, rtc2, ptc2);
        }

        int numEnds;
        int rtc1;
        int ptc1;
        int rtc2;
        int ptc2;
    }

    private static ConversionTester tester;

    private FileSystem fileSystem;
    private Properties importParams;
}
