package com.powsybl.iidm.network.extensions;

import com.powsybl.commons.extensions.ExtensionAdder;
import com.powsybl.iidm.network.Generator;

/**
 * @author Gautier Bureau {@literal <gautier.bureau at rte-france.com>}
 */
public interface SynchronousGeneratorPropertiesAdder extends ExtensionAdder<Generator, SynchronousGeneratorProperties> {

    @Override
    default Class<SynchronousGeneratorProperties> getExtensionClass() {
        return SynchronousGeneratorProperties.class;
    }

    SynchronousGeneratorPropertiesAdder withNumberOfWindings(int numberOfWindings);

    SynchronousGeneratorPropertiesAdder withGovernor(String governor);

    SynchronousGeneratorPropertiesAdder withVoltageRegulator(String voltageRegulator);

    SynchronousGeneratorPropertiesAdder withPss(String voltageRegulator);

    SynchronousGeneratorPropertiesAdder withAuxiliaries(boolean auxiliaries);

    SynchronousGeneratorPropertiesAdder withInternalTransformer(boolean internalTransformer);

    SynchronousGeneratorPropertiesAdder withRpcl(boolean rpcl);

    SynchronousGeneratorPropertiesAdder withRpcl2(boolean rpcl2);

    SynchronousGeneratorPropertiesAdder withUva(String uva);

    SynchronousGeneratorPropertiesAdder withFictitious(boolean fictitious);

    SynchronousGeneratorPropertiesAdder withQlim(boolean qlim);

}
