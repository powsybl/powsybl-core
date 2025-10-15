package com.powsybl.iidm.serde.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.commons.extensions.AbstractExtensionSerDe;
import com.powsybl.commons.extensions.ExtensionSerDe;
import com.powsybl.commons.io.DeserializerContext;
import com.powsybl.commons.io.SerializerContext;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.extensions.SynchronousGeneratorProperties;
import com.powsybl.iidm.network.extensions.SynchronousGeneratorPropertiesAdder;

/**
 * @author Gautier Bureau {@literal <gautier.bureau at rte-france.com>}
 */
@AutoService(ExtensionSerDe.class)
public class SynchronousGeneratorPropertiesSerDe extends AbstractExtensionSerDe<Generator, SynchronousGeneratorProperties> {

    public SynchronousGeneratorPropertiesSerDe() {
        super(SynchronousGeneratorProperties.NAME, "network", SynchronousGeneratorProperties.class, "synchronousGeneratorProperties.xsd",
                "http://www.powsybl.org/schema/iidm/ext/synchronous_generator_properties/1_0", "sgp");
    }

    @Override
    public void write(SynchronousGeneratorProperties synchronousGeneratorProperties, SerializerContext context) {
        context.getWriter().writeIntAttribute("numberOfWindings", synchronousGeneratorProperties.getNumberOfWindings());
        context.getWriter().writeStringAttribute("governor", synchronousGeneratorProperties.getGovernor());
        context.getWriter().writeStringAttribute("voltageRegulator", synchronousGeneratorProperties.getVoltageRegulator());
        context.getWriter().writeStringAttribute("pss", synchronousGeneratorProperties.getPss());
        context.getWriter().writeBooleanAttribute("auxiliaries", synchronousGeneratorProperties.getAuxiliaries());
        context.getWriter().writeBooleanAttribute("internalTransformer", synchronousGeneratorProperties.getInternalTransformer());
        context.getWriter().writeBooleanAttribute("rpcl", synchronousGeneratorProperties.getRpcl());
        context.getWriter().writeBooleanAttribute("rpcl2", synchronousGeneratorProperties.getRpcl2());
        context.getWriter().writeStringAttribute("uva", synchronousGeneratorProperties.getUva());
        context.getWriter().writeBooleanAttribute("fictitious", synchronousGeneratorProperties.getFictitious());
        context.getWriter().writeBooleanAttribute("qlim", synchronousGeneratorProperties.getQlim());
    }

    @Override
    public SynchronousGeneratorProperties read(Generator generator, DeserializerContext context) {
        int numberOfWindings = context.getReader().readIntAttribute("numberOfWindings");
        String governor = context.getReader().readStringAttribute("governor");
        String voltageRegulator = context.getReader().readStringAttribute("voltageRegulator");
        String pss = context.getReader().readStringAttribute("pss");
        boolean auxiliaries = context.getReader().readBooleanAttribute("auxiliaries");
        boolean internalTransformer = context.getReader().readBooleanAttribute("internalTransformer");
        boolean rpcl = context.getReader().readBooleanAttribute("rpcl");
        boolean rpcl2 = context.getReader().readBooleanAttribute("rpcl2");
        String uva = context.getReader().readStringAttribute("uva");
        boolean fictitious = context.getReader().readBooleanAttribute("fictitious");
        boolean qlim = context.getReader().readBooleanAttribute("qlim");
        context.getReader().readEndNode();
        return generator.newExtension(SynchronousGeneratorPropertiesAdder.class)
                .withNumberOfWindings(numberOfWindings)
                .withGovernor(governor)
                .withVoltageRegulator(voltageRegulator)
                .withPss(pss)
                .withAuxiliaries(auxiliaries)
                .withInternalTransformer(internalTransformer)
                .withRpcl(rpcl)
                .withRpcl2(rpcl2)
                .withUva(uva)
                .withFictitious(fictitious)
                .withQlim(qlim)
                .add();
    }

}
