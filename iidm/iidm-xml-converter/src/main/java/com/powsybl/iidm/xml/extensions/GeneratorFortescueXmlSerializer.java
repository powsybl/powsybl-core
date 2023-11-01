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
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
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
        XmlUtil.writeOptionalDouble("rz", generatorFortescue.getRz(), Double.NaN, context.getWriter());
        XmlUtil.writeOptionalDouble("xz", generatorFortescue.getXz(), Double.NaN, context.getWriter());
        XmlUtil.writeOptionalDouble("rn", generatorFortescue.getRn(), Double.NaN, context.getWriter());
        XmlUtil.writeOptionalDouble("xn", generatorFortescue.getXn(), Double.NaN, context.getWriter());
        context.getWriter().writeAttribute("grounded", Boolean.toString(generatorFortescue.isGrounded()));
        XmlUtil.writeOptionalDouble("groundingR", generatorFortescue.getGroundingR(), 0, context.getWriter());
        XmlUtil.writeOptionalDouble("groundingX", generatorFortescue.getGroundingX(), 0, context.getWriter());
    }

    @Override
    public GeneratorFortescue read(Generator generator, XmlReaderContext context) throws XMLStreamException {
        double rz = XmlUtil.readOptionalDoubleAttribute(context.getReader(), "rz");
        double xz = XmlUtil.readOptionalDoubleAttribute(context.getReader(), "xz");
        double rn = XmlUtil.readOptionalDoubleAttribute(context.getReader(), "rn");
        double xn = XmlUtil.readOptionalDoubleAttribute(context.getReader(), "xn");
        boolean toGround = XmlUtil.readBoolAttribute(context.getReader(), "grounded");
        double groundingR = XmlUtil.readOptionalDoubleAttribute(context.getReader(), "groundingR", 0);
        double groundingX = XmlUtil.readOptionalDoubleAttribute(context.getReader(), "groundingX", 0);
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
