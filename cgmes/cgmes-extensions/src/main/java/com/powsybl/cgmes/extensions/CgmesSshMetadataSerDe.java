/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.extensions.AbstractExtensionSerDe;
import com.powsybl.commons.extensions.ExtensionSerDe;
import com.powsybl.commons.io.TreeDataReader;
import com.powsybl.commons.io.TreeDataWriter;
import com.powsybl.commons.io.DeserializerContext;
import com.powsybl.commons.io.SerializerContext;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.serde.NetworkDeserializerContext;
import com.powsybl.iidm.serde.NetworkSerializerContext;

import java.util.Map;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
@AutoService(ExtensionSerDe.class)
public class CgmesSshMetadataSerDe extends AbstractExtensionSerDe<Network, CgmesSshMetadata> {

    public static final String DEPENDENCY_ROOT_ELEMENT = "dependentOn";
    public static final String DEPENDENCY_ARRAY_ELEMENT = "dependencies";

    public CgmesSshMetadataSerDe() {
        super("cgmesSshMetadata", "network", CgmesSshMetadata.class, "cgmesSshMetadata.xsd",
                "http://www.powsybl.org/schema/iidm/ext/cgmes_ssh_metadata/1_0", "csshm");
    }

    @Override
    public Map<String, String> getArrayNameToSingleNameMap() {
        return Map.of(DEPENDENCY_ARRAY_ELEMENT, DEPENDENCY_ROOT_ELEMENT);
    }

    @Override
    public void write(CgmesSshMetadata extension, SerializerContext context) {
        NetworkSerializerContext networkContext = (NetworkSerializerContext) context;
        TreeDataWriter writer = networkContext.getWriter();
        writer.writeStringAttribute("id", extension.getId());
        writer.writeStringAttribute("description", extension.getDescription());
        writer.writeIntAttribute("sshVersion", extension.getSshVersion());
        writer.writeStringAttribute("modelingAuthoritySet", extension.getModelingAuthoritySet());
        writer.writeStartNodes();
        for (String dep : extension.getDependencies()) {
            writer.writeStartNode(getNamespaceUri(), DEPENDENCY_ROOT_ELEMENT);
            writer.writeNodeContent(dep);
            writer.writeEndNode();
        }
        writer.writeEndNodes();
    }

    @Override
    public CgmesSshMetadata read(Network extendable, DeserializerContext context) {
        NetworkDeserializerContext networkContext = (NetworkDeserializerContext) context;
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
