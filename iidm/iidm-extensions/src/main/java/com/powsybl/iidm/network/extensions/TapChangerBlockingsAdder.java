package com.powsybl.iidm.network.extensions;

import com.powsybl.commons.extensions.ExtensionAdder;
import com.powsybl.iidm.network.Network;

/**
 * @author Gautier Bureau {@literal <gautier.bureau at rte-france.com>}
 */
public interface TapChangerBlockingsAdder extends ExtensionAdder<Network, TapChangerBlockings> {

    TapChangerBlockingAdder newTapChangerBlocking();
}
