package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.extensions.GeneratorConnectionLevel;
import com.powsybl.iidm.network.extensions.GeneratorConnectionLevelType;

/**
 * @author Gautier Bureau {@literal <gautier.bureau at rte-france.com>}
 */
public class GeneratorConnectionLevelImpl extends AbstractExtension<Generator> implements GeneratorConnectionLevel {

    private GeneratorConnectionLevelType level;

    public GeneratorConnectionLevelImpl(Generator generator, GeneratorConnectionLevelType level) {
        super(generator);
        this.level = level;
    }

    @Override
    public GeneratorConnectionLevelType getLevel() {
        return level;
    }

    @Override
    public void setLevel(GeneratorConnectionLevelType level) {
        this.level = level;
    }

}
