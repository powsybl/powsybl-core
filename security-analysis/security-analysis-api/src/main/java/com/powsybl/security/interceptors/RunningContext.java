/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.interceptors;

import com.powsybl.iidm.network.Network;

import java.util.Objects;

/**
 *
 * TODO: I think having state ID here is broken, as security analysis contract does not guarantee that
 *       the corresponding state will be filled with N results
 *
 * @author Mathieu Bague {@literal <mathieu.bague at rte-france.com>}
 */
public class RunningContext implements SecurityAnalysisResultContext {

    private final Network network;

    private final String initialStateId;

    public RunningContext(Network network, String initialStateId) {
        this.network = Objects.requireNonNull(network);
        this.initialStateId = initialStateId;
    }

    /**
     * Get the network on which computation is carried out.
     * @return the network on which computation is carried out
     */
    @Override
    public Network getNetwork() {
        return network;
    }

    /**
     * Get the state ID corresponding to the N situation computation.
     * TODO: I think thisis broken, as security analysis contract does not guarantee that
     *       the corresponding state will be filled with N results
     * @return the state ID corresponding to the N situation computation
     */
    public String getInitialStateId() {
        return initialStateId;
    }
}
