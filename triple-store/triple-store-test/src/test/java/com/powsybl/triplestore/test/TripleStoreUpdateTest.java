package com.powsybl.triplestore.test;

import org.junit.Before;
import org.junit.Test;

import com.powsybl.triplestore.api.QueryCatalog;
import com.powsybl.triplestore.api.TripleStoreException;
import com.powsybl.triplestore.api.TripleStoreFactory;
import com.powsybl.triplestore.test.TripleStoreTester.Expected;

public class TripleStoreUpdateTest {

    @Before
    public void setUp() {
        queries = new QueryCatalog("foaf/foaf.sparql");
        String base = "foo:foaf";
        String[] inputs = {"foaf/abc-nicks.ttl", "foaf/abc-lastNames.ttl" };
        tester = new TripleStoreTester(
            TripleStoreFactory.allImplementations(), base, inputs);
        tester.load();
        tester.createCopies();
    }

    @Test
    public void testInsert() {
        Expected expectedContents = new Expected().expect("nick", "SweetCaroline", "Wonderland");
        tester.testQuery(queries.get("selectNickName"), expectedContents);
        tester.testUpdate(queries.get("insertNickName"));
        Expected expectedContentsUpdated = new Expected().expect("nick", "BG", "SweetCaroline", "Wonderland");
        tester.testQuery(queries.get("selectNickName"), expectedContentsUpdated);
    }

    @Test
    public void testDelete() {
        Expected expectedContents = new Expected().expect("lastName", "Channing", "Liddell", "Marley");
        tester.testQuery(queries.get("selectLastName"), expectedContents);
        tester.testUpdate(queries.get("deleteLastName"));
        Expected expectedUpdated = new Expected().expect("lastName", "Liddell", "Marley");
        tester.testQuery(queries.get("selectLastName"), expectedUpdated);
    }

    @Test
    public void testUpdate() {
        Expected expectedContents = new Expected()
            .expect("lastName", "Marley")
            .expect("person", "http://example/bob");
        tester.testQuery(queries.get("selectLastNameForUpdate"), expectedContents);
        tester.testUpdate(queries.get("updateLastName"));
        Expected expectedUpdated = new Expected()
            .expect("lastName", "Grebenshchikov")
            .expect("person", "http://example/bob");
        tester.testQuery(queries.get("selectLastNameForUpdate"), expectedUpdated);
    }

    @Test
    public void testUpdateTwoGraphs() {
        Expected expectedContents = new Expected()
            .expect("lastName", "Channing", "Liddell", "Marley")
            .expect("person",
                "http://example/alice",
                "http://example/bob",
                "http://example/carol")
            .expect("mbox",
                "mailto:alice@example",
                "mailto:bob@example",
                "mailto:carol@example");
        tester.testQuery(queries.get("selectLastNameTwoGraphs"), expectedContents);
        tester.testUpdate(queries.get("updatePersonTwoGraphs"));
        Expected expectedUpdated = new Expected().expect("lastName", "Channing", "Marley", "Walker")
            .expect("person",
                "http://example/alice",
                "http://example/bob",
                "http://example/carol")
            .expect("mbox",
                "mailto:aliceNowWalker@example",
                "mailto:bob@example",
                "mailto:carol@example");
        tester.testQuery(queries.get("selectLastNameTwoGraphs"), expectedUpdated);
    }

    @Test
    public void testUpdateCopyNotOrigin() {
        Expected expectedContents = new Expected().expect("nick", "SweetCaroline", "Wonderland");
        tester.testQuery(queries.get("selectNickName"), expectedContents);
        tester.testQueryOnCopies(queries.get("selectNickName"), expectedContents);
        tester.testUpdateOnCopies(queries.get("insertNickName"));
        Expected expectedContentsUpdated = new Expected().expect("nick", "BG", "SweetCaroline", "Wonderland");
        tester.testQuery(queries.get("selectNickName"), expectedContents);
        tester.testQueryOnCopies(queries.get("selectNickName"), expectedContentsUpdated);
    }

    @Test(expected = TripleStoreException.class)
    public void testMalformedQuery() {
        tester.testUpdateOnCopies(queries.get("deleteLastNameMalformed"));
    }

    private static TripleStoreTester tester;
    private static QueryCatalog queries;
}
