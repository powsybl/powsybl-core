/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.triplestore.test;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.powsybl.triplestore.api.QueryCatalog;
import com.powsybl.triplestore.api.TripleStoreFactory;
import com.powsybl.triplestore.test.TripleStoreTester.Expected;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
class FoafOptionalsTest {

    @BeforeAll
    static void setUp() {
        queries = new QueryCatalog("foaf/foaf-optionals.sparql");
        String base = "foaf";
        tester = new TripleStoreTester(TripleStoreFactory.allImplementations(), base, "foaf/abc-nicks.ttl");
        tester.load();
    }

    @Test
    void testAllQuads() {
        Expected expected = new Expected().expect(
                "o",
                "Alice",
                "Bob",
                "SweetCaroline",
                "Wonderland",
                "mailto:alice@example",
                "mailto:bob@example",
                "mailto:carol@example");
        tester.testQuery("SELECT * { GRAPH ?g { ?s ?p ?o }}", expected);
    }

    @Test
    void testOptional() {
        Expected expected = new Expected().expect("name", "Alice", "Bob", null);
        tester.testQuery(queries.get("optional"), expected);
    }

    @Test
    void testMultipleOptionals() {
        Expected expected = new Expected()
                .expect("name", "Alice", "Bob", null)
                .expect("nick", "SweetCaroline", "Wonderland", null);
        tester.testQuery(queries.get("multipleOptionals"), expected);
    }

    @Test
    void testMultipleOptionalsSameVariable() {
        Expected expected = new Expected().expect("label", "Alice", "Bob", "SweetCaroline");
        tester.testQuery(queries.get("multipleOptionalsSameVariable"), expected);
    }

    @Test
    void testOptionalWithUnion() {
        Expected expected = new Expected()
                .expect("label", "Alice", "Bob", "SweetCaroline", "Wonderland");
        tester.testQuery(queries.get("optionalWithUnion"), expected);
    }

    @Test
    void testNestedOptionals() {
        Expected expected = new Expected()
                .expect("name", "Alice", null, null)
                .expect("nick", "SweetCaroline", "Wonderland", null);
        tester.testQuery(queries.get("nestedOptionals"), expected);
    }

    @Test
    void testOptionalAnd() {
        Expected expected = new Expected()
                .expect("name", "Alice", null, null)
                .expect("nick", "Wonderland", null, null);
        tester.testQuery(queries.get("optionalAnd"), expected);
    }

    @Test
    void testOptionalNestedFilter() {
        Expected expected = new Expected().expect("name", "Bob", null, null);
        tester.testQuery(queries.get("optionalNestedFilter"), expected);
    }

    @Test
    void testOptionalThenFilter() {
        Expected expected = new Expected().expect("name", "Bob");
        tester.testQuery(queries.get("optionalThenFilter"), expected);
    }

    @Test
    void testOptionalNotBound() {
        Expected expected = new Expected().expect("mbox", "mailto:carol@example");
        tester.testQuery(queries.get("optionalNotBound"), expected);
    }

    @Test
    void testFilterNotExists() {
        // Equivalent to optional not bound, the syntax is allowed since SPARQL 1.1
        Expected expected = new Expected().expect("mbox", "mailto:carol@example");
        tester.testQuery(queries.get("filterNotExists"), expected);
    }

    @Test
    void testMinus() {
        // Similar to filter not exists, subtract friends with name from friends with
        // mailbox
        Expected expected = new Expected().expect("mbox", "mailto:carol@example");
        tester.testQuery(queries.get("minus"), expected);
    }

    private static TripleStoreTester tester;
    private static QueryCatalog queries;
}
