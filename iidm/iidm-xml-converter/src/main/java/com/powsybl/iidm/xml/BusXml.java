/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import com.powsybl.commons.xml.XmlUtil;
import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.BusAdder;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.xml.util.IidmXmlUtil;

import javax.xml.stream.XMLStreamException;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class BusXml extends AbstractIdentifiableXml<Bus, BusAdder, VoltageLevel> {

    static final BusXml INSTANCE = new BusXml();

    static final String ROOT_ELEMENT_NAME = "bus";

    @Override
    protected String getRootElementName() {
        return ROOT_ELEMENT_NAME;
    }

    @Override
    protected boolean hasSubElements(Bus b) {
        return false;
    }

    @Override
    protected void writeRootElementAttributes(Bus b, VoltageLevel vl, NetworkXmlWriterContext context) throws XMLStreamException {
        XmlUtil.writeDouble("v", b.getV(), context.getWriter());
        XmlUtil.writeDouble("angle", b.getAngle(), context.getWriter());
        IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_8, context, () -> {
            XmlUtil.writeOptionalDouble("fictitiousP0", b.getFictitiousP0(), 0.0, context.getWriter());
            XmlUtil.writeOptionalDouble("fictitiousQ0", b.getFictitiousQ0(), 0.0, context.getWriter());
        });
    }

    @Override
    protected void writeSubElements(Bus b, VoltageLevel vl, NetworkXmlWriterContext context) throws XMLStreamException {
        // no sub elements to write
    }

    @Override
    protected BusAdder createAdder(VoltageLevel vl) {
        return vl.getBusBreakerView().newBus();
    }

    @Override
    protected Bus readRootElementAttributes(BusAdder adder, NetworkXmlReaderContext context) {
        double v = XmlUtil.readOptionalDoubleAttribute(context.getReader(), "v");
        double angle = XmlUtil.readOptionalDoubleAttribute(context.getReader(), "angle");
        Bus b = adder.add();
        b.setV(v);
        b.setAngle(angle);
        IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_8, context, () -> {
            double p0 = XmlUtil.readOptionalDoubleAttribute(context.getReader(), "fictitiousP0");
            double q0 = XmlUtil.readOptionalDoubleAttribute(context.getReader(), "fictitiousQ0");
            if (!Double.isNaN(p0)) {
                b.setFictitiousP0(p0);
            }
            if (!Double.isNaN(q0)) {
                b.setFictitiousQ0(q0);
            }
        });
        return b;
    }

    @Override
    protected void readSubElements(Bus b, NetworkXmlReaderContext context) throws XMLStreamException {
        readUntilEndRootElement(context.getReader(), () -> BusXml.super.readSubElements(b, context));
    }
}
