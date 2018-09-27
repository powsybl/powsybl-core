/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.PhaseTapChangerStep;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class PhaseTapChangerStepImpl extends TapChangerStepImpl<PhaseTapChangerStepImpl>
                              implements PhaseTapChangerStep {

    private double phaseShift;

    PhaseTapChangerStepImpl(double phaseShift, double ratio, double rdr, double rdx, double rdg, double rdb) {
        super(ratio, rdr, rdx, rdg, rdb);
        this.phaseShift = phaseShift;
    }

    @Override
    public double getPhaseShift() {
        return phaseShift;
    }

    @Override
    public PhaseTapChangerStep setPhaseShift(double phaseShift) {
        this.phaseShift = phaseShift;
        return this;
    }

}
