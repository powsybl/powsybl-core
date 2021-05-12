package com.powsybl.iidm.network.extensions;

import com.powsybl.commons.extensions.ExtensionAdder;
import com.powsybl.iidm.network.Generator;

public interface RemoteReactivePowerControlAdder extends ExtensionAdder<Generator, RemoteReactivePowerControl> {
    @Override
    default Class<RemoteReactivePowerControl> getExtensionClass() {
        return RemoteReactivePowerControl.class;
    }
}
