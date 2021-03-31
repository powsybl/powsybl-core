/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network;

import java.util.Objects;
import java.util.Optional;

/**
 * @author Yichen TANG <yichen.tang at rte-france.com>
 */
public abstract class AbstractBusRef implements BusRef {

    @Override
    public Optional<Bus> resolve(Network network, TopologyLevel level) {
        Objects.requireNonNull(network);
        Objects.requireNonNull(level);
        if (Objects.equals(TopologyLevel.NODE_BREAKER, level)) {
            throw new IllegalArgumentException(level + " is not supported in resolve a BusRef.");
        }
        return resolveByLevel(network, level);
    }

    static Optional<Bus> chooseBusByLevel(Terminal t, TopologyLevel level) {
        if (Objects.equals(level, TopologyLevel.BUS_BRANCH)) {
            return Optional.ofNullable(t.getBusView().getBus());
        } else {
            return Optional.ofNullable(t.getBusBreakerView().getBus());
        }
    }

    abstract Optional<Bus> resolveByLevel(Network network, TopologyLevel level);
}
