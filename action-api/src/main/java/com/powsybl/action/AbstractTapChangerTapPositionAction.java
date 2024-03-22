/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.action;

import com.powsybl.iidm.network.ThreeSides;

/**
 * An action modifying the tap position of a two or three windings transformer
 *
 * @author Etienne Lesot {@literal <etienne.lesot@rte-france.com>}
 * @author Anne Tilloy {@literal <anne.tilloy@rte-france.com>}
 */
public abstract class AbstractTapChangerTapPositionAction extends AbstractTapChangerAction {

    protected final int tapPosition;
    protected final boolean relativeValue; // true if relative value chosen, false if absolute value

    protected AbstractTapChangerTapPositionAction(String id, String transformerId, boolean relativeValue, int tapPosition, ThreeSides side) {
        super(id, transformerId, side);
        this.relativeValue = relativeValue;
        this.tapPosition = tapPosition;
    }

    public int getTapPosition() {
        return tapPosition;
    }

    public boolean isRelativeValue() {
        return relativeValue;
    }
}
