package com.powsybl.iidm.network.impl.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.commons.extensions.ExtensionAdderProvider;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.extensions.SynchronizedGeneratorProperties;

/**
 * @author Gautier Bureau {@literal <gautier.bureau at rte-france.com>}
 */
@AutoService(ExtensionAdderProvider.class)
public class SynchronizedGeneratorPropertiesAdderImplProvider implements
        ExtensionAdderProvider<Generator, SynchronizedGeneratorProperties, SynchronizedGeneratorPropertiesAdderImpl> {

    @Override
    public String getImplementationName() {
        return "Default";
    }

    @Override
    public String getExtensionName() {
        return SynchronizedGeneratorProperties.NAME;
    }

    @Override
    public Class<SynchronizedGeneratorPropertiesAdderImpl> getAdderClass() {
        return SynchronizedGeneratorPropertiesAdderImpl.class;
    }

    @Override
    public SynchronizedGeneratorPropertiesAdderImpl newAdder(Generator generator) {
        return new SynchronizedGeneratorPropertiesAdderImpl(generator);
    }
}
