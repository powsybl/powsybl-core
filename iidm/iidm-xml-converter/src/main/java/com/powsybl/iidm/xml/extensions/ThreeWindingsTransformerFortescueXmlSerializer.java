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
import com.powsybl.iidm.network.ThreeWindingsTransformer;
import com.powsybl.iidm.network.extensions.ThreeWindingsTransformerFortescue;
import com.powsybl.iidm.network.extensions.ThreeWindingsTransformerFortescueAdder;
import com.powsybl.iidm.network.extensions.WindingConnectionType;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@AutoService(ExtensionXmlSerializer.class)
public class ThreeWindingsTransformerFortescueXmlSerializer extends AbstractExtensionXmlSerializer<ThreeWindingsTransformer, ThreeWindingsTransformerFortescue> {

    public ThreeWindingsTransformerFortescueXmlSerializer() {
        super("threeWindingsTransformerFortescue", "network", ThreeWindingsTransformerFortescue.class, true,
                "threeWindingsTransformerFortescue_V1_0.xsd", "http://www.powsybl.org/schema/iidm/ext/three_windings_transformer_fortescue/1_0",
                "t3f");
    }

    private static void writeLeg(ThreeWindingsTransformerFortescue.LegFortescue legFortescue, XMLStreamWriter writer) throws XMLStreamException {
        XmlUtil.writeOptionalDouble("rz", legFortescue.getRz(), Double.NaN, writer);
        XmlUtil.writeOptionalDouble("xz", legFortescue.getXz(), Double.NaN, writer);
        writer.writeAttribute("freeFluxes", Boolean.toString(legFortescue.isFreeFluxes()));
        writer.writeAttribute("connectionType", legFortescue.getConnectionType().name());
        XmlUtil.writeOptionalDouble("groundingR", legFortescue.getGroundingR(), 0, writer);
        XmlUtil.writeOptionalDouble("groundingX", legFortescue.getGroundingX(), 0, writer);
    }

    @Override
    public void write(ThreeWindingsTransformerFortescue twtFortescue, XmlWriterContext context) throws XMLStreamException {
        context.getWriter().writeEmptyElement(getNamespaceUri(), "leg1");
        writeLeg(twtFortescue.getLeg1(), context.getWriter());
        context.getWriter().writeEmptyElement(getNamespaceUri(), "leg2");
        writeLeg(twtFortescue.getLeg2(), context.getWriter());
        context.getWriter().writeEmptyElement(getNamespaceUri(), "leg3");
        writeLeg(twtFortescue.getLeg3(), context.getWriter());
    }

    private void readLeg(ThreeWindingsTransformerFortescueAdder.LegFortescueAdder legAdder, XMLStreamReader reader) {
        double rz = XmlUtil.readOptionalDoubleAttribute(reader, "rz");
        double xz = XmlUtil.readOptionalDoubleAttribute(reader, "xz");
        boolean freeFluxes = XmlUtil.readBoolAttribute(reader, "freeFluxes");
        WindingConnectionType connectionType = WindingConnectionType.valueOf(reader.getAttributeValue(null, "connectionType"));
        double groundingR = XmlUtil.readOptionalDoubleAttribute(reader, "groundingR", 0);
        double groundingX = XmlUtil.readOptionalDoubleAttribute(reader, "groundingX", 0);
        legAdder.withRz(rz)
                .withXz(xz)
                .withFreeFluxes(freeFluxes)
                .withConnectionType(connectionType)
                .withGroundingR(groundingR)
                .withGroundingX(groundingX);
    }

    @Override
    public ThreeWindingsTransformerFortescue read(ThreeWindingsTransformer twt, XmlReaderContext context) throws XMLStreamException {
        ThreeWindingsTransformerFortescueAdder fortescueAdder = twt.newExtension(ThreeWindingsTransformerFortescueAdder.class);

        XmlUtil.readUntilEndElement(getExtensionName(), context.getReader(), () -> {
            switch (context.getReader().getLocalName()) {
                case "leg1":
                    readLeg(fortescueAdder.leg1(), context.getReader());
                    break;
                case "leg2":
                    readLeg(fortescueAdder.leg2(), context.getReader());
                    break;
                case "leg3":
                    readLeg(fortescueAdder.leg3(), context.getReader());
                    break;
                default:
                    break;
            }
        });

        return fortescueAdder.add();
    }
}
