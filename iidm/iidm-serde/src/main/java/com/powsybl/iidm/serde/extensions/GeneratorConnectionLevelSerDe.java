package com.powsybl.iidm.serde.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.commons.extensions.AbstractExtensionSerDe;
import com.powsybl.commons.extensions.ExtensionSerDe;
import com.powsybl.commons.io.DeserializerContext;
import com.powsybl.commons.io.SerializerContext;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.extensions.GeneratorConnectionLevel;
import com.powsybl.iidm.network.extensions.GeneratorConnectionLevelAdder;

/**
 * @author Gautier Bureau {@literal <gautier.bureau at rte-france.com>}
 */
@AutoService(ExtensionSerDe.class)
public class GeneratorConnectionLevelSerDe extends AbstractExtensionSerDe<Generator, GeneratorConnectionLevel> {

    public GeneratorConnectionLevelSerDe() {
        super(GeneratorConnectionLevel.NAME, "network", GeneratorConnectionLevel.class, "generatorConnectionLevel.xsd",
                "http://www.powsybl.org/schema/iidm/ext/generator_connection_level/1_0", "gcl");
    }

    @Override
    public void write(GeneratorConnectionLevel generatorConnectionLevel, SerializerContext context) {
        context.getWriter().writeStringAttribute("level", generatorConnectionLevel.getLevel());
    }

    @Override
    public GeneratorConnectionLevel read(Generator generator, DeserializerContext context) {
        String level = context.getReader().readStringAttribute("level");
        context.getReader().readEndNode();
        return generator.newExtension(GeneratorConnectionLevelAdder.class)
                .withLevel(level)
                .add();
    }

}
