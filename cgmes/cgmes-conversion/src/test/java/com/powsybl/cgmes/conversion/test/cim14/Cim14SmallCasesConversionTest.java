/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.conversion.test.cim14;

import com.powsybl.cgmes.conversion.test.ConversionTester;
import com.powsybl.cgmes.conversion.test.network.compare.ComparisonConfig;
import com.powsybl.cgmes.model.test.cim14.Cim14SmallCasesCatalog;
import com.powsybl.triplestore.api.TripleStoreFactory;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class Cim14SmallCasesConversionTest {
    @BeforeClass
    public static void setUp() {
        tester = new ConversionTester(
                TripleStoreFactory.onlyDefaultImplementation(),
                new ComparisonConfig()
                        .checkNetworkId(false)
                        // Expected cases are read using CIM1Importer, that uses floats to read numbers
                        // IIDM and CGMES now stores numbers as doubles
                        .tolerance(2.4e-4));
    }

    @Test
    public void txMicroBEAdapted() throws IOException {
        tester.testConversion(Cim14SmallCasesNetworkCatalog.txMicroBEAdapted(), Cim14SmallCasesCatalog.txMicroBEAdapted());
    }

    @Test
    public void smallcase1() throws IOException {
        tester.testConversion(Cim14SmallCasesNetworkCatalog.smallcase1(), Cim14SmallCasesCatalog.small1());
    }

    @Test
    public void ieee14() throws IOException {
        tester.testConversion(Cim14SmallCasesNetworkCatalog.ieee14(), Cim14SmallCasesCatalog.ieee14());
    }

    @Test
    public void nordic32() throws IOException {
        tester.testConversion(Cim14SmallCasesNetworkCatalog.nordic32(), Cim14SmallCasesCatalog.nordic32());
    }

    @Test
    public void m7buses() throws IOException {
        tester.testConversion(Cim14SmallCasesNetworkCatalog.m7buses(), Cim14SmallCasesCatalog.m7buses());
    }

    private static ConversionTester tester;
}
