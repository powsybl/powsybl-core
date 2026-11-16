/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.action.ial.simulator.loadflow;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.serde.NetworkSerDe;

import java.util.Objects;

/**
 * @author Teofil Calin BANC {@literal <teofil-calin.banc at rte-france.com>}
 */
class DeepCopyStrategy implements NetworkCopyStrategy {
    private final byte[] compressedBytes;

    DeepCopyStrategy(Network network) {
        this.compressedBytes = NetworkSerDe.gzip(Objects.requireNonNull(network));
    }

    @Override
    public Network createState(String id) {
        return NetworkSerDe.gunzip(compressedBytes);
    }

    @Override
    public void removeState() {
        // Nothing to do
    }
}
