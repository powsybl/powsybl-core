/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.measurements;

import com.powsybl.triplestore.api.PropertyBags;
import com.powsybl.triplestore.api.QueryCatalog;
import com.powsybl.triplestore.api.TripleStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
public class CgmesMeasurementsModel {

    private static final Logger LOG = LoggerFactory.getLogger(CgmesMeasurementsModel.class);

    private final TripleStore tripleStore;
    private final QueryCatalog queryCatalog = new QueryCatalog("CGMES-Meas.sparql");

    public CgmesMeasurementsModel(TripleStore tripleStore) {
        this.tripleStore = Objects.requireNonNull(tripleStore);
    }

    public PropertyBags analogs() {
        LOG.info("Querying triplestore for Analogs");
        return queryTripleStore("analogs");
    }

    public PropertyBags bays() {
        LOG.info("Querying triplestore for Bays");
        return queryTripleStore("bays");
    }

    public PropertyBags discretes() {
        LOG.info("Querying triplestore for Discretes");
        return queryTripleStore("discretes");
    }

    private PropertyBags queryTripleStore(String queryKey) {
        String query = queryCatalog.get(queryKey);
        if (query == null) {
            LOG.warn("Query [{}] not found in catalog", queryKey);
            return new PropertyBags();
        }
        return tripleStore.query(query);
    }
}
