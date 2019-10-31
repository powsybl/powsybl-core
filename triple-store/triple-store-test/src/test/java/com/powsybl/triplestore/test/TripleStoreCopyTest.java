package com.powsybl.triplestore.test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.powsybl.triplestore.api.QueryCatalog;
import com.powsybl.triplestore.api.TripleStore;
import com.powsybl.triplestore.api.TripleStoreFactory;
import com.powsybl.triplestore.test.TripleStoreTester.Expected;

public class TripleStoreCopyTest {

    @Test
    public void testCopy() {
        String base = "foo:foaf";
        String[] inputs = {"foaf/abc-nicks.ttl", "foaf/abc-lastNames.ttl"};
        QueryCatalog queries = new QueryCatalog("foaf/foaf.sparql");
        TripleStoreTester tester = new TripleStoreTester(TripleStoreFactory.allImplementations(), base, inputs);

        tester.load();
        tester.createCopies();

        TripleStoreFactory.allImplementations().forEach(impl -> {
            TripleStore expected = tester.tripleStore(impl);
            TripleStore actual = tester.tripleStoreCopy(impl);
            assertEquals(expected.getNamespaces(), actual.getNamespaces());
        });

        Expected expectedContents = new Expected().expect("nick", "SweetCaroline", "Wonderland");
        tester.testQuery(queries.get("selectNickName"), expectedContents);
        tester.testQueryOnCopies(queries.get("selectNickName"), expectedContents);

        tester.clear("contexts:foaf/abc-nicks.ttl", "");
        Expected expectedEmpty = new Expected();
        tester.testQuery(queries.get("selectNickName"), expectedEmpty);
        tester.testQueryOnCopies(queries.get("selectNickName"), expectedContents);
    }
}
