/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.shorcircuit;

import java.util.Objects;

import com.powsybl.triplestore.api.PropertyBags;
import com.powsybl.triplestore.api.QueryCatalog;
import com.powsybl.triplestore.api.TripleStore;

import static com.powsybl.triplestore.api.TripleStoreUtils.queryTripleStore;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */

public class CgmesShortCircuitModel {

    public static final String SYNCHRONOUS_MACHINE_SHORT_CIRCUIT_DATA_QUERY_KEY = "SynchronousMachineShortcircuitData";
    public static final String BUS_BAR_SECTION_SHORT_CIRCUIT_DATA_QUERY_KEY = "BusbarSectionShortcircuitData";

    private final TripleStore tripleStore;
    private final QueryCatalog queryCatalog;

    public CgmesShortCircuitModel(TripleStore tripleStore) {
        this(tripleStore, new QueryCatalog("CGMES-SHORT-CIRCUITS.sparql"));
    }

    public CgmesShortCircuitModel(TripleStore tripleStore, QueryCatalog queryCatalog) {
        this.tripleStore = Objects.requireNonNull(tripleStore);
        this.queryCatalog = Objects.requireNonNull(queryCatalog);
    }

    public PropertyBags getSynchronousMachinesShortcircuitData() {
        return queryTripleStore(SYNCHRONOUS_MACHINE_SHORT_CIRCUIT_DATA_QUERY_KEY, queryCatalog, tripleStore);
    }

    public PropertyBags getBusbarSectionsShortcircuitData() {
        return queryTripleStore(BUS_BAR_SECTION_SHORT_CIRCUIT_DATA_QUERY_KEY, queryCatalog, tripleStore);
    }
}
