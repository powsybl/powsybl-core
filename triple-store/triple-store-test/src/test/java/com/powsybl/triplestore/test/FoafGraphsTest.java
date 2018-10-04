package com.powsybl.triplestore.test;

/*
 * #%L
 * Triple stores for CGMES models
 * %%
 * Copyright (C) 2017 - 2018 RTE (http://rte-france.com)
 * %%
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * #L%
 */

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.BeforeClass;
import org.junit.Test;

import com.powsybl.triplestore.QueryCatalog;
import com.powsybl.triplestore.TripleStoreException;
import com.powsybl.triplestore.TripleStoreFactory;
import com.powsybl.triplestore.test.TripleStoreTester.Expected;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class FoafGraphsTest {

    @BeforeClass
    public static void setUp() throws TripleStoreException {
        Path folder = Paths.get("../../data/triple-store/foaf");
        Path input1 = folder.resolve("abc-nicks.ttl");
        Path input2 = folder.resolve("abc-lastNames.ttl");
        String base = folder.toUri().normalize().toString();
        Path workingDir = folder;
        boolean doAsserts = true;
        tester = new TripleStoreTester(
                doAsserts,
                TripleStoreFactory.allImplementations(),
                base,
                workingDir,
                input1, input2);
        tester.load();
        testerOnlyImplAllowingNestedGraphs = new TripleStoreTester(
                doAsserts,
                TripleStoreFactory.implementationsWorkingWithNestedGraphClauses(),
                base,
                workingDir,
                input1, input2);
        testerOnlyImplBadNestedGraphs = new TripleStoreTester(
                doAsserts,
                TripleStoreFactory.implementationsBadNestedGraphClauses(),
                base,
                workingDir,
                input1, input2);
        tester.load();
        testerOnlyImplAllowingNestedGraphs.load();
        testerOnlyImplBadNestedGraphs.load();
        queries = new QueryCatalog("foaf-graphs.sparql");
        queries.load(ClassLoader.getSystemResourceAsStream(queries.resource()));
    }

    @Test
    public void testLastNames() throws Exception {
        Expected expected = new Expected().expect("lastName", "Channing", "Liddell", "Marley");
        tester.testQuery(queries.get("lastNames"), expected);
    }

    @Test
    public void testLastNamesGraph() throws Exception {
        Expected expected = new Expected()
                .expect("lastName", "Channing", "Liddell", "Marley")
                .expect("graphLastnames",
                        "files:abc-lastNames.ttl",
                        "files:abc-lastNames.ttl",
                        "files:abc-lastNames.ttl")
                .expect("graphPersons",
                        "files:abc-nicks.ttl",
                        "files:abc-nicks.ttl",
                        "files:abc-nicks.ttl");
        tester.testQuery(queries.get("lastNamesGraphs"), expected);
    }

    @Test
    public void testLastNameOnlyIfNick() throws Exception {
        Expected expected = new Expected()
                .expect("lastName", "Channing", "Liddell", null)
                .expect("graphLastnames",
                        "files:abc-lastNames.ttl",
                        "files:abc-lastNames.ttl",
                        null)
                .expect("graphPersons",
                        "files:abc-nicks.ttl",
                        "files:abc-nicks.ttl",
                        "files:abc-nicks.ttl");
        testerOnlyImplAllowingNestedGraphs.testQuery(queries.get("lastNameOnlyIfNick"), expected);
    }

    @Test
    public void testLastNameOnlyIfNickFailsForBlazegraph() throws Exception {
        Expected expected = new Expected()
                .expect("lastName", null, null, null)
                .expect("graphLastnames", null, null, null)
                .expect("graphPersons",
                        "files:abc-nicks.ttl",
                        "files:abc-nicks.ttl",
                        "files:abc-nicks.ttl");
        testerOnlyImplBadNestedGraphs.testQuery(queries.get("lastNameOnlyIfNick"), expected);
    }

    private static TripleStoreTester tester;
    private static TripleStoreTester testerOnlyImplAllowingNestedGraphs;
    private static TripleStoreTester testerOnlyImplBadNestedGraphs;
    private static QueryCatalog      queries;
}
