package com.powsybl.iidm.network.impl.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.commons.extensions.ExtensionAdderProvider;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.extensions.SynchronousGeneratorProperties;

/**
 * @author Gautier Bureau {@literal <gautier.bureau at rte-france.com>}
 */
@AutoService(ExtensionAdderProvider.class)
public class SynchronousGeneratorPropertiesAdderImplProvider implements
        ExtensionAdderProvider<Generator, SynchronousGeneratorProperties, SynchronousGeneratorPropertiesAdderImpl> {

    @Override
    public String getImplementationName() {
        return "Default";
    }

    @Override
    public String getExtensionName() {
        return SynchronousGeneratorProperties.NAME;
    }

    @Override
    public Class<SynchronousGeneratorPropertiesAdderImpl> getAdderClass() {
        return SynchronousGeneratorPropertiesAdderImpl.class;
    }

    @Override
    public SynchronousGeneratorPropertiesAdderImpl newAdder(Generator generator) {
        return new SynchronousGeneratorPropertiesAdderImpl(generator);
    }
}
