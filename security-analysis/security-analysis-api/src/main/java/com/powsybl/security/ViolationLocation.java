/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security;

import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Network;

import java.util.stream.Stream;

/**
 * @author Étienne Lesot {@literal <etienne.lesot at rte-france.com>}
 */
public interface ViolationLocation {

    enum Type {
        NODE_BREAKER,
        BUS_BREAKER
    }

    Type getType();

    BusView getBusView(Network network);

    BusView getBusBreakerView(Network network);

    interface BusView {
        Stream<Bus> getBusStream();
    }
}
