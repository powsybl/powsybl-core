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
public abstract class AbstractLoadActionBuilder<T extends AbstractLoadAction, B extends AbstractLoadActionBuilder<T, B>>
    implements ActionBuilder<B> {

    private String id;
    private String elementId;
    private Boolean relativeValue;
    private Double activePowerValue;
    private Double reactivePowerValue;

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

    public Double getReactivePowerValue() {
        return reactivePowerValue;
    }

    public String getElementId() {
        return elementId;
    }

    public Boolean getRelativeValue() {
        return relativeValue;
    }

    public Double getActivePowerValue() {
        return activePowerValue;
    }
}
