/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.extensions;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.TwoWindingsTransformer;

/**
 * @author José Antonio Marqués <marquesja at aia.es>
 */
public class PhaseAngleClockTwoWindingsTransformer extends AbstractExtension<TwoWindingsTransformer> {

    private int phaseAngleClock1;
    private int phaseAngleClock2;

    public PhaseAngleClockTwoWindingsTransformer(TwoWindingsTransformer twt, int phaseAngleClock1, int phaseAngleClock2) {
        super(twt);
        this.phaseAngleClock1 = checkPhaseAngleClock(phaseAngleClock1);
        this.phaseAngleClock2 = checkPhaseAngleClock(phaseAngleClock2);
    }

    @Override
    public String getName() {
        return "phaseAngleClockTwoWindingsTransformer";
    }

    public int getPhaseAngleClock1() {
        return phaseAngleClock1;
    }

    public int getPhaseAngleClock2() {
        return phaseAngleClock2;
    }

    public void setPhaseAngleClock1(int phaseAngleClock1) {
        this.phaseAngleClock1 = checkPhaseAngleClock(phaseAngleClock1);
    }

    public void setPhaseAngleClock2(int phaseAngleClock2) {
        this.phaseAngleClock2 = checkPhaseAngleClock(phaseAngleClock2);
    }

    private static int checkPhaseAngleClock(int phaseAngleClock) {
        if (phaseAngleClock < 0 || phaseAngleClock > 11) {
            throw new PowsyblException("Unexpected value for phaseAngleClock: " + phaseAngleClock);
        }
        return phaseAngleClock;
    }
}
