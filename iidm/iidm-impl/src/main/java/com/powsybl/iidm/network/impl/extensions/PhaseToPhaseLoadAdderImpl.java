/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.extensions.Phase;
import com.powsybl.iidm.network.extensions.PhaseToPhaseLoad;
import com.powsybl.iidm.network.extensions.PhaseToPhaseLoadAdder;

import java.util.Objects;

/**
 * @author Coline Piloquet {@literal <coline.piloquet at rte-france.com>}
 */
class PhaseToPhaseLoadAdderImpl implements PhaseToPhaseLoadAdder {

    private final PhaseToPhaseLoadsImpl phaseToPhaseLoads;

    private Phase phase1;
    private Phase phase2;
    private double p = Double.NaN;
    private double q = Double.NaN;

    PhaseToPhaseLoadAdderImpl(PhaseToPhaseLoadsImpl phaseToPhaseLoads) {
        this.phaseToPhaseLoads = Objects.requireNonNull(phaseToPhaseLoads);
    }

    @Override
    public PhaseToPhaseLoadAdder withPhase1(Phase phase1) {
        this.phase1 = phase1;
        return this;
    }

    @Override
    public PhaseToPhaseLoadAdder withPhase2(Phase phase2) {
        this.phase2 = phase2;
        return this;
    }

    @Override
    public PhaseToPhaseLoadAdder withP(double p) {
        this.p = p;
        return this;
    }

    @Override
    public PhaseToPhaseLoadAdder withQ(double q) {
        this.q = q;
        return this;
    }

    @Override
    public PhaseToPhaseLoad add() {
        validate();
        PhaseToPhaseLoadImpl phaseToPhaseLoad = new PhaseToPhaseLoadImpl(phaseToPhaseLoads, phase1, phase2, p, q);
        phaseToPhaseLoads.add(phaseToPhaseLoad);
        return phaseToPhaseLoad;
    }

    private void validate() {
        String loadId = phaseToPhaseLoads.getExtendable().getId();
        if (phase1 == null) {
            throw new PowsyblException("phase1 is not set for phase to phase load of load '" + loadId + "'");
        }
        if (phase2 == null) {
            throw new PowsyblException("phase2 is not set for phase to phase load of load '" + loadId + "'");
        }
        if (phase1 == phase2) {
            throw new PowsyblException("A phase to phase load must be connected between two different phases (got "
                    + phase1 + " twice) for load '" + loadId + "'");
        }
        if (Double.isNaN(p)) {
            throw new PowsyblException("p is not set for phase to phase load between phases " + phase1 + " and " + phase2
                    + " of load '" + loadId + "'");
        }
        if (Double.isNaN(q)) {
            throw new PowsyblException("q is not set for phase to phase load between phases " + phase1 + " and " + phase2
                    + " of load '" + loadId + "'");
        }
    }
}
