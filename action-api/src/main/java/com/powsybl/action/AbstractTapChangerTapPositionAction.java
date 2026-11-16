/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.action;

import com.powsybl.iidm.network.ThreeSides;

import java.util.Objects;

/**
 * An action modifying the tap position of a two or three windings transformer
 *
 * @author Etienne Lesot {@literal <etienne.lesot@rte-france.com>}
 * @author Anne Tilloy {@literal <anne.tilloy@rte-france.com>}
 */
public abstract class AbstractTapChangerTapPositionAction extends AbstractTapChangerAction {

    private final int tapPosition;
    private final boolean relativeValue; // true if relative value chosen, false if absolute value

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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        AbstractTapChangerTapPositionAction that = (AbstractTapChangerTapPositionAction) o;
        return tapPosition == that.tapPosition && relativeValue == that.relativeValue;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), tapPosition, relativeValue);
    }
}
