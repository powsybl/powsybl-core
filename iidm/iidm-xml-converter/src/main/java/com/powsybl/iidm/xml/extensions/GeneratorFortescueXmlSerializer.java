/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
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
import com.powsybl.iidm.network.extensions.GeneratorFortescue;
import com.powsybl.iidm.network.extensions.GeneratorFortescueAdder;

import javax.xml.stream.XMLStreamException;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@AutoService(ExtensionXmlSerializer.class)
public class GeneratorFortescueXmlSerializer extends AbstractExtensionXmlSerializer<Generator, GeneratorFortescue> {

    public GeneratorFortescueXmlSerializer() {
        super("generatorFortescue", "network", GeneratorFortescue.class, false,
                "generatorFortescue_V1_0.xsd", "http://www.powsybl.org/schema/iidm/ext/generator_fortescue/1_0",
                "gf");
    }

    @Override
    public void write(GeneratorFortescue generatorFortescue, XmlWriterContext context) throws XMLStreamException {
        context.getWriter().writeAttribute("generatorType", generatorFortescue.getGeneratorType().name());
        XmlUtil.writeOptionalDouble("ro", generatorFortescue.getRo(), Double.NaN, context.getWriter());
        XmlUtil.writeOptionalDouble("xo", generatorFortescue.getXo(), Double.NaN, context.getWriter());
        XmlUtil.writeOptionalDouble("ri", generatorFortescue.getRi(), Double.NaN, context.getWriter());
        XmlUtil.writeOptionalDouble("xi", generatorFortescue.getXi(), Double.NaN, context.getWriter());
        context.getWriter().writeAttribute("toGround", Boolean.toString(generatorFortescue.isToGround()));
        XmlUtil.writeDouble("groundingR", generatorFortescue.getGroundingR(), context.getWriter());
        XmlUtil.writeDouble("groundingX", generatorFortescue.getGroundingX(), context.getWriter());
    }

    @Override
    public GeneratorFortescue read(Generator generator, XmlReaderContext context) throws XMLStreamException {
        GeneratorFortescue.GeneratorType generatorType = GeneratorFortescue.GeneratorType.valueOf(context.getReader().getAttributeValue(null, "generatorType"));
        double ro = XmlUtil.readOptionalDoubleAttribute(context.getReader(), "ro");
        double xo = XmlUtil.readOptionalDoubleAttribute(context.getReader(), "xo");
        double ri = XmlUtil.readOptionalDoubleAttribute(context.getReader(), "ri");
        double xi = XmlUtil.readOptionalDoubleAttribute(context.getReader(), "xi");
        boolean toGround = XmlUtil.readBoolAttribute(context.getReader(), "toGround");
        double groundingR = XmlUtil.readDoubleAttribute(context.getReader(), "groundingR");
        double groundingX = XmlUtil.readDoubleAttribute(context.getReader(), "groundingX");
        return generator.newExtension(GeneratorFortescueAdder.class)
                .withGeneratorType(generatorType)
                .withRo(ro)
                .withXo(xo)
                .withRi(ri)
                .withXi(xi)
                .withToGround(toGround)
                .withGroundingR(groundingR)
                .withGroundingX(groundingX)
                .add();
    }
}
