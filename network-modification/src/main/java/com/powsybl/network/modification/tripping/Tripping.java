package com.powsybl.network.modification.tripping;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Switch;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.network.modification.NetworkModification;

import java.util.Set;

public interface Tripping extends NetworkModification {
    void traverse(Network network, Set<Switch> switchesToOpen, Set<Terminal> terminalsToDisconnect);
}
