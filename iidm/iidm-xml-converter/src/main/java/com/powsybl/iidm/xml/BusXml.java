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

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class BusXml extends AbstractIdentifiableXml<Bus, BusAdder, VoltageLevel> {

    static final BusXml INSTANCE = new BusXml();

    static final String ROOT_ELEMENT_NAME = "bus";

    boolean hasStateValues(Bus bus) {
        return !Double.isNaN(bus.getV())  || !Double.isNaN(bus.getAngle());
    }

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
        if (!context.getOptions().isIncrementalConversion() ||
                context.getTargetFile() == IncrementalIidmFiles.STATE) {
            XmlUtil.writeDouble("v", b.getV(), context.getWriter());
            XmlUtil.writeDouble("angle", b.getAngle(), context.getWriter());
        }
    }

    static void updateBusStateValues(XMLStreamReader reader, VoltageLevel[] vl, IncrementalIidmFiles targetFile) {
        if (targetFile != IncrementalIidmFiles.STATE) {
            return;
        }
        String id = reader.getAttributeValue(null, "id");
        double v = XmlUtil.readDoubleAttribute(reader, "v");
        double angle = XmlUtil.readDoubleAttribute(reader, "angle");
        Bus b = vl[0].getBusBreakerView().getBus(id);
        if (b == null) {
            b = vl[0].getBusView().getBus(id);
        }
        b.setV(v > 0 ? v : Double.NaN).setAngle(angle);
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
        return b;
    }

    @Override
    protected void readSubElements(Bus b, NetworkXmlReaderContext context) throws XMLStreamException {
        readUntilEndRootElement(context.getReader(), () -> BusXml.super.readSubElements(b, context));
    }
}
