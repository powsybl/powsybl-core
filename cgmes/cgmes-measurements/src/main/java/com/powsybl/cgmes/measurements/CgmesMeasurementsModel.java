/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.measurements;

import com.powsybl.triplestore.api.PropertyBags;
import com.powsybl.triplestore.api.QueryCatalog;
import com.powsybl.triplestore.api.TripleStore;

import java.util.Objects;

import static com.powsybl.triplestore.api.TripleStoreUtils.queryTripleStore;

/**
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
 */
public class CgmesMeasurementsModel {

    private final TripleStore tripleStore;
    private final QueryCatalog queryCatalog = new QueryCatalog("CGMES-Meas.sparql");

    public CgmesMeasurementsModel(TripleStore tripleStore) {
        this.tripleStore = Objects.requireNonNull(tripleStore);
    }

    public PropertyBags analogs() {
        return queryTripleStore("analogs", queryCatalog, tripleStore);
    }

    public PropertyBags bays() {
        return queryTripleStore("bays", queryCatalog, tripleStore);
    }

    public PropertyBags discretes() {
        return queryTripleStore("discretes", queryCatalog, tripleStore);
    }
}
