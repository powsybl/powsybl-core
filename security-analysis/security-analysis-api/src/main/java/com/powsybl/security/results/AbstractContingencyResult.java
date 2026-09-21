/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.results;

import com.powsybl.iidm.network.ThreeSides;
import com.powsybl.security.LimitViolationsResult;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Etienne Lesot {@literal <etienne.lesot at rte-france.com>}
 */
public abstract class AbstractContingencyResult {
    private final LimitViolationsResult limitViolationsResult;
    private final NetworkResult networkResult;
    private final double distributedActivePower;
    private final Map<ChangedPhaseShifterKey, ChangedPhaseTapChanger> changedPhaseShifters;

    private record ChangedPhaseShifterKey(String transformerId, ThreeSides side) {
        private ChangedPhaseShifterKey(ChangedPhaseTapChanger changedPhaseShifter) {
            this(changedPhaseShifter.transformerId(), changedPhaseShifter.side());
        }
    }

    protected AbstractContingencyResult(LimitViolationsResult limitViolationsResult,
                                        NetworkResult networkResult,
                                        double distributedActivePower,
                                        List<ChangedPhaseTapChanger> changedPhaseShifters) {
        this.limitViolationsResult = limitViolationsResult;
        this.networkResult = Objects.requireNonNull(networkResult);
        this.distributedActivePower = distributedActivePower;
        this.changedPhaseShifters = changedPhaseShifters != null && !changedPhaseShifters.isEmpty()
                ? Collections.unmodifiableMap(changedPhaseShifters.stream()
                        .collect(Collectors.toMap(
                                ChangedPhaseShifterKey::new,
                                Function.identity())))
                : Collections.emptyMap();
    }

    public LimitViolationsResult getLimitViolationsResult() {
        return limitViolationsResult;
    }

    public NetworkResult getNetworkResult() {
        return networkResult;
    }

    public double getDistributedActivePower() {
        return distributedActivePower;
    }

    public Collection<ChangedPhaseTapChanger> getChangedPhaseShifters() {
        return changedPhaseShifters.values();
    }

    public ChangedPhaseTapChanger getChangedPhaseShifter(String transformerId) {
        return changedPhaseShifters.get(new ChangedPhaseShifterKey(transformerId, null));
    }

    public ChangedPhaseTapChanger getChangedPhaseShifter(String transformerId, ThreeSides side) {
        return changedPhaseShifters.get(new ChangedPhaseShifterKey(transformerId, side));
    }
}
