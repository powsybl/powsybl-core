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
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
 */
@AutoService(ExtensionSerDe.class)
public class CgmesSvMetadataSerDe extends AbstractExtensionSerDe<Network, CgmesSvMetadata> {

    public static final String DEPENDENCY_ROOT_ELEMENT = "dependentOn";
    public static final String DEPENDENCY_ARRAY_ELEMENT = "dependencies";

    public CgmesSvMetadataSerDe() {
        super("cgmesSvMetadata", "network", CgmesSvMetadata.class, "cgmesSvMetadata.xsd",
                "http://www.powsybl.org/schema/iidm/ext/cgmes_sv_metadata/1_0", "csm");
    }

    @Override
    public Map<String, String> getArrayNameToSingleNameMap() {
        return Map.of(DEPENDENCY_ARRAY_ELEMENT, DEPENDENCY_ROOT_ELEMENT);
    }

    @Override
    public void write(CgmesSvMetadata extension, SerializerContext context) {
        NetworkSerializerContext networkContext = (NetworkSerializerContext) context;
        TreeDataWriter writer = networkContext.getWriter();
        writer.writeStringAttribute("description", extension.getDescription());
        writer.writeIntAttribute("svVersion", extension.getSvVersion());
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
    public CgmesSvMetadata read(Network extendable, DeserializerContext context) {
        NetworkDeserializerContext networkContext = (NetworkDeserializerContext) context;
        TreeDataReader reader = networkContext.getReader();
        CgmesSvMetadataAdder adder = extendable.newExtension(CgmesSvMetadataAdder.class);
        adder.setDescription(reader.readStringAttribute("description"))
                .setSvVersion(reader.readIntAttribute("svVersion"))
                .setModelingAuthoritySet(reader.readStringAttribute("modelingAuthoritySet"));
        reader.readChildNodes(elementName -> {
            if (elementName.equals(DEPENDENCY_ROOT_ELEMENT)) {
                adder.addDependency(reader.readContent());
            } else {
                throw new PowsyblException("Unknown element name '" + elementName + "' in 'cgmesSvMetadata'");
            }
        });
        adder.add();
        return extendable.getExtension(CgmesSvMetadata.class);
    }
}
