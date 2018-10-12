/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.conversion.test.conformity;

import org.junit.BeforeClass;
import org.junit.Test;

import com.powsybl.cgmes.conformity.test.CgmesConformity1Catalog;
import com.powsybl.cgmes.conformity.test.CgmesConformity1NetworkCatalog;
import com.powsybl.cgmes.conversion.test.ConversionTester;
import com.powsybl.cgmes.conversion.test.network.compare.ComparisonConfig;
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
    public void microGridBaseCaseBE() {
        tester.testConversion(expecteds.microBE(), actuals.microGridBaseCaseBE());
    }

    @Test
    public void microGridBaseCaseNL() {
        tester.testConversion(null, actuals.microGridBaseCaseNL());
    }

    @Test
    public void microGridBaseCaseAssembled() {
        tester.testConversion(null, actuals.microGridBaseCaseAssembled());
    }

    @Test
    public void miniBusBranch() {
        tester.testConversion(null, actuals.miniBusBranch());
    }

    @Test
    public void miniNodeBreaker() {
        tester.testConversion(null, actuals.miniNodeBreaker());
    }

    @Test
    public void smallBusBranch() {
        tester.testConversion(null, actuals.smallBusBranch());
    }

    @Test
    public void smallNodeBreaker() {
        tester.testConversion(null, actuals.smallNodeBreaker());
    }

    private static CgmesConformity1Catalog actuals;
    private static CgmesConformity1NetworkCatalog expecteds;
    private static ConversionTester tester;
}
