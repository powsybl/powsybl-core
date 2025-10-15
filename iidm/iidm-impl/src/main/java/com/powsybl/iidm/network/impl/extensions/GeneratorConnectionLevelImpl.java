package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.extensions.GeneratorConnectionLevel;

/**
 * @author Gautier Bureau {@literal <gautier.bureau at rte-france.com>}
 */
public class GeneratorConnectionLevelImpl extends AbstractExtension<Generator> implements GeneratorConnectionLevel {

    private String level;

    public GeneratorConnectionLevelImpl(Generator generator, String level) {
        super(generator);
        this.level = level;
    }

    @Override
    public String getLevel() {
        return level;
    }

}
