/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.extensions.AbstractExtensionXmlSerializer;
import com.powsybl.commons.extensions.ExtensionXmlSerializer;
import com.powsybl.commons.extensions.XmlReaderContext;
import com.powsybl.commons.extensions.XmlWriterContext;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.xml.NetworkXmlReaderContext;
import com.powsybl.iidm.xml.NetworkXmlWriterContext;

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
    public void write(CgmesIidmMapping extension, XmlWriterContext context) {
        NetworkXmlWriterContext networkContext = (NetworkXmlWriterContext) context;
        extension.getUnmappedTopologicalNodes().forEach(cgmesTopologicalNode -> {
            context.getWriter().writeStartNode(getNamespaceUri(), "unmappedTopologicalNode");
            context.getWriter().writeStringAttribute("id", cgmesTopologicalNode.getCgmesId());
            context.getWriter().writeStringAttribute("name", cgmesTopologicalNode.getName());
            context.getWriter().writeEnumAttribute(SOURCE, cgmesTopologicalNode.getSource());
            context.getWriter().writeEndNode();
        });
        extension.getExtendable().getBusView().getBusStream()
                .filter(b -> extension.isTopologicalNodeMapped(b.getId()))
                .forEach(b -> {
                    context.getWriter().writeStartNode(getNamespaceUri(), "link");
                    writeBusIdentification(b, networkContext);
                    writeTopologicalNodes(extension, b, context);
                    context.getWriter().writeEndNode();
                });
    }

    @Override
    public CgmesIidmMapping read(Network extendable, XmlReaderContext context) {
        NetworkXmlReaderContext networkContext = (NetworkXmlReaderContext) context;
        CgmesIidmMappingAdder mappingAdder = extendable.newExtension(CgmesIidmMappingAdder.class);
        mappingAdder.add();
        CgmesIidmMapping mapping = extendable.getExtension(CgmesIidmMapping.class);
        context.getReader().readUntilEndNode("cgmesIidmMapping", () -> {
            switch (context.getReader().getNodeName()) {
                case "link":
                    String busId = readBusIdentification(extendable, networkContext);
                    readTopologicalNodes(context, mapping, busId);
                    break;
                case "unmappedTopologicalNode":
                    String name = context.getReader().readStringAttribute("name");
                    String id = context.getReader().readStringAttribute("id");
                    String sourceTN = context.getReader().readStringAttribute(SOURCE);
                    mapping.putUnmappedTopologicalNode(id, name, Source.valueOf(sourceTN));
                    break;
                default:
                    throw new PowsyblException("Unknown element name <" + context.getReader().getNodeName() + "> in <cgmesIidmMapping>");
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

    private static void writeBusIdentification(Bus b, NetworkXmlWriterContext context) {
        // BusView buses have a calculated bus identifier that should not be used as a persistent identification
        // We write a bus reference using the equipment and side of the first connected terminal
        Iterator<? extends Terminal> it = b.getConnectedTerminals().iterator();
        if (!it.hasNext()) {
            throw new PowsyblException("bus does not have connected terminals " + b.getId());
        }
        Terminal t = it.next();
        context.getWriter().writeStringAttribute("equipmentId", context.getAnonymizer().anonymizeString(t.getConnectable().getId()));
        context.getWriter().writeIntAttribute("side", terminalSide(t, t.getConnectable()));
    }

    private void writeTopologicalNodes(CgmesIidmMapping extension, Bus b, XmlWriterContext context) {
        extension.getTopologicalNodes(b.getId()).forEach(ctn -> {
            context.getWriter().writeStartNode(getNamespaceUri(), "topologicalNode");
            writeTopologicalNode(ctn, context);
            context.getWriter().writeEndNode();
        });
    }

    private void writeTopologicalNode(CgmesIidmMapping.CgmesTopologicalNode cgmesTopologicalNode, XmlWriterContext context) {
        context.getWriter().writeStringAttribute("id", cgmesTopologicalNode.getCgmesId());
        context.getWriter().writeStringAttribute("name", cgmesTopologicalNode.getName());
        context.getWriter().writeEnumAttribute(SOURCE, cgmesTopologicalNode.getSource());
    }

    private static String readBusIdentification(Network network, NetworkXmlReaderContext context) {
        String equipmentId = context.getAnonymizer().deanonymizeString(context.getReader().readStringAttribute("equipmentId"));
        int side = context.getReader().readIntAttribute("side");
        Identifiable i = network.getIdentifiable(equipmentId);
        if (!(i instanceof Connectable)) {
            throw new PowsyblException("Equipment is not connectable " + equipmentId);
        }
        return terminalSide((Connectable) i, side).getBusView().getBus().getId();
    }

    private void readTopologicalNodes(XmlReaderContext context, CgmesIidmMapping mapping, String busId) {
        context.getReader().readUntilEndNode("link", () -> {
            switch (context.getReader().getNodeName()) {
                case "topologicalNode":
                    String name = context.getReader().readStringAttribute("name");
                    String id = context.getReader().readStringAttribute("id");
                    String sourceTN = context.getReader().readStringAttribute(SOURCE);
                    mapping.putTopologicalNode(busId, id, name, Source.valueOf(sourceTN));
                    break;
                default:
                    throw new PowsyblException("Unknown element name <" + context.getReader().getNodeName() + "> in <cgmesIidmMapping>");
            }
        });
    }

    private static Terminal terminalSide(Connectable c, int side) {
        List<? extends Terminal> l = c.getTerminals();
        return l.get(side - 1);
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
