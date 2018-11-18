/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.alternatives.test;

import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;

import com.powsybl.cgmes.alternatives.test.AlternativeQueriesTester.Expected;
import com.powsybl.cgmes.conformity.test.CgmesConformity1Catalog;
import com.powsybl.cgmes.model.test.TestGridModel;
import com.powsybl.triplestore.api.QueryCatalog;
import com.powsybl.triplestore.api.TripleStoreFactory;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class AlternativeQueriesForSwitchesTest {

    @BeforeClass
    public static void setUp() {
        TestGridModel model = new CgmesConformity1Catalog().smallNodeBreaker();
        // Expected number of results when querying switches
        // We expected two results for each switch, one result for each side
        Expected expected = new Expected().resultSize(738);
        tester = new AlternativeQueriesTester(
                TripleStoreFactory.allImplementations(),
                new QueryCatalog("switches.sparql"),
                model,
                expected);
        tester.load();
    }

    @Test
    public void optionals() throws IOException {
        tester.test("optionals");
    }

    @Test
    public void subSelectUnion() throws IOException {
        tester.test("subSelectUnion");
    }

    @Test
    public void groupUnion() throws IOException {
        tester.test("groupUnion");
    }

    @Test
    public void assumeOnlyBays() throws IOException {
        tester.test("assumingOnlyBays");
    }

    private static AlternativeQueriesTester tester;
}
