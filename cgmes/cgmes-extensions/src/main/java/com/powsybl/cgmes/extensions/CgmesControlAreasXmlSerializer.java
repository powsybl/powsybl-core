/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
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
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.xml.NetworkXmlReaderContext;
import com.powsybl.iidm.xml.NetworkXmlWriterContext;
import com.powsybl.iidm.xml.TerminalRefXml;

import java.util.Map;

/**
 * @author Marcos de Miguel {@literal <demiguelm at aia.es>}
 */
@AutoService(ExtensionXmlSerializer.class)
public class CgmesControlAreasXmlSerializer extends AbstractExtensionXmlSerializer<Network, CgmesControlAreas> {

    private static final String CONTROL_AREA_ROOT_ELEMENT = "controlArea";
    private static final String CONTROL_AREA_ARRAY_ELEMENT = "controlAreas";
    public static final String TERMINAL_ROOT_ELEMENT = "terminal";
    public static final String TERMINAL_ARRAY_ELEMENT = "terminals";
    public static final String BOUNDARY_ROOT_ELEMENT = "boundary";
    public static final String BOUNDARY_ARRAY_ELEMENT = "boundaries";

    public CgmesControlAreasXmlSerializer() {
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
    public void write(CgmesControlAreas extension, XmlWriterContext context) {
        NetworkXmlWriterContext networkContext = (NetworkXmlWriterContext) context;
        TreeDataWriter writer = networkContext.getWriter();
        writer.writeStartNodes(CONTROL_AREA_ARRAY_ELEMENT);
        for (CgmesControlArea controlArea : extension.getCgmesControlAreas()) {
            writer.writeStartNode(getNamespaceUri(), CONTROL_AREA_ROOT_ELEMENT);
            writer.writeStringAttribute("id", controlArea.getId());
            writer.writeStringAttribute("name", controlArea.getName());
            writer.writeStringAttribute("energyIdentificationCodeEic", controlArea.getEnergyIdentificationCodeEIC());
            writer.writeDoubleAttribute("netInterchange", controlArea.getNetInterchange());
            writer.writeDoubleAttribute("pTolerance", controlArea.getPTolerance());

            writer.writeStartNodes(TERMINAL_ARRAY_ELEMENT);
            for (Terminal terminal : controlArea.getTerminals()) {
                TerminalRefXml.writeTerminalRef(terminal, networkContext, getNamespaceUri(), TERMINAL_ROOT_ELEMENT);
            }
            writer.writeEndNodes();

            writer.writeStartNodes(BOUNDARY_ARRAY_ELEMENT);
            for (Boundary boundary : controlArea.getBoundaries()) {
                if (boundary.getDanglingLine() != null) { // TODO: delete this later, only for compatibility
                    writer.writeStartNode(getNamespaceUri(), BOUNDARY_ROOT_ELEMENT);
                    writer.writeStringAttribute("id", networkContext.getAnonymizer().anonymizeString(boundary.getDanglingLine().getId()));

                    // TODO use TieLine Id and DanglingLine Id for reference instead of TieLine Id and Side
                    TwoSides side = getSide(boundary);
                    if (side != null) {
                        writer.writeEnumAttribute("side", side);
                    }
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
    public CgmesControlAreas read(Network extendable, XmlReaderContext context) {
        NetworkXmlReaderContext networkContext = (NetworkXmlReaderContext) context;
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

    private void readBoundariesAndTerminals(NetworkXmlReaderContext networkContext, CgmesControlArea cgmesControlArea, Network network) {
        TreeDataReader reader = networkContext.getReader();
        reader.readChildNodes(elementName -> {
            String id;
            String side;
            switch (elementName) {
                case BOUNDARY_ROOT_ELEMENT -> {
                    id = networkContext.getAnonymizer().deanonymizeString(reader.readStringAttribute("id"));
                    Identifiable identifiable = network.getIdentifiable(id);
                    if (identifiable instanceof DanglingLine dl) {
                        cgmesControlArea.add(dl.getBoundary());
                    } else if (identifiable instanceof TieLine tl) {
                        side = reader.readStringAttribute("side");
                        cgmesControlArea.add(tl.getDanglingLine(TwoSides.valueOf(side)).getBoundary());
                    } else {
                        throw new PowsyblException("Unexpected Identifiable instance: " + identifiable.getClass());
                    }
                    reader.readEndNode();
                }
                case TERMINAL_ROOT_ELEMENT -> cgmesControlArea.add(TerminalRefXml.readTerminal(networkContext, network));
                default -> throw new PowsyblException("Unknown element name '" + elementName + "' in 'controlArea'");
            }
        });
    }
}
