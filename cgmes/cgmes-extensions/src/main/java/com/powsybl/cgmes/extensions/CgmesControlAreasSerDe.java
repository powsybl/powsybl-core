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
import com.powsybl.commons.io.TreeDataReader;
import com.powsybl.commons.io.TreeDataWriter;
import com.powsybl.commons.io.DeserializerContext;
import com.powsybl.commons.io.SerializerContext;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.serde.NetworkDeserializerContext;
import com.powsybl.iidm.serde.NetworkSerializerContext;
import com.powsybl.iidm.serde.TerminalRefSerDe;

import java.util.Map;

/**
 * @author Marcos de Miguel {@literal <demiguelm at aia.es>}
 */
@AutoService(ExtensionSerDe.class)
public class CgmesControlAreasSerDe extends AbstractExtensionSerDe<Network, CgmesControlAreas> {

    private static final String CONTROL_AREA_ROOT_ELEMENT = "controlArea";
    private static final String CONTROL_AREA_ARRAY_ELEMENT = "controlAreas";
    public static final String TERMINAL_ROOT_ELEMENT = "terminal";
    public static final String TERMINAL_ARRAY_ELEMENT = "terminals";
    public static final String BOUNDARY_ROOT_ELEMENT = "boundary";
    public static final String BOUNDARY_ARRAY_ELEMENT = "boundaries";

    public CgmesControlAreasSerDe() {
        super("cgmesControlAreas", "network", CgmesControlAreas.class, "cgmesControlAreas.xsd",
                "http://www.powsybl.org/schema/iidm/ext/cgmes_control_areas/1_0", "cca");
    }

    @Override
    public Map<String, String> getArrayNameToSingleNameMap() {
        return Map.of(CONTROL_AREA_ARRAY_ELEMENT, CONTROL_AREA_ROOT_ELEMENT,
                TERMINAL_ARRAY_ELEMENT, TERMINAL_ROOT_ELEMENT,
                BOUNDARY_ARRAY_ELEMENT, BOUNDARY_ROOT_ELEMENT);
    }

    @Override
    public boolean isSerializable(CgmesControlAreas extension) {
        return !extension.getCgmesControlAreas().isEmpty();
    }

    @Override
    public void write(CgmesControlAreas extension, SerializerContext context) {
        NetworkSerializerContext networkContext = (NetworkSerializerContext) context;
        TreeDataWriter writer = networkContext.getWriter();
        writer.writeStartNodes();
        for (CgmesControlArea controlArea : extension.getCgmesControlAreas()) {
            writer.writeStartNode(getNamespaceUri(), CONTROL_AREA_ROOT_ELEMENT);
            writer.writeStringAttribute("id", controlArea.getId());
            writer.writeStringAttribute("name", controlArea.getName());
            writer.writeStringAttribute("energyIdentificationCodeEic", controlArea.getEnergyIdentificationCodeEIC());
            writer.writeDoubleAttribute("netInterchange", controlArea.getNetInterchange());
            writer.writeDoubleAttribute("pTolerance", controlArea.getPTolerance());

            writer.writeStartNodes();
            for (Terminal terminal : controlArea.getTerminals()) {
                TerminalRefSerDe.writeTerminalRef(terminal, networkContext, getNamespaceUri(), TERMINAL_ROOT_ELEMENT);
            }
            writer.writeEndNodes();

            writer.writeStartNodes();
            for (Boundary boundary : controlArea.getBoundaries()) {
                if (boundary.getDanglingLine() != null) { // TODO: delete this later, only for compatibility
                    writer.writeStartNode(getNamespaceUri(), BOUNDARY_ROOT_ELEMENT);
                    writer.writeStringAttribute("id", networkContext.getAnonymizer().anonymizeString(boundary.getDanglingLine().getId()));

                    // TODO use TieLine Id and DanglingLine Id for reference instead of TieLine Id and Side
                    writer.writeEnumAttribute("side", getSide(boundary));
                    writer.writeEndNode();
                }
            }
            writer.writeEndNodes();

            writer.writeEndNode();
        }
        writer.writeEndNodes();
    }

    private static TwoSides getSide(Boundary boundary) {
        // a TieLine with two dangingLines inside
        return boundary.getDanglingLine().getTieLine().map(tl -> {
            if (tl.getDanglingLine1() == boundary.getDanglingLine()) {
                return TwoSides.ONE;
            }
            return TwoSides.TWO;
        }).orElse(null);
    }

    @Override
    public CgmesControlAreas read(Network extendable, DeserializerContext context) {
        NetworkDeserializerContext networkContext = (NetworkDeserializerContext) context;
        TreeDataReader reader = networkContext.getReader();
        extendable.newExtension(CgmesControlAreasAdder.class).add();
        CgmesControlAreas mapping = extendable.getExtension(CgmesControlAreas.class);
        reader.readChildNodes(elementName -> {
            if (elementName.equals(CONTROL_AREA_ROOT_ELEMENT)) {
                CgmesControlArea cgmesControlArea = mapping.newCgmesControlArea()
                        .setId(reader.readStringAttribute("id"))
                        .setName(reader.readStringAttribute("name"))
                        .setEnergyIdentificationCodeEic(reader.readStringAttribute("energyIdentificationCodeEic"))
                        .setNetInterchange(reader.readDoubleAttribute("netInterchange"))
                        .setPTolerance(reader.readDoubleAttribute("pTolerance"))
                        .add();
                readBoundariesAndTerminals(networkContext, cgmesControlArea, extendable);
            } else {
                throw new PowsyblException("Unknown element name '" + elementName + "' in 'cgmesControlArea'");
            }
        });
        return extendable.getExtension(CgmesControlAreas.class);
    }

    private void readBoundariesAndTerminals(NetworkDeserializerContext networkContext, CgmesControlArea cgmesControlArea, Network network) {
        TreeDataReader reader = networkContext.getReader();
        reader.readChildNodes(elementName -> {
            switch (elementName) {
                case BOUNDARY_ROOT_ELEMENT -> {
                    String id = networkContext.getAnonymizer().deanonymizeString(reader.readStringAttribute("id"));
                    TwoSides side = reader.readEnumAttribute("side", TwoSides.class);
                    Identifiable<?> identifiable = network.getIdentifiable(id);
                    if (identifiable instanceof DanglingLine dl) {
                        cgmesControlArea.add(dl.getBoundary());
                    } else if (identifiable instanceof TieLine tl) {
                        cgmesControlArea.add(tl.getDanglingLine(side).getBoundary());
                    } else {
                        throw new PowsyblException("Unexpected Identifiable instance: " + identifiable.getClass());
                    }
                    reader.readEndNode();
                }
                case TERMINAL_ROOT_ELEMENT -> cgmesControlArea.add(TerminalRefSerDe.readTerminal(networkContext, network));
                default -> throw new PowsyblException("Unknown element name '" + elementName + "' in 'controlArea'");
            }
        });
    }
}
