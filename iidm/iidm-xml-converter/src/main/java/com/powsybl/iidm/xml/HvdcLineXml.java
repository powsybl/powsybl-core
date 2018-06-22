/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import com.powsybl.commons.xml.XmlUtil;
import com.powsybl.iidm.network.HvdcLine;
import com.powsybl.iidm.network.HvdcLineAdder;
import com.powsybl.iidm.network.Network;

import javax.xml.stream.XMLStreamException;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
class HvdcLineXml extends AbstractIdentifiableXml<HvdcLine, HvdcLineAdder, Network> {

    static final HvdcLineXml INSTANCE = new HvdcLineXml();

    static final String ROOT_ELEMENT_NAME = "hvdcLine";

    @Override
    protected String getRootElementName() {
        return ROOT_ELEMENT_NAME;
    }

    @Override
    protected boolean hasSubElements(HvdcLine identifiable) {
        return false;
    }

    @Override
    protected void writeRootElementAttributes(HvdcLine l, Network parent, NetworkXmlWriterContext context) throws XMLStreamException {
        XmlUtil.writeDouble("r", l.getR(), context.getWriter());
        XmlUtil.writeDouble("nominalV", l.getNominalV(), context.getWriter());
        context.getWriter().writeAttribute("convertersMode", l.getConvertersMode().name());
        XmlUtil.writeDouble("activePowerSetpoint", l.getActivePowerSetpoint(), context.getWriter());
        XmlUtil.writeDouble("maxP", l.getMaxP(), context.getWriter());
        context.getWriter().writeAttribute("converterStation1", l.getConverterStation1().getId());
        context.getWriter().writeAttribute("converterStation2", l.getConverterStation2().getId());
    }

    @Override
    protected HvdcLineAdder createAdder(Network n) {
        return n.newHvdcLine();
    }

    @Override
    protected HvdcLine readRootElementAttributes(HvdcLineAdder adder, NetworkXmlReaderContext context) {
        double r = XmlUtil.readDoubleAttribute(context.getReader(), "r");
        double nominalV = XmlUtil.readDoubleAttribute(context.getReader(), "nominalV");
        HvdcLine.ConvertersMode convertersMode = HvdcLine.ConvertersMode.valueOf(context.getReader().getAttributeValue(null, "convertersMode"));
        double activePowerSetpoint = XmlUtil.readDoubleAttribute(context.getReader(), "activePowerSetpoint");
        double maxP = XmlUtil.readDoubleAttribute(context.getReader(), "maxP");
        String converterStation1 = context.getReader().getAttributeValue(null, "converterStation1");
        String converterStation2 = context.getReader().getAttributeValue(null, "converterStation2");
        return adder.setR(r)
                .setNominalV(nominalV)
                .setConvertersMode(convertersMode)
                .setActivePowerSetpoint(activePowerSetpoint)
                .setMaxP(maxP)
                .setConverterStationId1(converterStation1)
                .setConverterStationId2(converterStation2)
                .add();
    }

    @Override
    protected void readSubElements(HvdcLine l, NetworkXmlReaderContext context) throws XMLStreamException {
        readUntilEndRootElement(context.getReader(), () -> HvdcLineXml.super.readSubElements(l, context));
    }
}
