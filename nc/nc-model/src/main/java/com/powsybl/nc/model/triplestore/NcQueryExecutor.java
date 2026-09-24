/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.nc.model.triplestore;

import com.powsybl.nc.model.NcException;
import com.powsybl.nc.model.io.NcConstants;
import com.powsybl.nc.model.io.NcQueryContext;
import com.powsybl.triplestore.api.PropertyBags;
import com.powsybl.triplestore.api.QueryCatalog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;

final class NcQueryExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(NcQueryExecutor.class);

    private final NcDatasetTripleStore dataset;
    private final QueryCatalog ncQueryCatalog;

    NcQueryExecutor(NcDatasetTripleStore dataset) {
        this.dataset = dataset;
        this.ncQueryCatalog = new QueryCatalog(NcConstants.SPARQL_FILE_NC_PROFILE);
    }

    PropertyBags query(List<String> queryKeys, Set<String> contexts) {
        PropertyBags result = new PropertyBags();
        queryKeys.forEach(queryKey -> result.addAll(query(queryKey, contexts)));
        return result;
    }

    PropertyBags query(String queryKey, Set<String> contexts) {
        dataset.checkOpen();
        if (contexts.isEmpty()) {
            return execute(ncQueryCatalog, queryKey, null);
        }
        PropertyBags result = new PropertyBags();
        contexts.forEach(context -> result.addAll(execute(ncQueryCatalog, queryKey, context)));
        return result;
    }

    PropertyBags queryExtension(Set<String> contexts, String contextQueryTemplate) {
        dataset.checkOpen();
        if (!contextQueryTemplate.contains(NcQueryContext.CONTEXT_PLACEHOLDER)) {
            throw new NcException("NC extension query does not contain the context placeholder");
        }
        PropertyBags result = new PropertyBags();
        contexts.forEach(context -> result.addAll(dataset.getTripleStore().query(
            contextQueryTemplate.replace(NcQueryContext.CONTEXT_PLACEHOLDER, context))));
        return result;
    }

    private PropertyBags execute(QueryCatalog queryCatalog, String queryKey, String context) {
        String query = queryCatalog.get(queryKey);
        if (query == null) {
            LOGGER.warn("Query [{}] not found in catalog", queryKey);
            return new PropertyBags();
        }
        if (context != null && !query.contains(NcQueryContext.CONTEXT_PLACEHOLDER)) {
            throw new NcException("NC query does not contain the context placeholder: " + queryKey);
        }
        return dataset.getTripleStore().query(context == null ? query
            : query.replace(NcQueryContext.CONTEXT_PLACEHOLDER, context));
    }
}
