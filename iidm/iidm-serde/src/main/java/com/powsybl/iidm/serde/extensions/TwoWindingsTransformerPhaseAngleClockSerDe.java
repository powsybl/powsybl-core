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
import com.powsybl.iidm.network.TwoWindingsTransformer;
import com.powsybl.iidm.network.extensions.TwoWindingsTransformerPhaseAngleClock;
import com.powsybl.iidm.network.extensions.TwoWindingsTransformerPhaseAngleClockAdder;

/**
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
@AutoService(ExtensionSerDe.class)
public class TwoWindingsTransformerPhaseAngleClockSerDe
        extends AbstractExtensionSerDe<TwoWindingsTransformer, TwoWindingsTransformerPhaseAngleClock> {

    public TwoWindingsTransformerPhaseAngleClockSerDe() {
        super("twoWindingsTransformerPhaseAngleClock", "network", TwoWindingsTransformerPhaseAngleClock.class,
                "twoWindingsTransformerPhaseAngleClock.xsd",
                "http://www.powsybl.org/schema/iidm/ext/two_windings_transformer_phase_angle_clock/1_0", "twowtpac");
    }

    @Override
    public void write(TwoWindingsTransformerPhaseAngleClock extension, SerializerContext context) {
        context.getWriter().writeIntAttribute("phaseAngleClock", extension.getPhaseAngleClock(), 0);
    }

    @Override
    public TwoWindingsTransformerPhaseAngleClock read(TwoWindingsTransformer extendable, DeserializerContext context) {
        int phaseAngleClock = context.getReader().readIntAttribute("phaseAngleClock", 0);
        context.getReader().readEndNode();
        return extendable.newExtension(TwoWindingsTransformerPhaseAngleClockAdder.class)
                .withPhaseAngleClock(phaseAngleClock)
                .add();
    }
}
