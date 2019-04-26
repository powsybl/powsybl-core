/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.model;

import java.util.Objects;

import com.powsybl.cgmes.model.triplestore.CgmesModelTripleStore;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.triplestore.api.TripleStore;
import com.powsybl.triplestore.api.TripleStoreFactory;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public final class CgmesModelFactory {

    private CgmesModelFactory() {
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

        CgmesModel cgmes = createImplementation(implementation, mainDataSource);
        cgmes.read(mainDataSource, alternativeDataSourceForBoundary);
        return cgmes;
    }

    private static CgmesModel createImplementation(String implementation, ReadOnlyDataSource ds) {
        // Only triple store implementations are available
        TripleStore tripleStore = TripleStoreFactory.create(implementation);
        String cimNamespace = new CgmesOnDataSource(ds).cimNamespace();
        return new CgmesModelTripleStore(cimNamespace, tripleStore);
    }
}
