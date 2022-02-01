/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.shortcircuit;

import com.powsybl.commons.extensions.AbstractExtendable;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Results for one fault computation.
 *
 * @author Boubakeur Brahimi
 */
public final class FaultResult extends AbstractExtendable<FaultResult> {

    private final String id;

    private final float threePhaseFaultCurrent;

    private final List<FeederResult> feederResults; //in case of systematic study, optional

    public FaultResult(String id, float threePhaseFaultCurrent, List<FeederResult> feederResults) {
        this.id = Objects.requireNonNull(id);
        this.threePhaseFaultCurrent = threePhaseFaultCurrent;
        this.feederResults = List.copyOf(feederResults);
    }

    public FaultResult(String id, float threePhaseFaultCurrent) {
        this(id, threePhaseFaultCurrent, Collections.emptyList());
    }

    /**
     * ID of the equipment for which a fault has been simulated. In a first simple approach, the equipment is a voltage
     * level, and no side is needed.
     */
    public String getId() {
        return id;
    }

    /**
     * Value of the 3-phase short-circuit current for this fault (in A).
     */
    public float getThreePhaseFaultCurrent() {
        return threePhaseFaultCurrent;
    }

    /**
     * List of contributions to the three phase fault current of each connectable connected to the equipment
     */
    public List<FeederResult> getFeederResults() {
        return feederResults;
    }

}
