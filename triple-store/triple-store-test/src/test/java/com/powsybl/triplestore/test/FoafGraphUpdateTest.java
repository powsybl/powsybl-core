package com.powsybl.triplestore.test;

import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.powsybl.triplestore.api.QueryCatalog;
import com.powsybl.triplestore.api.TripleStoreException;
import com.powsybl.triplestore.api.TripleStoreFactory;
import com.powsybl.triplestore.test.TripleStoreTester.Expected;

public class FoafGraphUpdateTest {

    private static String base = "foo:foaf";
    private static String[] inputs = {"foaf/abc-nicks.ttl", "foaf/abc-lastNames.ttl" };

    @BeforeClass
    public static void setUp() {
        queries = new QueryCatalog("foaf/foaf-graphs-update.sparql");
    }

    @Test
    public void testInsertNickName() {
        tester = new TripleStoreTester(
            TripleStoreFactory.allImplementations(), base, inputs);
        tester.load();
        Expected expected = new Expected().expect("nick", "SweetCaroline", "Wonderland");
        tester.testQuery(queries.get("selectNickName"), expected);
        tester.testUpdate(queries.get("insertNickName"));
        Expected expectedUpdated = new Expected().expect("nick", "BG", "SweetCaroline", "Wonderland");
        tester.testQuery(queries.get("selectNickName"), expectedUpdated);
    }

    @Test
    public void testDeletetLastName() {
        tester = new TripleStoreTester(
            TripleStoreFactory.onlyDefaultImplementation(), base, inputs);
        tester.load();
        Expected expected = new Expected().expect("lastName", "Channing", "Liddell", "Marley");
        tester.testQuery(queries.get("selectLastName"), expected);
        tester.testUpdate(queries.get("deleteLastName"));
        Expected expectedUpdated = new Expected().expect("lastName", "Liddell", "Marley");
        tester.testQuery(queries.get("selectLastName"), expectedUpdated);
    }

    @Test
    public void testUpdateLastNameGraph() {
        tester = new TripleStoreTester(
            TripleStoreFactory.allImplementations(), base, inputs);
        tester.load();

        LOG.info("Test delete and insert into the same graph....");

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
        tester.testQuery(queries.get("selectLastNameGraphs"), expected);
        tester.testUpdate(queries.get("updateLastNameGraph"));
        Expected expectedUpdated = new Expected()
            .expect("lastName", "Channing", "Grebenshchikov", "Liddell")
            .expect("graphLastnames",
                "contexts:foaf/abc-lastNames.ttl",
                "contexts:foaf/abc-lastNames.ttl",
                "contexts:foaf/abc-lastNames.ttl")
            .expect("graphPersons",
                "contexts:foaf/abc-nicks.ttl",
                "contexts:foaf/abc-nicks.ttl",
                "contexts:foaf/abc-nicks.ttl");
        tester.testQuery(queries.get("selectLastNameGraphs"), expectedUpdated);
    }

    @Test
    public void testUpdatePersonTwoGraphs() {
        tester = new TripleStoreTester(
            TripleStoreFactory.allImplementations(), base, inputs);
        tester.load();

        LOG.info("Test delete and insert in two graphs....");

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
        tester.testQuery(queries.get("selectLastNameGraphs"), expected);
        tester.testUpdate(queries.get("updatePersonTwoGraphs"));
        Expected expected1 = new Expected().expect("lastName", "Channing", "Cooper", "Marley");
        tester.testQuery(queries.get("selectLastNameGraphs"), expected1);
    }


    @Test(expected = TripleStoreException.class)
    public void testMalformedQuery() {
        tester = new TripleStoreTester(
            TripleStoreFactory.allImplementations(), base, inputs);
        tester.load();
        tester.testUpdate(queries.get("deleteLastNameMalformed"));
    }

    private static TripleStoreTester tester;
    private static QueryCatalog queries;
    private static final Logger LOG = LoggerFactory.getLogger(FoafGraphUpdateTest.class);

}
