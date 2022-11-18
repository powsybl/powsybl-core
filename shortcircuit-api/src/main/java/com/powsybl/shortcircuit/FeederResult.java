/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.shortcircuit;

import java.util.Objects;

/**
 * @author Coline Piloquet <coline.piloquet at rte-france.com>
 */
public class FeederResult {

    // VERSION = 1.0 connectableId, current
    // VERSION = 1.1 voltageDrop
    public static final String VERSION = "1.1";
    private final String connectableId;

    private final FortescueValue current;

    private final double voltageDrop;

    public FeederResult(String connectableId,
                        FortescueValue current,
                        double voltageDrop) {
        this.connectableId = Objects.requireNonNull(connectableId);
        this.current = current;
        this.voltageDrop = voltageDrop;
    }

    public FeederResult(String connectableId,
                        double feederThreePhaseCurrent,
                        double voltageDrop) {
        this(Objects.requireNonNull(connectableId), new FortescueValue(feederThreePhaseCurrent), voltageDrop);
    }

    /**
     * The ID of the connectable contributing to the three phase short circuit current.
     */
    public String getConnectableId() {
        return connectableId;
    }

    public FortescueValue getCurrent() {
        return current;
    }

    /**
     * The value of the current of the connectable contributing to the total fault current in kA
     */
    public double getFeederThreePhaseCurrent() {
        return current.getDirectMagnitude();
    }

    /**
     * The voltage drop at the connectable in %.
     */
    public double getVoltageDrop() {
        return voltageDrop;
    }
}
