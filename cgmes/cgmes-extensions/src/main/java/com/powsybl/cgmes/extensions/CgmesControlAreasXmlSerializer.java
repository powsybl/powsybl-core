/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.extensions.AbstractExtensionXmlSerializer;
import com.powsybl.commons.extensions.ExtensionXmlSerializer;
import com.powsybl.commons.xml.XmlReaderContext;
import com.powsybl.commons.xml.XmlUtil;
import com.powsybl.commons.xml.XmlWriterContext;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.xml.NetworkXmlReaderContext;
import com.powsybl.iidm.xml.NetworkXmlWriterContext;
import com.powsybl.iidm.xml.TerminalRefXml;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

/**
 * @author Marcos de Miguel <demiguelm at aia.es>
 */
@AutoService(ExtensionXmlSerializer.class)
public class CgmesControlAreasXmlSerializer extends AbstractExtensionXmlSerializer<Network, CgmesControlAreas> {

    private static final String CONTROL_AREA = "controlArea";

    public CgmesControlAreasXmlSerializer() {
        super("cgmesControlAreas", "network", CgmesControlAreas.class, true, "cgmesControlAreas.xsd",
                "http://www.powsybl.org/schema/iidm/ext/cgmes_control_areas/1_0", "cca");
    }

    @Override
    public boolean isSerializable(CgmesControlAreas extension) {
        return !extension.getCgmesControlAreas().isEmpty();
    }

    @Override
    public void write(CgmesControlAreas extension, XmlWriterContext context) throws XMLStreamException {
        NetworkXmlWriterContext networkContext = (NetworkXmlWriterContext) context;
        XMLStreamWriter writer = networkContext.getWriter();
        for (CgmesControlArea controlArea : extension.getCgmesControlAreas()) {
            writer.writeStartElement(getNamespaceUri(), CONTROL_AREA);
            writer.writeAttribute("id", controlArea.getId());
            writer.writeAttribute("name", controlArea.getName());
            if (controlArea.getEnergyIdentificationCodeEIC() != null) {
                writer.writeAttribute("energyIdentificationCodeEic", controlArea.getEnergyIdentificationCodeEIC());
            }
            XmlUtil.writeDouble("netInterchange", controlArea.getNetInterchange(), writer);
            for (Terminal terminal : controlArea.getTerminals()) {
                TerminalRefXml.writeTerminalRef(terminal, networkContext, getNamespaceUri(), "terminal");
            }
            for (Boundary boundary : controlArea.getBoundaries()) {
                if (boundary.getDanglingLine() != null) { // TODO: delete this later, only for compatibility
                    writer.writeEmptyElement(getNamespaceUri(), "boundary");
                    writer.writeAttribute("id", networkContext.getAnonymizer().anonymizeString(boundary.getDanglingLine().getId()));

                 // TODO use TieLine Id and DanglingLine Id for reference instead of TieLine Id and Side
                    Branch.Side side = getSide(boundary);
                    if (side != null) {
                        writer.writeAttribute("side", side.name());
                    }
                }
            }
            writer.writeEndElement();
        }
    }

    private static Branch.Side getSide(Boundary boundary) {
        // a TieLine with two dangingLines inside
        return boundary.getDanglingLine().getTieLine().map(tl -> {
            if (tl.getDanglingLine1() == boundary.getDanglingLine()) {
                return Branch.Side.ONE;
            }
            return Branch.Side.TWO;
        }).orElse(null);
    }

    @Override
    public CgmesControlAreas read(Network extendable, XmlReaderContext context) throws XMLStreamException {
        NetworkXmlReaderContext networkContext = (NetworkXmlReaderContext) context;
        XMLStreamReader reader = networkContext.getReader();
        extendable.newExtension(CgmesControlAreasAdder.class).add();
        CgmesControlAreas mapping = extendable.getExtension(CgmesControlAreas.class);
        XmlUtil.readUntilEndElement(getExtensionName(), reader, () -> {
            if (reader.getLocalName().equals(CONTROL_AREA)) {
                CgmesControlArea cgmesControlArea = mapping.newCgmesControlArea()
                        .setId(reader.getAttributeValue(null, "id"))
                        .setName(reader.getAttributeValue(null, "name"))
                        .setEnergyIdentificationCodeEic(reader.getAttributeValue(null, "energyIdentificationCodeEic"))
                        .setNetInterchange(XmlUtil.readOptionalDoubleAttribute(reader, "netInterchange"))
                        .add();
                readBoundariesAndTerminals(networkContext, reader, cgmesControlArea, extendable);
            } else {
                throw new PowsyblException("Unknown element name <" + reader.getLocalName() + "> in <cgmesControlArea>");
            }
        });
        return extendable.getExtension(CgmesControlAreas.class);
    }

    private void readBoundariesAndTerminals(NetworkXmlReaderContext networkContext, XMLStreamReader reader, CgmesControlArea cgmesControlArea, Network network) throws XMLStreamException {
        XmlUtil.readUntilEndElement(CONTROL_AREA, reader, () -> {
            String id;
            String side;
            switch (reader.getLocalName()) {
                case "boundary":
                    id = networkContext.getAnonymizer().deanonymizeString(networkContext.getReader().getAttributeValue(null, "id"));
                    Identifiable identifiable = network.getIdentifiable(id);
                    if (identifiable instanceof DanglingLine dl) {
                        cgmesControlArea.add(dl.getBoundary());
                    } else if (identifiable instanceof TieLine tl) {
                        side = networkContext.getReader().getAttributeValue(null, "side");
                        cgmesControlArea.add(tl.getDanglingLine(Branch.Side.valueOf(side)).getBoundary());
                    } else {
                        throw new PowsyblException("Unexpected Identifiable instance: " + identifiable.getClass());
                    }
                    break;
                case "terminal":
                    id = networkContext.getAnonymizer().deanonymizeString(networkContext.getReader().getAttributeValue(null, "id"));
                    side = networkContext.getReader().getAttributeValue(null, "side");
                    cgmesControlArea.add(TerminalRefXml.readTerminalRef(network, id, side));
                    break;
                default:
                    throw new PowsyblException("Unknown element name <" + reader.getLocalName() + "> in <controlArea>");
            }
        });
    }
}
