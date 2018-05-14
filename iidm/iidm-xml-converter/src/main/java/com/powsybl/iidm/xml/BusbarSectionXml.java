/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import com.powsybl.commons.xml.XmlUtil;
import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.BusbarSection;
import com.powsybl.iidm.network.BusbarSectionAdder;
import com.powsybl.iidm.network.VoltageLevel;

import javax.xml.stream.XMLStreamException;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class BusbarSectionXml extends AbstractIdentifiableXml<BusbarSection, BusbarSectionAdder, VoltageLevel> {

    static final BusbarSectionXml INSTANCE = new BusbarSectionXml();

    static final String ROOT_ELEMENT_NAME = "busbarSection";

    @Override
    protected String getRootElementName() {
        return ROOT_ELEMENT_NAME;
    }

    @Override
    protected boolean hasSubElements(BusbarSection bs) {
        return false;
    }

    @Override
    protected void writeRootElementAttributes(BusbarSection bs, VoltageLevel vl, NetworkXmlWriterContext context) throws XMLStreamException {
        XmlUtil.writeInt("node", bs.getTerminal().getNodeBreakerView().getNode(), context.getWriter());
        XmlUtil.writeDouble("v", bs.getV(), context.getWriter());
        XmlUtil.writeDouble("angle", bs.getAngle(), context.getWriter());
    }

    @Override
    protected BusbarSectionAdder createAdder(VoltageLevel vl) {
        return vl.getNodeBreakerView().newBusbarSection();
    }

    @Override
    protected BusbarSection readRootElementAttributes(BusbarSectionAdder adder, NetworkXmlReaderContext context) {
        int node = XmlUtil.readIntAttribute(context.getReader(), "node");
        double v = XmlUtil.readOptionalDoubleAttribute(context.getReader(), "v");
        double angle = XmlUtil.readOptionalDoubleAttribute(context.getReader(), "angle");
        BusbarSection bbs = adder.setNode(node)
                .add();
        context.getEndTasks().add(() -> {
            Bus b = bbs.getTerminal().getBusView().getBus();
            if (b != null) {
                b.setV(v).setAngle(angle);
            }
        });
        return bbs;
    }

    @Override
    protected void readSubElements(BusbarSection bs, NetworkXmlReaderContext context) throws XMLStreamException {
        readUntilEndRootElement(context.getReader(), () -> BusbarSectionXml.super.readSubElements(bs, context));
    }
}
