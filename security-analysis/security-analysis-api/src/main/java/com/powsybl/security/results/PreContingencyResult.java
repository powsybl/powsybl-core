/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.results;

import com.powsybl.loadflow.LoadFlowResult;
import com.powsybl.security.LimitViolationsResult;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author Etienne Lesot {@literal <etienne.lesot at rte-france.com>}
 */
public class PreContingencyResult extends AbstractContingencyResult {

    private final LoadFlowResult.ComponentResult.Status status;

    private final Map<String, MovedPhaseShifterResult> phaseShifterResults;

    public PreContingencyResult() {
        this(LoadFlowResult.ComponentResult.Status.CONVERGED, null, NetworkResult.empty(), Double.NaN,
                Collections.emptyMap());
    }

    public PreContingencyResult(LoadFlowResult.ComponentResult.Status status,
                                LimitViolationsResult limitViolationsResult,
                                NetworkResult networkResult,
                                double distributedActivePower) {
        this(status, limitViolationsResult, networkResult, distributedActivePower, Collections.emptyMap());
    }

    public PreContingencyResult(LoadFlowResult.ComponentResult.Status status,
                                LimitViolationsResult limitViolationsResult,
                                NetworkResult networkResult,
                                double distributedActivePower,
                                Map<String, MovedPhaseShifterResult> phaseShifterResults) {
        super(limitViolationsResult, networkResult, distributedActivePower);
        this.status = Objects.requireNonNull(status);
        this.phaseShifterResults = phaseShifterResults != null
                ? Collections.unmodifiableMap(new LinkedHashMap<>(phaseShifterResults))
                : Collections.emptyMap();
    }

    public LoadFlowResult.ComponentResult.Status getStatus() {
        return status;
    }

    public Map<String, MovedPhaseShifterResult> getPhaseShifterResults() {
        return phaseShifterResults;
    }

    public MovedPhaseShifterResult getPhaseShifterResult(String transformerId) {
        return phaseShifterResults.get(transformerId);
    }
}
