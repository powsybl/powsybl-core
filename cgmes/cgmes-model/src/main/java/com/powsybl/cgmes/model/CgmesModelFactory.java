/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.model;

import com.powsybl.cgmes.model.triplestore.CgmesModelTripleStore;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.triplestore.api.*;

import java.util.Objects;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public final class CgmesModelFactory {

    private CgmesModelFactory() {
    }

    public static CgmesModel create(ReadOnlyDataSource dataSource) {
        return create(dataSource, TripleStoreFactory.DEFAULT_IMPLEMENTATION);
    }

    public static CgmesModel create(ReadOnlyDataSource dataSource, String implementation) {
        Objects.requireNonNull(dataSource);
        Objects.requireNonNull(implementation);

        ReadOnlyDataSource alternativeDataSourceForBoundary = null;
        return create(dataSource, alternativeDataSourceForBoundary, implementation);
    }

    public static CgmesModel create(
        ReadOnlyDataSource mainDataSource,
        ReadOnlyDataSource alternativeDataSourceForBoundary,
        String implementation) {
        Objects.requireNonNull(mainDataSource);
        Objects.requireNonNull(implementation);

        CgmesModel cgmes = createImplementation(implementation, new TripleStoreOptions(), mainDataSource);
        cgmes.read(mainDataSource, alternativeDataSourceForBoundary);
        return cgmes;
    }

    public static CgmesModel create(
            ReadOnlyDataSource mainDataSource,
            ReadOnlyDataSource alternativeDataSourceForBoundary,
            String implementation,
            TripleStoreOptions tripleStoreOptions) {
        Objects.requireNonNull(mainDataSource);
        Objects.requireNonNull(implementation);
        Objects.requireNonNull(tripleStoreOptions);

        CgmesModel cgmes = createImplementation(implementation, tripleStoreOptions, mainDataSource);
        cgmes.read(mainDataSource, alternativeDataSourceForBoundary);
        return cgmes;
    }

    private static CgmesModel createImplementation(String implementation, TripleStoreOptions tripleStoreOptions, ReadOnlyDataSource ds) {
        // Only triple store implementations are available
        TripleStore tripleStore = TripleStoreFactory.create(implementation, tripleStoreOptions);
        String cimNamespace = new CgmesOnDataSource(ds).cimNamespace();
        return new CgmesModelTripleStore(cimNamespace, tripleStore);
    }

    public static CgmesModel copy(CgmesModel cgmes) {
        if (cgmes instanceof CgmesModelTripleStore) {
            CgmesModelTripleStore cgmests = (CgmesModelTripleStore) cgmes;
            TripleStore tripleStore = TripleStoreFactory.copy(cgmests.tripleStore());
            CgmesModel cgmesCopy = new CgmesModelTripleStore(cgmests.getCimNamespace(), tripleStore);
            cgmesCopy.setBasename(cgmes.getBasename());
            buildCaches(cgmesCopy);
            return cgmesCopy;
        } else {
            throw new PowsyblException("CGMES model copy not supported, soource is " + cgmes.getClass().getSimpleName());
        }
    }

    private static void buildCaches(CgmesModel cgmes) {
        // TODO This is rebuilding only some caches
        boolean isNodeBreaker = cgmes.isNodeBreaker();
        for (PropertyBags tends : cgmes.groupedTransformerEnds().values()) {
            for (PropertyBag end : tends) {
                CgmesTerminal t = cgmes.terminal(end.getId(CgmesNames.TERMINAL));
                cgmes.substation(t, isNodeBreaker);
                if (isNodeBreaker) {
                    t.connectivityNode();
                } else {
                    t.topologicalNode();
                }
            }
        }
    }
}
