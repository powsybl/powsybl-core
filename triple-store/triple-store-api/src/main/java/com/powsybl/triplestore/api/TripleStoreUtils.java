/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.triplestore.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Miora Vedelago {@literal <miora.ralambotiana at rte-france.com>}
 */
public final class TripleStoreUtils {

    private static final Logger LOG = LoggerFactory.getLogger(TripleStoreUtils.class);

    public static PropertyBags queryTripleStore(String queryKey, QueryCatalog queryCatalog, TripleStore tripleStore) {
        LOG.trace("Querying triplestore for {}", queryKey);
        String query = queryCatalog.get(queryKey);
        if (query == null) {
            LOG.warn("Query [{}] not found in catalog", queryKey);
            return new PropertyBags();
        }
        return tripleStore.query(query);
    }

    private TripleStoreUtils() {
    }
}
