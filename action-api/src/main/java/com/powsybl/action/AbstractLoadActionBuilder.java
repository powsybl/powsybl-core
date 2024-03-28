/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.action;

/**
 * @author Anne Tilloy {@literal <anne.tilloy@rte-france.com>}
 */
public abstract class AbstractLoadActionBuilder<T extends AbstractLoadAction, B extends AbstractLoadActionBuilder<T, B>> {

    protected String id;
    protected String elementId;
    protected Boolean relativeValue;
    protected Double activePowerValue;
    protected Double reactivePowerValue;

    public abstract T build();

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
}
