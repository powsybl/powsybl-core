/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
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
import com.powsybl.commons.io.DeserializerContext;
import com.powsybl.commons.io.SerializerContext;
import com.powsybl.commons.io.TreeDataReader;
import com.powsybl.commons.io.TreeDataWriter;
import com.powsybl.iidm.network.Connectable;
import com.powsybl.iidm.serde.NetworkDeserializerContext;
import com.powsybl.iidm.serde.NetworkSerializerContext;

import java.util.Map;

/**
 * @author Miora Vedelago {@literal <miora.ralambotiana at rte-france.com>}
 */
@AutoService(ExtensionSerDe.class)
public class CgmesTapChangersSerDe<C extends Connectable<C>> extends AbstractExtensionSerDe<C, CgmesTapChangers<C>> {

    public static final String TAP_CHANGER_ROOT_ELEMENT = "tapChanger";
    private static final String TAP_CHANGER_ARRAY_ELEMENT = "tapChangers";

    public CgmesTapChangersSerDe() {
        super("cgmesTapChangers", "network", CgmesTapChangers.class,
                "cgmesTapChangers.xsd",
                "http://www.powsybl.org/schema/iidm/ext/cgmes_tap_changers/1_0", "ctc");
    }

    @Override
    public Map<String, String> getArrayNameToSingleNameMap() {
        return Map.of(TAP_CHANGER_ARRAY_ELEMENT, TAP_CHANGER_ROOT_ELEMENT);
    }

    @Override
    public void write(CgmesTapChangers<C> extension, SerializerContext context) {
        NetworkSerializerContext networkContext = (NetworkSerializerContext) context;
        TreeDataWriter writer = networkContext.getWriter();
        writer.writeStartNodes();
        for (CgmesTapChanger tapChanger : extension.getTapChangers()) {
            writer.writeStartNode(getNamespaceUri(), TAP_CHANGER_ROOT_ELEMENT);
            writer.writeStringAttribute("id", tapChanger.getId());
            writer.writeStringAttribute("combinedTapChangerId", tapChanger.getCombinedTapChangerId());
            writer.writeStringAttribute("type", tapChanger.getType());
            writer.writeBooleanAttribute("hidden", tapChanger.isHidden(), false);
            writer.writeOptionalIntAttribute("step",
                    !tapChanger.isHidden() ? null : tapChanger.getStep().orElseThrow(() -> new PowsyblException("Step should be defined")));
            writer.writeStringAttribute("controlId", tapChanger.getControlId());
            writer.writeEndNode();
        }
        writer.writeEndNodes();
    }

    @Override
    public CgmesTapChangers<C> read(C extendable, DeserializerContext context) {
        NetworkDeserializerContext networkContext = (NetworkDeserializerContext) context;
        TreeDataReader reader = networkContext.getReader();
        extendable.newExtension(CgmesTapChangersAdder.class).add();
        CgmesTapChangers<C> tapChangers = extendable.getExtension(CgmesTapChangers.class);
        reader.readChildNodes(elementName -> {
            if (elementName.equals(TAP_CHANGER_ROOT_ELEMENT)) {
                CgmesTapChangerAdder adder = tapChangers.newTapChanger()
                        .setId(reader.readStringAttribute("id"))
                        .setCombinedTapChangerId(reader.readStringAttribute("combinedTapChangerId"))
                        .setType(reader.readStringAttribute("type"))
                        .setHiddenStatus(reader.readBooleanAttribute("hidden", false))
                        .setControlId(reader.readStringAttribute("controlId"));
                reader.readOptionalIntAttribute("step").ifPresent(adder::setStep);
                context.getReader().readEndNode();
                adder.add();
            } else {
                throw new PowsyblException("Unknown element name '" + elementName + "' in 'cgmesTapChangers'");
            }
        });
        return extendable.getExtension(CgmesTapChangers.class);
    }
}
