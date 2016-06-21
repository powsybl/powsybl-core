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
class LoadXml extends ConnectableXml<Load, LoadAdder, VoltageLevel> {

    static final LoadXml INSTANCE = new LoadXml();

    static final String ROOT_ELEMENT_NAME = "load";

    @Override
    protected String getRootElementName() {
        return ROOT_ELEMENT_NAME;
    }

    @Override
    protected boolean hasSubElements(Load l) {
        return false;
    }

    @Override
    protected void writeRootElementAttributes(Load l, VoltageLevel vl, XmlWriterContext context) throws XMLStreamException {
        context.getWriter().writeAttribute("loadType", l.getLoadType().name());
        XmlUtil.writeFloat("p0", l.getP0(), context.getWriter());
        XmlUtil.writeFloat("q0", l.getQ0(), context.getWriter());
        writeNodeOrBus(null, l.getTerminal(), context);
        writePQ(null, l.getTerminal(), context.getWriter());
    }

    @Override
    protected void writeSubElements(Load l, VoltageLevel vl, XmlWriterContext context) throws XMLStreamException {
    }

    @Override
    protected LoadAdder createAdder(VoltageLevel vl) {
        return vl.newLoad();
    }

    @Override
    protected Load readRootElementAttributes(LoadAdder adder, XMLStreamReader reader, List<Runnable> endTasks) {
        String loadTypeStr = reader.getAttributeValue(null, "loadType");
        LoadType loadType = loadTypeStr == null ? LoadType.UNDEFINED : LoadType.valueOf(loadTypeStr);
        float p0 = XmlUtil.readFloatAttribute(reader, "p0");
        float q0 = XmlUtil.readFloatAttribute(reader, "q0");
        readNodeOrBus(adder, reader);
        Load l = adder.setLoadType(loadType)
                .setP0(p0)
                .setQ0(q0)
                .add();
        readPQ(null, l.getTerminal(), reader);
        return l;
    }

    @Override
    protected void readSubElements(Load l, XMLStreamReader reader, List<Runnable> endTasks) throws XMLStreamException {
        readUntilEndRootElement(reader, () -> LoadXml.super.readSubElements(l, reader,endTasks));
    }
}
