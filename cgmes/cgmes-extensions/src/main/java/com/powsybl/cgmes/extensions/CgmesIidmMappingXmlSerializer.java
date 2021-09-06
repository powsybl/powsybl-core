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

    public CgmesIidmMappingXmlSerializer() {
        super("cgmesIidmMapping", "network", CgmesIidmMapping.class, true, "cgmesIidmMapping.xsd",
                "http://www.powsybl.org/schema/iidm/ext/cgmes_iidm_mapping/1_0", "ci");
    }

    @Override
    public void write(CgmesIidmMapping extension, XmlWriterContext context) throws XMLStreamException {
        NetworkXmlWriterContext networkContext = (NetworkXmlWriterContext) context;
        if (!extension.getUnmappedTopologicalNodes().isEmpty()) {
            context.getWriter().writeAttribute("unmappedTopologicalNodeIds", String.join(",", extension.getUnmappedTopologicalNodes()));
        }
        if (!extension.getUnmappedBaseVoltages().isEmpty()) {
            context.getWriter().writeAttribute("unmappedBaseVoltageIds", String.join(",", extension.getUnmappedBaseVoltages()));
        }
        extension.getExtendable().getBusView().getBusStream()
                .filter(b -> extension.isTopologicalNodeMapped(b.getId()))
                .forEach(b -> {
                    try {
                        context.getWriter().writeEmptyElement(getNamespaceUri(), "link");
                        writeBusIdentification(b, networkContext);
                        context.getWriter().writeAttribute("topologicalNodeIds", String.join(",", extension.getTopologicalNodes(b.getId())));
                    } catch (XMLStreamException e) {
                        throw new UncheckedXmlStreamException(e);
                    }
                });
        extension.getBaseVoltages()
                .forEach((nominalV, baseVoltage) -> {
                    try {
                        context.getWriter().writeEmptyElement(getNamespaceUri(), "base");
                        context.getWriter().writeAttribute("nominalVoltage", Double.toString(nominalV));
                        context.getWriter().writeAttribute("baseVoltage", baseVoltage);
                    } catch (XMLStreamException e) {
                        throw new UncheckedXmlStreamException(e);
                    }
                });
    }

    @Override
    public CgmesIidmMapping read(Network extendable, XmlReaderContext context) throws XMLStreamException {
        NetworkXmlReaderContext networkContext = (NetworkXmlReaderContext) context;
        CgmesIidmMappingAdder mappingAdder = extendable.newExtension(CgmesIidmMappingAdder.class);
        String unmappedTopologicalNodeIdsStr = context.getReader().getAttributeValue(null, "unmappedTopologicalNodeIds");
        if (unmappedTopologicalNodeIdsStr != null) {
            for (String unmappedTopologicalNodeId : unmappedTopologicalNodeIdsStr.split(",")) {
                mappingAdder.addTopologicalNode(unmappedTopologicalNodeId);
            }
        }
        String unmappedBaseVoltageIdsStr = context.getReader().getAttributeValue(null, "unmappedBaseVoltageIds");
        if (unmappedBaseVoltageIdsStr != null) {
            for (String unmappedBaseVoltageId : unmappedBaseVoltageIdsStr.split(",")) {
                mappingAdder.addBaseVoltage(unmappedBaseVoltageId);
            }
        }
        mappingAdder.add();
        CgmesIidmMapping mapping = extendable.getExtension(CgmesIidmMapping.class);
        XmlUtil.readUntilEndElement("cgmesIidmMapping", context.getReader(), () -> {
            switch (context.getReader().getLocalName()) {
                case "link":
                    String busId = readBusIdentification(extendable, networkContext);
                    String[] topologicalNodeIds = context.getReader().getAttributeValue(null, "topologicalNodeIds").split(",");
                    for (String topologicalNodeId : topologicalNodeIds) {
                        mapping.putTopologicalNode(busId, topologicalNodeId);
                    }
                    break;
                case "base":
                    double nominalV = Double.parseDouble(context.getReader().getAttributeValue(null, "nominalVoltage"));
                    String baseVoltage = context.getReader().getAttributeValue(null, "baseVoltage");
                    mapping.addBaseVoltage(nominalV, baseVoltage);
                    break;
                default:
                    throw new PowsyblException("Unknown element name <" + context.getReader().getLocalName() + "> in <cgmesIidmMapping>");
            }
        });
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
