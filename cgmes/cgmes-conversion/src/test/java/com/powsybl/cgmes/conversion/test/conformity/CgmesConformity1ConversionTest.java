/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.conversion.test.conformity;

import com.powsybl.cgmes.conformity.test.CgmesConformity1Catalog;
import com.powsybl.cgmes.conformity.test.CgmesConformity1NetworkCatalog;
import com.powsybl.cgmes.conversion.test.ConversionTester;
import com.powsybl.cgmes.conversion.test.network.compare.ComparisonConfig;
import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.import_.Importers;
import com.powsybl.iidm.network.*;
import com.powsybl.loadflow.LoadFlowParameters;
import com.powsybl.loadflow.mock.LoadFlowFactoryMock;
import com.powsybl.triplestore.api.TripleStoreFactory;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class CgmesConformity1ConversionTest {
    @BeforeClass
    public static void setUp() {
        actuals = new CgmesConformity1Catalog();
        expecteds = new CgmesConformity1NetworkCatalog();
        tester = new ConversionTester(
                TripleStoreFactory.onlyDefaultImplementation(),
                new ComparisonConfig());
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
        t.testConversion(null, actuals.microGridBaseCaseBE());

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
        importParams.put("convertBoundary", "true");
        ConversionTester t = new ConversionTester(
                importParams,
                TripleStoreFactory.onlyDefaultImplementation(),
                new ComparisonConfig());
        t.setTestExportImportCgmes(true);
        Network expected = null;
        t.testConversion(expected, actuals.microGridBaseCaseBE());
    }

    @Test
    public void microGridBaseCaseBERoundtrip() throws IOException {
        // TODO When we convert boundaries values for P0, Q0 at dangling lines
        // are recalculated and we need to increase the tolerance
        ConversionTester t = new ConversionTester(
                TripleStoreFactory.onlyDefaultImplementation(),
                new ComparisonConfig().tolerance(1e-5));
        t.setTestExportImportCgmes(true);
        t.testConversion(expecteds.microBaseCaseBE(), actuals.microGridBaseCaseBE());
    }

    @Test
    public void microGridBaseCaseBE() throws IOException {
        tester.testConversion(expecteds.microBaseCaseBE(), actuals.microGridBaseCaseBE());
    }

    @Test
    public void microGridType4BE() throws IOException {
        tester.testConversion(expecteds.microType4BE(), actuals.microGridType4BE());
    }

    @Test
    public void microGridBaseCaseNL() throws IOException {
        tester.testConversion(null, actuals.microGridBaseCaseNL());
    }

    @Test
    public void microGridBaseCaseAssembled() throws IOException {
        tester.testConversion(null, actuals.microGridBaseCaseAssembled());
    }

    @Test
    public void miniBusBranch() throws IOException {
        tester.testConversion(null, actuals.miniBusBranch());
    }

    @Test
    public void miniNodeBreakerBusBalanceValidation() throws IOException {
        // This test will check that IIDM buses,
        // that will be computed by IIDM from CGMES node-breaker ConnectivityNodes,
        // have proper balances
        ConversionTester t = new ConversionTester(
                TripleStoreFactory.onlyDefaultImplementation(),
                new ComparisonConfig());
        t.setValidateBusBalances(true);
        t.testConversion(null, actuals.miniNodeBreaker());
        t.lastConvertedNetwork().getVoltageLevels()
                .forEach(vl -> assertEquals(TopologyKind.NODE_BREAKER, vl.getTopologyKind()));
    }

    @Test
    public void miniNodeBreakerTestLimits() throws IOException {
        ComputationManager computationManager = Mockito.mock(ComputationManager.class);

        // Original test case
        Network network0 = Importers.importData("CGMES",
                actuals.miniNodeBreaker().dataSource(),
                null,
                computationManager);
        // The case has been manually modified to have OperationalLimits
        // defined for Equipment
        Network network1 = Importers.importData("CGMES",
                actuals.miniNodeBreakerLimitsforEquipment().dataSource(),
                null,
                computationManager);

        double tol = 0;

        // 1 - PATL Current defined for an Equipment ACTransmissionLine
        // Previous limit for one terminal has been modified to refer to the Equipment
        // In the modified case both ends have to see the same value
        Line l0 = network0.getLine("_1e7f52a9-21d0-4ebe-9a8a-b29281d5bfc9");
        Line l1 = network1.getLine("_1e7f52a9-21d0-4ebe-9a8a-b29281d5bfc9");
        assertEquals(525, l0.getCurrentLimits1().getPermanentLimit(), tol);
        assertNull(l0.getCurrentLimits2());
        assertEquals(525, l1.getCurrentLimits1().getPermanentLimit(), tol);
        assertEquals(525, l1.getCurrentLimits2().getPermanentLimit(), tol);

        // 2 - PATL Current defined for an ACTransmissionLine
        // that will be mapped to a DanglingLine in IIDM
        DanglingLine dl0 = network0.getDanglingLine("_f32baf36-7ea3-4b6a-9452-71e7f18779f8");
        DanglingLine dl1 = network1.getDanglingLine("_f32baf36-7ea3-4b6a-9452-71e7f18779f8");
        // In network0 limit is defined for the Terminal
        // In network1 limit is defined for the Equipment
        // In both cases the limit should be mapped to IIDM
        assertEquals(1000, dl0.getCurrentLimits().getPermanentLimit(), tol);
        assertEquals(1000, dl1.getCurrentLimits().getPermanentLimit(), tol);

        // 3 - PATL Current defined for a PowerTransformer, should be rejected
        TwoWindingsTransformer tx0 = network0.getTwoWindingsTransformer("_ceb5d06a-a7ff-4102-a620-7f3ea5fb4a51");
        TwoWindingsTransformer tx1 = network1.getTwoWindingsTransformer("_ceb5d06a-a7ff-4102-a620-7f3ea5fb4a51");
        assertEquals(158, tx0.getCurrentLimits1().getPermanentLimit(), tol);
        assertEquals(1732, tx0.getCurrentLimits2().getPermanentLimit(), tol);
        assertNull(tx1.getCurrentLimits1());
        assertEquals(1732, tx1.getCurrentLimits2().getPermanentLimit(), tol);

        // 4 - PATL Current defined for Switch, will be ignored
        TwoWindingsTransformer tx0s = network0.getTwoWindingsTransformer("_6c89588b-3df5-4120-88e5-26164afb43e9");
        TwoWindingsTransformer tx1s = network1.getTwoWindingsTransformer("_6c89588b-3df5-4120-88e5-26164afb43e9");
        assertEquals(1732, tx0s.getCurrentLimits2().getPermanentLimit(), tol);
        assertNull(tx1s.getCurrentLimits2());
    }

    @Test
    public void smallBusBranch() throws IOException {
        tester.testConversion(null, actuals.smallBusBranch());
    }

    @Test
    public void smallNodeBreaker() throws IOException {
        tester.testConversion(null, actuals.smallNodeBreaker());
    }

    @Test
    public void smallNodeBreakerHvdc() throws IOException {
        ComputationManager computationManager = Mockito.mock(ComputationManager.class);

        // Small Grid Node Breaker HVDC should be imported without errors
        Importers.importData("CGMES",
                actuals.smallNodeBreakerHvdc().dataSource(),
                null,
                computationManager);
    }

    @Test
    // This is to test that we have stable Identifiers for calculated buses
    // If no topology change has been made, running a LoadFlow (even a Mock LoadFlow)
    // must produce identical identifiers for calculated buses
    public void smallNodeBreakerStableBusNaming() throws IOException {
        ComputationManager computationManager = Mockito.mock(ComputationManager.class);

        Network network = Importers.importData("CGMES",
                actuals.smallNodeBreaker().dataSource(),
                null,
                computationManager);

        // Initial bus identifiers
        List<String> initialBusIds = network.getBusView().getBusStream()
                .map(Bus::getId).collect(Collectors.toList());

        // Compute a "mock" LoadFlow and obtain bus identifiers
        String lfVariantId = "lf";
        network.getVariantManager()
                .cloneVariant(network.getVariantManager().getWorkingVariantId(),
                        lfVariantId);
        new LoadFlowFactoryMock()
                .create(network,
                        computationManager,
                        1)
                .run(lfVariantId, new LoadFlowParameters()).join();
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

    private static CgmesConformity1Catalog actuals;
    private static CgmesConformity1NetworkCatalog expecteds;
    private static ConversionTester tester;
}
