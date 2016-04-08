/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.xml;

import eu.itesla_project.iidm.network.*;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.util.List;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class DanglingLineXml extends ConnectableXml<DanglingLine, DanglingLineAdder, VoltageLevel> {

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
    protected void writeRootElementAttributes(DanglingLine dl, VoltageLevel vl, XmlWriterContext context) throws XMLStreamException {
        writeFloat("p0", dl.getP0(), context.getWriter());
        writeFloat("q0", dl.getQ0(), context.getWriter());
        writeFloat("r", dl.getR(), context.getWriter());
        writeFloat("x", dl.getX(), context.getWriter());
        writeFloat("g", dl.getG(), context.getWriter());
        writeFloat("b", dl.getB(), context.getWriter());
        if (dl.getUcteXnodeCode() != null) {
            context.getWriter().writeAttribute("ucteXnodeCode", dl.getUcteXnodeCode());
        }
        writeNodeOrBus(null, dl.getTerminal(), context);
        writePQ(null, dl.getTerminal(), context.getWriter());
    }

    @Override
    protected void writeSubElements(DanglingLine dl, VoltageLevel vl, XmlWriterContext context) throws XMLStreamException {
        if (dl.getCurrentLimits() != null) {
            writeCurrentLimits(null, dl.getCurrentLimits(), context.getWriter());
        }
    }

    @Override
    protected DanglingLineAdder createAdder(VoltageLevel vl) {
        return vl.newDanglingLine();
    }

    @Override
    protected DanglingLine readRootElementAttributes(DanglingLineAdder adder, XMLStreamReader reader, List<Runnable> endTasks) {
        float p0 = readFloatAttribute(reader, "p0");
        float q0 = readFloatAttribute(reader, "q0");
        float r = readFloatAttribute(reader, "r");
        float x = readFloatAttribute(reader, "x");
        float g = readFloatAttribute(reader, "g");
        float b = readFloatAttribute(reader, "b");
        String ucteXnodeCode = reader.getAttributeValue(null, "ucteXnodeCode");
        readNodeOrBus(adder, reader);
        DanglingLine dl = adder.setP0(p0)
                .setQ0(q0)
                .setR(r)
                .setX(x)
                .setG(g)
                .setB(b)
                .setUcteXnodeCode(ucteXnodeCode)
                .add();
        readPQ(null, dl.getTerminal(), reader);
        return dl;
    }

    @Override
    protected void readSubElements(DanglingLine dl, XMLStreamReader reader, List<Runnable> endTasks) throws XMLStreamException {
        readUntilEndRootElement(reader, () -> {
            if ("currentLimits".equals(reader.getLocalName())) {
                readCurrentLimits(null, dl::newCurrentLimits, reader);
            } else {
                super.readSubElements(dl, reader, endTasks);
            }
        });
    }
}
