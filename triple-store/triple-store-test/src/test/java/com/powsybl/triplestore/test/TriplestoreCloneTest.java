package com.powsybl.triplestore.test;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.powsybl.triplestore.api.QueryCatalog;
import com.powsybl.triplestore.api.TripleStoreFactory;

public class TriplestoreCloneTest {

    @Test
    public void testCloneStatementsCgmesRtcs() {
        String base = "foo:cgmes-rtcs";
        String[] inputs = { "cgmes-rtcs/rtc-EQ.xml", "cgmes-rtcs/rtc-SSH.xml" };
        QueryCatalog queries = new QueryCatalog("cgmes-national-grid/cgmes-clone-update.sparql");
        tester = new TripleStoreTester(
            TripleStoreFactory.allImplementations(), base, inputs);
        tester.load();
        tester.testClone();

    }

    @Test
    public void testCloneStatementsNationalGrid() {
        String base = "foo:cgmes-national-grid";
        String[] inputs = { "cgmes-national-grid/20190312T2330Z_1D_NG_SSH_001.xml",
            "cgmes-national-grid/20190312T2330Z_1D_NG_SV_001.xml",
            "cgmes-national-grid/20190312T2330Z_1D_NG_TP_001.xml",
            "cgmes-national-grid/20190312T2330Z_NG_EQ_001.xml" };
        tester = new TripleStoreTester(
            TripleStoreFactory.allImplementations(), base, inputs);
        tester.load();
        tester.testClone();
    }

    private static TripleStoreTester tester;
    private static final Logger LOGGER = LoggerFactory.getLogger(FoafGraphUpdateTest.class);

}
