package com.powsybl.iidm.network.impl.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.commons.extensions.ExtensionAdderProvider;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.extensions.TapChangerBlockings;
import com.powsybl.iidm.network.extensions.TapChangerBlockingsAdder;

/**
 * @author Gautier Bureau {@literal <gautier.bureau at rte-france.com>}
 */
@AutoService(ExtensionAdderProvider.class)
public class TapChangerBlockingsAdderImplProvider implements ExtensionAdderProvider<Network, TapChangerBlockings, TapChangerBlockingsAdder> {

    @Override
    public String getImplementationName() {
        return "Default";
    }

    @Override
    public String getExtensionName() {
        return TapChangerBlockings.NAME;
    }

    @Override
    public Class<? super TapChangerBlockingsAdder> getAdderClass() {
        return TapChangerBlockingsAdder.class;
    }

    @Override
    public TapChangerBlockingsAdderImpl newAdder(Network network) {
        return new TapChangerBlockingsAdderImpl(network);
    }
}
