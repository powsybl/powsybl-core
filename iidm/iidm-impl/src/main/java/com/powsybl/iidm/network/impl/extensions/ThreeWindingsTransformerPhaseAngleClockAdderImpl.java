/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.extensions.AbstractExtensionAdder;
import com.powsybl.iidm.network.ThreeWindingsTransformer;
import com.powsybl.iidm.network.extensions.ThreeWindingsTransformerPhaseAngleClock;
import com.powsybl.iidm.network.extensions.ThreeWindingsTransformerPhaseAngleClockAdder;

/**
 * @author Jérémy LABOUS {@literal <jlabous at silicom.fr>}
 */
public class ThreeWindingsTransformerPhaseAngleClockAdderImpl
    extends AbstractExtensionAdder<ThreeWindingsTransformer, ThreeWindingsTransformerPhaseAngleClock>
    implements ThreeWindingsTransformerPhaseAngleClockAdder {

    private int phaseAngleClockLeg2 = -1;
    private int phaseAngleClockLeg3 = -1;

    protected ThreeWindingsTransformerPhaseAngleClockAdderImpl(ThreeWindingsTransformer extendable) {
        super(extendable);
    }

    @Override
    protected ThreeWindingsTransformerPhaseAngleClock createExtension(ThreeWindingsTransformer extendable) {
        return new ThreeWindingsTransformerPhaseAngleClockimpl(extendable, phaseAngleClockLeg2, phaseAngleClockLeg3);
    }

    @Override
    public ThreeWindingsTransformerPhaseAngleClockAdder withPhaseAngleClockLeg2(int phaseAngleClockLeg2) {
        this.phaseAngleClockLeg2 = phaseAngleClockLeg2;
        return this;
    }

    @Override
    public ThreeWindingsTransformerPhaseAngleClockAdder withPhaseAngleClockLeg3(int phaseAngleClockLeg3) {
        this.phaseAngleClockLeg3 = phaseAngleClockLeg3;
        return this;
    }
}
