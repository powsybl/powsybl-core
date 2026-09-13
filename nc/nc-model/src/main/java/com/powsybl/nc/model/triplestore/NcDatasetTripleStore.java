/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.nc.model.triplestore;

import com.powsybl.commons.report.ReportNode;
import com.powsybl.nc.model.NcDataset;
import com.powsybl.nc.model.NcException;
import com.powsybl.nc.model.NcKeyword;
import com.powsybl.nc.model.NcModel;
import com.powsybl.nc.model.NcProfileMetadata;
import com.powsybl.nc.model.io.NcModelPostProcessor;
import com.powsybl.triplestore.api.PropertyBags;
import com.powsybl.triplestore.api.TripleStore;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Triple-store-backed owner of an NC dataset.
 *
 * @author Roman Vykuka {@literal <vykuka at gmail.com>}
 */
public final class NcDatasetTripleStore implements NcDataset {
    private final TripleStore tripleStore;
    private final List<NcModelPostProcessor> postProcessors;
    private final ReportNode reportNode;
    private final NcQueryExecutor queryExecutor;
    private final Map<String, NcProfileMetadata> profileMetadata;
    private final NcModelTripleStore baselineModel;
    private final Map<QueryKey, PropertyBags> queryCache = new HashMap<>();
    private boolean closed;

    public NcDatasetTripleStore(TripleStore tripleStore, Map<String, Set<String>> contextsByKeyword,
                                List<NcModelPostProcessor> postProcessors, ReportNode reportNode) {
        this.tripleStore = Objects.requireNonNull(tripleStore);
        Map<String, Set<String>> immutableContextsByKeyword = Map.copyOf(contextsByKeyword);
        this.postProcessors = List.copyOf(postProcessors);
        this.reportNode = Objects.requireNonNull(reportNode);
        this.queryExecutor = new NcQueryExecutor(this);
        this.profileMetadata = NcProfileSelector.readMetadata(tripleStore.contextNames(), immutableContextsByKeyword,
            queryExecutor, reportNode);
        this.baselineModel = new NcModelTripleStore(this, null);
    }

    @Override
    public NcModel getModel() {
        checkOpen();
        return baselineModel;
    }

    @Override
    public NcModel forTimestamp(OffsetDateTime timestamp) {
        checkOpen();
        return new NcModelTripleStore(this, Objects.requireNonNull(timestamp));
    }

    TripleStore getTripleStore() {
        checkOpen();
        return tripleStore;
    }

    List<NcModelPostProcessor> getPostProcessors() {
        return postProcessors;
    }

    NcQueryExecutor getQueryExecutor() {
        return queryExecutor;
    }

    Map<String, NcProfileMetadata> getProfileMetadata() {
        return profileMetadata;
    }

    synchronized PropertyBags cachedQuery(NcKeyword keyword, List<String> queryKeys, Set<String> contexts,
                                          Supplier<PropertyBags> query) {
        checkOpen();
        QueryKey cacheKey = new QueryKey(keyword, List.copyOf(queryKeys), Set.copyOf(contexts));
        PropertyBags cached = queryCache.get(cacheKey);
        if (cached == null) {
            cached = query.get();
            queryCache.put(cacheKey, cached);
        }
        return new PropertyBags(cached);
    }

    ReportNode getReportNode() {
        return reportNode;
    }

    private synchronized void invalidateCaches() {
        queryCache.clear();
    }

    void checkOpen() {
        if (closed) {
            throw new NcException("NC dataset is closed");
        }
    }

    @Override
    public void close() {
        if (!closed) {
            invalidateCaches();
            closed = true;
            tripleStore.close();
        }
    }

    private record QueryKey(NcKeyword keyword, List<String> queryKeys, Set<String> contexts) {
    }
}
