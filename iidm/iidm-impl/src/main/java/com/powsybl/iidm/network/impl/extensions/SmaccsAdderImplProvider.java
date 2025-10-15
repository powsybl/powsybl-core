package com.powsybl.iidm.network.impl.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.commons.extensions.ExtensionAdderProvider;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.extensions.Smaccs;
import com.powsybl.iidm.network.extensions.SmaccsAdder;

/**
 * @author Gautier Bureau {@literal <gautier.bureau at rte-france.com>}
 */
@AutoService(ExtensionAdderProvider.class)
public class SmaccsAdderImplProvider implements ExtensionAdderProvider<Network, Smaccs, SmaccsAdder> {

    @Override
    public String getImplementationName() {
        return "Default";
    }

    @Override
    public String getExtensionName() {
        return Smaccs.NAME;
    }

    @Override
    public Class<? super SmaccsAdder> getAdderClass() {
        return SmaccsAdder.class;
    }

    @Override
    public SmaccsAdderImpl newAdder(Network network) {
        return new SmaccsAdderImpl(network);
    }
}
