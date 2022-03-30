/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.exceptions.UncheckedXmlStreamException;
import com.powsybl.commons.extensions.AbstractExtensionXmlSerializer;
import com.powsybl.commons.extensions.ExtensionXmlSerializer;
import com.powsybl.commons.xml.XmlReaderContext;
import com.powsybl.commons.xml.XmlUtil;
import com.powsybl.commons.xml.XmlWriterContext;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.xml.NetworkXmlReaderContext;
import com.powsybl.iidm.xml.NetworkXmlWriterContext;

import javax.xml.stream.XMLStreamException;
import java.util.Iterator;
import java.util.List;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 * <p>
 * WARNING: this class is still in a beta version, it will change in the future
 */
@AutoService(ExtensionXmlSerializer.class)
public class CgmesIidmMappingXmlSerializer extends AbstractExtensionXmlSerializer<Network, CgmesIidmMapping> {

    private static final String SOURCE = "source";

    public CgmesIidmMappingXmlSerializer() {
        super("cgmesIidmMapping", "network", CgmesIidmMapping.class, true, "cgmesIidmMapping.xsd",
                "http://www.powsybl.org/schema/iidm/ext/cgmes_iidm_mapping/1_0", "ci");
    }

    @Override
    public void write(CgmesIidmMapping extension, XmlWriterContext context) throws XMLStreamException {
        NetworkXmlWriterContext networkContext = (NetworkXmlWriterContext) context;
        extension.getUnmappedTopologicalNodes().forEach(cgmesTopologicalNode -> {
            try {
                context.getWriter().writeEmptyElement(getNamespaceUri(), "unmappedTopologicalNode");
                context.getWriter().writeAttribute("id", cgmesTopologicalNode.getCgmesId());
                context.getWriter().writeAttribute("name", cgmesTopologicalNode.getName());
                context.getWriter().writeAttribute(SOURCE, cgmesTopologicalNode.getSource().name());
            } catch (XMLStreamException e) {
                throw new UncheckedXmlStreamException(e);
            }
        });
        extension.getExtendable().getBusView().getBusStream()
                .filter(b -> extension.isTopologicalNodeMapped(b.getId()))
                .forEach(b -> {
                    try {
                        context.getWriter().writeStartElement(getNamespaceUri(), "link");
                        writeBusIdentification(b, networkContext);
                        writeTopologicalNodes(extension, b, context);
                        context.getWriter().writeEndElement();
                    } catch (XMLStreamException e) {
                        throw new UncheckedXmlStreamException(e);
                    }
                });
    }

    @Override
    public CgmesIidmMapping read(Network extendable, XmlReaderContext context) throws XMLStreamException {
        NetworkXmlReaderContext networkContext = (NetworkXmlReaderContext) context;
        CgmesIidmMappingAdder mappingAdder = extendable.newExtension(CgmesIidmMappingAdder.class);
        mappingAdder.add();
        CgmesIidmMapping mapping = extendable.getExtension(CgmesIidmMapping.class);
        XmlUtil.readUntilEndElement("cgmesIidmMapping", context.getReader(), () -> {
            switch (context.getReader().getLocalName()) {
                case "link":
                    String busId = readBusIdentification(extendable, networkContext);
                    readTopologicalNodes(context, mapping, busId);
                    break;
                case "unmappedTopologicalNode":
                    String name = context.getReader().getAttributeValue(null, "name");
                    String id = context.getReader().getAttributeValue(null, "id");
                    String sourceTN = context.getReader().getAttributeValue(null, SOURCE);
                    mapping.putUnmappedTopologicalNode(id, name, Source.valueOf(sourceTN));
                    break;
                default:
                    throw new PowsyblException("Unknown element name <" + context.getReader().getLocalName() + "> in <cgmesIidmMapping>");
            }
        });
        mapping.addTopologyListener();
        return mapping;
    }

    /**
     * A {@link CgmesIidmMapping} extension is serializable if it contains at least one link
     *
     * @param cgmesIidmMapping The extension to check
     * @return true if it contains at least one link, false if not
     */
    @Override
    public boolean isSerializable(CgmesIidmMapping cgmesIidmMapping) {
        return !cgmesIidmMapping.isTopologicalNodeEmpty();
    }

    private static void writeBusIdentification(Bus b, NetworkXmlWriterContext context) throws XMLStreamException {
        // BusView buses have a calculated bus identifier that should not be used as a persistent identification
        // We write a bus reference using the equipment and side of the first connected terminal
        Iterator<? extends Terminal> it = b.getConnectedTerminals().iterator();
        if (!it.hasNext()) {
            throw new PowsyblException("bus does not have connected terminals " + b.getId());
        }
        Terminal t = it.next();
        context.getWriter().writeAttribute("equipmentId", context.getAnonymizer().anonymizeString(t.getConnectable().getId()));
        context.getWriter().writeAttribute("side", Integer.toString(terminalSide(t, t.getConnectable())));
    }

    private void writeTopologicalNodes(CgmesIidmMapping extension, Bus b, XmlWriterContext context) {
        extension.getTopologicalNodes(b.getId()).forEach(ctn -> {
            try {
                context.getWriter().writeEmptyElement(getNamespaceUri(), "topologicalNode");
                writeTopologicalNode(ctn, context);
            } catch (XMLStreamException e) {
                throw new UncheckedXmlStreamException(e);
            }
        });
    }

    private void writeTopologicalNode(CgmesIidmMapping.CgmesTopologicalNode cgmesTopologicalNode, XmlWriterContext context) throws XMLStreamException {
        context.getWriter().writeAttribute("id", cgmesTopologicalNode.getCgmesId());
        context.getWriter().writeAttribute("name", cgmesTopologicalNode.getName());
        context.getWriter().writeAttribute(SOURCE, cgmesTopologicalNode.getSource().name());
    }

    private static String readBusIdentification(Network network, NetworkXmlReaderContext context) {
        String equipmentId = context.getAnonymizer().deanonymizeString(context.getReader().getAttributeValue(null, "equipmentId"));
        int side = Integer.parseInt(context.getReader().getAttributeValue(null, "side"));
        Identifiable i = network.getIdentifiable(equipmentId);
        if (!(i instanceof Connectable)) {
            throw new PowsyblException("Equipment is not connectable " + equipmentId);
        }
        String busId = terminalSide((Connectable) i, side).getBusView().getBus().getId();
        return busId;
    }

    private void readTopologicalNodes(XmlReaderContext context, CgmesIidmMapping mapping, String busId) throws XMLStreamException {
        XmlUtil.readUntilEndElement("link", context.getReader(), () -> {
            switch (context.getReader().getLocalName()) {
                case "topologicalNode":
                    String name = context.getReader().getAttributeValue(null, "name");
                    String id = context.getReader().getAttributeValue(null, "id");
                    String sourceTN = context.getReader().getAttributeValue(null, SOURCE);
                    mapping.putTopologicalNode(busId, id, name, Source.valueOf(sourceTN));
                    break;
                default:
                    throw new PowsyblException("Unknown element name <" + context.getReader().getLocalName() + "> in <cgmesIidmMapping>");
            }
        });
    }

    private static Terminal terminalSide(Connectable c, int side) {
        List<? extends Terminal> l = c.getTerminals();
        Terminal t = l.get(side - 1);
        return t;
    }

    private static int terminalSide(Terminal t, Connectable c) {
        if (c instanceof Injection) {
            return 1;
        }
        for (int k = 0; k < c.getTerminals().size(); k++) {
            if (t == c.getTerminals().get(k)) {
                return k + 1;
            }
        }
        throw new PowsyblException("terminal not found in connectable " + c.getId());
    }
}
