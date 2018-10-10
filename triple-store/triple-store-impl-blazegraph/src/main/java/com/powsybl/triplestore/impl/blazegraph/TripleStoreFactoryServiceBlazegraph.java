package com.powsybl.triplestore.impl.blazegraph;

import com.google.auto.service.AutoService;
import com.powsybl.triplestore.api.TripleStore;
import com.powsybl.triplestore.api.TripleStoreFactoryService;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
@AutoService(TripleStoreFactoryService.class)
public class TripleStoreFactoryServiceBlazegraph implements TripleStoreFactoryService {

    @Override
    public TripleStore create() {
        return new TripleStoreBlazegraph();
    }

    @Override
    public String implementation() {
        return "blazegraph";
    }

    @Override
    public boolean worksWithNestedGraphClauses() {
        return false;
    }

}
