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

    private final String connectableId;

    private final FortescueValue current;

    public FeederResult(String connectableId,
                        FortescueValue current) {
        this.connectableId = Objects.requireNonNull(connectableId);
        this.current = current;
    }

    public FeederResult(String connectableId,
                        double feederThreePhaseCurrent) {
        this(Objects.requireNonNull(connectableId), new FortescueValue(feederThreePhaseCurrent));
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
}
