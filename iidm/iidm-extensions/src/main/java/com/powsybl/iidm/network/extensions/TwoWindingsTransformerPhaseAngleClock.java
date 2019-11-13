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
public class TwoWindingsTransformerPhaseAngleClock extends AbstractExtension<TwoWindingsTransformer> {

    private int phaseAngleClock;

    public TwoWindingsTransformerPhaseAngleClock(TwoWindingsTransformer twt, int phaseAngleClock) {
        super(twt);
        this.phaseAngleClock = checkPhaseAngleClock(phaseAngleClock);
    }

    @Override
    public String getName() {
        return "twoWindingsTransformerPhaseAngleClock";
    }

    public int getPhaseAngleClock() {
        return phaseAngleClock;
    }

    public void setPhaseAngleClock(int phaseAngleClock) {
        this.phaseAngleClock = checkPhaseAngleClock(phaseAngleClock);
    }

    private static int checkPhaseAngleClock(int phaseAngleClock) {
        if (phaseAngleClock < 0 || phaseAngleClock > 11) {
            throw new PowsyblException("Unexpected value for phaseAngleClock: " + phaseAngleClock);
        }
        return phaseAngleClock;
    }
}
