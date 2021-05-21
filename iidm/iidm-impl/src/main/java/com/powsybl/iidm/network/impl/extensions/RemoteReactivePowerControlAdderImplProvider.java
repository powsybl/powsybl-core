package com.powsybl.iidm.network.impl.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.commons.extensions.ExtensionAdderProvider;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.extensions.RemoteReactivePowerControl;

/**
 * @author Bertrand Rix <bertrand.rix at artelys.com>
 */
@AutoService(ExtensionAdderProvider.class)
public class RemoteReactivePowerControlAdderImplProvider implements ExtensionAdderProvider<Generator, RemoteReactivePowerControl, RemoteReactivePowerAdderImpl> {

    @Override
    public String getImplementationName() {
        return "Default";
    }

    @Override
    public Class<RemoteReactivePowerAdderImpl> getAdderClass() {
        return RemoteReactivePowerAdderImpl.class;
    }

    @Override
    public RemoteReactivePowerAdderImpl newAdder(Generator extendable) {
        return new RemoteReactivePowerAdderImpl(extendable);
    }
}
