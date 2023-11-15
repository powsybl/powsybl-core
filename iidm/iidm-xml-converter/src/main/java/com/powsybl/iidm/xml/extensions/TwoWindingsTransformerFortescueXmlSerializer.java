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
import com.powsybl.iidm.network.TwoWindingsTransformer;
import com.powsybl.iidm.network.extensions.WindingConnectionType;
import com.powsybl.iidm.network.extensions.TwoWindingsTransformerFortescue;
import com.powsybl.iidm.network.extensions.TwoWindingsTransformerFortescueAdder;

import javax.xml.stream.XMLStreamException;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
@AutoService(ExtensionXmlSerializer.class)
public class TwoWindingsTransformerFortescueXmlSerializer extends AbstractExtensionXmlSerializer<TwoWindingsTransformer, TwoWindingsTransformerFortescue> {

    public TwoWindingsTransformerFortescueXmlSerializer() {
        super("twoWindingsTransformerFortescue", "network", TwoWindingsTransformerFortescue.class, false,
                "twoWindingsTransformerFortescue_V1_0.xsd", "http://www.powsybl.org/schema/iidm/ext/two_windings_transformer_fortescue/1_0",
                "t2f");
    }

    @Override
    public void write(TwoWindingsTransformerFortescue twtFortescue, XmlWriterContext context) throws XMLStreamException {
        XmlUtil.writeOptionalDouble("rz", twtFortescue.getRz(), Double.NaN, context.getWriter());
        XmlUtil.writeOptionalDouble("xz", twtFortescue.getXz(), Double.NaN, context.getWriter());
        context.getWriter().writeAttribute("freeFluxes", Boolean.toString(twtFortescue.isFreeFluxes()));
        context.getWriter().writeAttribute("connectionType1", twtFortescue.getConnectionType1().name());
        context.getWriter().writeAttribute("connectionType2", twtFortescue.getConnectionType2().name());
        XmlUtil.writeOptionalDouble("groundingR1", twtFortescue.getGroundingR1(), 0, context.getWriter());
        XmlUtil.writeOptionalDouble("groundingX1", twtFortescue.getGroundingX1(), 0, context.getWriter());
        XmlUtil.writeOptionalDouble("groundingR2", twtFortescue.getGroundingR2(), 0, context.getWriter());
        XmlUtil.writeOptionalDouble("groundingX2", twtFortescue.getGroundingX2(), 0, context.getWriter());
    }

    @Override
    public TwoWindingsTransformerFortescue read(TwoWindingsTransformer twt, XmlReaderContext context) throws XMLStreamException {
        double rz = XmlUtil.readOptionalDoubleAttribute(context.getReader(), "rz");
        double xz = XmlUtil.readOptionalDoubleAttribute(context.getReader(), "xz");
        boolean freeFluxes = XmlUtil.readBoolAttribute(context.getReader(), "freeFluxes");
        WindingConnectionType connectionType1 = WindingConnectionType.valueOf(context.getReader().getAttributeValue(null, "connectionType1"));
        WindingConnectionType connectionType2 = WindingConnectionType.valueOf(context.getReader().getAttributeValue(null, "connectionType2"));
        double groundingR1 = XmlUtil.readOptionalDoubleAttribute(context.getReader(), "groundingR1", 0);
        double groundingX1 = XmlUtil.readOptionalDoubleAttribute(context.getReader(), "groundingX1", 0);
        double groundingR2 = XmlUtil.readOptionalDoubleAttribute(context.getReader(), "groundingR2", 0);
        double groundingX2 = XmlUtil.readOptionalDoubleAttribute(context.getReader(), "groundingX2", 0);
        return twt.newExtension(TwoWindingsTransformerFortescueAdder.class)
                .withRz(rz)
                .withXz(xz)
                .withFreeFluxes(freeFluxes)
                .withConnectionType1(connectionType1)
                .withConnectionType2(connectionType2)
                .withGroundingR1(groundingR1)
                .withGroundingX1(groundingX1)
                .withGroundingR2(groundingR2)
                .withGroundingX2(groundingX2)
                .add();
    }
}
