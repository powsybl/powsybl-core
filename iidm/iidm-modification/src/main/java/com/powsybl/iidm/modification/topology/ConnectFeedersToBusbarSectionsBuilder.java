/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification.topology;

import com.powsybl.iidm.network.BusbarSection;
import com.powsybl.iidm.network.Connectable;

import java.util.Collections;
import java.util.List;

/**
 * @author Coline Piloquet {@literal <coline.piloquet at rte-france.com>}
 */
public class ConnectFeedersToBusbarSectionsBuilder {

    private List<Connectable> connectablesToConnect = Collections.emptyList();
    private List<BusbarSection> busbarSectionsToConnect;
    private boolean connectCouplingDevices = false;
    private String couplingDeviceSwitchPrefixId = "";

    public ConnectFeedersToBusbarSections build() {
        return new ConnectFeedersToBusbarSections(connectablesToConnect, busbarSectionsToConnect, connectCouplingDevices, couplingDeviceSwitchPrefixId);
    }

    public ConnectFeedersToBusbarSectionsBuilder withConnectablesToConnect(List<Connectable> connectablesToConnect) {
        this.connectablesToConnect = connectablesToConnect;
        return this;
    }

    public ConnectFeedersToBusbarSectionsBuilder withBusbarSectionsToConnect(List<BusbarSection> busbarSectionsToConnect) {
        this.busbarSectionsToConnect = busbarSectionsToConnect;
        return this;
    }

    public ConnectFeedersToBusbarSectionsBuilder withConnectCouplingDevices(boolean connectCouplingDevices) {
        this.connectCouplingDevices = connectCouplingDevices;
        return this;
    }

    public ConnectFeedersToBusbarSectionsBuilder withCouplingDeviceSwitchPrefixId(String couplingDeviceSwitchPrefixId) {
        this.couplingDeviceSwitchPrefixId = couplingDeviceSwitchPrefixId;
        return this;
    }

}
