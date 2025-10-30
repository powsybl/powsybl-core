package com.powsybl.iidm.network.extensions;

import com.powsybl.commons.extensions.ExtensionAdder;
import com.powsybl.iidm.network.Generator;

/**
 * @author Gautier Bureau {@literal <gautier.bureau at rte-france.com>}
 */
public interface GeneratorConnectionLevelAdder extends ExtensionAdder<Generator, GeneratorConnectionLevel> {

    @Override
    default Class<GeneratorConnectionLevel> getExtensionClass() {
        return GeneratorConnectionLevel.class;
    }

    GeneratorConnectionLevelAdder withLevel(GeneratorConnectionLevelType level);

}
