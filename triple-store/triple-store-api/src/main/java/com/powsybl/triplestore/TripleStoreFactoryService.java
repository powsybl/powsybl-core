package com.powsybl.triplestore;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public interface TripleStoreFactoryService {

    TripleStore create();

    String implementation();

    boolean worksWithNestedGraphClauses();

}
