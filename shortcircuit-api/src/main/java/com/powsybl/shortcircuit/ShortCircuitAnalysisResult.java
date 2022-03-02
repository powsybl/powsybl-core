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
 * Results of a short-circuit computation.
 * Will contain an exhaustive list of computed short-circuit current values,
 * and a list of {@link LimitViolation}s.
 *
 * @author Boubakeur Brahimi
 */
public class ShortCircuitAnalysisResult extends AbstractExtendable<ShortCircuitAnalysisResult> {

    private NetworkMetadata networkMetadata;

    private final List<FaultResult> faultResults = new ArrayList<>();

    private final List<LimitViolation> limitViolations = new ArrayList<>();

    private ShortCircuitBusResult busResult; //If selective study on a bus: results

    public ShortCircuitAnalysisResult(List<FaultResult> faultResults, List<LimitViolation> limitViolations) {
        this.faultResults.addAll(Objects.requireNonNull(faultResults));
        this.limitViolations.addAll(Objects.requireNonNull(limitViolations));
    }

    /**
     * A list of results, for each fault which have been simulated.
     */
    public List<FaultResult> getFaultResults() {
        return Collections.unmodifiableList(faultResults);
    }

    /**
     * The list of limit violations: for instance when the computed short-circuit current on a given equipment is higher
     * than the maximum admissible value for that equipment. In general, the equipment ID can be completed by the side where
     * the violation occurs. In a first simple approach, the equipment is a voltage level, and no side is needed.
     */
    public List<LimitViolation> getLimitViolations() {
        return Collections.unmodifiableList(limitViolations);
    }

    public NetworkMetadata getNetworkMetadata() {
        return networkMetadata;
    }

    public ShortCircuitAnalysisResult setNetworkMetadata(NetworkMetadata networkMetadata) {
        this.networkMetadata = networkMetadata;
        return this;
    }

}
