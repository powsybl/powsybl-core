/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.serializer.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.commons.extensions.AbstractExtensionSerializer;
import com.powsybl.commons.extensions.ExtensionSerializer;
import com.powsybl.commons.io.ReaderContext;
import com.powsybl.commons.io.WriterContext;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.extensions.GeneratorFortescue;
import com.powsybl.iidm.network.extensions.GeneratorFortescueAdder;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
@AutoService(ExtensionSerializer.class)
public class GeneratorFortescueSerializer extends AbstractExtensionSerializer<Generator, GeneratorFortescue> {

    public GeneratorFortescueSerializer() {
        super("generatorFortescue", "network", GeneratorFortescue.class,
                "generatorFortescue_V1_0.xsd", "http://www.powsybl.org/schema/iidm/ext/generator_fortescue/1_0",
                "gf");
    }

    @Override
    public void write(GeneratorFortescue generatorFortescue, WriterContext context) {
        context.getWriter().writeDoubleAttribute("rz", generatorFortescue.getRz(), Double.NaN);
        context.getWriter().writeDoubleAttribute("xz", generatorFortescue.getXz(), Double.NaN);
        context.getWriter().writeDoubleAttribute("rn", generatorFortescue.getRn(), Double.NaN);
        context.getWriter().writeDoubleAttribute("xn", generatorFortescue.getXn(), Double.NaN);
        context.getWriter().writeBooleanAttribute("grounded", generatorFortescue.isGrounded());
        context.getWriter().writeDoubleAttribute("groundingR", generatorFortescue.getGroundingR(), 0);
        context.getWriter().writeDoubleAttribute("groundingX", generatorFortescue.getGroundingX(), 0);
    }

    @Override
    public GeneratorFortescue read(Generator generator, ReaderContext context) {
        double rz = context.getReader().readDoubleAttribute("rz");
        double xz = context.getReader().readDoubleAttribute("xz");
        double rn = context.getReader().readDoubleAttribute("rn");
        double xn = context.getReader().readDoubleAttribute("xn");
        boolean toGround = context.getReader().readBooleanAttribute("grounded");
        double groundingR = context.getReader().readDoubleAttribute("groundingR", 0);
        double groundingX = context.getReader().readDoubleAttribute("groundingX", 0);
        context.getReader().readEndNode();
        return generator.newExtension(GeneratorFortescueAdder.class)
                .withRz(rz)
                .withXz(xz)
                .withRn(rn)
                .withXn(xn)
                .withGrounded(toGround)
                .withGroundingR(groundingR)
                .withGroundingX(groundingX)
                .add();
    }
}
