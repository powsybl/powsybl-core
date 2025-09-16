package com.powsybl.iidm.modification.topology;

import com.powsybl.iidm.network.BusbarSection;
import com.powsybl.iidm.network.Connectable;

import java.util.Collections;
import java.util.List;

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
