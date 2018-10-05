package com.powsybl.triplestore.test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystem;
import java.nio.file.Files;

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

import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.triplestore.QueryCatalog;
import com.powsybl.triplestore.TripleStoreException;
import com.powsybl.triplestore.TripleStoreFactory;
import com.powsybl.triplestore.test.TripleStoreTester.Expected;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class FoafGraphsTest {

    private static InputStream resourceStream(String resource) {
        return ClassLoader.getSystemResourceAsStream(resource);
    }

    @BeforeClass
    public static void setUp() throws TripleStoreException, IOException {
        queries = new QueryCatalog("foaf/foaf-graphs.sparql");
        queries.load(resourceStream(queries.resource()));

        try (FileSystem fileSystem = Jimfs.newFileSystem(Configuration.unix())) {
            Path data = fileSystem.getPath("foaf");
            Path folder = Files.createDirectories(data);
            Path input1 = folder.resolve("abc-nicks.ttl");
            Path input2 = folder.resolve("abc-lastNames.ttl");
            String base = folder.toUri().normalize().toString();
            Files.copy(resourceStream("foaf/abc-nicks.ttl"), input1);
            Files.copy(resourceStream("foaf/abc-lastNames.ttl"), input2);

            tester = new TripleStoreTester(
                    TripleStoreFactory.allImplementations(),
                    base,
                    input1, input2);
            testerOnlyImplAllowingNestedGraphs = new TripleStoreTester(
                    TripleStoreFactory.implementationsWorkingWithNestedGraphClauses(),
                    base,
                    input1, input2);
            testerOnlyImplBadNestedGraphs = new TripleStoreTester(
                    TripleStoreFactory.implementationsBadNestedGraphClauses(),
                    base,
                    input1, input2);
            tester.load();
            testerOnlyImplAllowingNestedGraphs.load();
            testerOnlyImplBadNestedGraphs.load();
        }
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
    private static QueryCatalog queries;
}
