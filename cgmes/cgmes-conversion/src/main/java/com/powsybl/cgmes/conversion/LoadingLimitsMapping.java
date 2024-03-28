/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion;

import com.powsybl.iidm.network.LoadingLimitsAdder;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
 */
public class LoadingLimitsMapping {

    protected final Map<String, LoadingLimitsAdder<?, ?>> adders = new HashMap<>();
    private final Context context;

    LoadingLimitsMapping(Context context) {
        this.context = Objects.requireNonNull(context);
    }

    public LoadingLimitsAdder<?, ?> computeIfAbsentLoadingLimitsAdder(String id, Supplier<LoadingLimitsAdder<?, ?>> supplier) {
        return adders.computeIfAbsent(id, s -> supplier.get());
    }

    void addAll() {
        for (Map.Entry<String, LoadingLimitsAdder<?, ?>> entry : adders.entrySet()) {
            if (!Double.isNaN(entry.getValue().getPermanentLimit()) || entry.getValue().hasTemporaryLimits()) {
                entry.getValue()
                        .fixLimits(context.config().getMissingPermanentLimitPercentage(), context::fixed)
                        .add();
            }
        }
        adders.clear();
    }
}
