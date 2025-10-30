package com.powsybl.iidm.network.extensions;

import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.Generator;

/**
 * @author Gautier Bureau {@literal <gautier.bureau at rte-france.com>}
 */
public interface GeneratorConnectionLevel extends Extension<Generator> {

    String NAME = "generatorConnectionLevel";

    @Override
    default String getName() {
        return NAME;
    }

    GeneratorConnectionLevelType getLevel();

    void setLevel(GeneratorConnectionLevelType level);

}
