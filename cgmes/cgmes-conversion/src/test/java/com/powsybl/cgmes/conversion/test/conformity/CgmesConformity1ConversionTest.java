/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.conversion.test.conformity;

import com.google.common.collect.ImmutableSet;
import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.cgmes.conformity.test.CgmesConformity1Catalog;
import com.powsybl.cgmes.conformity.test.CgmesConformity1NetworkCatalog;
import com.powsybl.cgmes.conversion.CgmesImport;
import com.powsybl.cgmes.conversion.test.ConversionTester;
import com.powsybl.cgmes.conversion.test.network.compare.ComparisonConfig;
import com.powsybl.iidm.network.*;
import com.powsybl.triplestore.api.TripleStoreFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class CgmesConformity1ConversionTest {

    @BeforeClass
    public static void setUpBeforeClass() {
        tester = new ConversionTester(
            TripleStoreFactory.onlyDefaultImplementation(),
            new ComparisonConfig());
    }

    @Before
    public void setUp() {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
    }

    @After
    public void tearDown() throws IOException {
        fileSystem.close();
    }

    @Test
    public void microGridBaseCaseBEReport() throws IOException {
        ConversionTester t = new ConversionTester(TripleStoreFactory.onlyDefaultImplementation(),
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
        expected.put("_84ed55f4-61f5-4d9d-8755-bba7b877a246", new TxData(3, 0, 0, 1, 0));
        expected.put("_a708c3bc-465d-4fe7-b6ef-6fa6408a62b0", new TxData(2, 0, 1, 0, 0));
        expected.put("_b94318f6-6d24-4f56-96b9-df2531ad6543", new TxData(2, 1, 0, 0, 0));
        expected.put("_e482b89a-fa84-4ea9-8e70-a83d44790957", new TxData(2, 0, 0, 1, 0));
        actual.keySet().forEach(tx -> assertEquals(expected.get(tx), actual.get(tx)));
    }

    @Test
    public void microGridBaseCaseBERoundtripBoundary() throws IOException {
        Properties importParams = new Properties();
        importParams.put(CgmesImport.CONVERT_BOUNDARY, "true");
        ConversionTester t = new ConversionTester(
            importParams,
            TripleStoreFactory.onlyDefaultImplementation(),
            new ComparisonConfig());
        t.setTestExportImportCgmes(true);
        Network expected = null;
        t.testConversion(expected, CgmesConformity1Catalog.microGridBaseCaseBE());
    }

    @Test
    public void microGridBaseCaseBERoundtrip() throws IOException {
        // TODO When we convert boundaries values for P0, Q0 at dangling lines
        // are recalculated and we need to increase the tolerance
        ConversionTester t = new ConversionTester(
            TripleStoreFactory.onlyDefaultImplementation(),
            new ComparisonConfig().tolerance(1e-5));
        t.setTestExportImportCgmes(true);
        t.testConversion(CgmesConformity1NetworkCatalog.microBaseCaseBE(), CgmesConformity1Catalog.microGridBaseCaseBE());
    }

    @Test
    public void microGridBaseCaseBEWithoutUnsupportedTapChangersRoundtrip() throws IOException {
        // TODO When we convert boundaries values for P0, Q0 at dangling lines
        // are recalculated and we need to increase the tolerance
        Properties properties = new Properties();
        properties.put(CgmesImport.ALLOW_UNSUPPORTED_TAP_CHANGERS, "false");
        ConversionTester t = new ConversionTester(
            properties,
            TripleStoreFactory.onlyDefaultImplementation(),
            new ComparisonConfig().tolerance(1e-5));
        t.setTestExportImportCgmes(true);
        t.testConversion(CgmesConformity1NetworkCatalog.microBaseCaseBE(), CgmesConformity1Catalog.microGridBaseCaseBE());
    }

    @Test
    public void microGridBaseCaseBEBusBalanceValidation() throws IOException {
        // Check bus balance mismatches are low if we use SV voltages
        // MicroGrid BaseCase BE contains an RTC defined at transformerEnd1
        // with step != neutralStep,
        // resulting in a significant ratio (far from 1.0).
        // Validating bus balance of buses after conversion verifies that
        // the interpretation of the location of tap changer
        // relative to the transmission impedance is correct
        Properties params = new Properties();
        params.put(CgmesImport.PROFILE_USED_FOR_INITIAL_STATE_VALUES, "SV");
        ConversionTester t = new ConversionTester(
            params,
            TripleStoreFactory.onlyDefaultImplementation(),
            new ComparisonConfig());
        t.setValidateBusBalancesUsingThreshold(1.2);
        t.testConversion(null, CgmesConformity1Catalog.microGridBaseCaseBE());
    }

    @Test
    public void microGridBaseCaseBE() throws IOException {
        tester.testConversion(CgmesConformity1NetworkCatalog.microBaseCaseBE(), CgmesConformity1Catalog.microGridBaseCaseBE());
    }

    @Test
    public void microGridType4BE() throws IOException {
        tester.testConversion(CgmesConformity1NetworkCatalog.microType4BE(), CgmesConformity1Catalog.microGridType4BE());
    }

    @Test
    public void microGridType4BEOnlyEqTpSsh() throws IOException {
        tester.testConversion(null, CgmesConformity1Catalog.microGridType4BEOnlyEqTpSsh());
    }

    @Test
    public void microGridBaseCaseNL() throws IOException {
        tester.testConversion(null, CgmesConformity1Catalog.microGridBaseCaseNL());
    }

    @Test
    public void microGridBaseCaseAssembled() throws IOException {
        tester.testConversion(null, CgmesConformity1Catalog.microGridBaseCaseAssembled());
    }

    @Test
    public void miniBusBranch() throws IOException {
        tester.testConversion(null, CgmesConformity1Catalog.miniBusBranch());
    }

    @Test
    public void miniNodeBreakerBusBalanceValidation() throws IOException {
        // This test will check that IIDM buses,
        // that will be computed by IIDM from CGMES node-breaker ConnectivityNodes,
        // have proper balances from SV values
        Properties params = new Properties();
        params.put(CgmesImport.PROFILE_USED_FOR_INITIAL_STATE_VALUES, "SV");
        ConversionTester t = new ConversionTester(
            params,
            TripleStoreFactory.onlyDefaultImplementation(),
            new ComparisonConfig());
        t.setValidateBusBalances(true);
        t.testConversion(null, CgmesConformity1Catalog.miniNodeBreaker());
        t.lastConvertedNetwork().getVoltageLevels()
            .forEach(vl -> assertEquals(TopologyKind.NODE_BREAKER, vl.getTopologyKind()));
    }

    @Test
    public void microNodeBreakerBoundary() throws IOException {
        Properties importParams = new Properties();
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
    public void miniNodeBreakerBoundary() throws IOException {
        Properties importParams = new Properties();
        importParams.put(CgmesImport.CONVERT_BOUNDARY, "true");
        ConversionTester t = new ConversionTester(
            importParams,
            TripleStoreFactory.onlyDefaultImplementation(),
            new ComparisonConfig());
        Network expected = null;
        t.testConversion(expected, CgmesConformity1Catalog.miniNodeBreaker());
        Substation substation = t.lastConvertedNetwork().getSubstation("_183d126d-2522-4ff2-a8cd-c5016cf09c1b_S");
        assertNotNull(substation);
        assertEquals("boundary", substation.getOptionalName().orElse(null));
        VoltageLevel voltageLevel = t.lastConvertedNetwork().getVoltageLevel("_183d126d-2522-4ff2-a8cd-c5016cf09c1b_VL");
        assertNotNull(voltageLevel);
        assertEquals("boundary", voltageLevel.getOptionalName().orElse(null));
    }

    @Test
    public void smallBusBranch() throws IOException {
        tester.testConversion(null, CgmesConformity1Catalog.smallBusBranch());
    }

    @Test
    public void smallNodeBreaker() throws IOException {
        tester.testConversion(null, CgmesConformity1Catalog.smallNodeBreaker());
    }

    @Test
    public void smallNodeBreakerHvdc() {
        // Small Grid Node Breaker HVDC should be imported without errors
        assertNotNull(new CgmesImport().importData(CgmesConformity1Catalog.smallNodeBreakerHvdc().dataSource(), null));

    }

    @Test
    // This is to test that we have stable Identifiers for calculated buses
    // If no topology change has been made, running a LoadFlow (even a Mock
    // LoadFlow)
    // must produce identical identifiers for calculated buses
    public void smallNodeBreakerStableBusNaming() {
        Network network = new CgmesImport().importData(CgmesConformity1Catalog.smallNodeBreaker().dataSource(), null);

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
}
