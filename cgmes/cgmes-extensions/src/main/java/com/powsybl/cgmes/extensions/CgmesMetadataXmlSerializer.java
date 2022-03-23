/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
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
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.xml.NetworkXmlReaderContext;
import com.powsybl.iidm.xml.NetworkXmlWriterContext;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

/**
 * @author Miora Vedelago <miora.ralambotiana at rte-france.com>
 */
@AutoService(ExtensionXmlSerializer.class)
public class CgmesMetadataXmlSerializer extends AbstractExtensionXmlSerializer<Network, CgmesMetadata> {

    public CgmesMetadataXmlSerializer() {
        super(CgmesMetadata.NAME, "network", CgmesMetadata.class, true,
                "cgmesMetadata.xsd", "http://www.powsybl.org/schema/iidm/ext/cgmes_metadata/1_0",
                "cm");
    }

    @Override
    public void write(CgmesMetadata extension, XmlWriterContext context) throws XMLStreamException {
        NetworkXmlWriterContext networkContext = (NetworkXmlWriterContext) context;
        XMLStreamWriter writer = networkContext.getWriter();
        writeModel("eq", extension.getEq(), writer);
        extension.getTp().ifPresent(tp -> {
            try {
                writeModel("tp", tp, writer);
            } catch (XMLStreamException e) {
                throw new UncheckedXmlStreamException(e);
            }
        });
        extension.getSsh().ifPresent(ssh -> {
            try {
                writeModel("ssh", ssh, writer);
            } catch (XMLStreamException e) {
                throw new UncheckedXmlStreamException(e);
            }
        });
        extension.getSv().ifPresent(sv -> {
            try {
                writeModel("sv", sv, writer);
            } catch (XMLStreamException e) {
                throw new UncheckedXmlStreamException(e);
            }
        });
    }

    private void writeModel(String type, CgmesMetadata.Model model, XMLStreamWriter writer) throws XMLStreamException {
        writer.writeStartElement(getNamespaceUri(), type);
        if (model.getDescription() != null) {
            writer.writeAttribute("description", model.getDescription());
        }
        XmlUtil.writeInt("version", model.getVersion(), writer);
        writer.writeAttribute("modelingAuthoritySet", model.getModelingAuthoritySet());
        for (String dep : model.getDependencies()) {
            writer.writeStartElement(getNamespaceUri(), "dependentOn");
            writer.writeCharacters(dep);
            writer.writeEndElement();
        }
        writer.writeEndElement();
    }

    @Override
    public CgmesMetadata read(Network extendable, XmlReaderContext context) throws XMLStreamException {
        NetworkXmlReaderContext networkContext = (NetworkXmlReaderContext) context;
        XMLStreamReader reader = networkContext.getReader();
        CgmesMetadataAdder adder = extendable.newExtension(CgmesMetadataAdder.class);
        XmlUtil.readUntilEndElement(getName(), reader, () -> {
            switch (reader.getLocalName()) {
                case "eq":
                    readModel("eq", adder.newEq(), reader);
                    break;
                case "tp":
                    readModel("tp", adder.newTp(), reader);
                    break;
                case "ssh":
                    readModel("ssh", adder.newSsh(), reader);
                    break;
                case "sv":
                    readModel("sv", adder.newSv(), reader);
                    break;
                default:
                    throw new PowsyblException("Unknown element name <" + reader.getLocalName() + "> in <cgmesMetadata>");
            }
        });
        adder.add();
        return extendable.getExtension(CgmesMetadata.class);
    }

    private void readModel(String profile, CgmesMetadataAdder.ModelAdder adder, XMLStreamReader reader) throws XMLStreamException {
        adder.setDescription(reader.getAttributeValue(null, "description"))
                .setVersion(XmlUtil.readIntAttribute(reader, "version"))
                .setModelingAuthoritySet(reader.getAttributeValue(null, "modelingAuthoritySet"));
        XmlUtil.readUntilEndElement(profile, reader, () -> {
            if (reader.getLocalName().equals("dependentOn")) {
                adder.addDependency(reader.getElementText());
            } else {
                throw new PowsyblException("Unknown element name <" + reader.getLocalName() + "> in <cgmesMetadata>");
            }
        });
        adder.add();
    }
}
