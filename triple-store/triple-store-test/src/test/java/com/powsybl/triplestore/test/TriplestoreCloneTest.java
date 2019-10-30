package com.powsybl.triplestore.test;

import org.junit.Test;

import com.powsybl.triplestore.api.QueryCatalog;
import com.powsybl.triplestore.api.TripleStoreFactory;
import com.powsybl.triplestore.test.TripleStoreTester.Expected;

public class TriplestoreCloneTest {

    @Test
    public void testCloneUndependent() {
        String base = "foo:foaf";
        String[] inputs = {"foaf/abc-nicks.ttl", "foaf/abc-lastNames.ttl" };
        QueryCatalog queries = new QueryCatalog("foaf/foaf-graphs-update.sparql");
        tester = new TripleStoreTester(
            TripleStoreFactory.allImplementations(), base, inputs);
        tester.load();
        tester.loadClone();
        Expected expectedBeforeClean = new Expected().expect("nick", "SweetCaroline", "Wonderland");
        tester.testQuery(queries.get("selectNickName"), expectedBeforeClean);
        tester.testQueryClone(queries.get("selectNickName"), expectedBeforeClean);
        tester.testClear("contexts:foaf/abc-nicks.ttl", "");
        tester.testQueryClone(queries.get("selectNickName"), expectedBeforeClean);
    }

    private static TripleStoreTester tester;
}
