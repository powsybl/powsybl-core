/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.action;

import java.util.Objects;

/**
 *
 * @author Etienne Lesot {@literal <etienne.lesot at rte-france.com>}
 */
public abstract class AbstractTapChangerTapPositionActionBuilder<T extends ActionBuilder<T>> extends AbstractTapChangerActionBuilder<T> {

    private int tapPosition;
    private boolean relativeValue; // true if relative value chosen, false if absolute value

    public T withTapPosition(int tapPosition) {
        this.tapPosition = tapPosition;
        return (T) this;
    }

    public T withRelativeValue(boolean relativeValue) {
        this.relativeValue = relativeValue;
        return (T) this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AbstractTapChangerTapPositionActionBuilder<?> that = (AbstractTapChangerTapPositionActionBuilder<?>) o;
        return tapPosition == that.tapPosition && relativeValue == that.relativeValue;
    }

    @Override
    public int hashCode() {
        return Objects.hash(tapPosition, relativeValue);
    }

    public int getTapPosition() {
        return tapPosition;
    }

    public boolean isRelativeValue() {
        return relativeValue;
    }
}
