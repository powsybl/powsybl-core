package com.powsybl.iidm.serde.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.commons.extensions.AbstractExtensionSerDe;
import com.powsybl.commons.extensions.ExtensionSerDe;
import com.powsybl.commons.io.DeserializerContext;
import com.powsybl.commons.io.SerializerContext;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.extensions.SynchronizedGeneratorProperties;
import com.powsybl.iidm.network.extensions.SynchronizedGeneratorPropertiesAdder;

/**
 * @author Gautier Bureau {@literal <gautier.bureau at rte-france.com>}
 */
@AutoService(ExtensionSerDe.class)
public class SynchronizedGeneratorPropertiesSerDe extends AbstractExtensionSerDe<Generator, SynchronizedGeneratorProperties> {

    public SynchronizedGeneratorPropertiesSerDe() {
        super(SynchronizedGeneratorProperties.NAME, "network", SynchronizedGeneratorProperties.class, "synchronizedGeneratorProperties.xsd",
                "http://www.powsybl.org/schema/iidm/ext/synchronized_generator_properties/1_0", "sdgp");
    }

    @Override
    public void write(SynchronizedGeneratorProperties synchronizedGeneratorProperties, SerializerContext context) {
        context.getWriter().writeStringAttribute("type", synchronizedGeneratorProperties.getType());
        context.getWriter().writeBooleanAttribute("rpcl2", synchronizedGeneratorProperties.getRpcl2());
    }

    @Override
    public SynchronizedGeneratorProperties read(Generator generator, DeserializerContext context) {
        String type = context.getReader().readStringAttribute("type");
        boolean rpcl2 = context.getReader().readBooleanAttribute("rpcl2");
        context.getReader().readEndNode();
        return generator.newExtension(SynchronizedGeneratorPropertiesAdder.class)
                .withType(type)
                .withRpcl2(rpcl2)
                .add();
    }

}
