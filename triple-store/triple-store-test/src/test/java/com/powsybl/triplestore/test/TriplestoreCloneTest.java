package com.powsybl.triplestore.test;

import org.junit.Test;

import com.powsybl.triplestore.api.TripleStoreFactory;

public class TriplestoreCloneTest {

    @Test
    public void testCgmesRtcsClone() {
        String base = "foo:cgmes-rtcs";
        String[] inputs = {"cgmes-rtcs/rtc-EQ.xml", "cgmes-rtcs/rtc-SSH.xml" };
        tester = new TripleStoreTester(
            TripleStoreFactory.allImplementations(), base, inputs);
        tester.load();
        tester.testPerformanceCloneRepo();
    }
    
  //@Test
  public void testNationalGridCloneByRepo() {
      String base = "foo:cgmes-national-grid";
      String[] inputs = { "cgmes-national-grid/20190312T2330Z_1D_NG_SSH_001.xml",
          "cgmes-national-grid/20190312T2330Z_1D_NG_SV_001.xml",
          "cgmes-national-grid/20190312T2330Z_1D_NG_TP_001.xml",
          "cgmes-national-grid/20190312T2330Z_NG_EQ_001.xml" };
      tester = new TripleStoreTester(
          TripleStoreFactory.allImplementations(), base, inputs);
      tester.load();
      tester.testPerformanceCloneRepo();
  }

    //@Test
    public void testNationalGridCloneByStatements() {
        String base = "foo:cgmes-national-grid";
        String[] inputs = { "cgmes-national-grid/20190312T2330Z_1D_NG_SSH_001.xml",
            "cgmes-national-grid/20190312T2330Z_1D_NG_SV_001.xml",
            "cgmes-national-grid/20190312T2330Z_1D_NG_TP_001.xml",
            "cgmes-national-grid/20190312T2330Z_NG_EQ_001.xml" };
        tester = new TripleStoreTester(
            TripleStoreFactory.allImplementations(), base, inputs);
        tester.load();
        tester.testPerformanceCloneByStatements();
    }

    //@Test
    public void testNationalGridImportFiles() {
        String base = "foo:cgmes-national-grid";
        String[] inputs = { "cgmes-national-grid/20190312T2330Z_1D_NG_SSH_001.xml",
            "cgmes-national-grid/20190312T2330Z_1D_NG_SV_001.xml",
            "cgmes-national-grid/20190312T2330Z_1D_NG_TP_001.xml",
            "cgmes-national-grid/20190312T2330Z_NG_EQ_001.xml" };
        tester = new TripleStoreTester(
            TripleStoreFactory.allImplementations(), base, inputs);
        tester.testPerformanceImportFiles();
    }

    private static TripleStoreTester tester;
}
