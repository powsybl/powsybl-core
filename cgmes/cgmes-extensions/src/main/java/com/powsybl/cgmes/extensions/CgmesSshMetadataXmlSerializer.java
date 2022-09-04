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
import com.powsybl.commons.xml.XmlReaderContext;
import com.powsybl.commons.xml.XmlUtil;
import com.powsybl.commons.xml.XmlWriter;
import com.powsybl.commons.xml.XmlWriterContext;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.xml.NetworkXmlReaderContext;
import com.powsybl.iidm.xml.NetworkXmlWriterContext;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
@AutoService(ExtensionXmlSerializer.class)
public class CgmesSshMetadataXmlSerializer extends AbstractExtensionXmlSerializer<Network, CgmesSshMetadata> {

    public CgmesSshMetadataXmlSerializer() {
        super("cgmesSshMetadata", "network", CgmesSshMetadata.class, true, "cgmesSshMetadata.xsd",
                "http://www.powsybl.org/schema/iidm/ext/cgmes_ssh_metadata/1_0", "csshm");
    }

    @Override
    public void write(CgmesSshMetadata extension, XmlWriterContext context) throws XMLStreamException {
        NetworkXmlWriterContext networkContext = (NetworkXmlWriterContext) context;
        XmlWriter writer = networkContext.getWriter();
        writer.writeStringAttribute("description", extension.getDescription());
        writer.writeIntAttribute("sshVersion", extension.getSshVersion());
        writer.writeStringAttribute("modelingAuthoritySet", extension.getModelingAuthoritySet());
        for (String dep : extension.getDependencies()) {
            writer.writeStartElement(getNamespaceUri(), "dependentOn");
            writer.writeElementContent(dep);
            writer.writeEndElement();
        }
    }

    @Override
    public CgmesSshMetadata read(Network extendable, XmlReaderContext context) throws XMLStreamException {
        NetworkXmlReaderContext networkContext = (NetworkXmlReaderContext) context;
        XMLStreamReader reader = networkContext.getReader();
        CgmesSshMetadataAdder adder = extendable.newExtension(CgmesSshMetadataAdder.class);
        adder.setDescription(reader.getAttributeValue(null, "description"))
                .setSshVersion(XmlUtil.readIntAttribute(reader, "sshVersion"))
                .setModelingAuthoritySet(reader.getAttributeValue(null, "modelingAuthoritySet"));
        XmlUtil.readUntilEndElement("cgmesSshMetadata", reader, () -> {
            if (reader.getLocalName().equals("dependentOn")) {
                adder.addDependency(reader.getElementText());
            } else {
                throw new PowsyblException("Unknown element name <" + reader.getLocalName() + "> in <cgmesSshMetadata>");
            }
        });
        adder.add();
        return extendable.getExtension(CgmesSshMetadata.class);
    }
}
