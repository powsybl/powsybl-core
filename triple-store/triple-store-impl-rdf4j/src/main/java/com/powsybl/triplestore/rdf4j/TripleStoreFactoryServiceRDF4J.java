package com.powsybl.triplestore.rdf4j;

import com.google.auto.service.AutoService;
import com.powsybl.triplestore.TripleStore;
import com.powsybl.triplestore.TripleStoreFactoryService;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
@AutoService(TripleStoreFactoryService.class)
public class TripleStoreFactoryServiceRDF4J implements TripleStoreFactoryService {

    @Override
    public TripleStore create() {
        return new TripleStoreRDF4J();
    }

    @Override
    public String implementation() {
        return "rdf4j";
    }

    @Override
    public boolean worksWithNestedGraphClauses() {
        return true;
    }

}
