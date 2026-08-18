/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.reducer;

import com.google.auto.service.AutoService;

/**
 * @author Coline Piloquet {@literal <coline.piloquet at rte-france.com>}
 */
@AutoService(NetworkReducerProvider.class)
public class DefaultNetworkReducerProvider implements NetworkReducerProvider {

    @Override
    public String getName() {
        return "Default";
    }

    @Override
    public String getVersion() {
        return "1.0";
    }

    @Override
    public NetworkReducer create(NetworkPredicate predicate, ReductionParameters parameters) {
        ReductionOptions options = parameters instanceof ReductionOptions reductionOptions ? reductionOptions : new ReductionOptions();
        return new DefaultNetworkReducer(predicate, options);
    }
}
