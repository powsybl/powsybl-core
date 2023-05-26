/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.modification;

import com.powsybl.commons.reporter.Reporter;
import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Terminal;

import java.util.Objects;

/**
 * Network modification to connect or disconnect a Shunt compensator.
 *
 * @author Nicolas Pierre <nicolas.pierre at artelys.com>
 */
public class ConnectionShuntModification extends AbstractNetworkModification {

    private final String shuntId;
    private final boolean connect;

    /**
     * @param shuntId the shunt to connect/disconnect
     * @param connect false to disconnect, true to connect.
     */
    public ConnectionShuntModification(String shuntId, boolean connect) {
        this.shuntId = Objects.requireNonNull(shuntId);
        this.connect = connect;
    }

    @Override
    public void apply(Network network, boolean throwException, ComputationManager computationManager,
                      Reporter reporter) {
        Terminal t = network.getShuntCompensator(shuntId).getTerminal();
        if (connect) {
            Bus connectable = t.getBusView().getConnectableBus();
            if (connectable != null) {
                t.connect();
            } else {
                logOrThrow(throwException, "The shunt '" + shuntId + "' has no connectable bus.");
            }
        } else {
            t.disconnect();
        }
    }

    public String getShuntId() {
        return shuntId;
    }

    public boolean isConnect() {
        return connect;
    }

}
