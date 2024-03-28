/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.commons.extensions.AbstractExtensionSerDe;
import com.powsybl.commons.extensions.ExtensionSerDe;
import com.powsybl.commons.io.DeserializerContext;
import com.powsybl.commons.io.SerializerContext;
import com.powsybl.iidm.network.TwoWindingsTransformer;
import com.powsybl.iidm.network.extensions.TwoWindingsTransformerFortescue;
import com.powsybl.iidm.network.extensions.TwoWindingsTransformerFortescueAdder;
import com.powsybl.iidm.network.extensions.WindingConnectionType;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
@AutoService(ExtensionSerDe.class)
public class TwoWindingsTransformerFortescueSerDe extends AbstractExtensionSerDe<TwoWindingsTransformer, TwoWindingsTransformerFortescue> {

    public TwoWindingsTransformerFortescueSerDe() {
        super("twoWindingsTransformerFortescue", "network", TwoWindingsTransformerFortescue.class,
                "twoWindingsTransformerFortescue_V1_0.xsd", "http://www.powsybl.org/schema/iidm/ext/two_windings_transformer_fortescue/1_0",
                "t2f");
    }

    @Override
    public void write(TwoWindingsTransformerFortescue twtFortescue, SerializerContext context) {
        context.getWriter().writeDoubleAttribute("rz", twtFortescue.getRz(), Double.NaN);
        context.getWriter().writeDoubleAttribute("xz", twtFortescue.getXz(), Double.NaN);
        context.getWriter().writeBooleanAttribute("freeFluxes", twtFortescue.isFreeFluxes());
        context.getWriter().writeEnumAttribute("connectionType1", twtFortescue.getConnectionType1());
        context.getWriter().writeEnumAttribute("connectionType2", twtFortescue.getConnectionType2());
        context.getWriter().writeDoubleAttribute("groundingR1", twtFortescue.getGroundingR1(), 0);
        context.getWriter().writeDoubleAttribute("groundingX1", twtFortescue.getGroundingX1(), 0);
        context.getWriter().writeDoubleAttribute("groundingR2", twtFortescue.getGroundingR2(), 0);
        context.getWriter().writeDoubleAttribute("groundingX2", twtFortescue.getGroundingX2(), 0);
    }

    @Override
    public TwoWindingsTransformerFortescue read(TwoWindingsTransformer twt, DeserializerContext context) {
        double rz = context.getReader().readDoubleAttribute("rz");
        double xz = context.getReader().readDoubleAttribute("xz");
        boolean freeFluxes = context.getReader().readBooleanAttribute("freeFluxes");
        WindingConnectionType connectionType1 = context.getReader().readEnumAttribute("connectionType1", WindingConnectionType.class);
        WindingConnectionType connectionType2 = context.getReader().readEnumAttribute("connectionType2", WindingConnectionType.class);
        double groundingR1 = context.getReader().readDoubleAttribute("groundingR1", 0);
        double groundingX1 = context.getReader().readDoubleAttribute("groundingX1", 0);
        double groundingR2 = context.getReader().readDoubleAttribute("groundingR2", 0);
        double groundingX2 = context.getReader().readDoubleAttribute("groundingX2", 0);
        context.getReader().readEndNode();
        return twt.newExtension(TwoWindingsTransformerFortescueAdder.class)
                .withRz(rz)
                .withXz(xz)
                .withFreeFluxes(freeFluxes)
                .withConnectionType1(connectionType1)
                .withConnectionType2(connectionType2)
                .withGroundingR1(groundingR1)
                .withGroundingX1(groundingX1)
                .withGroundingR2(groundingR2)
                .withGroundingX2(groundingX2)
                .add();
    }
}
