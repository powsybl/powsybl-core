/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.action;

/**
 * @author Anne Tilloy <anne.tilloy@rte-france.com>
 */
public class LoadActionBuilder {

    private String id;
    private String loadId;
    private Boolean relativeValue;
    private Double activePowerValue;
    private Double reactivePowerValue;

    public LoadAction build() {
        if (relativeValue == null) {
            throw new IllegalArgumentException("For a load action, relativeValue must be provided");
        }
        if (activePowerValue == null && reactivePowerValue == null) {
            throw new IllegalArgumentException("For a load action, activePowerValue or reactivePowerValue must be provided");
        }
        return new LoadAction(id, loadId, relativeValue, activePowerValue, reactivePowerValue);
    }

    public LoadActionBuilder withId(String id) {
        this.id = id;
        return this;
    }

    public LoadActionBuilder withLoadId(String loadId) {
        this.loadId = loadId;
        return this;
    }

    public LoadActionBuilder withRelativeValue(boolean relativeValue) {
        this.relativeValue = relativeValue;
        return this;
    }

    public LoadActionBuilder withActivePowerValue(double activePowerValue) {
        this.activePowerValue = activePowerValue;
        return this;
    }

    public LoadActionBuilder withReactivePowerValue(double reactivePowerValue) {
        this.reactivePowerValue = reactivePowerValue;
        return this;
    }
}
