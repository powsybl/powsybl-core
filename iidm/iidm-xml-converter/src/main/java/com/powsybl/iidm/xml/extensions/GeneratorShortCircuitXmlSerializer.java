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
import com.powsybl.commons.xml.XmlReaderContext;
import com.powsybl.commons.xml.XmlUtil;
import com.powsybl.commons.xml.XmlWriterContext;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.extensions.GeneratorShortCircuit;
import com.powsybl.iidm.network.extensions.GeneratorShortCircuitAdder;

import javax.xml.stream.XMLStreamException;

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
    public void write(GeneratorShortCircuit generatorShortCircuit, XmlWriterContext context) throws XMLStreamException {
        XmlUtil.writeDouble("directSubtransX", generatorShortCircuit.getDirectSubtransX(), context.getWriter());
        XmlUtil.writeDouble("directTransX", generatorShortCircuit.getDirectTransX(), context.getWriter());
        XmlUtil.writeDouble("stepUpTransformerX", generatorShortCircuit.getStepUpTransformerX(), context.getWriter());
    }

    @Override
    public GeneratorShortCircuit read(Generator generator, XmlReaderContext context) throws XMLStreamException {
        double directSubtransX = XmlUtil.readOptionalDoubleAttribute(context.getReader(), "directSubtransX");
        double directTransX = XmlUtil.readDoubleAttribute(context.getReader(), "directTransX");
        double stepUpTransformerX = XmlUtil.readOptionalDoubleAttribute(context.getReader(), "stepUpTransformerX");
        generator.newExtension(GeneratorShortCircuitAdder.class)
                .withDirectSubtransX(directSubtransX)
                .withDirectTransX(directTransX)
                .withStepUpTransformerX(stepUpTransformerX)
                .add();
        return generator.getExtension(GeneratorShortCircuit.class);
    }
}
