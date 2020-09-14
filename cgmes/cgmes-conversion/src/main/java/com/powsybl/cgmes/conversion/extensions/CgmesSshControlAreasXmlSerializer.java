/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.cgmes.conversion.extensions.CgmesSshControlAreasImpl.ControlArea;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.extensions.AbstractExtensionXmlSerializer;
import com.powsybl.commons.extensions.ExtensionXmlSerializer;
import com.powsybl.commons.xml.XmlReaderContext;
import com.powsybl.commons.xml.XmlUtil;
import com.powsybl.commons.xml.XmlWriterContext;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.xml.NetworkXmlReaderContext;
import com.powsybl.iidm.xml.NetworkXmlWriterContext;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
@AutoService(ExtensionXmlSerializer.class)
public class CgmesSshControlAreasXmlSerializer extends AbstractExtensionXmlSerializer<Network, CgmesSshControlAreas> {

    public CgmesSshControlAreasXmlSerializer() {
        super("cgmesSshControlAreas", "network", CgmesSshControlAreas.class, true, "cgmesSshControlAreas.xsd",
                "http://www.powsybl.org/schema/iidm/ext/cgmes_ssh_control_areas/1_0", "csm");
    }

    @Override
    public void write(CgmesSshControlAreas extension, XmlWriterContext context) throws XMLStreamException {
        NetworkXmlWriterContext networkContext = (NetworkXmlWriterContext) context;
        XMLStreamWriter writer = networkContext.getWriter();
        for (ControlArea controlArea : extension.getControlAreas()) {
            writer.writeStartElement(getNamespaceUri(), "controlArea");
            writer.writeAttribute("id", controlArea.getId());
            XmlUtil.writeDouble("netInterchange", controlArea.getNetInterchange(), writer);
            XmlUtil.writeDouble("pTolerance", controlArea.getPTolerance(), writer);
            writer.writeEndElement();
        }
    }

    @Override
    public CgmesSshControlAreas read(Network extendable, XmlReaderContext context) throws XMLStreamException {
        NetworkXmlReaderContext networkContext = (NetworkXmlReaderContext) context;
        XMLStreamReader reader = networkContext.getReader();
        CgmesSshControlAreasAdder adder = extendable.newExtension(CgmesSshControlAreasAdder.class);
        XmlUtil.readUntilEndElement("cgmesSshControlAreas", reader, () -> {
            if (reader.getLocalName().equals("controlArea")) {
                ControlArea controlArea = new ControlArea(reader.getAttributeValue(null, "id"),
                    XmlUtil.readDoubleAttribute(reader, "netInterchange"),
                    XmlUtil.readDoubleAttribute(reader, "pTolerance"));
                adder.addControlArea(controlArea);
            } else {
                throw new PowsyblException("Unknown element name <" + reader.getLocalName() + "> in <cgmesSshControlAreas>");
            }
        });
        adder.add();
        return extendable.getExtension(CgmesSshControlAreas.class);
    }
}
