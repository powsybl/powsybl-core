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
 * Will contain an exhaustive list of computed short-circuit fault results.
 *
 * @author Boubakeur Brahimi
 */
public class ShortCircuitAnalysisResult extends AbstractExtendable<ShortCircuitAnalysisResult> {

    private NetworkMetadata networkMetadata;

    private final List<FaultResult> faultResults = new ArrayList<>();

    public ShortCircuitAnalysisResult(List<FaultResult> faultResults) {
        this.faultResults.addAll(Objects.requireNonNull(faultResults));
    }

    /**
     * A list of results, for each fault which have been simulated.
     */
    public List<FaultResult> getFaultResults() {
        return Collections.unmodifiableList(faultResults);
    }

    public NetworkMetadata getNetworkMetadata() {
        return networkMetadata;
    }

    public ShortCircuitAnalysisResult setNetworkMetadata(NetworkMetadata networkMetadata) {
        this.networkMetadata = networkMetadata;
        return this;
    }

}
