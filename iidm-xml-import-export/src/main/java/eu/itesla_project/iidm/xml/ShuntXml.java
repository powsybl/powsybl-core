/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.xml;

import eu.itesla_project.iidm.network.ShuntCompensator;
import eu.itesla_project.iidm.network.ShuntCompensatorAdder;
import eu.itesla_project.iidm.network.VoltageLevel;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.util.List;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class ShuntXml extends ConnectableXml<ShuntCompensator, ShuntCompensatorAdder, VoltageLevel> {

    static final ShuntXml INSTANCE = new ShuntXml();

    static final String ROOT_ELEMENT_NAME = "shunt";

    @Override
    protected String getRootElementName() {
        return ROOT_ELEMENT_NAME;
    }

    @Override
    protected boolean hasSubElements(ShuntCompensator sc) {
        return false;
    }

    @Override
    protected void writeRootElementAttributes(ShuntCompensator sc, VoltageLevel vl, XmlWriterContext context) throws XMLStreamException {
        XmlUtil.writeFloat("bPerSection", sc.getbPerSection(), context.getWriter());
        context.getWriter().writeAttribute("maximumSectionCount", Integer.toString(sc.getMaximumSectionCount()));
        context.getWriter().writeAttribute("currentSectionCount", Integer.toString(sc.getCurrentSectionCount()));
        writeNodeOrBus(null, sc.getTerminal(), context);
        writePQ(null, sc.getTerminal(), context.getWriter());
    }

    @Override
    protected void writeSubElements(ShuntCompensator sc, VoltageLevel vl, XmlWriterContext context) throws XMLStreamException {
    }

    @Override
    protected ShuntCompensatorAdder createAdder(VoltageLevel vl) {
        return vl.newShunt();
    }

    @Override
    protected ShuntCompensator readRootElementAttributes(ShuntCompensatorAdder adder, XMLStreamReader reader, List<Runnable> endTasks) {
        float bPerSection = XmlUtil.readFloatAttribute(reader, "bPerSection");
        int maximumSectionCount = XmlUtil.readIntAttribute(reader, "maximumSectionCount");
        int currentSectionCount = XmlUtil.readIntAttribute(reader, "currentSectionCount");
        adder.setbPerSection(bPerSection)
                .setMaximumSectionCount(maximumSectionCount)
                .setCurrentSectionCount(currentSectionCount);
        readNodeOrBus(adder, reader);
        ShuntCompensator sc = adder.add();
        readPQ(null, sc.getTerminal(), reader);
        return sc;
    }

    @Override
    protected void readSubElements(ShuntCompensator sc, XMLStreamReader reader, List<Runnable> endTasks) throws XMLStreamException {
        readUntilEndRootElement(reader, () -> ShuntXml.super.readSubElements(sc, reader,endTasks));
    }
}
