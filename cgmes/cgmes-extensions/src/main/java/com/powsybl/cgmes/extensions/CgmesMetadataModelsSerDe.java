/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.cgmes.model.CgmesMetadataModel;
import com.powsybl.cgmes.model.CgmesSubset;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.extensions.AbstractExtensionSerDe;
import com.powsybl.commons.extensions.ExtensionSerDe;
import com.powsybl.commons.io.DeserializerContext;
import com.powsybl.commons.io.SerializerContext;
import com.powsybl.commons.io.TreeDataReader;
import com.powsybl.commons.io.TreeDataWriter;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.serde.NetworkSerializerContext;

import java.util.Collection;
import java.util.Map;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
@AutoService(ExtensionSerDe.class)
public class CgmesMetadataModelsSerDe extends AbstractExtensionSerDe<Network, CgmesMetadataModels> {

    private static final String MODEL = "model";
    private static final String PROFILE = "profile";
    private static final String DEPENDENT_ON_MODEL = "dependentOnModel";
    private static final String SUPERSEDES_MODEL = "supersedesModel";
    private static final String MODELS = "models";
    private static final String PROFILES = "profiles";
    private static final String DEPENDENT_ON_MODELS = "dependentOnModels";
    private static final String SUPERSEDES_MODELS = "supersedesModels";

    public CgmesMetadataModelsSerDe() {
        super(CgmesMetadataModels.NAME, "network", CgmesMetadataModels.class,
                "cgmesMetadataModels.xsd",
                "http://www.powsybl.org/schema/iidm/ext/cgmes_metadata_models/1_0", "cmm");
    }

    @Override
    public Map<String, String> getArrayNameToSingleNameMap() {
        return Map.of(
                MODELS, MODEL,
                PROFILES, PROFILE,
                DEPENDENT_ON_MODELS, DEPENDENT_ON_MODEL,
                SUPERSEDES_MODELS, SUPERSEDES_MODEL
                );
    }

    @Override
    public void write(CgmesMetadataModels extension, SerializerContext context) {
        NetworkSerializerContext networkContext = (NetworkSerializerContext) context;
        TreeDataWriter writer = networkContext.getWriter();
        writer.writeStartNodes();

        // Always write models sorted
        for (CgmesMetadataModel model : extension.getSortedModels()) {
            writeModel(model, context);
        }

        writer.writeEndNodes();
    }

    private void writeModel(CgmesMetadataModel model, SerializerContext context) {
        NetworkSerializerContext networkContext = (NetworkSerializerContext) context;
        TreeDataWriter writer = networkContext.getWriter();
        writer.writeStartNode(getNamespaceUri(), MODEL);
        writer.writeEnumAttribute("subset", model.getSubset());
        writer.writeStringAttribute("modelingAuthoritySet", model.getModelingAuthoritySet());
        writer.writeStringAttribute("id", model.getId());
        writer.writeIntAttribute("version", model.getVersion());
        writer.writeStringAttribute("description", model.getDescription());
        writeReferences(sorted(model.getProfiles(), context), PROFILE, writer);
        writeReferences(sorted(model.getDependentOn(), context), DEPENDENT_ON_MODEL, writer);
        writeReferences(sorted(model.getSupersedes(), context), SUPERSEDES_MODEL, writer);
        writer.writeEndNode();
    }

    private void writeReferences(Collection<String> refs, String refElementName, TreeDataWriter writer) {
        writer.writeStartNodes();
        for (String ref : refs) {
            writer.writeStartNode(getNamespaceUri(), refElementName);
            writer.writeNodeContent(ref);
            writer.writeEndNode();
        }
        writer.writeEndNodes();
    }

    private static Collection<String> sorted(Collection<String> strings, SerializerContext context) {
        if (!((NetworkSerializerContext) context).getOptions().isSorted()) {
            return strings.stream().sorted().toList();
        } else {
            return strings;
        }
    }

    @Override
    public CgmesMetadataModels read(Network extendable, DeserializerContext context) {
        CgmesMetadataModelsAdder adder = extendable.newExtension(CgmesMetadataModelsAdder.class);
        context.getReader().readChildNodes(element -> read(adder.newModel(), context));
        adder.add();
        return extendable.getExtension(CgmesMetadataModels.class);
    }

    private static void read(CgmesMetadataModelsAdder.ModelAdder adder, DeserializerContext context) {
        TreeDataReader reader = context.getReader();
        adder.setSubset(reader.readEnumAttribute("subset", CgmesSubset.class))
                .setModelingAuthoritySet(reader.readStringAttribute("modelingAuthoritySet"))
                .setId(reader.readStringAttribute("id"))
                .setVersion(reader.readIntAttribute("version"))
                .setDescription(reader.readStringAttribute("description"));
        reader.readChildNodes(elementName -> {
            switch (elementName) {
                case PROFILE -> adder.addProfile(reader.readContent());
                case DEPENDENT_ON_MODEL -> adder.addDependentOn(reader.readContent());
                case SUPERSEDES_MODEL -> adder.addSupersedes(reader.readContent());
                default -> throw new PowsyblException("Unknown element name '" + elementName + "' in '" + CgmesMetadataModels.NAME + "'");
            }
        });
        adder.add();
    }
}
