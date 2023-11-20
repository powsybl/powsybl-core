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
import com.powsybl.commons.io.TreeDataReader;
import com.powsybl.commons.io.TreeDataWriter;
import com.powsybl.commons.extensions.XmlReaderContext;
import com.powsybl.commons.extensions.XmlWriterContext;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.serializer.NetworkXmlReaderContext;
import com.powsybl.iidm.serializer.NetworkXmlWriterContext;

import java.util.Map;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
@AutoService(ExtensionXmlSerializer.class)
public class CgmesSshMetadataXmlSerializer extends AbstractExtensionXmlSerializer<Network, CgmesSshMetadata> {

    public static final String DEPENDENCY_ROOT_ELEMENT = "dependentOn";
    public static final String DEPENDENCY_ARRAY_ELEMENT = "dependencies";

    public CgmesSshMetadataXmlSerializer() {
        super("cgmesSshMetadata", "network", CgmesSshMetadata.class, "cgmesSshMetadata.xsd",
                "http://www.powsybl.org/schema/iidm/ext/cgmes_ssh_metadata/1_0", "csshm");
    }

    @Override
    public Map<String, String> getArrayNameToSingleNameMap() {
        return Map.of(DEPENDENCY_ARRAY_ELEMENT, DEPENDENCY_ROOT_ELEMENT);
    }

    @Override
    public void write(CgmesSshMetadata extension, XmlWriterContext context) {
        NetworkXmlWriterContext networkContext = (NetworkXmlWriterContext) context;
        TreeDataWriter writer = networkContext.getWriter();
        writer.writeStringAttribute("id", extension.getId());
        writer.writeStringAttribute("description", extension.getDescription());
        writer.writeIntAttribute("sshVersion", extension.getSshVersion());
        writer.writeStringAttribute("modelingAuthoritySet", extension.getModelingAuthoritySet());
        writer.writeStartNodes(DEPENDENCY_ARRAY_ELEMENT);
        for (String dep : extension.getDependencies()) {
            writer.writeStartNode(getNamespaceUri(), DEPENDENCY_ROOT_ELEMENT);
            writer.writeNodeContent(dep);
            writer.writeEndNode();
        }
        writer.writeEndNodes();
    }

    @Override
    public CgmesSshMetadata read(Network extendable, XmlReaderContext context) {
        NetworkXmlReaderContext networkContext = (NetworkXmlReaderContext) context;
        TreeDataReader reader = networkContext.getReader();
        CgmesSshMetadataAdder adder = extendable.newExtension(CgmesSshMetadataAdder.class);
        adder.setId(reader.readStringAttribute("id"))
                .setDescription(reader.readStringAttribute("description"))
                .setSshVersion(reader.readIntAttribute("sshVersion"))
                .setModelingAuthoritySet(reader.readStringAttribute("modelingAuthoritySet"));
        reader.readChildNodes(elementName -> {
            if (elementName.equals(DEPENDENCY_ROOT_ELEMENT)) {
                adder.addDependency(reader.readContent());
            } else {
                throw new PowsyblException("Unknown element name '" + elementName + "' in 'cgmesSshMetadata'");
            }
        });
        adder.add();
        return extendable.getExtension(CgmesSshMetadata.class);
    }
}
