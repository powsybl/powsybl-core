package com.powsybl.iidm.network.impl.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.commons.extensions.ExtensionAdderProvider;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.extensions.Acmcs;
import com.powsybl.iidm.network.extensions.AcmcsAdder;

/**
 * @author Gautier Bureau {@literal <gautier.bureau at rte-france.com>}
 */
@AutoService(ExtensionAdderProvider.class)
public class AcmcsAdderImplProvider implements ExtensionAdderProvider<Network, Acmcs, AcmcsAdder> {

    @Override
    public String getImplementationName() {
        return "Default";
    }

    @Override
    public String getExtensionName() {
        return Acmcs.NAME;
    }

    @Override
    public Class<? super AcmcsAdder> getAdderClass() {
        return AcmcsAdder.class;
    }

    @Override
    public AcmcsAdderImpl newAdder(Network network) {
        return new AcmcsAdderImpl(network);
    }
}
