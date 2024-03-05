/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.action;

/**
 * @author Anne Tilloy {@literal <anne.tilloy@rte-france.com>}
 */
public abstract class AbstractLoadActionBuilder<T extends AbstractLoadAction> {

    protected String id;
    protected String elementId;
    protected Boolean relativeValue;
    protected Double activePowerValue;
    protected Double reactivePowerValue;

    public abstract T build();

    public AbstractLoadActionBuilder<T> withId(String id) {
        this.id = id;
        return this;
    }

    public AbstractLoadActionBuilder<T> withElementId(String loadId) {
        this.elementId = loadId;
        return this;
    }

    public AbstractLoadActionBuilder<T> withRelativeValue(boolean relativeValue) {
        this.relativeValue = relativeValue;
        return this;
    }

    public AbstractLoadActionBuilder<T> withActivePowerValue(double activePowerValue) {
        this.activePowerValue = activePowerValue;
        return this;
    }

    public AbstractLoadActionBuilder<T> withReactivePowerValue(double reactivePowerValue) {
        this.reactivePowerValue = reactivePowerValue;
        return this;
    }
}
