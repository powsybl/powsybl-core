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
 * @author Anne Tilloy {@literal <anne.tilloy@rte-france.com>}
 */
public abstract class AbstractLoadActionBuilder<T extends AbstractLoadAction, B extends AbstractLoadActionBuilder<T, B>>
    implements ActionBuilder<B> {

    protected String id;
    protected String elementId;
    protected Boolean relativeValue;
    protected Double activePowerValue;
    protected Double reactivePowerValue;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public abstract T build();

    @Override
    public B withNetworkElementId(String elementId) {
        this.elementId = elementId;
        return (B) this;
    }

    @Override
    public B withId(String id) {
        this.id = id;
        return (B) this;
    }

    public B withRelativeValue(boolean relativeValue) {
        this.relativeValue = relativeValue;
        return (B) this;
    }

    public B withActivePowerValue(double activePowerValue) {
        this.activePowerValue = activePowerValue;
        return (B) this;
    }

    public B withReactivePowerValue(double reactivePowerValue) {
        this.reactivePowerValue = reactivePowerValue;
        return (B) this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AbstractLoadActionBuilder<?, ?> that = (AbstractLoadActionBuilder<?, ?>) o;
        return Objects.equals(id, that.id) && Objects.equals(elementId, that.elementId) && Objects.equals(relativeValue, that.relativeValue) && Objects.equals(activePowerValue, that.activePowerValue) && Objects.equals(reactivePowerValue, that.reactivePowerValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, elementId, relativeValue, activePowerValue, reactivePowerValue);
    }
}
