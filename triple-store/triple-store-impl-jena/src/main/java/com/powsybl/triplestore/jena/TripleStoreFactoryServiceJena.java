package com.powsybl.triplestore.jena;

import com.google.auto.service.AutoService;
import com.powsybl.triplestore.TripleStore;
import com.powsybl.triplestore.TripleStoreFactoryService;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
@AutoService(TripleStoreFactoryService.class)
public class TripleStoreFactoryServiceJena implements TripleStoreFactoryService {

    @Override
    public TripleStore create() {
        return new TripleStoreJena();
    }

    @Override
    public String implementation() {
        return "jena";
    }

    @Override
    public boolean worksWithNestedGraphClauses() {
        return true;
    }

}
