/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
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
import com.powsybl.iidm.network.ThreeWindingsTransformer;
import com.powsybl.iidm.network.extensions.ThreeWindingsTransformerPhaseAngleClock;
import com.powsybl.iidm.network.extensions.ThreeWindingsTransformerPhaseAngleClockAdder;

/**
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
@AutoService(ExtensionSerDe.class)
public class ThreeWindingsTransformerPhaseAngleClockSerDe
        extends AbstractExtensionSerDe<ThreeWindingsTransformer, ThreeWindingsTransformerPhaseAngleClock> {

    public ThreeWindingsTransformerPhaseAngleClockSerDe() {
        super("threeWindingsTransformerPhaseAngleClock", "network", ThreeWindingsTransformerPhaseAngleClock.class,
                "threeWindingsTransformerPhaseAngleClock.xsd",
                "http://www.powsybl.org/schema/iidm/ext/three_windings_transformer_phase_angle_clock/1_0",
                "threewtpac");
    }

    @Override
    public void write(ThreeWindingsTransformerPhaseAngleClock extension, SerializerContext context) {
        context.getWriter().writeIntAttribute("phaseAngleClockLeg2", extension.getPhaseAngleClockLeg2(), 0);
        context.getWriter().writeIntAttribute("phaseAngleClockLeg3", extension.getPhaseAngleClockLeg3(), 0);
    }

    @Override
    public ThreeWindingsTransformerPhaseAngleClock read(ThreeWindingsTransformer extendable, DeserializerContext context) {
        int phaseAngleClockLeg2 = context.getReader().readIntAttribute("phaseAngleClockLeg2", 0);
        int phaseAngleClockLeg3 = context.getReader().readIntAttribute("phaseAngleClockLeg3", 0);
        context.getReader().readEndNode();
        return extendable.newExtension(ThreeWindingsTransformerPhaseAngleClockAdder.class)
                .withPhaseAngleClockLeg2(phaseAngleClockLeg2)
                .withPhaseAngleClockLeg3(phaseAngleClockLeg3)
                .add();
    }
}
