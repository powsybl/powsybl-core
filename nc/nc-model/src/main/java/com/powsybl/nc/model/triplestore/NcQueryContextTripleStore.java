/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.nc.model.triplestore;

import com.powsybl.nc.model.NcKeyword;
import com.powsybl.nc.model.NcModelExtension;
import com.powsybl.nc.model.io.NcQueryContext;
import com.powsybl.triplestore.api.PropertyBags;

import java.util.Objects;

/**
 * @author Roman Vykuka {@literal <vykuka at gmail.com>}
 */
public final class NcQueryContextTripleStore implements NcQueryContext {
    private final NcModelTripleStore model;

    public NcQueryContextTripleStore(NcModelTripleStore model) {
        this.model = Objects.requireNonNull(model);
    }

    @Override
    public PropertyBags query(NcKeyword keyword, String contextQueryTemplate) {
        return model.queryExtension(keyword, contextQueryTemplate);
    }

    @Override
    public <E extends NcModelExtension> void addExtension(Class<E> type, E extension) {
        model.addExtension(type, extension);
    }
}
