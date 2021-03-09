/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.commons.exceptions.UncheckedXmlStreamException;
import com.powsybl.commons.extensions.AbstractExtensionXmlSerializer;
import com.powsybl.commons.extensions.ExtensionXmlSerializer;
import com.powsybl.commons.xml.XmlReaderContext;
import com.powsybl.commons.xml.XmlUtil;
import com.powsybl.commons.xml.XmlWriterContext;
import com.powsybl.iidm.network.Network;

import javax.xml.stream.XMLStreamException;

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
        if (!extension.getUnmappedTopologicalNodes().isEmpty()) {
            context.getWriter().writeAttribute("unmappedTopologicalNodeIds", String.join(","));
        }
        extension.getExtendable().getBusBreakerView().getBusStream()
                .filter(b -> extension.isMapped(b.getId()))
                .forEach(b -> {
                    try {
                        context.getWriter().writeEmptyElement(getNamespaceUri(), "link");
                        context.getWriter().writeAttribute("busId", b.getId());
                        context.getWriter().writeAttribute("topologicalNodeIds", String.join(",", extension.getTopologicalNodes(b.getId())));
                    } catch (XMLStreamException e) {
                        throw new UncheckedXmlStreamException(e);
                    }
                });
    }

    @Override
    public CgmesIidmMapping read(Network extendable, XmlReaderContext context) throws XMLStreamException {
        CgmesIidmMappingAdder mappingAdder = extendable.newExtension(CgmesIidmMappingAdder.class);
        String unmappedTopologicalNodeIdsStr = context.getReader().getAttributeValue(null, "unmappedTopologicalNodeIds");
        if (unmappedTopologicalNodeIdsStr != null) {
            for (String unmappedTopologicalNodeId : unmappedTopologicalNodeIdsStr.split(",")) {
                mappingAdder.addTopologicalNode(unmappedTopologicalNodeId);
            }
        }
        mappingAdder.add();
        CgmesIidmMapping mapping = extendable.getExtension(CgmesIidmMapping.class);
        XmlUtil.readUntilEndElement("cgmesIidmMapping", context.getReader(), () -> {
            String busId = context.getReader().getAttributeValue(null, "busId");
            String[] topologicalNodeIds = context.getReader().getAttributeValue(null, "topologicalNodeIds").split(",");
            for (String topologicalNodeId : topologicalNodeIds) {
                mapping.put(busId, topologicalNodeId);
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
        return !cgmesIidmMapping.isEmpty();
    }
}
