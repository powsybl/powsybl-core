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
import java.util.stream.Collectors;

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
            return new PropertyBags();
        }
        return execute(queryKey, contexts);
    }

    PropertyBags queryExtension(Set<String> contexts, String contextsQueryTemplate) {
        dataset.checkOpen();
        if (contexts.isEmpty()) {
            return new PropertyBags();
        }
        if (!contextsQueryTemplate.contains(NcQueryContext.CONTEXTS_PLACEHOLDER)) {
            throw new NcException("NC extension query does not contain the contexts placeholder");
        }
        String executableQuery = expandContexts(contextsQueryTemplate, contexts);
        LOGGER.debug("Executing NC extension query in contexts [{}]:{}{}", contexts,
            System.lineSeparator(), executableQuery);
        return dataset.getTripleStore().query(executableQuery);
    }

    private PropertyBags execute(String queryKey, Set<String> contexts) {
        String query = ncQueryCatalog.get(queryKey);
        if (query == null) {
            LOGGER.warn("Query [{}] not found in catalog", queryKey);
            return new PropertyBags();
        }
        if (!query.contains(NcQueryContext.CONTEXTS_PLACEHOLDER)) {
            throw new NcException("NC query does not contain the contexts placeholder: " + queryKey);
        }
        String executableQuery = expandContexts(query, contexts);
        LOGGER.debug("Executing NC query [{}] in contexts [{}]:{}{}", queryKey, contexts,
            System.lineSeparator(), executableQuery);
        return dataset.getTripleStore().query(executableQuery);
    }

    private static String expandContexts(String query, Set<String> contexts) {
        String contextValues = contexts.stream()
            .sorted()
            .map(context -> "<" + context + ">")
            .collect(Collectors.joining(" "));
        return query.replace(NcQueryContext.CONTEXTS_PLACEHOLDER, contextValues);
    }
}
