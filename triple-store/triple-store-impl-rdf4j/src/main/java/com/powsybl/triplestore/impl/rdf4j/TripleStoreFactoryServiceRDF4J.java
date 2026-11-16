/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.triplestore.impl.rdf4j;

import com.google.auto.service.AutoService;
import com.powsybl.triplestore.api.TripleStore;
import com.powsybl.triplestore.api.TripleStoreFactoryService;
import com.powsybl.triplestore.api.TripleStoreOptions;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
@AutoService(TripleStoreFactoryService.class)
public class TripleStoreFactoryServiceRDF4J implements TripleStoreFactoryService {

    @Override
    public TripleStore create() {
        return new TripleStoreRDF4J();
    }

    @Override
    public TripleStore create(TripleStoreOptions options) {
        return new TripleStoreRDF4J(options);
    }

    @Override
    public TripleStore copy(TripleStore source) {
        TripleStore ts = new TripleStoreRDF4J(source.getOptions());
        ts.add(source);
        return ts;
    }

    @Override
    public String getImplementationName() {
        return TripleStoreRDF4J.NAME;
    }

    @Override
    public boolean isWorkingWithNestedGraphClauses() {
        return true;
    }

}
