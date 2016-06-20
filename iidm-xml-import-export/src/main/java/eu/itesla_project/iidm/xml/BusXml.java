/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.xml;

import eu.itesla_project.iidm.network.Bus;
import eu.itesla_project.iidm.network.BusAdder;
import eu.itesla_project.iidm.network.VoltageLevel;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.util.List;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class BusXml extends IdentifiableXml<Bus, BusAdder, VoltageLevel> {

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
    protected void writeRootElementAttributes(Bus b, VoltageLevel vl, XmlWriterContext context) throws XMLStreamException {
        XmlUtil.writeFloat("v", b.getV(), context.getWriter());
        XmlUtil.writeFloat("angle", b.getAngle(), context.getWriter());
    }

    @Override
    protected void writeSubElements(Bus b, VoltageLevel vl, XmlWriterContext context) throws XMLStreamException {
    }

    @Override
    protected BusAdder createAdder(VoltageLevel vl) {
        return vl.getBusBreakerView().newBus();
    }

    @Override
    protected Bus readRootElementAttributes(BusAdder adder, XMLStreamReader reader, List<Runnable> endTasks) {
        float v = XmlUtil.readOptionalFloatAttribute(reader, "v");
        float angle = XmlUtil.readOptionalFloatAttribute(reader, "angle");
        Bus b = adder.add();
        b.setV(v);
        b.setAngle(angle);
        return b;
    }

    @Override
    protected void readSubElements(Bus b, XMLStreamReader reader, List<Runnable> endTasks) throws XMLStreamException {
        readUntilEndRootElement(reader, () -> BusXml.super.readSubElements(b, reader,endTasks));
    }
}