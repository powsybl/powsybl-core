/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.commons.extensions.AbstractExtensionSerDe;
import com.powsybl.commons.extensions.ExtensionSerDe;
import com.powsybl.commons.io.DeserializerContext;
import com.powsybl.commons.io.SerializerContext;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.extensions.GeneratorShortCircuit;
import com.powsybl.iidm.network.extensions.GeneratorShortCircuitAdder;

/**
 *
 * @author Coline Piloquet {@literal <coline.piloquet@rte-france.fr>}
 */
@AutoService(ExtensionSerDe.class)
public class GeneratorShortCircuitSerDe extends AbstractExtensionSerDe<Generator, GeneratorShortCircuit> {

    public GeneratorShortCircuitSerDe() {
        super("generatorShortCircuit", "network", GeneratorShortCircuit.class,
                "generatorShortCircuit.xsd", "http://www.itesla_project.eu/schema/iidm/ext/generator_short_circuit/1_0",
                "gsc");
    }

    @Override
    public void write(GeneratorShortCircuit generatorShortCircuit, SerializerContext context) {
        context.getWriter().writeDoubleAttribute("directSubtransX", generatorShortCircuit.getDirectSubtransX());
        context.getWriter().writeDoubleAttribute("directTransX", generatorShortCircuit.getDirectTransX());
        context.getWriter().writeDoubleAttribute("stepUpTransformerX", generatorShortCircuit.getStepUpTransformerX());
    }

    @Override
    public GeneratorShortCircuit read(Generator generator, DeserializerContext context) {
        double directSubtransX = context.getReader().readDoubleAttribute("directSubtransX");
        double directTransX = context.getReader().readDoubleAttribute("directTransX");
        double stepUpTransformerX = context.getReader().readDoubleAttribute("stepUpTransformerX");
        context.getReader().readEndNode();
        return generator.newExtension(GeneratorShortCircuitAdder.class)
                .withDirectSubtransX(directSubtransX)
                .withDirectTransX(directTransX)
                .withStepUpTransformerX(stepUpTransformerX)
                .add();
    }
}
