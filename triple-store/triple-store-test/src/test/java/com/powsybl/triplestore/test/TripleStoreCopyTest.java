package com.powsybl.triplestore.test;

import org.junit.Test;

import com.powsybl.triplestore.api.QueryCatalog;
import com.powsybl.triplestore.api.TripleStoreFactory;
import com.powsybl.triplestore.test.TripleStoreTester.Expected;

public class TripleStoreCopyTest {

    @Test
    public void testCopySourceEmptyTargetContents() {
        String base = "foo:foaf";
        String[] inputs = {"foaf/abc-nicks.ttl", "foaf/abc-lastNames.ttl"};
        QueryCatalog queries = new QueryCatalog("foaf/foaf.sparql");
        tester = new TripleStoreTester(TripleStoreFactory.allImplementations(), base, inputs);
        tester.load();
        tester.createCopies();

        Expected expectedContents = new Expected().expect("nick", "SweetCaroline", "Wonderland");
        tester.testQuery(queries.get("selectNickName"), expectedContents);
        tester.testQueryOnCopies(queries.get("selectNickName"), expectedContents);

        tester.clear("contexts:foaf/abc-nicks.ttl", "");
        Expected expectedEmpty = new Expected();
        tester.testQuery(queries.get("selectNickName"), expectedEmpty);
        tester.testQueryOnCopies(queries.get("selectNickName"), expectedContents);
    }

    private static TripleStoreTester tester;
}
