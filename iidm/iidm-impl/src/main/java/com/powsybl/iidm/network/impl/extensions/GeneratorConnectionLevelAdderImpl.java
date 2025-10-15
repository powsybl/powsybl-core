package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.extensions.AbstractExtensionAdder;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.extensions.GeneratorConnectionLevel;
import com.powsybl.iidm.network.extensions.GeneratorConnectionLevelAdder;

/**
 * @author Gautier Bureau {@literal <gautier.bureau at rte-france.com>}
 */
public class GeneratorConnectionLevelAdderImpl extends AbstractExtensionAdder<Generator, GeneratorConnectionLevel> implements GeneratorConnectionLevelAdder {

    private String level;

    public GeneratorConnectionLevelAdderImpl(Generator generator) {
        super(generator);
    }

    @Override
    protected GeneratorConnectionLevel createExtension(Generator extendable) {
        return new GeneratorConnectionLevelImpl(extendable, level);
    }

    @Override
    public GeneratorConnectionLevelAdderImpl withLevel(String level) {
        this.level = level;
        return this;
    }

}
