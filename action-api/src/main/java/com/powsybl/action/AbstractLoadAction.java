/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.action;

import java.util.OptionalDouble;

/**
 * An action to:
 * <ul>
 *     <li>change the P0 of a load or a dangling line , either by specifying a new absolute value (MW) or a relative change (MW).</li>
 *     <li>change the Q0 of a load or a dangling line, either by specifying a new absolute value (MVar) or a relative change (MVar).</li>
 * </ul>
 *
 * @author Anne Tilloy {@literal <anne.tilloy@rte-france.com>}
 */
public abstract class AbstractLoadAction extends AbstractAction {

    protected final boolean relativeValue;
    protected final Double activePowerValue;
    protected final Double reactivePowerValue;

    /**
     * @param id the id of the action.
     * @param relativeValue True if the load P0 and/or Q0 variation is relative, False if absolute.
     * @param activePowerValue The new load P0 (MW) if relativeValue equals False, otherwise the relative variation of load P0 (MW).
     * @param reactivePowerValue The new load Q0 (MVar) if relativeValue equals False, otherwise the relative variation of load Q0 (MVar).
     */
    AbstractLoadAction(String id, boolean relativeValue, Double activePowerValue, Double reactivePowerValue) {
        super(id);
        this.relativeValue = relativeValue;
        this.activePowerValue = activePowerValue;
        this.reactivePowerValue = reactivePowerValue;
    }

    public boolean isRelativeValue() {
        return relativeValue;
    }

    public OptionalDouble getActivePowerValue() {
        return activePowerValue == null ? OptionalDouble.empty() : OptionalDouble.of(activePowerValue);
    }

    public OptionalDouble getReactivePowerValue() {
        return reactivePowerValue == null ? OptionalDouble.empty() : OptionalDouble.of(reactivePowerValue);
    }
}
