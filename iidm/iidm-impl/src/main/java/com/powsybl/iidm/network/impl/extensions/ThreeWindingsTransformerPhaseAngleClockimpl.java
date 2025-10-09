/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.ThreeWindingsTransformer;
import com.powsybl.iidm.network.extensions.ThreeWindingsTransformerPhaseAngleClock;

/**
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
public class ThreeWindingsTransformerPhaseAngleClockimpl extends AbstractExtension<ThreeWindingsTransformer> implements ThreeWindingsTransformerPhaseAngleClock {

    private int phaseAngleClockLeg2;
    private int phaseAngleClockLeg3;

    public ThreeWindingsTransformerPhaseAngleClockimpl(ThreeWindingsTransformer twt, int phaseAngleClockLeg2, int phaseAngleClockLeg3) {
        super(twt);
        this.phaseAngleClockLeg2 = checkPhaseAngleClock(phaseAngleClockLeg2);
        this.phaseAngleClockLeg3 = checkPhaseAngleClock(phaseAngleClockLeg3);
    }

    @Override
    public String getName() {
        return "threeWindingsTransformerPhaseAngleClock";
    }

    @Override
    public int getPhaseAngleClockLeg2() {
        return phaseAngleClockLeg2;
    }

    @Override
    public int getPhaseAngleClockLeg3() {
        return phaseAngleClockLeg3;
    }

    @Override
    public void setPhaseAngleClockLeg2(int phaseAngleClock) {
        this.phaseAngleClockLeg2 = checkPhaseAngleClock(phaseAngleClock);
    }

    @Override
    public void setPhaseAngleClockLeg3(int phaseAngleClock) {
        this.phaseAngleClockLeg3 = checkPhaseAngleClock(phaseAngleClock);
    }

    private static int checkPhaseAngleClock(int phaseAngleClock) {
        if (phaseAngleClock < 0 || phaseAngleClock > 11) {
            throw new PowsyblException("Unexpected value for phaseAngleClock: " + phaseAngleClock);
        }
        return phaseAngleClock;
    }
}
