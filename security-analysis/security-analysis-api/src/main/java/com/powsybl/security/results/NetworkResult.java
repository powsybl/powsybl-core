/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.results;

import com.google.common.collect.ImmutableMap;

import java.util.*;
import java.util.function.Function;

/**
 * Provides information about the state of some elements of the network, either:
 *  - on N state
 *  - on post-contingency states
 *  - on post-corrective actions states
 *
 * @author Etienne Lesot {@literal <etienne.lesot@rte-france.com>}
 */
public class NetworkResult {

    private final Map<String, BranchResult> branchResults;

    private final Map<String, BusResult> busResults;

    private final Map<String, ThreeWindingsTransformerResult> threeWindingsTransformerResults;

    public NetworkResult(Collection<BranchResult> branchResults, Collection<BusResult> busResults,
                         Collection<ThreeWindingsTransformerResult> threeWindingsTransformerResults) {
        this.branchResults = Objects.requireNonNull(branchResults)
                        .stream()
                        .collect(ImmutableMap.toImmutableMap(BranchResult::getBranchId, Function.identity()));
        this.busResults = Objects.requireNonNull(busResults)
                        .stream()
                        .collect(ImmutableMap.toImmutableMap(BusResult::getBusId, Function.identity()));
        this.threeWindingsTransformerResults = Objects.requireNonNull(threeWindingsTransformerResults)
                        .stream()
                        .collect(ImmutableMap
                                .toImmutableMap(ThreeWindingsTransformerResult::getThreeWindingsTransformerId, Function.identity()));
    }

    public List<BranchResult> getBranchResults() {
        return List.copyOf(branchResults.values());
    }

    public BranchResult getBranchResult(String branchId) {
        return branchResults.get(branchId);
    }

    public List<BusResult> getBusResults() {
        return List.copyOf(busResults.values());
    }

    public BusResult getBusResult(String busId) {
        return busResults.get(busId);
    }

    public List<ThreeWindingsTransformerResult> getThreeWindingsTransformerResults() {
        return List.copyOf(threeWindingsTransformerResults.values());
    }

    public ThreeWindingsTransformerResult getThreeWindingsTransformerResult(String threeWindingsTransformerId) {
        return threeWindingsTransformerResults.get(threeWindingsTransformerId);
    }
}
