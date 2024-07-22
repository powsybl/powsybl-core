/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.cgmes.model;

import com.powsybl.cgmes.model.triplestore.CgmesModelTripleStore;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.triplestore.api.*;

import java.util.Objects;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
public final class CgmesModelFactory {

    private CgmesModelFactory() {
    }

    public static CgmesModel create(ReadOnlyDataSource dataSource) {
        return create(dataSource, TripleStoreFactory.DEFAULT_IMPLEMENTATION, ReportNode.NO_OP);
    }

    public static CgmesModel create(ReadOnlyDataSource dataSource, String implementation) {
        ReadOnlyDataSource alternativeDataSourceForBoundary = null;
        return create(dataSource, alternativeDataSourceForBoundary, implementation, ReportNode.NO_OP);
    }

    public static CgmesModel create(ReadOnlyDataSource dataSource, ReportNode reportNode) {
        ReadOnlyDataSource alternativeDataSourceForBoundary = null;
        return create(dataSource, alternativeDataSourceForBoundary, TripleStoreFactory.DEFAULT_IMPLEMENTATION, reportNode);
    }

    public static CgmesModel create(ReadOnlyDataSource dataSource, String implementation, ReportNode reportNode) {
        ReadOnlyDataSource alternativeDataSourceForBoundary = null;
        return create(dataSource, alternativeDataSourceForBoundary, implementation, reportNode);
    }

    public static CgmesModel create(
        ReadOnlyDataSource mainDataSource,
        ReadOnlyDataSource alternativeDataSourceForBoundary,
        String implementation,
        ReportNode reportNode) {
        return create(mainDataSource, alternativeDataSourceForBoundary, implementation, reportNode, new TripleStoreOptions());
    }

    public static CgmesModel create(
            ReadOnlyDataSource mainDataSource,
            ReadOnlyDataSource alternativeDataSourceForBoundary,
            String implementation,
            ReportNode reportNode,
            TripleStoreOptions tripleStoreOptions) {
        Objects.requireNonNull(mainDataSource);
        Objects.requireNonNull(implementation);
        Objects.requireNonNull(reportNode);
        Objects.requireNonNull(tripleStoreOptions);

        CgmesModel cgmes = createImplementation(implementation, tripleStoreOptions, mainDataSource, alternativeDataSourceForBoundary);
        cgmes.read(mainDataSource, alternativeDataSourceForBoundary, reportNode);
        return cgmes;
    }

    private static CgmesModel createImplementation(String implementation, TripleStoreOptions tripleStoreOptions, ReadOnlyDataSource ds, ReadOnlyDataSource alternativeDataSourceForBoundary) {
        // Only triple store implementations are available
        TripleStore tripleStore = TripleStoreFactory.create(implementation, tripleStoreOptions);
        String cimNamespace = obtainCimNamespace(ds, alternativeDataSourceForBoundary);
        return new CgmesModelTripleStore(cimNamespace, tripleStore, tripleStoreOptions.queryCatalog());
    }

    private static String obtainCimNamespace(ReadOnlyDataSource ds, ReadOnlyDataSource dsBoundary) {
        try {
            return new CgmesOnDataSource(ds).cimNamespace();
        } catch (CgmesModelException x) {
            if (dsBoundary != null) {
                try {
                    return new CgmesOnDataSource(dsBoundary).cimNamespace();
                } catch (CgmesModelException x2) {
                    throw x;
                }
            } else {
                throw x;
            }
        }
    }
}
