/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.conversion.test.conformity;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

import org.junit.BeforeClass;
import org.junit.Test;

import com.powsybl.cgmes.conformity.test.CgmesConformity1Catalog;
import com.powsybl.cgmes.conformity.test.CgmesConformity1NetworkCatalog;
import com.powsybl.cgmes.conversion.test.ConversionTester;
import com.powsybl.cgmes.conversion.test.network.compare.ComparisonConfig;
import com.powsybl.iidm.network.Network;
import com.powsybl.triplestore.api.TripleStoreFactory;

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
        t.testConversion(expecteds.microBE(), actuals.microGridBaseCaseBE());
    }

    @Test
    public void microGridBaseCaseBE() throws IOException {
        tester.testConversion(expecteds.microBE(), actuals.microGridBaseCaseBE());
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
    public void miniNodeBreaker() throws IOException {
        tester.testConversion(null, actuals.miniNodeBreaker());
    }

    @Test
    public void smallBusBranch() throws IOException {
        tester.testConversion(null, actuals.smallBusBranch());
    }

    @Test
    public void smallNodeBreaker() throws IOException {
        tester.testConversion(null, actuals.smallNodeBreaker());
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
