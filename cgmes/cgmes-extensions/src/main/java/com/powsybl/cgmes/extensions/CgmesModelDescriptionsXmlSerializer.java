/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
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
import java.util.Collection;
import java.util.Comparator;
import java.util.stream.Collectors;

/**
 * @author Miora Vedelago <miora.ralambotiana at rte-france.com>
 */
@AutoService(ExtensionXmlSerializer.class)
public class CgmesModelDescriptionsXmlSerializer extends AbstractExtensionXmlSerializer<Network, CgmesModelDescriptions> {

    public CgmesModelDescriptionsXmlSerializer() {
        super(CgmesModelDescriptions.NAME, "network", CgmesModelDescriptions.class, true,
                "cgmesModelDescriptions.xsd", "http://www.powsybl.org/schema/iidm/ext/cgmes_model_descriptions/1_0",
                "cm");
    }

    @Override
    public void write(CgmesModelDescriptions extension, XmlWriterContext context) throws XMLStreamException {
        NetworkXmlWriterContext networkContext = (NetworkXmlWriterContext) context;
        XMLStreamWriter writer = networkContext.getWriter();
        for (CgmesModelDescriptions.Model model : sortedModels(extension.getModels(), networkContext)) {
            writeModel(model, writer, networkContext);
        }
    }

    private static Collection<CgmesModelDescriptions.Model> sortedModels(Collection<CgmesModelDescriptions.Model> models, NetworkXmlWriterContext context) {
        if (!context.getOptions().isSorted()) {
            return models;
        }
        return models.stream()
                .sorted(Comparator.comparing(CgmesModelDescriptions.Model::getId))
                .collect(Collectors.toList());
    }

    private void writeModel(CgmesModelDescriptions.Model model, XMLStreamWriter writer, NetworkXmlWriterContext context) throws XMLStreamException {
        if (model.getDependencies().isEmpty()) {
            writer.writeEmptyElement(getNamespaceUri(), "model");
        } else {
            writer.writeStartElement(getNamespaceUri(), "model");
        }
        writer.writeAttribute("id", model.getId());
        if (model.getDescription() != null) {
            writer.writeAttribute("description", model.getDescription());
        }
        XmlUtil.writeInt("version", model.getVersion(), writer);
        writer.writeAttribute("modelingAuthoritySet", model.getModelingAuthoritySet());
        writer.writeAttribute("profiles", String.join(",", sorted(model.getProfiles(), context)));
        for (String dep : sorted(model.getDependencies(), context)) {
            writer.writeStartElement(getNamespaceUri(), "dependentOn");
            writer.writeCharacters(dep);
            writer.writeEndElement();
        }
        if (!model.getDependencies().isEmpty()) {
            writer.writeEndElement();
        }
    }

    private static Collection<String> sorted(Collection<String> strings, NetworkXmlWriterContext context) {
        if (!context.getOptions().isSorted()) {
            return strings;
        }
        return strings.stream().sorted().collect(Collectors.toList());
    }

    @Override
    public CgmesModelDescriptions read(Network extendable, XmlReaderContext context) throws XMLStreamException {
        NetworkXmlReaderContext networkContext = (NetworkXmlReaderContext) context;
        XMLStreamReader reader = networkContext.getReader();
        CgmesModelDescriptionsAdder adder = extendable.newExtension(CgmesModelDescriptionsAdder.class);
        XmlUtil.readUntilEndElement(getName(), reader, () -> {
            if ("model".equals(reader.getLocalName())) {
                readModel(adder.newModel(), reader);
            } else {
                throw new PowsyblException("Unknown element name <" + reader.getLocalName() + "> in <cgmesModelDescriptions>");
            }
        });
        adder.add();
        return extendable.getExtension(CgmesModelDescriptions.class);
    }

    private static void readModel(CgmesModelDescriptionsAdder.ModelAdder adder, XMLStreamReader reader) throws XMLStreamException {
        adder.setId(reader.getAttributeValue(null, "id"))
                .setDescription(reader.getAttributeValue(null, "description"))
                .setVersion(XmlUtil.readIntAttribute(reader, "version"))
                .setModelingAuthoritySet(reader.getAttributeValue(null, "modelingAuthoritySet"));
        for (String profile : reader.getAttributeValue(null, "profiles").split(",")) {
            adder.addProfile(profile);
        }
        XmlUtil.readUntilEndElement("model", reader, () -> {
            if (reader.getLocalName().equals("dependentOn")) {
                adder.addDependency(reader.getElementText());
            } else {
                throw new PowsyblException("Unknown element name <" + reader.getLocalName() + "> in <cgmesModelDescriptions>");
            }
        });
        adder.add();
    }
}
