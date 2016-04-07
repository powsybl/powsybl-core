/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.eurostag.network;

import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class EsgCouplingDevice {

    public enum ConnectionStatus {
        CLOSED,
        OPEN
    }

    private final EsgBranchName name;
    private final ConnectionStatus connectionStatus;

    public EsgCouplingDevice(EsgBranchName name, ConnectionStatus connectionStatus) {
        this.name = Objects.requireNonNull(name);
        this.connectionStatus = Objects.requireNonNull(connectionStatus);
    }

    public EsgBranchName getName() {
        return name;
    }

    public ConnectionStatus getConnectionStatus() {
        return connectionStatus;
    }

}
