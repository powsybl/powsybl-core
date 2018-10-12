/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.triplestore.test;

import org.junit.BeforeClass;
import org.junit.Test;

import com.powsybl.triplestore.api.QueryCatalog;
import com.powsybl.triplestore.api.TripleStoreFactory;
import com.powsybl.triplestore.test.TripleStoreTester.Expected;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class FoafGraphsTest {

    @BeforeClass
    public static void setUp() {
        queries = new QueryCatalog("foaf/foaf-graphs.sparql");
        String base = "foo:foaf";
        String[] inputs = {"foaf/abc-nicks.ttl", "foaf/abc-lastNames.ttl"};
        tester = new TripleStoreTester(
                TripleStoreFactory.allImplementations(), base, inputs);
        testerOnlyImplAllowingNestedGraphs = new TripleStoreTester(
                TripleStoreFactory.implementationsWorkingWithNestedGraphClauses(), base, inputs);
        testerOnlyImplBadNestedGraphs = new TripleStoreTester(
                TripleStoreFactory.implementationsBadNestedGraphClauses(), base, inputs);
        tester.load();
        testerOnlyImplAllowingNestedGraphs.load();
        testerOnlyImplBadNestedGraphs.load();
    }

    @Test
    public void testLastNames() {
        Expected expected = new Expected().expect("lastName", "Channing", "Liddell", "Marley");
        tester.testQuery(queries.get("lastNames"), expected);
    }

    @Test
    public void testLastNamesGraph() {
        Expected expected = new Expected()
                .expect("lastName", "Channing", "Liddell", "Marley")
                .expect("graphLastnames",
                        "contexts:foaf/abc-lastNames.ttl",
                        "contexts:foaf/abc-lastNames.ttl",
                        "contexts:foaf/abc-lastNames.ttl")
                .expect("graphPersons",
                        "contexts:foaf/abc-nicks.ttl",
                        "contexts:foaf/abc-nicks.ttl",
                        "contexts:foaf/abc-nicks.ttl");
        tester.testQuery(queries.get("lastNamesGraphs"), expected);
    }

    @Test
    public void testLastNameOnlyIfNick() {
        Expected expected = new Expected()
                .expect("lastName", "Channing", "Liddell", null)
                .expect("graphLastnames",
                        "contexts:foaf/abc-lastNames.ttl",
                        "contexts:foaf/abc-lastNames.ttl",
                        null)
                .expect("graphPersons",
                        "contexts:foaf/abc-nicks.ttl",
                        "contexts:foaf/abc-nicks.ttl",
                        "contexts:foaf/abc-nicks.ttl");
        testerOnlyImplAllowingNestedGraphs.testQuery(queries.get("lastNameOnlyIfNick"), expected);
    }

    @Test
    public void testLastNameOnlyIfNickFailsForBlazegraph() {
        Expected expected = new Expected()
                .expect("lastName", null, null, null)
                .expect("graphLastnames", null, null, null)
                .expect("graphPersons",
                        "contexts:foaf/abc-nicks.ttl",
                        "contexts:foaf/abc-nicks.ttl",
                        "contexts:foaf/abc-nicks.ttl");
        testerOnlyImplBadNestedGraphs.testQuery(queries.get("lastNameOnlyIfNick"), expected);
    }

    private static TripleStoreTester tester;
    private static TripleStoreTester testerOnlyImplAllowingNestedGraphs;
    private static TripleStoreTester testerOnlyImplBadNestedGraphs;
    private static QueryCatalog queries;
}
