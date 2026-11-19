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
import gnu.trove.list.array.TDoubleArrayList;

import java.util.Objects;

/**
 * @author Coline Piloquet {@literal <coline.piloquet at rte-france.com>}
 */
class PhaseToPhaseLoadImpl implements PhaseToPhaseLoad {

    private final PhaseToPhaseLoadsImpl parent;
    private final Phase phase1;
    private final Phase phase2;
    private final TDoubleArrayList p;
    private final TDoubleArrayList q;

    PhaseToPhaseLoadImpl(PhaseToPhaseLoadsImpl parent, Phase phase1, Phase phase2, double p, double q) {
        this.parent = Objects.requireNonNull(parent);
        this.phase1 = Objects.requireNonNull(phase1);
        this.phase2 = Objects.requireNonNull(phase2);
        checkValue(p, "p");
        checkValue(q, "q");
        int variantArraySize = parent.getVariantArraySize();
        this.p = new TDoubleArrayList(variantArraySize);
        this.q = new TDoubleArrayList(variantArraySize);
        for (int i = 0; i < variantArraySize; i++) {
            this.p.add(p);
            this.q.add(q);
        }
    }

    private void checkValue(double value, String name) {
        if (Double.isNaN(value)) {
            throw new PowsyblException("Invalid " + name + " (NaN) for phase to phase load between phases "
                    + phase1 + " and " + phase2 + " of load '" + parent.getExtendable().getId() + "'");
        }
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
        return p.get(parent.getCurrentVariantIndex());
    }

    @Override
    public PhaseToPhaseLoadImpl setP(double p) {
        checkValue(p, "p");
        this.p.set(parent.getCurrentVariantIndex(), p);
        return this;
    }

    @Override
    public double getQ() {
        return q.get(parent.getCurrentVariantIndex());
    }

    @Override
    public PhaseToPhaseLoadImpl setQ(double q) {
        checkValue(q, "q");
        this.q.set(parent.getCurrentVariantIndex(), q);
        return this;
    }

    void extendVariantArraySize(int number, int sourceIndex) {
        p.ensureCapacity(p.size() + number);
        q.ensureCapacity(q.size() + number);
        for (int i = 0; i < number; ++i) {
            p.add(p.get(sourceIndex));
            q.add(q.get(sourceIndex));
        }
    }

    void reduceVariantArraySize(int number) {
        p.remove(p.size() - number, number);
        q.remove(q.size() - number, number);
    }

    void allocateVariantArrayElement(int[] indexes, int sourceIndex) {
        for (int index : indexes) {
            p.set(index, p.get(sourceIndex));
            q.set(index, q.get(sourceIndex));
        }
    }
}
