/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion;

import com.powsybl.iidm.network.CurrentLimitsAdder;
import com.powsybl.iidm.network.Terminal;

import java.util.*;
import java.util.function.Function;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
public class CurrentLimitsMapping {

    private static final String PERMANENT_CURRENT_LIMIT = "Permanent Current Limit";
    private static final String TEMPORARY_CURRENT_LIMIT = "Temporary Current Limit";

    private final Context context;
    private final Map<Terminal, CurrentLimitsAdder> adders = new HashMap<>();

    public CurrentLimitsMapping(Context context) {
        this.context = context;
    }

    public CurrentLimitsAdder computeAdderIfAbsent(Terminal terminal,
                                                   Function<? super Terminal, ? extends CurrentLimitsAdder> mappingFunction) {
        return adders.computeIfAbsent(terminal, mappingFunction);
    }

    public void addPermanentLimit(double value, CurrentLimitsAdder adder, String terminalId, String equipmentId) {
        if (Double.isNaN(adder.getPermanentLimit())) {
            adder.setPermanentLimit(value);
        } else { // MRA: this can happen for example when several seasons are defined (there is not standard way to indicate the reason)
            if (terminalId != null) {
                context.fixed(PERMANENT_CURRENT_LIMIT,
                        String.format("Several permanent limits defined for Terminal %s. Only the lowest is kept.", terminalId));
            } else {
                context.fixed(PERMANENT_CURRENT_LIMIT,
                        String.format("Several permanent limits defined for Equipment %s. Only the lowest is kept.", equipmentId));
            }
            if (value < adder.getPermanentLimit()) {
                adder.setPermanentLimit(value);
            }
        }
    }

    public void addTemporaryLimit(String name, double value, int acceptableDuration, CurrentLimitsAdder adder, String terminalId, String equipmentId) {
        if (Double.isNaN(adder.getTemporaryLimit(acceptableDuration))) {
            adder.beginTemporaryLimit()
                    .setName(name)
                    .setValue(value)
                    .setAcceptableDuration(acceptableDuration)
                    .endTemporaryLimit();
        } else { // MRA: this can happen for example when several seasons are defined (there is not standard way to indicate the reason)
            if (terminalId != null) {
                context.fixed(TEMPORARY_CURRENT_LIMIT,
                        String.format("Several temporary limits defined for same acceptable duration %d for Terminal %s. Only the lowest is kept.", acceptableDuration, terminalId));
            } else {
                context.fixed(TEMPORARY_CURRENT_LIMIT,
                        String.format("Several temporary limits defined for same acceptable duration %d for Equipment %s. Only the lowest is kept.", acceptableDuration, equipmentId));
            }
            if (value < adder.getTemporaryLimit(acceptableDuration)) {
                adder.beginTemporaryLimit()
                        .setName(name)
                        .setValue(value)
                        .setAcceptableDuration(acceptableDuration)
                        .endTemporaryLimit();
            }
        }
    }

    public void addAll() {
        for (Map.Entry<Terminal, CurrentLimitsAdder> entry : adders.entrySet()) {
            entry.getValue().add();
        }
    }
}
