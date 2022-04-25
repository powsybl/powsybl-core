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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Results of an localized short-circuit computation.
 * Will contain a fault result, with feeder results.
 * Will contain a list of short-circuit bus result for each buses of the network.
 *
 * @author Boubakeur Brahimi
 */
public class ShortCircuitAnalysisResult extends AbstractExtendable<ShortCircuitAnalysisResult> {

    private NetworkMetadata networkMetadata;

    private final List<FaultResult> faultResult;

    private final List<LimitViolation> limitViolations = new ArrayList<>();

    public ShortCircuitAnalysisResult(List<FaultResult> faultResult) {
        this(faultResult, Collections.emptyList());
    }

    public ShortCircuitAnalysisResult(List<FaultResult> faultResult, List<LimitViolation> limitViolations) {
        this.faultResult = Objects.requireNonNull(faultResult);
        this.limitViolations.addAll(Objects.requireNonNull(limitViolations));
    }

    /**
     * The associated fault result.
     */
    public List<FaultResult> getFaultResults() {
        return faultResult;
    }

    public NetworkMetadata getNetworkMetadata() {
        return networkMetadata;
    }

    public ShortCircuitAnalysisResult setNetworkMetadata(NetworkMetadata networkMetadata) {
        this.networkMetadata = networkMetadata;
        return this;
    }

    public List<LimitViolation> getLimitViolations() {
        return limitViolations;
    }

}
