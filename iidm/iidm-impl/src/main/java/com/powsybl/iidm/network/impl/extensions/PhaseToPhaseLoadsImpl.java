/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Load;
import com.powsybl.iidm.network.extensions.Phase;
import com.powsybl.iidm.network.extensions.PhaseToPhaseLoad;
import com.powsybl.iidm.network.extensions.PhaseToPhaseLoadAdder;
import com.powsybl.iidm.network.extensions.PhaseToPhaseLoads;
import com.powsybl.iidm.network.impl.AbstractMultiVariantIdentifiableExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * @author Coline Piloquet {@literal <coline.piloquet at rte-france.com>}
 */
public class PhaseToPhaseLoadsImpl extends AbstractMultiVariantIdentifiableExtension<Load> implements PhaseToPhaseLoads {

    private final List<PhaseToPhaseLoadImpl> phaseToPhaseLoads = new ArrayList<>();

    public PhaseToPhaseLoadsImpl(Load load) {
        super(load);
    }

    @Override
    public List<PhaseToPhaseLoad> getPhaseToPhaseLoads() {
        return Collections.unmodifiableList(phaseToPhaseLoads);
    }

    @Override
    public PhaseToPhaseLoadAdder newPhaseToPhaseLoad() {
        return new PhaseToPhaseLoadAdderImpl(this);
    }

    @Override
    public Optional<PhaseToPhaseLoad> findPhaseToPhaseLoad(Phase phase1, Phase phase2) {
        Objects.requireNonNull(phase1);
        Objects.requireNonNull(phase2);
        return phaseToPhaseLoads.stream()
                .filter(l -> matches(l, phase1, phase2))
                .map(PhaseToPhaseLoad.class::cast)
                .findFirst();
    }

    @Override
    public boolean removePhaseToPhaseLoad(PhaseToPhaseLoad phaseToPhaseLoad) {
        return phaseToPhaseLoads.remove(phaseToPhaseLoad);
    }

    @Override
    public boolean removePhaseToPhaseLoad(Phase phase1, Phase phase2) {
        Objects.requireNonNull(phase1);
        Objects.requireNonNull(phase2);
        return phaseToPhaseLoads.removeIf(l -> matches(l, phase1, phase2));
    }

    private static boolean matches(PhaseToPhaseLoad l, Phase phase1, Phase phase2) {
        return l.getPhase1() == phase1 && l.getPhase2() == phase2
                || l.getPhase1() == phase2 && l.getPhase2() == phase1;
    }

    int getCurrentVariantIndex() {
        return getVariantIndex();
    }

    int getVariantArraySize() {
        return getVariantManagerHolder().getVariantManager().getVariantArraySize();
    }

    /**
     * Adds a phase to phase load, checking that no load already exists between the same two phases (in any order).
     */
    void add(PhaseToPhaseLoadImpl phaseToPhaseLoad) {
        Objects.requireNonNull(phaseToPhaseLoad);
        if (findPhaseToPhaseLoad(phaseToPhaseLoad.getPhase1(), phaseToPhaseLoad.getPhase2()).isPresent()) {
            throw new PowsyblException("A phase to phase load between phases " + phaseToPhaseLoad.getPhase1()
                    + " and " + phaseToPhaseLoad.getPhase2() + " already exists for load '" + getExtendable().getId() + "'");
        }
        phaseToPhaseLoads.add(phaseToPhaseLoad);
    }

    @Override
    public void extendVariantArraySize(int initVariantArraySize, int number, int sourceIndex) {
        phaseToPhaseLoads.forEach(l -> l.extendVariantArraySize(number, sourceIndex));
    }

    @Override
    public void reduceVariantArraySize(int number) {
        phaseToPhaseLoads.forEach(l -> l.reduceVariantArraySize(number));
    }

    @Override
    public void deleteVariantArrayElement(int index) {
        // Does nothing
    }

    @Override
    public void allocateVariantArrayElement(int[] indexes, int sourceIndex) {
        phaseToPhaseLoads.forEach(l -> l.allocateVariantArrayElement(indexes, sourceIndex));
    }
}
