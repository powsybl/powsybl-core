package com.powsybl.iidm.network.impl.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.commons.extensions.ExtensionAdderProvider;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.extensions.GeneratorConnectionLevel;

/**
 * @author Gautier Bureau {@literal <gautier.bureau at rte-france.com>}
 */
@AutoService(ExtensionAdderProvider.class)
public class GeneratorConnectionLevelAdderImplProvider implements
        ExtensionAdderProvider<Generator, GeneratorConnectionLevel, GeneratorConnectionLevelAdderImpl> {

    @Override
    public String getImplementationName() {
        return "Default";
    }

    @Override
    public String getExtensionName() {
        return GeneratorConnectionLevel.NAME;
    }

    @Override
    public Class<GeneratorConnectionLevelAdderImpl> getAdderClass() {
        return GeneratorConnectionLevelAdderImpl.class;
    }

    @Override
    public GeneratorConnectionLevelAdderImpl newAdder(Generator generator) {
        return new GeneratorConnectionLevelAdderImpl(generator);
    }
}
