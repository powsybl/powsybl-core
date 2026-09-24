/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.nc.systemconstraints;

import com.powsybl.nc.model.NcObject;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author Roman Vykuka {@literal <vykuka at gmail.com>}
 */
public record NcPowerTransferCorridor(String mrid, String name, boolean enabled,
                                      Set<InfeedLimit> infeedLimits) implements NcObject {

    public NcPowerTransferCorridor {
        infeedLimits = Collections.unmodifiableSet(new LinkedHashSet<>(infeedLimits));
    }

    public record InfeedLimit(String mrid, String name, Double valueW, Double valueA, boolean minimum, String direction,
                              String mainTerminal, Set<InfeedTerminal> infeedTerminals) implements NcObject {

        public InfeedLimit {
            infeedTerminals = Collections.unmodifiableSet(new LinkedHashSet<>(infeedTerminals));
        }

        public Set<String> terminals() {
            Set<String> terminals = new LinkedHashSet<>();
            if (mainTerminal != null) {
                terminals.add(mainTerminal);
            }
            infeedTerminals.stream().map(InfeedTerminal::terminal).forEach(terminals::add);
            return Collections.unmodifiableSet(terminals);
        }
    }

    public record InfeedTerminal(String mrid, String terminal) implements NcObject {
    }
}
