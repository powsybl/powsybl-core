/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.interceptors;

import com.powsybl.iidm.network.Network;

import java.util.Objects;

/**
 * @author Mathieu Bague <mathieu.bague@rte-france.com>
 */
public class RunningContext {

    private final Network network;

    private final String initialStateId;

    public RunningContext(Network network, String initialStateId) {
        this.network = Objects.requireNonNull(network);
        this.initialStateId = initialStateId;
    }

    public Network getNetwork() {
        return network;
    }

    public String getInitialStateId() {
        return initialStateId;
    }
}
