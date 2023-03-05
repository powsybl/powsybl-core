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
import com.powsybl.iidm.network.extensions.LegConnectionType;
import com.powsybl.iidm.network.extensions.TwoWindingsTransformerFortescue;
import com.powsybl.iidm.network.extensions.TwoWindingsTransformerFortescueAdder;

import javax.xml.stream.XMLStreamException;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
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
        context.getWriter().writeAttribute("partOfGeneratingUnit", Boolean.toString(twtFortescue.isPartOfGeneratingUnit()));
        XmlUtil.writeOptionalDouble("ro", twtFortescue.getRo(), Double.NaN, context.getWriter());
        XmlUtil.writeOptionalDouble("xo", twtFortescue.getXo(), Double.NaN, context.getWriter());
        context.getWriter().writeAttribute("freeFluxes", Boolean.toString(twtFortescue.isFreeFluxes()));
        context.getWriter().writeAttribute("leg1ConnectionType", twtFortescue.getLeg1ConnectionType().name());
        context.getWriter().writeAttribute("leg2ConnectionType", twtFortescue.getLeg2ConnectionType().name());
        XmlUtil.writeOptionalDouble("groundingR1", twtFortescue.getGroundingR1(), 0, context.getWriter());
        XmlUtil.writeOptionalDouble("groundingX1", twtFortescue.getGroundingX1(), 0, context.getWriter());
        XmlUtil.writeOptionalDouble("groundingR2", twtFortescue.getGroundingR2(), 0, context.getWriter());
        XmlUtil.writeOptionalDouble("groundingX2", twtFortescue.getGroundingX2(), 0, context.getWriter());
    }

    @Override
    public TwoWindingsTransformerFortescue read(TwoWindingsTransformer twt, XmlReaderContext context) throws XMLStreamException {
        boolean partOfGeneratingUnit = XmlUtil.readBoolAttribute(context.getReader(), "partOfGeneratingUnit");
        double ro = XmlUtil.readOptionalDoubleAttribute(context.getReader(), "ro");
        double xo = XmlUtil.readOptionalDoubleAttribute(context.getReader(), "xo");
        boolean freeFluxes = XmlUtil.readBoolAttribute(context.getReader(), "freeFluxes");
        LegConnectionType leg1ConnectionType = LegConnectionType.valueOf(context.getReader().getAttributeValue(null, "leg1ConnectionType"));
        LegConnectionType leg2ConnectionType = LegConnectionType.valueOf(context.getReader().getAttributeValue(null, "leg2ConnectionType"));
        double groundingR1 = XmlUtil.readOptionalDoubleAttribute(context.getReader(), "groundingR1", 0);
        double groundingX1 = XmlUtil.readOptionalDoubleAttribute(context.getReader(), "groundingX1", 0);
        double groundingR2 = XmlUtil.readOptionalDoubleAttribute(context.getReader(), "groundingR2", 0);
        double groundingX2 = XmlUtil.readOptionalDoubleAttribute(context.getReader(), "groundingX2", 0);
        return twt.newExtension(TwoWindingsTransformerFortescueAdder.class)
                .withPartOfGeneratingUnit(partOfGeneratingUnit)
                .withRo(ro)
                .withXo(xo)
                .withFreeFluxes(freeFluxes)
                .withLeg1ConnectionType(leg1ConnectionType)
                .withLeg2ConnectionType(leg2ConnectionType)
                .withGroundingR1(groundingR1)
                .withGroundingX1(groundingX1)
                .withGroundingR2(groundingR2)
                .withGroundingX2(groundingX2)
                .add();
    }
}
