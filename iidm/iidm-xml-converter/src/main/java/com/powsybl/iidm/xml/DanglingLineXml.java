/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import com.powsybl.commons.xml.XmlUtil;
import com.powsybl.iidm.network.DanglingLine;
import com.powsybl.iidm.network.DanglingLineAdder;
import com.powsybl.iidm.network.VoltageLevel;

import javax.xml.stream.XMLStreamException;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class DanglingLineXml extends AbstractConnectableXml<DanglingLine, DanglingLineAdder, VoltageLevel> {

    static final DanglingLineXml INSTANCE = new DanglingLineXml();

    static final String ROOT_ELEMENT_NAME = "danglingLine";

    @Override
    protected String getRootElementName() {
        return ROOT_ELEMENT_NAME;
    }

    @Override
    protected boolean hasSubElements(DanglingLine dl) {
        return dl.getCurrentLimits() != null;
    }

    @Override
    protected void writeRootElementAttributes(DanglingLine dl, VoltageLevel vl, NetworkXmlWriterContext context) throws XMLStreamException {
        XmlUtil.writeFloat("p0", dl.getP0(), context.getWriter());
        XmlUtil.writeFloat("q0", dl.getQ0(), context.getWriter());
        XmlUtil.writeFloat("r", dl.getR(), context.getWriter());
        XmlUtil.writeFloat("x", dl.getX(), context.getWriter());
        XmlUtil.writeFloat("g", dl.getG(), context.getWriter());
        XmlUtil.writeFloat("b", dl.getB(), context.getWriter());
        if (dl.getUcteXnodeCode() != null) {
            context.getWriter().writeAttribute("ucteXnodeCode", dl.getUcteXnodeCode());
        }
        writeNodeOrBus(null, dl.getTerminal(), context);
        writePQ(null, dl.getTerminal(), context.getWriter());
    }

    @Override
    protected void writeSubElements(DanglingLine dl, VoltageLevel vl, NetworkXmlWriterContext context) throws XMLStreamException {
        if (dl.getCurrentLimits() != null) {
            writeCurrentLimits(null, dl.getCurrentLimits(), context.getWriter());
        }
    }

    @Override
    protected DanglingLineAdder createAdder(VoltageLevel vl) {
        return vl.newDanglingLine();
    }

    @Override
    protected DanglingLine readRootElementAttributes(DanglingLineAdder adder, NetworkXmlReaderContext context) {
        float p0 = XmlUtil.readFloatAttribute(context.getReader(), "p0");
        float q0 = XmlUtil.readFloatAttribute(context.getReader(), "q0");
        float r = XmlUtil.readFloatAttribute(context.getReader(), "r");
        float x = XmlUtil.readFloatAttribute(context.getReader(), "x");
        float g = XmlUtil.readFloatAttribute(context.getReader(), "g");
        float b = XmlUtil.readFloatAttribute(context.getReader(), "b");
        String ucteXnodeCode = context.getReader().getAttributeValue(null, "ucteXnodeCode");
        readNodeOrBus(adder, context);
        DanglingLine dl = adder.setP0(p0)
                .setQ0(q0)
                .setR(r)
                .setX(x)
                .setG(g)
                .setB(b)
                .setUcteXnodeCode(ucteXnodeCode)
                .add();
        readPQ(null, dl.getTerminal(), context.getReader());
        return dl;
    }

    @Override
    protected void readSubElements(DanglingLine dl, NetworkXmlReaderContext context) throws XMLStreamException {
        readUntilEndRootElement(context.getReader(), () -> {
            if ("currentLimits".equals(context.getReader().getLocalName())) {
                readCurrentLimits(null, dl::newCurrentLimits, context.getReader());
            } else {
                super.readSubElements(dl, context);
            }
        });
    }
}
