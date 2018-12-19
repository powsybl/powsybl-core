/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.util;

import org.junit.Test;

import com.powsybl.iidm.network.util.AbstractTestCase.BranchTestCase;
import com.powsybl.iidm.network.util.AbstractTestCase.BusBalance;
import com.powsybl.iidm.network.util.AbstractTestCase.DanglingLineTestCase;
import com.powsybl.iidm.network.util.AbstractTestCase.Flow;
import com.powsybl.iidm.network.util.AbstractTestCase.Voltage;
import com.powsybl.iidm.network.util.TestCaseCatalog.EntsoeMicroGridVariant;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class BranchDataTest {

    // Check flows in a transmission line disconnected at one end

    @Test
    public void testDanglingLine() {
        DanglingLineTestCase t = TestCaseCatalog.danglingLineEnd2Disconnected();
        String label0 = t.label;

        // First obtain results when end 2 is disconnected
        t.label = label0 + " End 2 disconnected";
        t.test();

        // Now change the disconnected end and check the same results are obtained
        // The expected values at the disconnected end must be the same
        // when we disconnect end 1 or end 2
        // If we use the same voltage at the connected end
        // Admittance to ground has the same value at both ends
        t.label = label0 + " End 1 disconnected";
        t.end2.voltage = t.end1.voltage;
        t.end1.voltage = Voltage.UNKNOWN;
        t.end2.connected = true;
        t.end1.connected = false;
        t.end2.expectedFlow = t.end1.expectedFlow;
        t.end1.expectedFlow = Flow.UNKNOWN;
        t.test();
    }

    @Test
    public void testDanglingLineDifferentY() {
        DanglingLineTestCase t = TestCaseCatalog.danglingLineEnd2Disconnected();
        String label0 = t.label + " Y1, Y2 different";

        t.label = label0 + " End 2 disconnected";
        // End 1 admittance to ground has different value of End 2
        t.end1.b = t.end2.b * 2;
        // When end 2 is disconnected, the voltage at end 2
        // should be the same that was obtained when Y1 = Y2
        // because the voltage at end 2 depends only on Ytr and Y2
        // But the flow seen at end 1 will be different
        t.test();

        // Now when we disconnect end 1 both the voltage drop and
        // the expected values for flow are different
        t.label = label0 + " End 1 disconnected";
        t.end2.voltage = t.end1.voltage;
        t.end1.voltage = Voltage.UNKNOWN;
        t.end2.connected = true;
        t.end1.connected = false;
        t.end2.expectedFlow = t.end1.expectedFlow;
        t.end1.expectedFlow = Flow.UNKNOWN;
        t.test();
    }

    // ENTSO-E CASv2.0 ENTSOE_LoadFlowExplicit
    // There are two parallel branches between a generator and a load bus:
    // a transmission line and a phase shift transformer

    @Test
    public void testCAS2EntsoeLoadFlowExplicitLine() {
        TestCaseCatalog.entsoeCAS2LoadFlowExplicitLine().test();
    }

    @Test
    public void testCAS2EntsoeLoadFlowExplicitPhaseShiftTransformer() {
        TestCaseCatalog.entsoeCAS2LoadFlowExplicitPhaseShiftTransformer().test();
    }

    @Test
    public void testCAS2EntsoeLoadFlowExplicitLineAndPhaseShifter() {
        BranchTestCase line = TestCaseCatalog.entsoeCAS2LoadFlowExplicitLine();
        BranchTestCase pst = TestCaseCatalog.entsoeCAS2LoadFlowExplicitPhaseShiftTransformer();
        Flow load = TestCaseCatalog.entsoeCAS2LoadFlowExplicitLoad();
        Flow generator = TestCaseCatalog.entsoeCAS2LoadFlowExplicitGenerator();

        // Obtain flows at both ends of line and phase shifter
        BranchData pline = line.piModelFlows();
        BranchData ppst = pst.piModelFlows();

        // And check bus balance at each side
        // At end1 (load) the mismatch should be zero
        // because load values are given with all significant digits
        // and voltages have been copied from Excel documentation,
        // where they have sufficient precision
        // At end2 (generator, slack bus) the mismatch should be small:
        // generator injection in P and Q are provided with low precision
        // (P with 10e-2, Q with 10e-4)
        Flow line1 = Flow.get(pline, 1);
        Flow line2 = Flow.get(pline, 2);
        Flow pst1 = Flow.get(ppst, 1);
        Flow pst2 = Flow.get(ppst, 2);
        new BusBalance("LoadFlowExplicit. Balance at end 1", BusBalance.EXACT, line1, pst1, load).test();
        new BusBalance("LoadFlowExplicit. Balance at end 2", 1e-2, 1e-4, line2, pst2, generator).test();
    }

    @Test
    public void testCAS2EntsoeLoadFlowExplicitLineAndPhaseShifterMovedToEnd1() {
        BranchTestCase line = TestCaseCatalog.entsoeCAS2LoadFlowExplicitLine();
        BranchTestCase pst = TestCaseCatalog.entsoeCAS2LoadFlowExplicitPhaseShiftTransformer();
        Flow load = TestCaseCatalog.entsoeCAS2LoadFlowExplicitLoad();
        Flow generator = TestCaseCatalog.entsoeCAS2LoadFlowExplicitGenerator();

        // Move phase shift from end 2 to end 1
        pst.end1.tap.rho = 1 / pst.end2.tap.rho;
        pst.end1.tap.alpha = -pst.end2.tap.alpha;
        double rho2square = pst.end2.tap.rho * pst.end2.tap.rho;
        double dz = 100 * (1 / rho2square - 1);
        double dy = 100 * (rho2square - 1);
        pst.end1.tap.r = dz;
        pst.end1.tap.x = dz;
        pst.end1.tap.g = dy;
        pst.end1.tap.b = dy;
        pst.end2.tap.rho = 1;
        pst.end2.tap.alpha = 0;

        BranchData pline = line.piModelFlows();
        BranchData ppst = pst.piModelFlows();

        Flow line1 = Flow.get(pline, 1);
        Flow line2 = Flow.get(pline, 2);
        Flow pst1 = Flow.get(ppst, 1);
        Flow pst2 = Flow.get(ppst, 2);
        String label1 = String.format("Balance after model phase shifter at end1. End %d of parallel branches %s, %s",
                1, pline.getId(), ppst.getId());
        String label2 = String.format("Balance after model phase shifter at end1. End %d of parallel branches %s, %s",
                2, pline.getId(), ppst.getId());
        new BusBalance(label1, BusBalance.EXACT, line1, pst1, load).test();
        new BusBalance(label2, 1e-2, 1e-4, line2, pst2, generator).test();
    }

    // ENTSO-E CAS 1.1.3 MicroGrid
    // Check flows in 2-winding transformer BE-TR2_3

    @Test
    public void testentsoeCAS1MicroGrid2wTxBC() {
        testentsoeCAS1MicroGrid2wTx(EntsoeMicroGridVariant.BC);
    }

    @Test
    public void testentsoeCAS1MicroGrid2wTxBCAreaControlOn() {
        testentsoeCAS1MicroGrid2wTx(EntsoeMicroGridVariant.BC_AREA_CONTROL_ON);
    }

    @Test
    public void testentsoeCAS1MicroGrid2wTxBCNoTxRegulation() {
        testentsoeCAS1MicroGrid2wTx(EntsoeMicroGridVariant.BC_NO_TRANSFORMER_REGULATION);
    }

    @Test
    public void testentsoeCAS1MicroGrid2wTxT1() {
        testentsoeCAS1MicroGrid2wTx(EntsoeMicroGridVariant.T1);
    }

    @Test
    public void testentsoeCAS1MicroGrid2wTxT2() {
        testentsoeCAS1MicroGrid2wTx(EntsoeMicroGridVariant.T2);
    }

    @Test
    public void testentsoeCAS1MicroGrid2wTxT3() {
        testentsoeCAS1MicroGrid2wTx(EntsoeMicroGridVariant.T3);
    }

    @Test
    public void testentsoeCAS1MicroGrid2wTxT4() {
        testentsoeCAS1MicroGrid2wTx(EntsoeMicroGridVariant.T4);
    }

    private void testentsoeCAS1MicroGrid2wTx(EntsoeMicroGridVariant variant) {
        BranchTestCase t = TestCaseCatalog.entsoeCAS1MicroGrid2wTx(variant);
        t.label = t.label + " CGMES model";
        t.test();
        t.convertToIidm().test();
    }

    // ENTSO-E CAS 1.1.3 MicroGrid
    // Check flows in 3-winding transformer BE-TR3_1

    @Test
    public void testentsoeCAS1MicroGrid3wTxBC() {
        testEntsoeCAS1MicroGrid3wTx(EntsoeMicroGridVariant.BC);
    }

    @Test
    public void testentsoeCAS1MicroGrid3wTxBCNoTxRegulation() {
        testEntsoeCAS1MicroGrid3wTx(EntsoeMicroGridVariant.BC_NO_TRANSFORMER_REGULATION);
    }

    @Test
    public void testentsoeCAS1MicroGrid3wTxBCAreaControlOn() {
        testEntsoeCAS1MicroGrid3wTx(EntsoeMicroGridVariant.BC_AREA_CONTROL_ON);
    }

    @Test
    public void testentsoeCAS1MicroGrid3wTxT1() {
        testEntsoeCAS1MicroGrid3wTx(EntsoeMicroGridVariant.T1);
    }

    @Test
    public void testentsoeCAS1MicroGrid3wTxT2() {
        testEntsoeCAS1MicroGrid3wTx(EntsoeMicroGridVariant.T2);
    }

    @Test
    public void testentsoeCAS1MicroGrid3wTxT3() {
        testEntsoeCAS1MicroGrid3wTx(EntsoeMicroGridVariant.T3);
    }

    @Test
    public void testentsoeCAS1MicroGrid3wTxT4() {
        testEntsoeCAS1MicroGrid3wTx(EntsoeMicroGridVariant.T4);
    }

    private void testEntsoeCAS1MicroGrid3wTx(EntsoeMicroGridVariant variant) {
        TestCaseCatalog.entsoeCAS1MicroGrid3wTx(variant).test();
    }

    // DACF NGET 11:30 e67767ee

    @Test
    public void testDacfNget1130Elst2Sgt8() {
        TestCaseCatalog.dacfNget1130Elst2Sgt8().test();
    }
}
