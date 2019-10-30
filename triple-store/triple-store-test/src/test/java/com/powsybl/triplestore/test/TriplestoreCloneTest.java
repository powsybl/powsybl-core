package com.powsybl.triplestore.test;

import org.junit.Test;

import com.powsybl.triplestore.api.QueryCatalog;
import com.powsybl.triplestore.api.TripleStoreFactory;
import com.powsybl.triplestore.test.TripleStoreTester.Expected;

public class TriplestoreCloneTest {

    @Test
    public void testCgmesRtcsClone() {
        String base = "foo:cgmes-rtcs";
        String[] inputs = {"cgmes-rtcs/rtc-EQ.xml", "cgmes-rtcs/rtc-SSH.xml" };
        tester = new TripleStoreTester(
            TripleStoreFactory.allImplementations(), base, inputs);
        tester.load();
        tester.testPerformanceCloneByStatements();
    }

    //@Test
    public void testUpdateClone() {
        String base = "foo:foaf";
        String[] inputs = {"foaf/abc-nicks.ttl", "foaf/abc-lastNames.ttl" };
        QueryCatalog queries = new QueryCatalog("foaf/foaf-graphs-update.sparql");
        tester = new TripleStoreTester(
            TripleStoreFactory.allImplementations(), base, inputs);
        tester.load();
        tester.loadClone();
        Expected expectedBeforeUpdate = new Expected().expect("nick", "SweetCaroline", "Wonderland");
        tester.testQuery(queries.get("selectNickName"), expectedBeforeUpdate);
        tester.testQueryClone(queries.get("selectNickName"), expectedBeforeUpdate);
//        tester.testUpdateClone(queries.get("insertNickName"));
        Expected expectedAfterUpdate = new Expected().expect("nick", "BG", "SweetCaroline", "Wonderland");
        tester.testQuery(queries.get("selectNickName"), expectedBeforeUpdate);
        tester.testQueryClone(queries.get("selectNickName"), expectedAfterUpdate);
    }

    //@Test
    public void testNationalGridCloneByStatements() {
        String base = "foo:cgmes-national-grid";
        String[] inputs = {"cgmes-national-grid/20190312T2330Z_1D_NG_SSH_001.xml",
            "cgmes-national-grid/20190312T2330Z_1D_NG_SV_001.xml",
            "cgmes-national-grid/20190312T2330Z_1D_NG_TP_001.xml",
            "cgmes-national-grid/20190312T2330Z_NG_EQ_001.xml" };
        tester = new TripleStoreTester(
            TripleStoreFactory.allImplementations(), base, inputs);
        tester.load();
        tester.testPerformanceCloneByStatements();
    }

    // @Test
    public void testNationalGridImportFiles() {
        String base = "foo:cgmes-national-grid";
        String[] inputs = {"cgmes-national-grid/20190312T2330Z_1D_NG_SSH_001.xml",
            "cgmes-national-grid/20190312T2330Z_1D_NG_SV_001.xml",
            "cgmes-national-grid/20190312T2330Z_1D_NG_TP_001.xml",
            "cgmes-national-grid/20190312T2330Z_NG_EQ_001.xml" };
        tester = new TripleStoreTester(
            TripleStoreFactory.allImplementations(), base, inputs);
        tester.testPerformanceImportFiles();
    }

    private static TripleStoreTester tester;
}
