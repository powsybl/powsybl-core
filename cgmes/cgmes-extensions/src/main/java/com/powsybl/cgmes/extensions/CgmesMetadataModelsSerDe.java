/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.extensions;

import com.google.auto.service.AutoService;
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

    private static final String MODEL = "Model";
    private static final String PROFILE = "Profile";
    private static final String DEPENDENT_ON = "DependentOn";
    private static final String SUPERSEDES = "Supersedes";
    private static final String MODELS = "Models";
    private static final String PROFILES = "Profiles";
    private static final String DEPENDENT_ONS = "DependentOns";
    private static final String SUPERSEDESS = "Supersedess";

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
                DEPENDENT_ONS, DEPENDENT_ON,
                SUPERSEDESS, SUPERSEDES
                );
    }

    @Override
    public void write(CgmesMetadataModels extension, SerializerContext context) {
        NetworkSerializerContext networkContext = (NetworkSerializerContext) context;
        TreeDataWriter writer = networkContext.getWriter();
        writer.writeStartNodes();

        boolean isSorted = ((NetworkSerializerContext) context).getOptions().isSorted();
        for (CgmesMetadataModels.Model model : isSorted ? extension.getSortedModels() : extension.getModels()) {
            writeModel(model, context);
        }

        writer.writeEndNodes();
    }

    private void writeModel(CgmesMetadataModels.Model model, SerializerContext context) {
        NetworkSerializerContext networkContext = (NetworkSerializerContext) context;
        TreeDataWriter writer = networkContext.getWriter();
        writer.writeStartNode(getNamespaceUri(), MODEL);
        writer.writeStringAttribute("id", model.getId());
        writer.writeStringAttribute("part", model.getPart());
        writer.writeStringAttribute("description", model.getDescription());
        writer.writeIntAttribute("version", model.getVersion());
        writer.writeStringAttribute("modelingAuthoritySet", model.getModelingAuthoritySet());
        writeReferences(sorted(model.getProfiles(), context), PROFILE, writer);
        writeReferences(sorted(model.getDependentOn(), context), DEPENDENT_ON, writer);
        writeReferences(sorted(model.getSupersedes(), context), SUPERSEDES, writer);
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
        adder.setId(reader.readStringAttribute("id"))
                .setPart(reader.readStringAttribute("part"))
                .setDescription(reader.readStringAttribute("description"))
                .setVersion(reader.readIntAttribute("version"))
                .setModelingAuthoritySet(reader.readStringAttribute("modelingAuthoritySet"));
        reader.readChildNodes(elementName -> {
            switch (elementName) {
                case PROFILE -> adder.addProfile(reader.readContent());
                case DEPENDENT_ON -> adder.addDependentOn(reader.readContent());
                case SUPERSEDES -> adder.addSupersedes(reader.readContent());
                default -> throw new PowsyblException("Unknown element name '" + elementName + "' in '" + CgmesMetadataModels.NAME + "'");
            }
        });
        adder.add();
    }
}
