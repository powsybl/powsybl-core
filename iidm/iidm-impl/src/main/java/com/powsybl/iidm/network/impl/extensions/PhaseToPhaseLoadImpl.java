/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.util.fastutil.ExtendedDoubleArrayList;
import com.powsybl.iidm.network.extensions.Phase;
import com.powsybl.iidm.network.extensions.PhaseToPhaseLoad;

import java.util.Objects;

/**
 * @author Coline Piloquet {@literal <coline.piloquet at rte-france.com>}
 */
class PhaseToPhaseLoadImpl implements PhaseToPhaseLoad {

    private final PhaseToPhaseLoadsImpl parent;
    private final Phase phase1;
    private final Phase phase2;
    private final ExtendedDoubleArrayList p;
    private final ExtendedDoubleArrayList q;
    private boolean removed = false;

    PhaseToPhaseLoadImpl(PhaseToPhaseLoadsImpl parent, Phase phase1, Phase phase2, double p, double q) {
        this.parent = Objects.requireNonNull(parent);
        this.phase1 = Objects.requireNonNull(phase1);
        this.phase2 = Objects.requireNonNull(phase2);
        checkValue(p, "p");
        checkValue(q, "q");
        int variantArraySize = parent.getVariantArraySize();
        this.p = new ExtendedDoubleArrayList(variantArraySize, p);
        this.q = new ExtendedDoubleArrayList(variantArraySize, q);
    }

    @Override
    public Phase getPhase1() {
        return phase1;
    }

    @Override
    public Phase getPhase2() {
        return phase2;
    }

    @Override
    public double getP() {
        checkNotRemoved();
        return p.getDouble(parent.getCurrentVariantIndex());
    }

    @Override
    public PhaseToPhaseLoadImpl setP(double p) {
        checkNotRemoved();
        checkValue(p, "p");
        this.p.set(parent.getCurrentVariantIndex(), p);
        return this;
    }

    @Override
    public double getQ() {
        checkNotRemoved();
        return q.getDouble(parent.getCurrentVariantIndex());
    }

    @Override
    public PhaseToPhaseLoadImpl setQ(double q) {
        checkNotRemoved();
        checkValue(q, "q");
        this.q.set(parent.getCurrentVariantIndex(), q);
        return this;
    }

    void extendVariantArraySize(int number, int sourceIndex) {
        p.ensureCapacity(p.size() + number);
        q.ensureCapacity(q.size() + number);
        for (int i = 0; i < number; ++i) {
            p.add(p.getDouble(sourceIndex));
            q.add(q.getDouble(sourceIndex));
        }
    }

    void reduceVariantArraySize(int number) {
        p.removeElements(number);
        q.removeElements(number);
    }

    void allocateVariantArrayElement(int[] indexes, int sourceIndex) {
        for (int index : indexes) {
            p.set(index, p.getDouble(sourceIndex));
            q.set(index, q.getDouble(sourceIndex));
        }
    }

    void markRemoved() {
        this.removed = true;
    }

    private void checkNotRemoved() {
        if (removed) {
            throw new PowsyblException("Cannot access p/q of removed phase to phase load between phases "
                    + phase1 + " and " + phase2);
        }
    }

    private void checkValue(double value, String name) {
        if (Double.isNaN(value)) {
            throw new PowsyblException("Invalid " + name + " (NaN) for phase to phase load between phases "
                    + phase1 + " and " + phase2 + " of load '" + parent.getExtendable().getId() + "'");
        }
    }
}
