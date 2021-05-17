package com.powsybl.iidm.network.extensions;

import com.powsybl.commons.extensions.ExtensionAdder;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Terminal;

public interface RemoteReactivePowerControlAdder extends ExtensionAdder<Generator, RemoteReactivePowerControl> {
    @Override
    default Class<RemoteReactivePowerControl> getExtensionClass() {
        return RemoteReactivePowerControl.class;
    }

    RemoteReactivePowerControlAdder withTargetQ(double targetQ);

    RemoteReactivePowerControlAdder withRegulatingTerminal(Terminal regulatingTerminal);

    RemoteReactivePowerControlAdder withEnabled(boolean enabled);
}
