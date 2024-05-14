/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.extensions.AbstractExtensionSerDe;
import com.powsybl.commons.extensions.ExtensionSerDe;
import com.powsybl.commons.io.DeserializerContext;
import com.powsybl.commons.io.SerializerContext;
import com.powsybl.commons.io.TreeDataReader;
import com.powsybl.commons.io.TreeDataWriter;
import com.powsybl.iidm.network.ThreeWindingsTransformer;
import com.powsybl.iidm.network.extensions.ThreeWindingsTransformerFortescue;
import com.powsybl.iidm.network.extensions.ThreeWindingsTransformerFortescueAdder;
import com.powsybl.iidm.network.extensions.WindingConnectionType;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
@AutoService(ExtensionSerDe.class)
public class ThreeWindingsTransformerFortescueSerDe extends AbstractExtensionSerDe<ThreeWindingsTransformer, ThreeWindingsTransformerFortescue> {

    public ThreeWindingsTransformerFortescueSerDe() {
        super("threeWindingsTransformerFortescue", "network", ThreeWindingsTransformerFortescue.class,
                "threeWindingsTransformerFortescue_V1_0.xsd", "http://www.powsybl.org/schema/iidm/ext/three_windings_transformer_fortescue/1_0",
                "t3f");
    }

    private static void writeLeg(ThreeWindingsTransformerFortescue.LegFortescue legFortescue, TreeDataWriter writer) {
        writer.writeDoubleAttribute("rz", legFortescue.getRz(), Double.NaN);
        writer.writeDoubleAttribute("xz", legFortescue.getXz(), Double.NaN);
        writer.writeBooleanAttribute("freeFluxes", legFortescue.isFreeFluxes());
        writer.writeEnumAttribute("connectionType", legFortescue.getConnectionType());
        writer.writeDoubleAttribute("groundingR", legFortescue.getGroundingR(), 0);
        writer.writeDoubleAttribute("groundingX", legFortescue.getGroundingX(), 0);
    }

    @Override
    public void write(ThreeWindingsTransformerFortescue twtFortescue, SerializerContext context) {
        context.getWriter().writeStartNode(getNamespaceUri(), "leg1");
        writeLeg(twtFortescue.getLeg1(), context.getWriter());
        context.getWriter().writeEndNode();
        context.getWriter().writeStartNode(getNamespaceUri(), "leg2");
        writeLeg(twtFortescue.getLeg2(), context.getWriter());
        context.getWriter().writeEndNode();
        context.getWriter().writeStartNode(getNamespaceUri(), "leg3");
        writeLeg(twtFortescue.getLeg3(), context.getWriter());
        context.getWriter().writeEndNode();
    }

    private void readLeg(ThreeWindingsTransformerFortescueAdder.LegFortescueAdder legAdder, TreeDataReader reader) {
        double rz = reader.readDoubleAttribute("rz");
        double xz = reader.readDoubleAttribute("xz");
        boolean freeFluxes = reader.readBooleanAttribute("freeFluxes");
        WindingConnectionType connectionType = reader.readEnumAttribute("connectionType", WindingConnectionType.class);
        double groundingR = reader.readDoubleAttribute("groundingR", 0);
        double groundingX = reader.readDoubleAttribute("groundingX", 0);
        reader.readEndNode();
        legAdder.withRz(rz)
                .withXz(xz)
                .withFreeFluxes(freeFluxes)
                .withConnectionType(connectionType)
                .withGroundingR(groundingR)
                .withGroundingX(groundingX);
    }

    @Override
    public ThreeWindingsTransformerFortescue read(ThreeWindingsTransformer twt, DeserializerContext context) {
        ThreeWindingsTransformerFortescueAdder fortescueAdder = twt.newExtension(ThreeWindingsTransformerFortescueAdder.class);

        context.getReader().readChildNodes(elementName -> {
            switch (elementName) {
                case "leg1" -> readLeg(fortescueAdder.leg1(), context.getReader());
                case "leg2" -> readLeg(fortescueAdder.leg2(), context.getReader());
                case "leg3" -> readLeg(fortescueAdder.leg3(), context.getReader());
                default -> throw new PowsyblException("Unknown element name '" + elementName + "' in 'threeWindingsTransformerFortescue'");
            }
        });

        return fortescueAdder.add();
    }
}
