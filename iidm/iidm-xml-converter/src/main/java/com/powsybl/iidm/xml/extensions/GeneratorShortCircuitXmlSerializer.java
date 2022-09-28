/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.commons.extensions.AbstractExtensionXmlSerializer;
import com.powsybl.commons.extensions.ExtensionXmlSerializer;
import com.powsybl.commons.extensions.XmlReaderContext;
import com.powsybl.commons.extensions.XmlWriterContext;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.extensions.GeneratorShortCircuit;
import com.powsybl.iidm.network.extensions.GeneratorShortCircuitAdder;

/**
 *
 * @author Coline Piloquet <coline.piloquet@rte-france.fr>
 */
@AutoService(ExtensionXmlSerializer.class)
public class GeneratorShortCircuitXmlSerializer extends AbstractExtensionXmlSerializer<Generator, GeneratorShortCircuit> {

    public GeneratorShortCircuitXmlSerializer() {
        super("generatorShortCircuit", "network", GeneratorShortCircuit.class, false,
                "generatorShortCircuit.xsd", "http://www.itesla_project.eu/schema/iidm/ext/generator_short_circuit/1_0",
                "gsc");
    }

    @Override
    public void write(GeneratorShortCircuit generatorShortCircuit, XmlWriterContext context) {
        context.getWriter().writeDoubleAttribute("directSubtransX", generatorShortCircuit.getDirectSubtransX());
        context.getWriter().writeDoubleAttribute("directTransX", generatorShortCircuit.getDirectTransX());
        context.getWriter().writeDoubleAttribute("stepUpTransformerX", generatorShortCircuit.getStepUpTransformerX());
    }

    @Override
    public GeneratorShortCircuit read(Generator generator, XmlReaderContext context) {
        double directSubtransX = context.getReader().readDoubleAttribute("directSubtransX");
        double directTransX = context.getReader().readDoubleAttribute("directTransX");
        double stepUpTransformerX = context.getReader().readDoubleAttribute("stepUpTransformerX");
        return generator.newExtension(GeneratorShortCircuitAdder.class)
                .withDirectSubtransX(directSubtransX)
                .withDirectTransX(directTransX)
                .withStepUpTransformerX(stepUpTransformerX)
                .add();
    }
}
