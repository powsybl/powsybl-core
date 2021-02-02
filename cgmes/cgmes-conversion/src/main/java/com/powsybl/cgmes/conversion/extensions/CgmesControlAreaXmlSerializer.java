/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion.extensions;

import com.google.auto.service.AutoService;
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
 * @author Marcos de Miguel <demiguelm at aia.es>
 */
@AutoService(ExtensionXmlSerializer.class)
public class CgmesControlAreaXmlSerializer extends AbstractExtensionXmlSerializer<Network, CgmesControlAreaMapping> {

    public CgmesControlAreaXmlSerializer() {
        super("cgmesControlArea", "network", CgmesControlAreaMapping.class, true, "cgmesControlArea.xsd",
                "http://www.powsybl.org/schema/iidm/ext/cgmes_control_area/1_0", "cca");
    }

    @Override
    public void write(CgmesControlAreaMapping extension, XmlWriterContext context) throws XMLStreamException {
        NetworkXmlWriterContext networkContext = (NetworkXmlWriterContext) context;
        XMLStreamWriter writer = networkContext.getWriter();
        for (Object id : extension.getCgmesControlAreaIds()) {
            String cgmesControlAreaId = (String) id;
            writer.writeStartElement(getNamespaceUri(), "controlArea");
            writer.writeAttribute("id", cgmesControlAreaId);
            writer.writeAttribute("name", extension.getCgmesControlArea(cgmesControlAreaId).getName());
            writer.writeAttribute("energyIdentCodeEic", extension.getCgmesControlArea(cgmesControlAreaId).getEnergyIdentCodeEic());
            XmlUtil.writeDouble("netInterchange", extension.getCgmesControlArea(cgmesControlAreaId).getNetInterchange(), writer);
            for (CgmesControlArea.EquipmentEnd eqEnd : extension.getTerminals(cgmesControlAreaId)) {
                writer.writeStartElement(getNamespaceUri(), "terminal");
                writer.writeAttribute("equipment", eqEnd.getEquipmentId());
                XmlUtil.writeInt("end", eqEnd.getEnd(), writer);
                writer.writeEndElement();
            }
            writer.writeEndElement();
        }
    }

    @Override
    public CgmesControlAreaMapping read(Network extendable, XmlReaderContext context) throws XMLStreamException {
        NetworkXmlReaderContext networkContext = (NetworkXmlReaderContext) context;
        XMLStreamReader reader = networkContext.getReader();
        CgmesControlAreaAdder adder = extendable.newExtension(CgmesControlAreaAdder.class);
        XmlUtil.readUntilEndElement("cgmesControlArea", reader, () -> {
            if (reader.getLocalName().equals("controlArea")) {
                CgmesControlArea cgmesControlArea = adder.newCgmesControlArea(reader.getAttributeValue(null, "id"),
                        reader.getAttributeValue(null, "name"),
                        reader.getAttributeValue(null, "energyIdentCodeEic"),
                        XmlUtil.readDoubleAttribute(reader, "netInterchange"));
                readTerminals(networkContext, reader, cgmesControlArea);
            } else {
                throw new PowsyblException("Unknown element name <" + reader.getLocalName() + "> in <cgmesControlArea>");
            }
        });
        adder.add();
        return extendable.getExtension(CgmesControlAreaMapping.class);
    }

    private void readTerminals(NetworkXmlReaderContext networkContext, XMLStreamReader reader, CgmesControlArea cgmesControlArea) throws XMLStreamException {
        XmlUtil.readUntilEndElement("controlArea", reader, () -> {
            if (reader.getLocalName().equals("terminal")) {
                cgmesControlArea.addTerminal(reader.getAttributeValue(null, "equipment"),
                        XmlUtil.readIntAttribute(reader, "end"));
            } else {
                throw new PowsyblException("Unknown element name <" + reader.getLocalName() + "> in <controlArea>");
            }
        });
    }
}
