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

    private final Map<Terminal, CurrentLimitsAdder> adders;
    private final Map<CurrentLimitsAdder, Set<Double>> permanentLimits;

    public CurrentLimitsMapping() {
        adders = new HashMap<>();
        permanentLimits = new HashMap<>();
    }

    public CurrentLimitsAdder computeAdderIfAbsent(Terminal terminal,
                                                   Function<? super Terminal, ? extends CurrentLimitsAdder> mappingFunction) {
        return adders.computeIfAbsent(terminal, mappingFunction);
    }

    public void addPermanentLimit(double value, CurrentLimitsAdder adder) {
        permanentLimits.computeIfAbsent(adder, a -> new HashSet<>()).add(value);
    }

    public void addAll() {
        fillPermanentLimits();
        for (Map.Entry<Terminal, CurrentLimitsAdder> entry : adders.entrySet()) {
            entry.getValue().add();
        }
    }

    private void fillPermanentLimits() {
        for (Map.Entry<CurrentLimitsAdder, Set<Double>> entry : permanentLimits.entrySet()) {
            entry.getKey().setPermanentLimit(Collections.min(entry.getValue())); // Only keep the lowest permanent limit. best solution ?
        }
    }
}
