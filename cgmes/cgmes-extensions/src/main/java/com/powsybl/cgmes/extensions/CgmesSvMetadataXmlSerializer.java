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
import com.powsybl.commons.xml.XmlWriterContext;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.xml.NetworkXmlReaderContext;
import com.powsybl.iidm.xml.NetworkXmlWriterContext;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 *
 * @deprecated Use {@link CgmesModelDescriptionsXmlSerializer} instead.
 */
@Deprecated(since = "4.8.0")
@AutoService(ExtensionXmlSerializer.class)
public class CgmesSvMetadataXmlSerializer extends AbstractExtensionXmlSerializer<Network, CgmesSvMetadata> {

    public CgmesSvMetadataXmlSerializer() {
        super("cgmesSvMetadata", "network", CgmesSvMetadata.class, true, "cgmesSvMetadata.xsd",
                "http://www.powsybl.org/schema/iidm/ext/cgmes_sv_metadata/1_0", "csm");
    }

    @Override
    public void write(CgmesSvMetadata extension, XmlWriterContext context) throws XMLStreamException {
        NetworkXmlWriterContext networkContext = (NetworkXmlWriterContext) context;
        XMLStreamWriter writer = networkContext.getWriter();
        if (extension.getDescription() != null) {
            writer.writeAttribute("description", extension.getDescription());
        }
        XmlUtil.writeInt("svVersion", extension.getSvVersion(), writer);
        writer.writeAttribute("modelingAuthoritySet", extension.getModelingAuthoritySet());
        for (String dep : extension.getDependencies()) {
            writer.writeStartElement(getNamespaceUri(), "dependentOn");
            writer.writeCharacters(dep);
            writer.writeEndElement();
        }
    }

    @Override
    public CgmesSvMetadata read(Network extendable, XmlReaderContext context) throws XMLStreamException {
        NetworkXmlReaderContext networkContext = (NetworkXmlReaderContext) context;
        XMLStreamReader reader = networkContext.getReader();
        CgmesSvMetadataAdder adder = extendable.newExtension(CgmesSvMetadataAdder.class);
        adder.setDescription(reader.getAttributeValue(null, "description"))
                .setSvVersion(XmlUtil.readIntAttribute(reader, "svVersion"))
                .setModelingAuthoritySet(reader.getAttributeValue(null, "modelingAuthoritySet"));
        XmlUtil.readUntilEndElement("cgmesSvMetadata", reader, () -> {
            if (reader.getLocalName().equals("dependentOn")) {
                adder.addDependency(reader.getElementText());
            } else {
                throw new PowsyblException("Unknown element name <" + reader.getLocalName() + "> in <cgmesSvMetadata>");
            }
        });
        adder.add();
        return extendable.getExtension(CgmesSvMetadata.class);
    }
}
