/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.shortcircuit;

import com.powsybl.commons.extensions.AbstractExtendable;

import java.util.Objects;

/**
 * Results for one fault computation.
 *
 * @author Boubakeur Brahimi
 */
public final class FaultResult extends AbstractExtendable<FaultResult> {

    private final String id;

    private final float threePhaseFaultCurrent;

    public FaultResult(String id, float threePhaseFaultCurrent) {
        this.id = Objects.requireNonNull(id);
        this.threePhaseFaultCurrent = threePhaseFaultCurrent;
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

}
