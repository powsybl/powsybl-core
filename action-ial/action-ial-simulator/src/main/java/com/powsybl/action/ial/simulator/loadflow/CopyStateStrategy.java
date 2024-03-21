/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.action.ial.simulator.loadflow;

import com.powsybl.iidm.network.Network;

import java.util.Objects;
import java.util.UUID;

/**
 * @author Teofil Calin BANC {@literal <teofil-calin.banc at rte-france.com>}
 */
class CopyStateStrategy implements NetworkCopyStrategy {
    private final Network network;
    private final String stateId;

    private String workingStateId;

    CopyStateStrategy(Network network) {
        this.network = Objects.requireNonNull(network);
        this.stateId = network.getVariantManager().getWorkingVariantId();
    }

    @Override
    public Network createState(String id) {
        workingStateId = id + "-" + UUID.randomUUID();
        network.getVariantManager().cloneVariant(stateId, workingStateId);
        network.getVariantManager().setWorkingVariant(workingStateId);

        return network;
    }

    @Override
    public void removeState() {
        network.getVariantManager().setWorkingVariant(stateId);
        network.getVariantManager().removeVariant(workingStateId);
    }
}
