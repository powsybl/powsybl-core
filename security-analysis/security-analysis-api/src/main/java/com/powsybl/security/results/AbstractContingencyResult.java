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
    private final Map<PhaseShifterResultKey, MovedPhaseShifterResult> phaseShifterResults;

    private record PhaseShifterResultKey(String transformerId, ThreeSides side) {
        private PhaseShifterResultKey(MovedPhaseShifterResult phaseShifterResult) {
            this(phaseShifterResult.transformerId(), phaseShifterResult.side());
        }
    }

    protected AbstractContingencyResult(LimitViolationsResult limitViolationsResult,
                                        NetworkResult networkResult,
                                        double distributedActivePower,
                                        List<MovedPhaseShifterResult> phaseShifterResults) {
        this.limitViolationsResult = limitViolationsResult;
        this.networkResult = Objects.requireNonNull(networkResult);
        this.distributedActivePower = distributedActivePower;
        this.phaseShifterResults = phaseShifterResults != null && !phaseShifterResults.isEmpty()
                ? Collections.unmodifiableMap(phaseShifterResults.stream()
                        .collect(Collectors.toMap(
                                PhaseShifterResultKey::new,
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

    public Collection<MovedPhaseShifterResult> getPhaseShifterResults() {
        return phaseShifterResults.values();
    }

    public MovedPhaseShifterResult getPhaseShifterResult(String transformerId) {
        return phaseShifterResults.get(new PhaseShifterResultKey(transformerId, null));
    }

    public MovedPhaseShifterResult getPhaseShifterResult(String transformerId, ThreeSides side) {
        return phaseShifterResults.get(new PhaseShifterResultKey(transformerId, side));
    }
}
