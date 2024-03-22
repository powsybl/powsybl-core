/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
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
public class DanglingLineAction extends AbstractLoadAction {

    public static final String NAME = "DANGLING_LINE";

    private final String danglingLineId;

    /**
     * @param id                 the id of the action.
     * @param danglingLineId     the id of the dangling line on which the action would be applied.
     * @param relativeValue      True if the dangling line P0 and/or Q0 variation is relative, False if absolute.
     * @param activePowerValue   The new dangling line P0 (MW) if relativeValue equals False, otherwise the relative variation of dangling line P0 (MW).
     * @param reactivePowerValue The new dangling line Q0 (MVar) if relativeValue equals False, otherwise the relative variation of dangling line Q0 (MVar).
     */
    DanglingLineAction(String id, String danglingLineId, boolean relativeValue, Double activePowerValue, Double reactivePowerValue) {
        super(id, relativeValue, activePowerValue, reactivePowerValue);
        this.danglingLineId = danglingLineId;
    }

    public String getDanglingLineId() {
        return danglingLineId;
    }

    @Override
    public String getType() {
        return NAME;
    }

    @Override
    public DanglingLineActionBuilder convertToBuilder() {
        return new DanglingLineActionBuilder().withId(id)
            .withDanglingLineId(danglingLineId)
            .withActivePowerValue(activePowerValue)
            .withRelativeValue(relativeValue)
            .withReactivePowerValue(reactivePowerValue);
    }
}
