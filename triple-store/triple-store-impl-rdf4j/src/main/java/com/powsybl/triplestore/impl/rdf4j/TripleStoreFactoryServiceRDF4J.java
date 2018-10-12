/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.triplestore.impl.rdf4j;

import com.google.auto.service.AutoService;
import com.powsybl.triplestore.api.TripleStore;
import com.powsybl.triplestore.api.TripleStoreFactoryService;

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
    public String getImplementationName() {
        return "rdf4j";
    }

    @Override
    public boolean isWorkingWithNestedGraphClauses() {
        return true;
    }

}
