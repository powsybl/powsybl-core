/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.triplestore.test;

import com.powsybl.triplestore.api.QueryCatalog;
import com.powsybl.triplestore.api.TripleStoreFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
class TripleStoreSubqueryLimit1Test {

    private static final TripleStoreTester.Expected EXPECTED_MIN_ID_PROJECTS = new TripleStoreTester.Expected()
            .expect("name", "Alice", "Bob")
            .expect("projectName", "Personal finances", "Industrial plant control");
    private static final TripleStoreTester.Expected EXPECTED_FIRST_PROJECTS = new TripleStoreTester.Expected()
            .expect("name", "Alice", "Bob")
            .expect("projectName", "Railway simulator", "SCADA system");
    private static final TripleStoreTester.Expected EXPECTED_WRONG_FIRST_PROJECTS = new TripleStoreTester.Expected()
            .expect("name", "Alice", "Bob")
            .expect("projectName", "Railway simulator", "Railway simulator");
    private static final TripleStoreTester.Expected EXPECTED_ALL_PROJECTS = new TripleStoreTester.Expected()
            .expect("name", "Alice", "Alice", "Bob", "Bob")
            .expect("projectName", "Railway simulator", "Personal finances", "SCADA system", "Industrial plant control");

    @BeforeEach
    void setUp() {
        String base = "foo:foaf";
        String[] inputs = {"foaf/abc-multiple-projects.ttl"};
        tester = new TripleStoreTester(
                TripleStoreFactory.allImplementations(), base, inputs);
        tester.load();
        queries = new QueryCatalog("foaf/foaf.sparql");
    }

    @Test
    void testAllProjects() {
        tester.testQuery(queries.get("projects"), EXPECTED_ALL_PROJECTS);
    }

    @Test
    void testMinProjectRightQuery() {
        tester.testQuery(queries.get("minProjectRightQuery"), EXPECTED_MIN_ID_PROJECTS);
    }

    @Test
    void testFirstProjectBadQueryButReturnsRightResults() {
        tester.testQuery(queries.get("firstProjectBadQueryButReturnsRightResults"), EXPECTED_FIRST_PROJECTS);
    }

    @Test
    void testFirstProjectBadQueryAndReturnsWrongResults() {
        tester.testQuery(queries.get("firstProjectBadQueryAndReturnsWrongResults"), EXPECTED_WRONG_FIRST_PROJECTS);
    }

    @Test
    void testFirstProjectBadQueryNoOptionalAndReturnsWrongResults() {
        tester.testQuery(queries.get("firstProjectBadQueryNoOptionalAndReturnsWrongResults"), EXPECTED_WRONG_FIRST_PROJECTS);
    }

    private static QueryCatalog queries;
    private TripleStoreTester tester;
}
