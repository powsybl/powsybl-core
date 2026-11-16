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
    private final Map<ChangedPhaseTapChangerKey, ChangedPhaseTapChanger> changedPhaseTapChangers;

    private record ChangedPhaseTapChangerKey(String transformerId, ThreeSides side) {
        private ChangedPhaseTapChangerKey(ChangedPhaseTapChanger changedPhaseTapChanger) {
            this(changedPhaseTapChanger.transformerId(), changedPhaseTapChanger.side());
        }
    }

    protected AbstractContingencyResult(LimitViolationsResult limitViolationsResult,
                                        NetworkResult networkResult,
                                        double distributedActivePower,
                                        List<ChangedPhaseTapChanger> changedPhaseTapChangers) {
        this.limitViolationsResult = limitViolationsResult;
        this.networkResult = Objects.requireNonNull(networkResult);
        this.distributedActivePower = distributedActivePower;
        this.changedPhaseTapChangers = changedPhaseTapChangers != null && !changedPhaseTapChangers.isEmpty()
                ? Collections.unmodifiableMap(changedPhaseTapChangers.stream()
                        .collect(Collectors.toMap(
                                ChangedPhaseTapChangerKey::new,
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

    public Collection<ChangedPhaseTapChanger> getChangedPhaseTapChangers() {
        return changedPhaseTapChangers.values();
    }

    public ChangedPhaseTapChanger getChangedPhaseTapChanger(String transformerId) {
        return changedPhaseTapChangers.get(new ChangedPhaseTapChangerKey(transformerId, null));
    }

    public ChangedPhaseTapChanger getChangedPhaseTapChanger(String transformerId, ThreeSides side) {
        return changedPhaseTapChangers.get(new ChangedPhaseTapChangerKey(transformerId, side));
    }
}
