/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.shortcircuit;

import com.powsybl.commons.extensions.AbstractExtendable;
import com.powsybl.security.LimitViolation;
import com.powsybl.security.NetworkMetadata;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Results of an localized short-circuit computation.
 * Will contain a fault result, with feeder results.
 * Will contain a list of short-circuit bus result for each buses of the network.
 *
 * @author Boubakeur Brahimi
 */
public class ShortCircuitAnalysisResult extends AbstractExtendable<ShortCircuitAnalysisResult> {

    private NetworkMetadata networkMetadata;

    private final List<FaultResult> faultResults;

    public ShortCircuitAnalysisResult(List<FaultResult> faultResults) {
        this.faultResults = Objects.requireNonNull(faultResults);
    }

    /**
     * The associated fault result.
     */
    public List<FaultResult> getFaultResults() {
        return faultResults;
    }

    public NetworkMetadata getNetworkMetadata() {
        return networkMetadata;
    }

    public ShortCircuitAnalysisResult setNetworkMetadata(NetworkMetadata networkMetadata) {
        this.networkMetadata = networkMetadata;
        return this;
    }

    public Map<String, List<LimitViolation>> getLimitViolations() {
        Map<String, List<LimitViolation>> result = new TreeMap<>(); // Ordered by Fault Identity
        faultResults.forEach(r -> result.put(r.getFault().getId(), r.getLimitViolations()));
        return result;
    }
}
