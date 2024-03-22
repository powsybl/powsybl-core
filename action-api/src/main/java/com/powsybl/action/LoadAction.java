/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.action;

/**
 * An action to:
 * <ul>
 *     <li>change the P0 of a load, either by specifying a new absolute value (MW) or a relative change (MW).</li>
 *     <li>change the Q0 of a load, either by specifying a new absolute value (MVar) or a relative change (MVar).</li>
 * </ul>
 *
 * @author Anne Tilloy {@literal <anne.tilloy@rte-france.com>}
 */
public class LoadAction extends AbstractLoadAction {

    public static final String NAME = "LOAD";

    private final String loadId;

    /**
     * @param id                 the id of the action.
     * @param loadId             the id of the load on which the action would be applied.
     * @param relativeValue      True if the load P0 and/or Q0 variation is relative, False if absolute.
     * @param activePowerValue   The new load P0 (MW) if relativeValue equals False, otherwise the relative variation of load P0 (MW).
     * @param reactivePowerValue The new load Q0 (MVar) if relativeValue equals False, otherwise the relative variation of load Q0 (MVar).
     */
    LoadAction(String id, String loadId, boolean relativeValue, Double activePowerValue, Double reactivePowerValue) {
        super(id, relativeValue, activePowerValue, reactivePowerValue);
        this.loadId = loadId;
    }

    public String getLoadId() {
        return loadId;
    }

    @Override
    public String getType() {
        return NAME;
    }

    @Override
    public LoadActionBuilder convertToBuilder() {
        return new LoadActionBuilder()
            .withId(id)
            .withLoadId(loadId)
            .withRelativeValue(relativeValue)
            .withActivePowerValue(activePowerValue)
            .withReactivePowerValue(reactivePowerValue);
    }
}
