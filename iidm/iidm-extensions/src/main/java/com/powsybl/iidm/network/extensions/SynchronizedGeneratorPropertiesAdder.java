package com.powsybl.iidm.network.extensions;

import com.powsybl.commons.extensions.ExtensionAdder;
import com.powsybl.iidm.network.Generator;

/**
 * @author Gautier Bureau {@literal <gautier.bureau at rte-france.com>}
 */
public interface SynchronizedGeneratorPropertiesAdder extends ExtensionAdder<Generator, SynchronizedGeneratorProperties> {

    @Override
    default Class<SynchronizedGeneratorProperties> getExtensionClass() {
        return SynchronizedGeneratorProperties.class;
    }

    SynchronizedGeneratorPropertiesAdder withType(String type);

    SynchronizedGeneratorPropertiesAdder withRpcl2(boolean rpcl2);

}
