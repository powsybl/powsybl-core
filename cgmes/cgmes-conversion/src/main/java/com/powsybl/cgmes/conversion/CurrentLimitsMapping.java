/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion;

import com.powsybl.iidm.network.CurrentLimitsAdder;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
public class CurrentLimitsMapping {

    private final Map<String, CurrentLimitsAdder> adders = new HashMap<>();
    private final Context context;

    CurrentLimitsMapping(Context context) {
        this.context = Objects.requireNonNull(context);
    }

    public CurrentLimitsAdder getCurrentLimitsAdder(String id, Supplier<CurrentLimitsAdder> supplier) {
        return adders.computeIfAbsent(id, s -> supplier.get());
    }

    void addAll() {
        for (Map.Entry<String, CurrentLimitsAdder> entry : adders.entrySet()) {
            if (Double.isNaN(entry.getValue().getPermanentLimit())) {
                context.ignored("Operational Limit Set of " + entry.getKey(), "An operational limit set must at least contain one value for permanent limit.");
            } else {
                entry.getValue().add();
            }
        }
        adders.clear();
    }
}
