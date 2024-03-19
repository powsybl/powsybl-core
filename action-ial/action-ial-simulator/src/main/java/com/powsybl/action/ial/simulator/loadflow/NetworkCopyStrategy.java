/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.action.ial.simulator.loadflow;

import com.powsybl.iidm.network.Network;

import java.util.Objects;

/**
 * @author Teofil Calin BANC {@literal <teofil-calin.banc at rte-france.com>}
 */
public interface NetworkCopyStrategy {

    Network createState(String id);

    void removeState();

    static NetworkCopyStrategy getInstance(CopyStrategy strategy, Network network) {
        Objects.requireNonNull(strategy);

        return (strategy == CopyStrategy.DEEP) ? new DeepCopyStrategy(network) : new CopyStateStrategy(network);
    }
}
