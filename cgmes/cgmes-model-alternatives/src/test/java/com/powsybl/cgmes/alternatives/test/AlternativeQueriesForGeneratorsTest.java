/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.alternatives.test;

import java.io.IOException;
import java.util.function.Consumer;

import org.junit.BeforeClass;
import org.junit.Test;

import com.powsybl.cgmes.alternatives.test.AlternativeQueriesTester.Expected;
import com.powsybl.cgmes.conformity.test.CgmesConformity1Catalog;
import com.powsybl.cgmes.model.test.TestGridModel;
import com.powsybl.triplestore.api.PropertyBags;
import com.powsybl.triplestore.api.QueryCatalog;
import com.powsybl.triplestore.api.TripleStoreFactory;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class AlternativeQueriesForGeneratorsTest {

    @BeforeClass
    public static void setUp()  {
        TestGridModel model = new CgmesConformity1Catalog().smallBusBranch();
        Expected expected = new Expected()
                .resultSize(19)
                .propertyCount("regulatingControlTargetValue", 19)
                .propertyCount("regulatingControlTerminalConnected", 19);

        int experiments = 1;
        boolean doAssert = true;
        Consumer<PropertyBags> consumer = null;
        boolean cacheModels = false;

        tester = new AlternativeQueriesTester(
                TripleStoreFactory.allImplementations(),
                new QueryCatalog("generators.sparql"),
                model,
                expected,
                experiments,
                doAssert,
                consumer,
                cacheModels);
        tester.load();
        testerWorkingWithNestedGraphClauses = new AlternativeQueriesTester(
                TripleStoreFactory.implementationsWorkingWithNestedGraphClauses(),
                new QueryCatalog("generators.sparql"),
                model,
                expected,
                experiments,
                doAssert,
                consumer,
                cacheModels);
        testerWorkingWithNestedGraphClauses.load();
    }

    @Test
    public void usingGraphClauses() throws IOException {
        testerWorkingWithNestedGraphClauses.test("usingGraphClauses");
    }

    @Test
    public void noGraphClauses() throws IOException {
        tester.test("noGraphClauses");
    }

    private static AlternativeQueriesTester tester;
    private static AlternativeQueriesTester testerWorkingWithNestedGraphClauses;
}
