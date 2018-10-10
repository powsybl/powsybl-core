package com.powsybl.cgmes.alternatives.test;

/*
 * #%L
 * CGMES Model Alternatives
 * %%
 * Copyright (C) 2017 - 2018 RTE (http://rte-france.com)
 * %%
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * #L%
 */

import java.util.function.Consumer;

import org.junit.BeforeClass;
import org.junit.Test;

import com.powsybl.cgmes.alternatives.test.AlternativeQueriesTester.Expected;
import com.powsybl.cgmes.conformity.test.CgmesConformity1Catalog;
import com.powsybl.cgmes.test.TestGridModel;
import com.powsybl.triplestore.api.PropertyBags;
import com.powsybl.triplestore.api.QueryCatalog;
import com.powsybl.triplestore.api.TripleStoreFactory;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class AlternativeQueriesForGeneratorsTest {

    @BeforeClass
    public static void setUp()  {
        TestGridModel model = new CgmesConformity1Catalog().real();
        Expected expected = new Expected()
                .resultSize(1347)
                .propertyCount("regulatingControlTargetValue", 1347)
                .propertyCount("regulatingControlTerminalConnected", 1347);

        int experiments = 1;
        boolean doAssert = false;
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
    public void usingGraphClauses() {
        testerWorkingWithNestedGraphClauses.test("usingGraphClauses");
    }

    @Test
    public void noGraphClauses() {
        tester.test("noGraphClauses");
    }

    private static AlternativeQueriesTester tester;
    private static AlternativeQueriesTester testerWorkingWithNestedGraphClauses;
}
