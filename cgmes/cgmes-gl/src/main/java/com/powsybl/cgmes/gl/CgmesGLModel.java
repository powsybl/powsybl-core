/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.gl;

import com.powsybl.cgmes.model.CgmesNamespace;
import com.powsybl.triplestore.api.PropertyBags;
import com.powsybl.triplestore.api.QueryCatalog;
import com.powsybl.triplestore.api.TripleStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 *
 * @author Massimo Ferraro {@literal <massimo.ferraro@techrain.eu>}
 */
public class CgmesGLModel {

    public static final String SUBSTATION_VL_POSITION_QUERY_KEY = "substationVoltageLevelPosition";
    public static final String LINE_POSITION_QUERY_KEY = "linePosition";

    private static final Logger LOG = LoggerFactory.getLogger(CgmesGLModel.class);

    private final TripleStore tripleStore;
    private final QueryCatalog queryCatalog;

    public CgmesGLModel(TripleStore tripleStore) {
        this(tripleStore, new QueryCatalog("CGMES-GL.sparql"));
    }

    public CgmesGLModel(TripleStore tripleStore, QueryCatalog queryCatalog) {
        this.tripleStore = Objects.requireNonNull(tripleStore);
        tripleStore.defineQueryPrefix("cim", CgmesNamespace.CIM_16_NAMESPACE);
        this.queryCatalog = Objects.requireNonNull(queryCatalog);
    }

    private PropertyBags queryTripleStore(String queryKey) {
        String query = queryCatalog.get(queryKey);
        if (query == null) {
            LOG.warn("Query [{}] not found in catalog", queryKey);
            return new PropertyBags();
        }
        return tripleStore.query(query);
    }

    public PropertyBags getSubstationVoltageLevelPosition() {
        LOG.info("Querying triple store for substation and voltage level positions");
        return queryTripleStore(SUBSTATION_VL_POSITION_QUERY_KEY);
    }

    public PropertyBags getLinesPositions() {
        LOG.info("Querying triple store for lines positions");
        return queryTripleStore(LINE_POSITION_QUERY_KEY);
    }

}
